package cz.maxtechnik.dif.entity.portal;

import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Portálová entita. Každý hráč může mít 2 portály (blue + orange).
 * Portály mohou být na zdi, podlaze i stropě. Jakákoliv entita může projít párem portálů.
 *
 * Koordinátový systém portálu:
 *   facing = normálový směr ven ze stěny (směr, kam se entita „vynoří")
 *   upDir  = „nahoru" portálu (výšková osa 2-blokového otvoru)
 *   right  = facing × upDir
 */
public class PortalEntity extends Entity{

	// -------------------- Synched data --------------------

	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Boolean> DATA_IS_BLUE=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_IS_LINKED=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<String> DATA_FACING=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.STRING);
	private static final EntityDataAccessor<String> DATA_UP_DIR=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.STRING);

	private static final int MAX_ENTITIES_PER_TICK=5;
	public long lastTeleportTime=0;
	private final Map<UUID,Long> cooldowns=new HashMap<>();

	// -------------------- Constructors --------------------

	public PortalEntity(EntityType<?> type,Level level){
		super(type,level);
	}

	public PortalEntity(Level level,UUID owner,boolean isBlue,Direction facing,Direction upDir,Vec3 pos){
		super(DifModEntities.PORTAL.get(),level);
		setOwner(owner);
		setIsBlue(isBlue);
		setFacing(facing);
		setUpDir(upDir);
		setPos(pos);
		setBoundingBox(buildPortalAABB());
	}

	// -------------------- Accessors --------------------

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder b){
		b.define(DATA_OWNER,Optional.empty());
		b.define(DATA_IS_BLUE,true);
		b.define(DATA_IS_LINKED,false);
		b.define(DATA_FACING,Direction.NORTH.getName());
		b.define(DATA_UP_DIR,Direction.UP.getName());
	}

	public UUID getOwner(){ return entityData.get(DATA_OWNER).orElse(null); }
	public void setOwner(UUID o){ entityData.set(DATA_OWNER,Optional.ofNullable(o)); }
	public boolean isBlue(){ return entityData.get(DATA_IS_BLUE); }
	public void setIsBlue(boolean v){ entityData.set(DATA_IS_BLUE,v); }
	public boolean isLinked(){ return entityData.get(DATA_IS_LINKED); }
	public void setIsLinked(boolean v){ entityData.set(DATA_IS_LINKED,v); }

	public Direction getFacing(){
		return Direction.byName(entityData.get(DATA_FACING));
	}
	public void setFacing(Direction d){
		entityData.set(DATA_FACING,d!=null?d.getName():Direction.NORTH.getName());
	}
	public Direction getUpDir(){
		return Direction.byName(entityData.get(DATA_UP_DIR));
	}
	public void setUpDir(Direction d){
		entityData.set(DATA_UP_DIR,d!=null?d.getName():Direction.UP.getName());
	}

	// -------------------- Bounding box --------------------

	public AABB buildPortalAABB(){
		double x=getX(),y=getY(),z=getZ();
		Direction facing=getFacing();
		Direction up=getUpDir();
		Vec3 upVec=dirVec(up);
		Vec3 rightVec=dirVec(facing).cross(upVec);

		// Tenká deska: 1/16 bloku tlustá, 1 blok široká, 2 bloky vysoká
		Vec3 halfThick=dirVec(facing).scale(0.03125);
		Vec3 halfWidth=rightVec.scale(0.5);
		Vec3 halfHeight=upVec.scale(1.0);

		double minX=x-Math.abs(halfThick.x)-Math.abs(halfWidth.x)-Math.abs(halfHeight.x);
		double maxX=x+Math.abs(halfThick.x)+Math.abs(halfWidth.x)+Math.abs(halfHeight.x);
		double minY=y-Math.abs(halfThick.y)-Math.abs(halfWidth.y)-Math.abs(halfHeight.y);
		double maxY=y+Math.abs(halfThick.y)+Math.abs(halfWidth.y)+Math.abs(halfHeight.y);
		double minZ=z-Math.abs(halfThick.z)-Math.abs(halfWidth.z)-Math.abs(halfHeight.z);
		double maxZ=z+Math.abs(halfThick.z)+Math.abs(halfWidth.z)+Math.abs(halfHeight.z);

		return new AABB(minX,minY,minZ,maxX,maxY,maxZ);
	}

	@Override
	protected @NotNull AABB makeBoundingBox(){
		return buildPortalAABB();
	}

	// -------------------- Footprint (bloky pod portálem) --------------------

	/**
	 * Vrátí sadu BlockPos na vzduchové straně portálu, které portál zabírá.
	 * Používá se pro ověření placementu i pro kontrolu podpory v ticku.
	 */
	public static Set<BlockPos> getPortalFootprint(Vec3 pos,Direction upDir,Direction facing){
		Vec3 upVec=dirVec(upDir);
		Vec3 rightVec=dirVec(facing).cross(upVec);

		Vec3 c1=pos.subtract(upVec).subtract(rightVec.scale(0.5));
		Vec3 c2=pos.subtract(upVec).add(rightVec.scale(0.5));
		Vec3 c3=pos.add(upVec).subtract(rightVec.scale(0.5));
		Vec3 c4=pos.add(upVec).add(rightVec.scale(0.5));

		double minX=min4(c1.x,c2.x,c3.x,c4.x);
		double maxX=max4(c1.x,c2.x,c3.x,c4.x);
		double minY=min4(c1.y,c2.y,c3.y,c4.y);
		double maxY=max4(c1.y,c2.y,c3.y,c4.y);
		double minZ=min4(c1.z,c2.z,c3.z,c4.z);
		double maxZ=max4(c1.z,c2.z,c3.z,c4.z);

		Set<BlockPos> set=new HashSet<>();
		int sx=(int)Math.floor(minX+1e-4), ex=(int)Math.floor(maxX-1e-4);
		int sy=(int)Math.floor(minY+1e-4), ey=(int)Math.floor(maxY-1e-4);
		int sz=(int)Math.floor(minZ+1e-4), ez=(int)Math.floor(maxZ-1e-4);
		for(int bx=sx;bx<=ex;bx++)
			for(int by=sy;by<=ey;by++)
				for(int bz=sz;bz<=ez;bz++)
					set.add(new BlockPos(bx,by,bz));
		return set;
	}

	private static double min4(double a,double b,double c,double d){ return Math.min(Math.min(a,b),Math.min(c,d)); }
	private static double max4(double a,double b,double c,double d){ return Math.max(Math.max(a,b),Math.max(c,d)); }

	// -------------------- Tick --------------------

	@Override
	public void tick(){
		super.tick();
		setBoundingBox(buildPortalAABB());
		if(level().isClientSide()) return;
		if(getOwner()==null){ discard(); return; }

		ServerLevel sl=(ServerLevel)level();

		// Kontrola podpory každé 2 ticky
		if(tickCount%2==0&&!checkSupport(sl)){
			PortalData.get(sl).remove(getOwner(),isBlue());
			discard();
			return;
		}

		if(!isLinked()) return;

		// Teleportace entit
		AABB box=getBoundingBox().inflate(0.1);
		long now=sl.getGameTime();

		// Hráči (vždy)
		for(Player p: sl.getEntitiesOfClass(Player.class,box)){
			if(isOnCooldown(p.getUUID(),now)) continue;
			teleport(p,sl,now,true);
		}

		// Ostatní entity (pokud povoleno v configu)
		int count=0;
		if(DifModServerConfig.PORTAL_ALLOW_ENTITIES.get()){
			for(Entity e: sl.getEntitiesOfClass(Entity.class,box,e->!(e instanceof Player)&&!(e instanceof PortalEntity)&&!(e instanceof Projectile))){
				if(count>=MAX_ENTITIES_PER_TICK) break;
				if(isOnCooldown(e.getUUID(),now)) continue;
				teleport(e,sl,now,false);
				count++;
			}
		}

		// Itemy (vždy)
		for(ItemEntity item: sl.getEntitiesOfClass(ItemEntity.class,box)){
			if(count>=MAX_ENTITIES_PER_TICK) break;
			if(isOnCooldown(item.getUUID(),now)) continue;
			teleport(item,sl,now,false);
			count++;
		}

		cooldowns.entrySet().removeIf(e->now-e.getValue()>200);
	}

	private boolean isOnCooldown(UUID id,long now){
		return cooldowns.containsKey(id)&&now-cooldowns.get(id)<=15;
	}

	private boolean checkSupport(ServerLevel sl){
		Set<BlockPos> blocks=getPortalFootprint(position(),getUpDir(),getFacing());
		Direction facing=getFacing();
		for(BlockPos p: blocks){
			BlockPos behind=p.relative(facing.getOpposite());
			if(!sl.getBlockState(behind).isFaceSturdy(sl,behind,facing)) return false;
		}
		return true;
	}

	// -------------------- Teleportace --------------------

	private void teleport(Entity entity,ServerLevel sl,long now,boolean isPlayer){
		BlockPos targetPos=PortalData.get(sl).getPos(getOwner(),!isBlue());
		if(targetPos==null){
			if(isPlayer) entity.sendSystemMessage(Component.literal("[!] Linked portal not found"));
			return;
		}
		int maxDist=DifModServerConfig.PORTAL_MAX_DISTANCE.get();
		if(blockPosition().distSqr(targetPos)>(long)maxDist*maxDist){
			if(isPlayer) ((Player)entity).displayClientMessage(Component.literal("[!] Portal too far away"),true);
			return;
		}
		if(!sl.isLoaded(targetPos)) return;

		PortalEntity other=findLinkedPortal(sl,targetPos);
		if(other==null){
			PortalData.get(sl).remove(getOwner(),!isBlue());
			return;
		}

		// Výpočet nové pozice, rotace a hybnosti
		Vec3 dest=calcDestination(other,entity);
		float yawDelta=calcYawDelta(this,other);
		float newYaw=entity.getYRot()+yawDelta;
		Vec3 newMotion=transformMotion(entity.getDeltaMovement(),this,other);

		if(isPlayer&&entity instanceof ServerPlayer sp){
			sp.teleportTo(sl,dest.x,dest.y,dest.z,Set.of(),newYaw,sp.getXRot());
			sp.setYBodyRot(newYaw);
			sp.setYHeadRot(newYaw);
			sp.yRotO=newYaw;
			sp.setDeltaMovement(newMotion);
			sp.hurtMarked=true;
			sp.connection.send(new ClientboundSetEntityMotionPacket(sp));
		}else{
			entity.teleportTo(dest.x,dest.y,dest.z);
			entity.setYRot(newYaw);
			entity.setYBodyRot(newYaw);
			entity.setYHeadRot(newYaw);
			entity.setDeltaMovement(newMotion);
			entity.hurtMarked=true;
		}

		other.lastTeleportTime=this.lastTeleportTime=now;
		other.cooldowns.put(entity.getUUID(),now);
	}

	// -------------------- Výstupní pozice --------------------

	private static Vec3 calcDestination(PortalEntity out,Entity entity){
		Vec3 center=out.position();
		Direction face=out.getFacing();
		Vec3 faceVec=dirVec(face);

		if(face==Direction.UP){
			// Na podlaze: entita se objeví nad portálem
			return center.add(0,0.05,0);
		}else if(face==Direction.DOWN){
			// Na stropě: entita se objeví pod portálem
			return new Vec3(center.x,center.y-entity.getBbHeight()-0.1,center.z);
		}else{
			// Na zdi: entita se objeví před portálem, nohy na spodku
			double dist=entity.getBbWidth()*0.5+0.1;
			return new Vec3(
					center.x+faceVec.x*dist,
					center.y-1.0+0.01,
					center.z+faceVec.z*dist
			);
		}
	}

	// -------------------- Rotace kamery --------------------

	/**
	 * Každý portál má „referenční yaw" – směr, kterým se hráč dívá, když stojí kolmo k portálu.
	 *
	 * Zeď:    Hráč se dívá DO portálu = opačný směr k facing → enterYaw = facing.opposite.toYRot
	 *         Hráč vychází VEN z portálu = směr facing → exitYaw = facing.toYRot
	 *
	 * Podlaha (UP): „Vpřed" portálu = upDir (dolní polovina je za tebou, horní před tebou)
	 *         enterYaw = upDir.opposite.toYRot (díváš se dolů do portálu, nohy směrem k upDir)
	 *         exitYaw  = upDir.opposite.toYRot
	 *
	 * Strop (DOWN): Zrcadlově oproti podlaze
	 *         enterYaw = upDir.toYRot
	 *         exitYaw  = upDir.toYRot
	 */
	private static float portalRefYaw(Direction facing,Direction upDir,boolean entering){
		if(facing==Direction.UP){
			// Podlaha: „vpřed" portálu = upDir
			return upDir.toYRot();
		}else if(facing==Direction.DOWN){
			// Strop
			return upDir.getOpposite().toYRot();
		}else{
			// Zeď
			return entering?facing.getOpposite().toYRot():facing.toYRot();
		}
	}

	private static float calcYawDelta(PortalEntity in,PortalEntity out){
		float enterRef=portalRefYaw(in.getFacing(),in.getUpDir(),true);
		float exitRef=portalRefYaw(out.getFacing(),out.getUpDir(),false);
		return exitRef-enterRef;
	}

	// -------------------- Transformace hybnosti --------------------

	/**
	 * Rozloží hybnost do lokálního systému vstupního portálu (normal, up, right)
	 * a přemapuje do lokálního systému výstupního portálu.
	 */
	private static Vec3 transformMotion(Vec3 vel,PortalEntity in,PortalEntity out){
		double speed=vel.length();
		if(speed<0.001) return vel;

		// Vstupní báze
		Vec3 inN=dirVec(in.getFacing());
		Vec3 inU=dirVec(in.getUpDir());
		Vec3 inR=inN.cross(inU);

		// Rozložení do lokálních os (komponenta „dovnitř" = záporný normálový směr)
		double cIn=vel.dot(inN.scale(-1));
		double cUp=vel.dot(inU);
		double cRi=vel.dot(inR);

		// Výstupní báze
		Vec3 outN=dirVec(out.getFacing());
		Vec3 outU=dirVec(out.getUpDir());
		Vec3 outR=outN.cross(outU);

		// Složení do výstupní báze (ven z portálu = kladný normálový směr)
		Vec3 result=outN.scale(Math.max(cIn,0.05))
				.add(outU.scale(cUp))
				.add(outR.scale(cRi));

		return result.lengthSqr()>0.001?result.normalize().scale(speed):outN.scale(speed);
	}

	// -------------------- Hledání protějšího portálu --------------------

	private PortalEntity findLinkedPortal(ServerLevel sl,BlockPos targetPos){
		List<PortalEntity> list=sl.getEntitiesOfClass(PortalEntity.class,new AABB(targetPos).inflate(2),
				p->getOwner().equals(p.getOwner())&&p.isBlue()!=isBlue());
		return list.isEmpty()?null:list.getFirst();
	}

	// -------------------- NBT --------------------

	@Override
	protected void readAdditionalSaveData(@NotNull CompoundTag tag){
		if(tag.hasUUID("owner")) setOwner(tag.getUUID("owner"));
		setIsBlue(tag.getBoolean("isBlue"));
		setIsLinked(tag.getBoolean("isLinked"));
		if(tag.contains("facing")) setFacing(Direction.byName(tag.getString("facing")));
		if(tag.contains("upDir")) setUpDir(Direction.byName(tag.getString("upDir")));
	}

	@Override
	protected void addAdditionalSaveData(@NotNull CompoundTag tag){
		if(getOwner()!=null) tag.putUUID("owner",getOwner());
		tag.putBoolean("isBlue",isBlue());
		tag.putBoolean("isLinked",isLinked());
		if(getFacing()!=null) tag.putString("facing",getFacing().getName());
		if(getUpDir()!=null) tag.putString("upDir",getUpDir().getName());
	}

	// -------------------- Správa portálů --------------------

	public static void removeOldPortal(ServerLevel sl,UUID owner,boolean isBlue){
		BlockPos pos=PortalData.get(sl).getPos(owner,isBlue);
		if(pos!=null){
			PortalData.get(sl).remove(owner,isBlue);
			boolean wasLoaded=sl.isLoaded(pos);
			if(!wasLoaded) sl.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);
			for(PortalEntity p: sl.getEntitiesOfClass(PortalEntity.class,new AABB(pos).inflate(2),
					e->owner.equals(e.getOwner())&&e.isBlue()==isBlue)){
				p.discard();
			}
		}
	}

	public static PortalEntity findPortal(ServerLevel sl,UUID owner,boolean isBlue){
		BlockPos pos=PortalData.get(sl).getPos(owner,isBlue);
		if(pos==null) return null;
		List<PortalEntity> list=sl.getEntitiesOfClass(PortalEntity.class,new AABB(pos).inflate(2),
				p->owner.equals(p.getOwner())&&p.isBlue()==isBlue);
		return list.isEmpty()?null:list.getFirst();
	}

	public static void updateLinks(ServerLevel sl,UUID owner){
		PortalEntity blue=findPortal(sl,owner,true);
		PortalEntity orange=findPortal(sl,owner,false);
		boolean linked=blue!=null&&orange!=null;
		if(blue!=null) blue.setIsLinked(linked);
		if(orange!=null) orange.setIsLinked(linked);
	}

	// -------------------- Lifecycle --------------------

	@Override
	public void onAddedToLevel(){
		super.onAddedToLevel();
		if(!level().isClientSide()){
			ServerLevel sl=(ServerLevel)level();
			sl.setChunkForced(chunkPosition().x,chunkPosition().z,true);
			BlockPos partnerPos=PortalData.get(sl).getPos(getOwner(),!isBlue());
			if(partnerPos!=null) sl.setChunkForced(partnerPos.getX()>>4,partnerPos.getZ()>>4,true);
			updateLinks(sl,getOwner());
		}
	}

	@Override
	public void onRemovedFromLevel(){
		super.onRemovedFromLevel();
		if(!level().isClientSide()){
			ServerLevel sl=(ServerLevel)level();
			sl.setChunkForced(chunkPosition().x,chunkPosition().z,false);
			BlockPos partnerPos=PortalData.get(sl).getPos(getOwner(),!isBlue());
			if(partnerPos!=null) sl.setChunkForced(partnerPos.getX()>>4,partnerPos.getZ()>>4,false);
			updateLinks(sl,getOwner());
		}
	}

	@Override
	public boolean hurt(@NotNull DamageSource source,float amount){
		if(!level().isClientSide()&&!isRemoved()){
			PortalData.get((ServerLevel)level()).remove(getOwner(),isBlue());
			discard();
			return true;
		}
		return false;
	}

	@Override
	public boolean isPickable(){
		return !isRemoved();
	}

	// -------------------- Utility --------------------

	private static Vec3 dirVec(Direction d){
		return new Vec3(d.getStepX(),d.getStepY(),d.getStepZ());
	}
}
