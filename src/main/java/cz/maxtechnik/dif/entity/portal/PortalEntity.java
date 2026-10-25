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
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Boolean> DATA_IS_BLUE=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_IS_LINKED=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<String> DATA_FACING=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.STRING);
	private static final EntityDataAccessor<String> DATA_UP_DIR=SynchedEntityData.defineId(PortalEntity.class,EntityDataSerializers.STRING);
	private static final int MAX_ENTITIES_PER_TICK=5;
	public long lastTeleportTime=0;
	private final Map<UUID,Long> cooldowns=new HashMap<>();
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
	@Override
	public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key){
		super.onSyncedDataUpdated(key);
		if(DATA_FACING.equals(key)||DATA_UP_DIR.equals(key)) setBoundingBox(buildPortalAABB());
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
		Direction d=Direction.byName(entityData.get(DATA_FACING));
		return d!=null?d:Direction.NORTH;
	}
	public void setFacing(Direction direction){
		entityData.set(DATA_FACING,direction!=null?direction.getName():Direction.NORTH.getName());
		setBoundingBox(buildPortalAABB());
	}
	public Direction getUpDir(){
		Direction d=Direction.byName(entityData.get(DATA_UP_DIR));
		return d!=null?d:Direction.UP;
	}
	public void setUpDir(Direction direction){
		entityData.set(DATA_UP_DIR,direction!=null?direction.getName():Direction.UP.getName());
		setBoundingBox(buildPortalAABB());
	}
	// Výpočet hitboxu portálu podle os (tloušťka 0.0625, výška 2 bloky, šířka 1 blok)
	public AABB buildPortalAABB(){
		Direction.Axis normalAxis=getFacing().getAxis();
		Direction.Axis heightAxis=getUpDir().getAxis();
		double rx=getAxisRadius(Direction.Axis.X,normalAxis,heightAxis);
		double ry=getAxisRadius(Direction.Axis.Y,normalAxis,heightAxis);
		double rz=getAxisRadius(Direction.Axis.Z,normalAxis,heightAxis);
		double cx=getX()+getFacing().getStepX()*0.03;
		double cy=getY()+getFacing().getStepY()*0.03;
		double cz=getZ()+getFacing().getStepZ()*0.03;
		return new AABB(cx-rx,cy-ry,cz-rz,cx+rx,cy+ry,cz+rz);
	}
	private static double getAxisRadius(Direction.Axis axis,Direction.Axis normalAxis,Direction.Axis heightAxis){
		if(axis==normalAxis) return 0.035;
		if(axis==heightAxis) return 0.995;
		return 0.495;
	}
	@Override
	protected @NotNull AABB makeBoundingBox(){
		return buildPortalAABB();
	}
	@Override
	public void tick(){
		this.tickCount++;
		if(level().isClientSide()) return;
		if(getOwner()==null){
			discard();
			return;
		}
		ServerLevel sl=(ServerLevel)level();
		if(tickCount%20==0&&!isStillSupported(sl)){
			PortalData.get(sl).remove(getOwner(),isBlue());
			discard();
			return;
		}
		if(!isLinked()) return;
		long now=sl.getGameTime();
		boolean allowNonPlayers=DifModServerConfig.PORTAL_ALLOW_ENTITIES.get();
		int nonPlayerCount=0;
		for(Entity e: sl.getEntitiesOfClass(Entity.class,getBoundingBox(),entity->!(entity instanceof PortalEntity))){
			if(cooldowns.getOrDefault(e.getUUID(),0L)+15>=now) continue;
			if(e instanceof Player player){
				teleport(player,sl,now,true);
			}else if(allowNonPlayers&&nonPlayerCount<MAX_ENTITIES_PER_TICK){
				teleport(e,sl,now,false);
				nonPlayerCount++;
			}
		}
		if(!cooldowns.isEmpty()&&(tickCount&31)==0) cooldowns.values().removeIf(t->now-t>200);
	}
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
		Vec3 newMotion=calcExitMotion(entity.getDeltaMovement(),other);
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
	private static Vec3 calcDestination(PortalEntity out,Entity entity){
		Vec3 center=out.position();
		Direction face=out.getFacing();
		if(face==Direction.UP) return center.add(0,0.05,0);
		if(face==Direction.DOWN) return new Vec3(center.x,center.y-entity.getBbHeight()-0.1,center.z);
		double dist=entity.getBbWidth()*0.5+0.1;
		return new Vec3(center.x+face.getStepX()*dist,center.y-0.99,center.z+face.getStepZ()*dist);
	}
	private static float calcNewYaw(float oldYaw,PortalEntity in,PortalEntity out){
		float inYaw=(in.getFacing().getAxis()==Direction.Axis.Y)?in.getUpDir().toYRot():in.getFacing().getOpposite().toYRot();
		float outYaw=(out.getFacing().getAxis()==Direction.Axis.Y)?out.getUpDir().getOpposite().toYRot():out.getFacing().toYRot();
		return Mth.wrapDegrees(outYaw+Mth.wrapDegrees(oldYaw-inYaw));
	}
	// Zachování rychlosti a přesměrování ven z výstupního portálu
	private static Vec3 calcExitMotion(Vec3 oldMotion,PortalEntity out){
		double speed=oldMotion.length();
		if(speed<0.05) return Vec3.ZERO;
		Direction outFace=out.getFacing();
		return new Vec3(outFace.getStepX()*speed,outFace.getStepY()*speed,outFace.getStepZ()*speed);
	}
	// Kontrola 2 bloků (spodní a horní půlka portálu)
	private boolean isStillSupported(ServerLevel world){
		BlockPos bottomAir=BlockPos.containing(position().relative(getUpDir().getOpposite(),0.5).relative(getFacing(),0.2));
		return canPlaceAt(world,bottomAir,getFacing(),getUpDir());
	}
	public static boolean canPlaceAt(ServerLevel world,BlockPos bottomAir,Direction face,Direction upDir){
		BlockPos topAir=bottomAir.relative(upDir);
		BlockPos bottomWall=bottomAir.relative(face.getOpposite());
		BlockPos topWall=topAir.relative(face.getOpposite());
		boolean airFree=(world.isEmptyBlock(bottomAir)||world.getBlockState(bottomAir).canBeReplaced())
				&&(world.isEmptyBlock(topAir)||world.getBlockState(topAir).canBeReplaced());
		boolean wallSturdy=world.getBlockState(bottomWall).isFaceSturdy(world,bottomWall,face)
				&&world.getBlockState(topWall).isFaceSturdy(world,topWall,face);
		return airFree&&wallSturdy;
	}
	// Najde pozici pro 1x2 portál na kliknutém bloku (zkusí směrem nahoru/dopředu, případně o 1 blok níž)
	public static Vec3 findPlacementPos(ServerLevel world,BlockPos hitPos,Direction face,Direction upDir){
		BlockPos clickedAir=hitPos.relative(face);
		BlockPos bottomAir= null;
		if(canPlaceAt(world,clickedAir,face,upDir)){
			bottomAir=clickedAir;
		}else if(canPlaceAt(world,clickedAir.relative(upDir.getOpposite()),face,upDir)){
			bottomAir=clickedAir.relative(upDir.getOpposite());
		}
		if(bottomAir==null) return null;
		BlockPos topAir=bottomAir.relative(upDir);
		Vec3 mid=Vec3.atCenterOf(bottomAir).add(Vec3.atCenterOf(topAir)).scale(0.5);
		return mid.relative(face.getOpposite(),0.495);
	}
	public static boolean hasOverlap(ServerLevel world,AABB box,UUID owner,boolean isBlue){
		for(PortalEntity o: world.getEntitiesOfClass(PortalEntity.class,box.inflate(0.05))){
			if(owner.equals(o.getOwner())&&o.isBlue()==isBlue) continue;
			if(box.intersects(o.getBoundingBox())) return true;
		}
		return false;
	}
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
		tag.putString("facing",getFacing().getName());
		tag.putString("upDir",getUpDir().getName());
	}
	@Override
	public void onAddedToLevel(){
		super.onAddedToLevel();
		if(level() instanceof ServerLevel sl){
			sl.setChunkForced(chunkPosition().x,chunkPosition().z,true);
			PortalData.get(sl).updateLinks(sl,getOwner());
		}
	}
	@Override
	public void onRemovedFromLevel(){
		super.onRemovedFromLevel();
		if(level() instanceof ServerLevel sl){
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
	@Override public boolean isPickable(){return !isRemoved();}
	@Override public boolean canCollideWith(@NotNull Entity entity){return false;}
	@Override public void push(@NotNull Entity entity){}
	@Override public void push(double x,double y,double z){}
	@Override public void setDeltaMovement(@NotNull Vec3 motion){super.setDeltaMovement(Vec3.ZERO);}
	@Override public void setDeltaMovement(double x,double y,double z){super.setDeltaMovement(Vec3.ZERO);}
	@Override public void move(@NotNull MoverType type,@NotNull Vec3 pos){}
	@Override public @NotNull PushReaction getPistonPushReaction(){return PushReaction.IGNORE;}
	@Override public boolean isNoGravity(){return true;}
	@Override public boolean isPushedByFluid(@NotNull FluidType type){return false;}
	@Override public boolean ignoreExplosion(@NotNull Explosion explosion){return true;}
	@Override public void lerpTo(double x,double y,double z,float yRot,float xRot,int steps){}
}

