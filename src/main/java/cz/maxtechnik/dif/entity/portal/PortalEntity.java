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
	protected void defineSynchedData(SynchedEntityData.Builder builder){
		builder.define(DATA_OWNER,Optional.empty());
		builder.define(DATA_IS_BLUE,true);
		builder.define(DATA_IS_LINKED,false);
		builder.define(DATA_FACING,Direction.NORTH.getName());
		builder.define(DATA_UP_DIR,Direction.UP.getName());
	}
	@Override
	public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key){
		super.onSyncedDataUpdated(key);
		if(DATA_FACING.equals(key)||DATA_UP_DIR.equals(key)) setBoundingBox(buildPortalAABB());
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
		Direction direction=Direction.byName(entityData.get(DATA_UP_DIR));
		return direction!=null?direction:Direction.UP;
	}
	public void setUpDir(Direction direction){
		entityData.set(DATA_UP_DIR,direction!=null?direction.getName():Direction.UP.getName());
		setBoundingBox(buildPortalAABB());
	}
	public AABB buildPortalAABB(){
		return buildPortalAABB(getX(),getY(),getZ(),getFacing(),getUpDir());
	}
	public static AABB buildPortalAABB(double x,double y,double z,Direction facing,Direction upDir){
		Direction.Axis normalAxis=facing.getAxis();
		Direction.Axis heightAxis=upDir.getAxis();
		double rx=getAxisSize(Direction.Axis.X,normalAxis,heightAxis);
		double ry=getAxisSize(Direction.Axis.Y,normalAxis,heightAxis);
		double rz=getAxisSize(Direction.Axis.Z,normalAxis,heightAxis);
		double cx=x+facing.getStepX()*0.03;
		double cy=y+facing.getStepY()*0.03;
		double cz=z+facing.getStepZ()*0.03;
		return new AABB(cx-rx,cy-ry,cz-rz,cx+rx,cy+ry,cz+rz);
	}
	private static double getAxisSize(Direction.Axis axis,Direction.Axis normalAxis,Direction.Axis heightAxis){
		if(axis.equals(normalAxis)) return 0.03;
		if(axis.equals(heightAxis)) return 0.995;
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
		ServerLevel serverLevel=(ServerLevel)level();
		if(tickCount%20==0&&!isValidPosition(serverLevel,getX(),getY(),getZ(),getFacing(),getUpDir())){
			PortalData.get(serverLevel).remove(getOwner(),isBlue());
			discard();
			return;
		}
		if(!isLinked()) return;
		long now=serverLevel.getGameTime();
		boolean allowNonPlayers=DifModServerConfig.PORTAL_ALLOW_ENTITIES.get();
		int count=0;
		for(Entity entity: serverLevel.getEntitiesOfClass(Entity.class,getBoundingBox(),ent->!(ent instanceof PortalEntity))){
			if(cooldowns.getOrDefault(entity.getUUID(),0L)+15>=now){
				cooldowns.put(entity.getUUID(),now);
				continue;
			}
			if(entity instanceof Player player) teleport(player,serverLevel,now,true);
			else if(allowNonPlayers&&count<MAX_ENTITIES_PER_TICK){
				teleport(entity,serverLevel,now,false);
				count++;
			}
		}
		if(!cooldowns.isEmpty()&&tickCount%32==0) cooldowns.values().removeIf(t->now-t>200);
	}
	private void teleport(Entity entity,ServerLevel serverLevel,long now,boolean isPlayer){
		PortalData data=PortalData.get(serverLevel);
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
		PortalEntity other=data.findEntity(serverLevel,getOwner(),!isBlue());
		if(other==null){
			data.remove(getOwner(),!isBlue());
			return;
		}
		Direction outFace=other.getFacing();
		double destX=other.getX();
		double destY=other.getY();
		double destZ=other.getZ();
		if(outFace.equals(Direction.UP)) destY+=0.07;
		else if(outFace==Direction.DOWN) destY-=entity.getBbHeight()+0.1;
		else{
			double offset=entity.getBbWidth()*0.5+0.1;
			destX+=outFace.getStepX()*offset;
			destY-=0.99;
			destZ+=outFace.getStepZ()*offset;
		}
		float newYaw=calcNewYaw(entity.getYRot(),this,other);
		float newPitch=entity.getXRot();
		if(getFacing().getAxis()!=outFace.getAxis()){
			if(outFace==Direction.UP) newPitch=-90F;
			else if(outFace==Direction.DOWN) newPitch=90F;
			else if(getFacing().getAxis()==Direction.Axis.Y) newPitch=0F;
		}
		double speed=Math.max(entity.getDeltaMovement().length(),outFace==Direction.UP?0.3:0.0);
		Vec3 newMotion=speed<0.05?Vec3.ZERO:new Vec3(outFace.getStepX()*speed,outFace.getStepY()*speed,outFace.getStepZ()*speed);
		if(isPlayer&&entity instanceof ServerPlayer serverPlayer){
			serverPlayer.teleportTo(serverLevel,destX,destY,destZ,Set.of(),newYaw,newPitch);
			serverPlayer.setYBodyRot(newYaw);
			serverPlayer.setYHeadRot(newYaw);
			serverPlayer.yRotO=newYaw;
			serverPlayer.setDeltaMovement(newMotion);
			serverPlayer.hurtMarked=true;
			serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
		}else{
			entity.teleportTo(destX,destY,destZ);
			entity.setYRot(newYaw);
			entity.setXRot(newPitch);
			entity.setYBodyRot(newYaw);
			entity.setYHeadRot(newYaw);
			entity.setDeltaMovement(newMotion);
			entity.hurtMarked=true;
		}
		other.lastTeleportTime=this.lastTeleportTime=now;
		this.cooldowns.put(entity.getUUID(),now);
		other.cooldowns.put(entity.getUUID(),now);
	}
	private static float calcNewYaw(float oldYaw,PortalEntity portalIn,PortalEntity portalOut){
		float inYaw=(portalIn.getFacing().getAxis()==Direction.Axis.Y)?portalIn.getUpDir().toYRot():portalIn.getFacing().getOpposite().toYRot();
		float outYaw=(portalOut.getFacing().getAxis()==Direction.Axis.Y)?portalOut.getUpDir().getOpposite().toYRot():portalOut.getFacing().toYRot();
		return Mth.wrapDegrees(outYaw+Mth.wrapDegrees(oldYaw-inYaw));
	}
	private static double snapPixel(double value){
		return Math.round(value*16D)/16D;
	}
	private static double[] getCandidatesForAxis(Direction.Axis axis,Direction face,Direction upDir,double blockCenter,double hitCoord){
		if(axis.equals(face.getAxis())){
			int step=face.getAxisDirection().getStep();
			return new double[]{blockCenter+step*0.505};
		}
		double snapped=snapPixel(Math.clamp(hitCoord,blockCenter-0.5,blockCenter+0.5));
		if(axis.equals(upDir.getAxis())){
			return new double[]{snapped,blockCenter+0.5,blockCenter-0.5,blockCenter};
		}
		return new double[]{snapped,blockCenter};
	}
	public static Vec3 findSpawnPos(ServerLevel world,BlockPos hitPos,Direction face,Direction upDir,Vec3 hitLoc){
		double[] xList=getCandidatesForAxis(Direction.Axis.X,face,upDir,hitPos.getX()+0.5,hitLoc.x);
		double[] yList=getCandidatesForAxis(Direction.Axis.Y,face,upDir,hitPos.getY()+0.5,hitLoc.y);
		double[] zList=getCandidatesForAxis(Direction.Axis.Z,face,upDir,hitPos.getZ()+0.5,hitLoc.z);
		for(double x: xList) for(double y: yList) for(double z: zList) if(isValidPosition(world,x,y,z,face,upDir)) return new Vec3(x,y,z);
		return null;
	}
	public static boolean isValidPosition(ServerLevel serverLevel,double x,double y,double z,Direction face,Direction upDir){
		AABB box=buildPortalAABB(x,y,z,face,upDir);
		int minX=Mth.floor(box.minX+0.001), maxX=Mth.floor(box.maxX-0.001);
		int minY=Mth.floor(box.minY+0.001), maxY=Mth.floor(box.maxY-0.001);
		int minZ=Mth.floor(box.minZ+0.001), maxZ=Mth.floor(box.maxZ-0.001);
		for(int bx=minX;bx<=maxX;bx++){
			for(int by=minY;by<=maxY;by++){
				for(int bz=minZ;bz<=maxZ;bz++){
					BlockPos frontPos=new BlockPos(bx,by,bz);
					BlockPos wallPos=frontPos.relative(face.getOpposite());
					if(!serverLevel.getBlockState(wallPos).isFaceSturdy(serverLevel,wallPos,face)) return false;
					if(!serverLevel.isEmptyBlock(frontPos)&&!serverLevel.getBlockState(frontPos).canBeReplaced()) return false;
				}
			}
		}
		return true;
	}
	public static boolean hasOverlap(ServerLevel serverLevel,AABB box,UUID owner,boolean isBlue){
		for(PortalEntity o: serverLevel.getEntitiesOfClass(PortalEntity.class,box.inflate(0.05))){
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
	@Override
	public boolean isPickable(){
		return !isRemoved();
	}
	@Override
	public boolean canCollideWith(@NotNull Entity entity){
		return false;
	}
	@Override
	public void push(@NotNull Entity entity){
	}
	@Override
	public void push(double x,double y,double z){
	}
	@Override
	public void setDeltaMovement(@NotNull Vec3 motion){
		super.setDeltaMovement(Vec3.ZERO);
	}
	@Override
	public void setDeltaMovement(double x,double y,double z){
		super.setDeltaMovement(Vec3.ZERO);
	}
	@Override
	public void move(@NotNull MoverType type,@NotNull Vec3 pos){
	}
	@Override
	public @NotNull PushReaction getPistonPushReaction(){
		return PushReaction.IGNORE;
	}
	@Override
	public boolean isNoGravity(){
		return true;
	}
	@Override
	public boolean isPushedByFluid(@NotNull FluidType type){
		return false;
	}
	@Override
	public boolean ignoreExplosion(@NotNull Explosion explosion){
		return true;
	}
	@Override
	public void lerpTo(double x,double y,double z,float yRot,float xRot,int steps){
	}
}
