package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.block.PortalBlock;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
public class PortalBlockEntity extends BlockEntity{
	private UUID owner;
	private boolean isBlue;
	private Direction facing;
	public long lastTeleportTime=0;
	private int linkedCheckTimer=0;
	private static final int LINKED_CHECK_INTERVAL=40;
	private static final Map<UUID,Long> waitingPlayers=new HashMap<>();
	private static final Map<UUID,Long> entityCooldowns=new HashMap<>();
	public UUID getOwner(){
		return owner;
	}
	public boolean isBlue(){
		return isBlue;
	}
	public PortalBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.PORTAL.get(),pos,blockState);
	}
	public void setup(UUID owner,boolean isBlue,Direction facing){
		this.owner=owner;
		this.isBlue=isBlue;
		this.facing=facing;
		this.setChanged();
	}
	public static void tick(Level level,BlockPos pos,PortalBlockEntity blockEntity){
		if(blockEntity.owner==null) return;
		if(!(level instanceof ServerLevel serverLevel)) return;
		if(!pos.equals(getPortal(serverLevel,blockEntity.owner,blockEntity.isBlue))) return;
		BlockState currentState=level.getBlockState(pos);
		if(++blockEntity.linkedCheckTimer>=LINKED_CHECK_INTERVAL){
			blockEntity.linkedCheckTimer=0;
			boolean linked=getPortal(serverLevel,blockEntity.owner,!blockEntity.isBlue)!=null;
			boolean currentLinked=currentState.getValue(PortalBlock.IS_LINKED);
			if(linked!=currentLinked){
				BlockState newState=currentState.setValue(PortalBlock.IS_LINKED,linked);
				level.setBlock(pos,newState,3);
				BlockPos extPos=pos.relative(currentState.getValue(PortalBlock.EXTENSION_DIR));
				BlockState extState=level.getBlockState(extPos);
				if(extState.is(currentState.getBlock())&&extState.getValue(PortalBlock.HALF)==DoubleBlockHalf.UPPER) level.setBlock(extPos,extState.setValue(PortalBlock.IS_LINKED,linked),3);
				currentState=newState;
			}
		}
		if(!currentState.getValue(PortalBlock.IS_LINKED)) return;
		AABB box=currentState.getShape(level,pos).bounds().move(pos);
		BlockPos extPos=pos.relative(currentState.getValue(PortalBlock.EXTENSION_DIR));
		if(level.getBlockState(extPos).is(currentState.getBlock())) box=box.minmax(level.getBlockState(extPos).getShape(level,extPos).bounds().move(extPos));
		long now=level.getGameTime();
		List<Player> players=level.getEntitiesOfClass(Player.class,box);
		for(Player p: players){
			UUID pid=p.getUUID();
			if(now-blockEntity.lastTeleportTime<=DifModCommonConfig.PORTAL_TELEPORT_COOLDOWN.get()) continue;
			if(entityCooldowns.containsKey(pid)&&now-entityCooldowns.get(pid)<=DifModCommonConfig.PORTAL_TELEPORT_COOLDOWN.get()) continue;
			blockEntity.tryTeleportPlayer(p,serverLevel,now);
		}
		if(DifModCommonConfig.PORTAL_ALLOW_ENTITIES.get()){
			List<LivingEntity> mobs=level.getEntitiesOfClass(LivingEntity.class,box);
			int count=0;
			for(LivingEntity mob: mobs){
				if(mob instanceof Player) continue;
				if(count>=DifModCommonConfig.PORTAL_MAX_ENTITIES_PER_TICK.get()) break;
				UUID mid=mob.getUUID();
				if(entityCooldowns.containsKey(mid)&&now-entityCooldowns.get(mid)<=DifModCommonConfig.PORTAL_TELEPORT_COOLDOWN.get()) continue;
				blockEntity.tryTeleportEntity(mob,serverLevel,now,false);
				count++;
			}
			List<Entity> misc=level.getEntitiesOfClass(Entity.class,box,e->!(e instanceof LivingEntity)&&!(e instanceof Projectile)&&!(e instanceof ItemEntity));
			for(Entity e: misc){
				if(count>=DifModCommonConfig.PORTAL_MAX_ENTITIES_PER_TICK.get()) break;
				UUID eid=e.getUUID();
				if(entityCooldowns.containsKey(eid)&&now-entityCooldowns.get(eid)<=DifModCommonConfig.PORTAL_TELEPORT_COOLDOWN.get()) continue;
				blockEntity.tryTeleportEntity(e,serverLevel,now,false);
				count++;
			}
		}
		if(DifModCommonConfig.PORTAL_ALLOW_ITEMS.get()){
			List<ItemEntity> items=level.getEntitiesOfClass(ItemEntity.class,box);
			int count=0;
			for(ItemEntity e: items){
				if(count>=DifModCommonConfig.PORTAL_MAX_ENTITIES_PER_TICK.get()) break;
				UUID eid=e.getUUID();
				if(entityCooldowns.containsKey(eid)&&now-entityCooldowns.get(eid)<=10) continue;
				blockEntity.tryTeleportEntity(e,serverLevel,now,true);
				count++;
			}
		}
		entityCooldowns.entrySet().removeIf(e->now-e.getValue()>200);
	}
	private void tryTeleportPlayer(Player p,ServerLevel sl,long now){
		UUID pid=p.getUUID();
		BlockPos target=getPortal(sl,this.owner,!this.isBlue);
		if(target==null){
			p.displayClientMessage(Component.literal("[!] Linked portal not found"),true);
			waitingPlayers.remove(pid);
			return;
		}
		if(waitingPlayers.containsKey(pid)){
			assert level!=null;
			AABB box=this.getBlockState().getShape(level,worldPosition).bounds().move(worldPosition);
			BlockPos extPos=worldPosition.relative(this.getBlockState().getValue(PortalBlock.EXTENSION_DIR));
			if(level.getBlockState(extPos).is(this.getBlockState().getBlock())) box=box.minmax(level.getBlockState(extPos).getShape(level,extPos).bounds().move(extPos));
			box=box.inflate(1.5);
			if(!box.contains(p.position())){
				waitingPlayers.remove(pid);
				return;
			}
		}
		if(this.worldPosition.distSqr(target)>(long)DifModCommonConfig.PORTAL_MAX_DISTANCE.get()*DifModCommonConfig.PORTAL_MAX_DISTANCE.get()){
			p.displayClientMessage(Component.literal("[!] Portal too far away"),true);
			waitingPlayers.remove(pid);
			return;
		}
		if(!sl.isLoaded(target)){
			long startTick=waitingPlayers.getOrDefault(pid,now);
			if(!waitingPlayers.containsKey(pid)) waitingPlayers.put(pid,now);
			p.displayClientMessage(Component.literal("Please wait..."),true);
			sl.setChunkForced(target.getX()>>4,target.getZ()>>4,true);
			if(now-startTick>DifModCommonConfig.PORTAL_CHUNK_LOAD_TIMEOUT.get()){
				p.displayClientMessage(Component.literal("[!] Portal unreachable"),true);
				sl.setChunkForced(target.getX()>>4,target.getZ()>>4,false);
				waitingPlayers.remove(pid);
			}
			return;
		}
		sl.setChunkForced(target.getX()>>4,target.getZ()>>4,false);
		waitingPlayers.remove(pid);
		if(!(sl.getBlockEntity(target) instanceof PortalBlockEntity other)){
			PortalData.get(sl).remove(this.owner,!this.isBlue);
			p.displayClientMessage(Component.literal("[!] Linked portal not found"),true);
			return;
		}
		double tx=target.getX()+0.5-(other.facing.getStepX()*0.1875);
		double ty=target.getY()+(other.facing==Direction.UP?0.1:(other.facing==Direction.DOWN?-2:0));
		double tz=target.getZ()+0.5-(other.facing.getStepZ()*0.1875);
		p.teleportTo(tx,ty,tz);
		p.setYRot(other.facing.toYRot());
		assert level!=null;
		level.playSound(null,this.worldPosition,SoundEvents.BEACON_POWER_SELECT,SoundSource.BLOCKS,1F,1.2F);
		level.playSound(null,target,SoundEvents.BEACON_POWER_SELECT,SoundSource.BLOCKS,1F,1.2F);
		other.lastTeleportTime=this.lastTeleportTime=now;
		entityCooldowns.put(pid,now);
	}
	private void tryTeleportEntity(Entity entity,ServerLevel sl,long now,boolean isItem){
		BlockPos target=getPortal(sl,this.owner,!this.isBlue);
		if(target==null) return;
		if(!(sl.getBlockEntity(target) instanceof PortalBlockEntity other)){
			PortalData.get(sl).remove(this.owner,!this.isBlue);
			return;
		}
		if(this.worldPosition.distSqr(target)>(long)DifModCommonConfig.PORTAL_MAX_DISTANCE.get()*DifModCommonConfig.PORTAL_MAX_DISTANCE.get())
			return;
		if(!sl.isLoaded(target)) return;
		Vec3 newMotion=transformVelocity(entity.getDeltaMovement(),this.facing,other.facing);
		double offsetScale=isItem?0.3:0.6;
		double tx=target.getX()+0.5+(other.facing.getStepX()*offsetScale);
		double ty=target.getY()+0.5+(other.facing.getStepY()*offsetScale);
		double tz=target.getZ()+0.5+(other.facing.getStepZ()*offsetScale);
		if(!isItem&&entity instanceof LivingEntity living) ty=target.getY()+(other.facing==Direction.UP?0.5:(other.facing==Direction.DOWN?-living.getBbHeight():0));
		entity.teleportTo(tx,ty,tz);
		entity.setDeltaMovement(newMotion);
		entity.hurtMarked=true;
		entityCooldowns.put(entity.getUUID(),now);
		assert level!=null;
		level.playSound(null,target,SoundEvents.BEACON_POWER_SELECT,SoundSource.BLOCKS,0.5F,1.4F);
	}
	private static Vec3 transformVelocity(Vec3 velocity,Direction inFacing,Direction outFacing){
		double speed=velocity.length();
		if(speed<0.001) return velocity;
		Vec3 norm=velocity.normalize();
		Vec3 inAxis=dirToVec(inFacing);
		Vec3 outAxis=dirToVec(outFacing.getOpposite());
		Vec3 transformed=rotateVector(norm,inAxis,outAxis);
		return transformed.scale(speed);
	}
	private static Vec3 dirToVec(Direction d){
		return new Vec3(d.getStepX(),d.getStepY(),d.getStepZ());
	}
	private static Vec3 rotateVector(Vec3 v,Vec3 from,Vec3 to){
		Vec3 axis=from.cross(to);
		double sinAngle=axis.length();
		double cosAngle=from.dot(to);
		if(sinAngle<0.001){
			if(cosAngle>0) return v;
			return v.scale(-1);
		}
		axis=axis.normalize();
		return v.scale(cosAngle).add(axis.cross(v).scale(sinAngle)).add(axis.scale(axis.dot(v)*(1-cosAngle)));
	}
	public static void savePortal(ServerLevel serverLevel,UUID uuid,boolean b,BlockPos pos){
		PortalData.get(serverLevel).set(uuid,b,pos);
	}
	public static BlockPos getPortal(ServerLevel serverLevel,UUID uuid,boolean b){
		return PortalData.get(serverLevel).getPos(uuid,b);
	}
	public static void removeOldPortal(ServerLevel serverLevel,UUID uuid,boolean b){
		BlockPos portal=getPortal(serverLevel,uuid,b);
		if(portal==null) return;
		PortalData.get(serverLevel).remove(uuid,b);
		if(serverLevel.isLoaded(portal)) if(serverLevel.getBlockEntity(portal) instanceof PortalBlockEntity) serverLevel.destroyBlock(portal,false);
		else{
			serverLevel.setChunkForced(portal.getX()>>4,portal.getZ()>>4,true);
			if(serverLevel.getBlockEntity(portal) instanceof PortalBlockEntity) serverLevel.destroyBlock(portal,false);
			serverLevel.setChunkForced(portal.getX()>>4,portal.getZ()>>4,false);
		}
	}
	@Override
	public void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		if(tag.hasUUID("owner")) owner=tag.getUUID("owner");
		isBlue=tag.getBoolean("b");
		if(tag.contains("f")) facing=Direction.byName(tag.getString("f"));
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		if(owner!=null) tag.putUUID("owner",owner);
		tag.putBoolean("b",isBlue);
		if(facing!=null) tag.putString("f",facing.getName());
	}
	public static class PortalData extends SavedData{
		private final Map<UUID,Map<Boolean,BlockPos>> map=new HashMap<>();
		public static PortalData get(ServerLevel serverLevel){
			return serverLevel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(PortalData::new,PortalData::load),"dif_portals");
		}
		public static PortalData load(CompoundTag t,HolderLookup.Provider provider){
			PortalData d=new PortalData();
			t.getAllKeys().forEach(k->{
				CompoundTag pt=t.getCompound(k);
				Map<Boolean,BlockPos> m=new HashMap<>();
				if(pt.contains("b")) NbtUtils.readBlockPos(pt,"b").ifPresent(pos->m.put(true,pos));
				if(pt.contains("o")) NbtUtils.readBlockPos(pt,"o").ifPresent(pos->m.put(false,pos));
				d.map.put(UUID.fromString(k),m);
			});
			return d;
		}
		@Override
		public @NotNull CompoundTag save(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
			map.forEach((k,v)->{
				CompoundTag pt=new CompoundTag();
				if(v.containsKey(true)) pt.put("b",NbtUtils.writeBlockPos(v.get(true)));
				if(v.containsKey(false)) pt.put("o",NbtUtils.writeBlockPos(v.get(false)));
				tag.put(k.toString(),pt);
			});
			return tag;
		}
		public void set(UUID uuid,boolean b,BlockPos pos){
			map.computeIfAbsent(uuid,k->new HashMap<>()).put(b,pos);
			setDirty();
		}
		public BlockPos getPos(UUID uuid,boolean b){
			return map.getOrDefault(uuid,Map.of()).get(b);
		}
		public void remove(UUID id,boolean b){
			Map<Boolean,BlockPos> m=map.get(id);
			if(m!=null){
				m.remove(b);
				if(m.isEmpty()) map.remove(id);
				setDirty();
			}
		}
	}
}