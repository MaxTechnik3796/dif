package cz.maxtechnik.dif.entity.portal;

import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
public class PortalEntity extends Entity{
	// Synched data
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Boolean> DATA_IS_BLUE=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_IS_LINKED=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<String> DATA_FACING=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.STRING);
	private static final EntityDataAccessor<String> DATA_UP_DIR=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.STRING);
	private static final int MAX_ENTITIES_PER_TICK=5;
	public long lastTeleportTime=0;
	private final Map<UUID,Long> cooldowns=new HashMap<>();
	// Constructors
	public PortalEntity(EntityType<?> type,Level level){
		super(type,level);
		this.noPhysics=true;
		setNoGravity(true);
	}
	public PortalEntity(Level level,UUID owner,boolean isBlue,Direction facing,Direction upDir,Vec3 pos){
		super(DifModEntities.PORTAL.get(),level);
		this.noPhysics=true;
		setNoGravity(true);
		setOwner(owner);
		setIsBlue(isBlue);
		setFacing(facing);
		setUpDir(upDir);
		setPos(pos);
		setOldPosAndRot();
		setBoundingBox(buildPortalAABB());
	}
	// Accessors
	@Override
	public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key){
		super.onSyncedDataUpdated(key);
		if(DATA_FACING.equals(key)||DATA_UP_DIR.equals(key)){
			setBoundingBox(buildPortalAABB());
		}
	}
	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder){
		builder.define(DATA_OWNER,Optional.empty());
		builder.define(DATA_IS_BLUE,true);
		builder.define(DATA_IS_LINKED,false);
		builder.define(DATA_FACING,Direction.NORTH.getName());
		builder.define(DATA_UP_DIR,Direction.UP.getName());
	}
	public UUID getOwner(){
		return entityData.get(DATA_OWNER).orElse(null);
	}
	public void setOwner(UUID uuid){
		entityData.set(DATA_OWNER,Optional.ofNullable(uuid));
	}
	public boolean isBlue(){
		return entityData.get(DATA_IS_BLUE);
	}
	public void setIsBlue(boolean isBlue){
		entityData.set(DATA_IS_BLUE,isBlue);
	}
	public boolean isLinked(){
		return entityData.get(DATA_IS_LINKED);
	}
	public void setIsLinked(boolean isLinked){
		entityData.set(DATA_IS_LINKED,isLinked);
	}
	public Direction getFacing(){
		return Direction.byName(entityData.get(DATA_FACING));
	}
	public void setFacing(Direction direction){
		entityData.set(DATA_FACING,direction!=null?direction.getName():Direction.NORTH.getName());
		setBoundingBox(buildPortalAABB());
	}
	public Direction getUpDir(){
		return Direction.byName(entityData.get(DATA_UP_DIR));
	}
	public void setUpDir(Direction direction){
		entityData.set(DATA_UP_DIR,direction!=null?direction.getName():Direction.UP.getName());
		setBoundingBox(buildPortalAABB());
	}
	// Bounding box
	public AABB buildPortalAABB(){
		double x=getX(),y=getY(),z=getZ();
		Vec3 normal=dirVec(getFacing());
		Vec3 upVec=dirVec(getUpDir());
		Vec3 rightVec=normal.cross(upVec);
		// Tenká deska: 1/16 bloku tlustá, 1 blok široká, 2 bloky vysoká
		// Bounding box se rozšiřuje od povrchu stěny dopředu do prostoru, ne dovnitř do bloku
		double inset=0.005;
		Vec3 thickVec=normal.scale(0.0625);
		Vec3 halfW=rightVec.scale(0.5-inset);
		Vec3 halfH=upVec.scale(1.0-inset);
		double minX=x+Math.min(0,thickVec.x)-Math.abs(halfW.x)-Math.abs(halfH.x);
		double maxX=x+Math.max(0,thickVec.x)+Math.abs(halfW.x)+Math.abs(halfH.x);
		double minY=y+Math.min(0,thickVec.y)-Math.abs(halfW.y)-Math.abs(halfH.y);
		double maxY=y+Math.max(0,thickVec.y)+Math.abs(halfW.y)+Math.abs(halfH.y);
		double minZ=z+Math.min(0,thickVec.z)-Math.abs(halfW.z)-Math.abs(halfH.z);
		double maxZ=z+Math.max(0,thickVec.z)+Math.abs(halfW.z)+Math.abs(halfH.z);
		return new AABB(minX,minY,minZ,maxX,maxY,maxZ);
	}
	@Override
	protected @NotNull AABB makeBoundingBox(){
		return buildPortalAABB();
	}
	// Tick
	@Override
	public void tick(){
		this.tickCount++;
		if(level().isClientSide()) return;
		if(getOwner()==null){
			discard();
			return;
		}
		ServerLevel sl=(ServerLevel)level();
		// Kontrola podpory každých 20 ticků (1x za sekundu)
		if(tickCount%20==0&&!PortalPlacement.isValidPosition(sl,position(),getUpDir(),getFacing())){
			PortalData.get(sl).remove(getOwner(),isBlue());
			discard();
			return;
		}
		if(!isLinked()) return;
		// Teleportace entit
		AABB box=getBoundingBox();
		long now=sl.getGameTime();
		boolean allowNonPlayers=DifModServerConfig.PORTAL_ALLOW_ENTITIES.get();
		int nonPlayerCount=0;
		for(Entity e:sl.getEntitiesOfClass(Entity.class,box,entity->!(entity instanceof PortalEntity))){
			if(isOnCooldown(e.getUUID(),now)) continue;
			if(e instanceof Player p){
				teleport(p,sl,now,true);
			}else if(allowNonPlayers&&nonPlayerCount<MAX_ENTITIES_PER_TICK){
				teleport(e,sl,now,false);
				nonPlayerCount++;
			}
		}
		if(!cooldowns.isEmpty()&&(tickCount&31)==0) cooldowns.values().removeIf(t->now-t>200);
	}
	private boolean isOnCooldown(UUID id,long now){
		return cooldowns.containsKey(id)&&now-cooldowns.get(id)<=15;
	}
	// Teleportace
	private void teleport(Entity entity,ServerLevel sl,long now,boolean isPlayer){
		PortalData data=PortalData.get(sl);
		BlockPos targetPos=data.getPos(getOwner(),!isBlue());
		if(targetPos==null){
			if(isPlayer) entity.sendSystemMessage(Component.literal("[!] Linked portal not found"));
			return;
		}
		int maxDist=DifModServerConfig.PORTAL_MAX_DISTANCE.get();
		if(blockPosition().distSqr(targetPos)>(long)maxDist*maxDist){
			if(isPlayer) ((Player)entity).displayClientMessage(Component.literal("[!] Portal too far away"),true);
			return;
		}
		PortalEntity other=data.findEntity(sl,getOwner(),!isBlue());
		if(other==null){
			data.remove(getOwner(),!isBlue());
			return;
		}
		Vec3 dest=calcDestination(other,entity);
		float newYaw=calcNewYaw(entity.getYRot(),this,other);
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
	// Výstupní pozice
	private static Vec3 calcDestination(PortalEntity out,Entity entity){
		Vec3 center=out.position();
		Direction face=out.getFacing();
		Vec3 faceVec=dirVec(face);
		if(face==Direction.UP) return center.add(0,0.05,0);
		if(face==Direction.DOWN) return new Vec3(center.x,center.y-entity.getBbHeight()-0.1,center.z);
		double dist=entity.getBbWidth()*0.5+0.1;
		return new Vec3(center.x+faceVec.x*dist,center.y-1.0+0.01,center.z+faceVec.z*dist);
	}
	// Rotace kamery – sloučeno z getEntryYaw/getExitYaw/calcNewYaw do jedné metody
	private static float calcNewYaw(float oldYaw,PortalEntity in,PortalEntity out){
		boolean inV=in.getFacing().getAxis()==Direction.Axis.Y;
		boolean outV=out.getFacing().getAxis()==Direction.Axis.Y;
		float inYaw=inV?in.getUpDir().toYRot():in.getFacing().getOpposite().toYRot();
		float outYaw=outV?out.getUpDir().getOpposite().toYRot():out.getFacing().toYRot();
		return Mth.wrapDegrees(outYaw+Mth.wrapDegrees(oldYaw-inYaw));
	}
	// Rotace a hybnost 3D
	private static Vec3 transformVector(Vec3 vec,PortalEntity in,PortalEntity out){
		Vec3 inN=dirVec(in.getFacing()),inU=dirVec(in.getUpDir()),inR=inN.cross(inU);
		double cN=-vec.dot(inN),cU=vec.dot(inU),cR=vec.dot(inR);
		Vec3 outN=dirVec(out.getFacing()),outU=dirVec(out.getUpDir()),outR=outN.cross(outU);
		return outN.scale(cN).add(outU.scale(cU)).add(outR.scale(cR));
	}
	private static Vec3 transformMotion(Vec3 vel,PortalEntity in,PortalEntity out){
		double speed=vel.length();
		if(speed<0.001) return vel;
		// Zajistit minimální složku rychlosti směrem do portálu
		Vec3 inNormal=dirVec(in.getFacing());
		double inward=-vel.dot(inNormal); // kladné = vstupuje do portálu
		if(inward<0.05) vel=vel.subtract(inNormal.scale(0.05-inward));
		Vec3 result=transformVector(vel,in,out);
		return result.lengthSqr()>0.001?result.normalize().scale(speed):dirVec(out.getFacing()).scale(speed);
	}
	// NBT
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
	// Lifecycle
	@Override
	public void onAddedToLevel(){
		super.onAddedToLevel();
		if(!level().isClientSide()){
			ServerLevel sl=(ServerLevel)level();
			sl.setChunkForced(chunkPosition().x,chunkPosition().z,true);
			PortalData data=PortalData.get(sl);
			BlockPos partnerPos=data.getPos(getOwner(),!isBlue());
			if(partnerPos!=null) sl.setChunkForced(partnerPos.getX()>>4,partnerPos.getZ()>>4,true);
			data.updateLinks(sl,getOwner());
		}
	}
	@Override
	public void onRemovedFromLevel(){
		super.onRemovedFromLevel();
		if(!level().isClientSide()){
			ServerLevel sl=(ServerLevel)level();
			sl.setChunkForced(chunkPosition().x,chunkPosition().z,false);
			PortalData.get(sl).updateLinks(sl,getOwner());
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
	// Fyzická imunita – portál nelze posunout, odstrčit, rozstřelit ani stáhnout vodou
	@Override public boolean isPickable(){ return !isRemoved(); }
	@Override public boolean canCollideWith(@NotNull Entity entity){ return false; }
	@Override public void push(@NotNull Entity entity){}
	@Override public void push(double x,double y,double z){}
	@Override public void setDeltaMovement(@NotNull Vec3 motion){ super.setDeltaMovement(Vec3.ZERO); }
	@Override public void setDeltaMovement(double x,double y,double z){ super.setDeltaMovement(Vec3.ZERO); }
	@Override public void move(@NotNull MoverType type,@NotNull Vec3 pos){}
	@Override public @NotNull PushReaction getPistonPushReaction(){ return PushReaction.IGNORE; }
	@Override public boolean isNoGravity(){ return true; }
	@Override public boolean isPushedByFluid(@NotNull FluidType type){ return false; }
	@Override public boolean ignoreExplosion(@NotNull Explosion explosion){ return true; }
	@Override public void lerpTo(double x,double y,double z,float yRot,float xRot,int steps){}
	// Utility
	private static final Vec3[] DIR_VECS=Arrays.stream(Direction.values())
			.map(d->new Vec3(d.getStepX(),d.getStepY(),d.getStepZ()))
			.toArray(Vec3[]::new);
	static Vec3 dirVec(Direction direction){
		return direction!=null?DIR_VECS[direction.ordinal()]:Vec3.ZERO;
	}
}
