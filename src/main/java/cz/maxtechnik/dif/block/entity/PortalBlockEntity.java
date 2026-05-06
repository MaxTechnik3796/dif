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
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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

	// Čekající hráči na načtení chunku: UUID -> tick kdy začal čekat
	private static final Map<UUID,Long> waitingPlayers=new HashMap<>();
	// Cooldown entit aby se hned znovu neteleportovaly
	private static final Map<UUID,Long> entityCooldowns=new HashMap<>();

	public PortalBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.PORTAL.get(),pos,state);
	}

	public void setup(UUID owner,boolean isBlue,Direction facing){
		this.owner=owner;
		this.isBlue=isBlue;
		this.facing=facing;
		this.setChanged();
	}

	public static void tick(Level level,BlockPos pos,BlockState state,PortalBlockEntity be){
		if(be.owner==null) return;
		if(!(level instanceof ServerLevel sl)) return;
		if(!pos.equals(getPortal(sl,be.owner,be.isBlue))) return;

		AABB box=state.getShape(level,pos).bounds().move(pos);
		BlockPos extPos=pos.relative(state.getValue(PortalBlock.EXTENSION_DIR));
		if(level.getBlockState(extPos).is(state.getBlock()))
			box=box.minmax(level.getBlockState(extPos).getShape(level,extPos).bounds().move(extPos));

		long now=level.getGameTime();

		// Hráči
		List<Player> players=level.getEntitiesOfClass(Player.class,box);
		for(Player p: players){
			UUID pid=p.getUUID();
			if(now-be.lastTeleportTime<=DifModCommonConfig.portalTeleportCooldown) continue;
			if(entityCooldowns.containsKey(pid)&&now-entityCooldowns.get(pid)<=DifModCommonConfig.portalTeleportCooldown) continue;
			be.tryTeleportPlayer(p,sl, now);
		}

		// Moby
		if(DifModCommonConfig.portalAllowMobs){
			List<LivingEntity> mobs=level.getEntitiesOfClass(LivingEntity.class,box);
			int count=0;
			for(LivingEntity mob: mobs){
				if(mob instanceof Player) continue;
				if(count>=DifModCommonConfig.portalMaxEntitiesPerTick) break;
				UUID mid=mob.getUUID();
				if(entityCooldowns.containsKey(mid)&&now-entityCooldowns.get(mid)<=DifModCommonConfig.portalTeleportCooldown) continue;
				be.tryTeleportEntity(mob,sl, now);
				count++;
			}
		}

		// Itemy, projektily, falling blocky
		if(DifModCommonConfig.portalAllowItemsAndProjectiles){
			List<Entity> misc=level.getEntitiesOfClass(Entity.class,box,
					e->e instanceof ItemEntity||e instanceof Projectile||e instanceof FallingBlockEntity);
			int count=0;
			for(Entity e: misc){
				if(count>=DifModCommonConfig.portalMaxEntitiesPerTick) break;
				UUID eid=e.getUUID();
				if(entityCooldowns.containsKey(eid)&&now-entityCooldowns.get(eid)<=10) continue;
				be.tryTeleportEntity(e,sl, now);
				count++;
			}
		}

		// Vyčisti staré cooldowny
		entityCooldowns.entrySet().removeIf(e->now-e.getValue()>200);
	}

	private void tryTeleportPlayer(Player p, ServerLevel sl, long now){
		UUID pid=p.getUUID();
		BlockPos target=getPortal(sl,this.owner,!this.isBlue);
		if(target==null){
			p.displayClientMessage(Component.literal("[!] Linked portal not found"),true);
			waitingPlayers.remove(pid);
			return;
		}

		// Zkontroluj jestli hráč stále stojí u portálu
		if(waitingPlayers.containsKey(pid)){
            assert level != null;
            AABB box=this.getBlockState().getShape(level,worldPosition).bounds().move(worldPosition);
			BlockPos extPos=worldPosition.relative(this.getBlockState().getValue(PortalBlock.EXTENSION_DIR));
			if(level.getBlockState(extPos).is(this.getBlockState().getBlock()))
				box=box.minmax(level.getBlockState(extPos).getShape(level,extPos).bounds().move(extPos));
			box=box.inflate(1.5);
			if(!box.contains(p.position())){
				waitingPlayers.remove(pid);
				return;
			}
		}

		// Zkontroluj vzdálenost
		if(this.worldPosition.distSqr(target)>
				(long)DifModCommonConfig.portalMaxDistance*DifModCommonConfig.portalMaxDistance){
			p.displayClientMessage(Component.literal("[!] Portal too far away"),true);
			waitingPlayers.remove(pid);
			return;
		}

		// Zkontroluj jestli je chunk načtený
		if(!sl.isLoaded(target)){
			long startTick=waitingPlayers.getOrDefault(pid,now);
			if(!waitingPlayers.containsKey(pid)) waitingPlayers.put(pid,now);
			p.displayClientMessage(Component.literal("Please wait..."),true);
			// Force-load chunk dočasně
			sl.setChunkForced(target.getX()>>4,target.getZ()>>4,true);
			if(now-startTick>DifModCommonConfig.portalChunkLoadTimeout){
				p.displayClientMessage(Component.literal("[!] Portal unreachable"),true);
				sl.setChunkForced(target.getX()>>4,target.getZ()>>4,false);
				waitingPlayers.remove(pid);
			}
			return;
		}

		// Chunk načtený — uvolni force-load a teleportuj
		sl.setChunkForced(target.getX()>>4,target.getZ()>>4,false);
		waitingPlayers.remove(pid);

		if(!(sl.getBlockEntity(target) instanceof PortalBlockEntity other)){
			p.displayClientMessage(Component.literal("[!] Linked portal not found"),true);
			return;
		}

		double tx=target.getX()+0.5-(other.facing.getStepX()*0.1875);
		double ty=target.getY()+(other.facing==Direction.UP?0.1:(other.facing==Direction.DOWN?-2.0:0.0));
		double tz=target.getZ()+0.5-(other.facing.getStepZ()*0.1875);
		p.teleportTo(tx,ty,tz);
		p.setYRot(other.facing.toYRot());
        assert level != null;
        level.playSound(null,this.worldPosition,SoundEvents.BEACON_POWER_SELECT,SoundSource.BLOCKS,1.0F,1.2F);
		level.playSound(null,target,SoundEvents.BEACON_POWER_SELECT,SoundSource.BLOCKS,1.0F,1.2F);
		other.lastTeleportTime=this.lastTeleportTime=now;
		entityCooldowns.put(pid,now);
	}

	private void tryTeleportEntity(Entity entity, ServerLevel sl, long now){
		BlockPos target=getPortal(sl,this.owner,!this.isBlue);
		if(target==null||!(sl.getBlockEntity(target) instanceof PortalBlockEntity other)) return;
		if(this.worldPosition.distSqr(target)>
				(long)DifModCommonConfig.portalMaxDistance*DifModCommonConfig.portalMaxDistance) return;
		if(!sl.isLoaded(target)) return;

		Vec3 newMotion=transformVelocity(entity.getDeltaMovement(),this.facing,other.facing);

		double tx=target.getX()+0.5-(other.facing.getStepX()*0.5);
		double ty=target.getY()+(other.facing==Direction.UP?0.5:(other.facing==Direction.DOWN?-1.0:0.5));
		double tz=target.getZ()+0.5-(other.facing.getStepZ()*0.5);
		entity.teleportTo(tx,ty,tz);
		entity.setDeltaMovement(newMotion);
		entity.hurtMarked=true;
		entityCooldowns.put(entity.getUUID(),now);
        assert level != null;
        level.playSound(null,target,SoundEvents.BEACON_POWER_SELECT,SoundSource.BLOCKS,0.5F,1.4F);
	}

	/**
	 * Přepočítá vektor pohybu z orientace vstupního portálu na výstupní.
	 * Zachovává rychlost (délku vektoru).
	 */
	private static Vec3 transformVelocity(Vec3 velocity,Direction inFacing,Direction outFacing){
		// Převeď velocity do lokálního souřadnicového systému vstupního portálu
		// pak aplikuj rotaci na výstupní portál
		double speed=velocity.length();
		if(speed<0.001) return velocity;

		// Normalizuj
		Vec3 norm=velocity.normalize();

		// Rotační transformace: mapuj inFacing -> outFacing
		// inFacing je směr do kterého portál kouká (normála plochy)
		// outFacing je směr z kterého výstupní portál kouká ven
		Vec3 inAxis=dirToVec(inFacing);
		Vec3 outAxis=dirToVec(outFacing.getOpposite()); // Výstup je obrácený

		// Reflexe/rotace vektoru
		Vec3 transformed=rotateVector(norm,inAxis,outAxis);

		return transformed.scale(speed);
	}

	private static Vec3 dirToVec(Direction d){
		return new Vec3(d.getStepX(),d.getStepY(),d.getStepZ());
	}

	private static Vec3 rotateVector(Vec3 v,Vec3 from,Vec3 to){
		// Rodriguesova rotační formule pro rotaci z `from` na `to`
		Vec3 axis=from.cross(to);
		double sinAngle=axis.length();
		double cosAngle=from.dot(to);
		if(sinAngle<0.001){
			// Vektory jsou rovnoběžné
			if(cosAngle>0) return v; // Stejný směr
			return v.scale(-1); // Opačný směr
		}
		axis=axis.normalize();
		// v*cos + (axis x v)*sin + axis*(axis·v)*(1-cos)
		return v.scale(cosAngle)
				.add(axis.cross(v).scale(sinAngle))
				.add(axis.scale(axis.dot(v)*(1-cosAngle)));
	}

	public static void savePortal(ServerLevel l,UUID id,boolean b,BlockPos p){
		PortalData.get(l).set(id,b,p);
	}

	public static BlockPos getPortal(ServerLevel l,UUID id,boolean b){
		return PortalData.get(l).getPos(id,b);
	}

	public static void removeOldPortal(ServerLevel l,UUID id,boolean b){
		BlockPos p=getPortal(l,id,b);
		if(p!=null&&l.isLoaded(p)&&l.getBlockEntity(p) instanceof PortalBlockEntity) l.destroyBlock(p,false);
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

		public static PortalData get(ServerLevel l){
			return l.getDataStorage().computeIfAbsent(
					new SavedData.Factory<>(PortalData::new,PortalData::load),
					"dif_portals"
			);
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
		public @NotNull CompoundTag save(@NotNull CompoundTag t,@NotNull HolderLookup.Provider provider){
			map.forEach((k,v)->{
				CompoundTag pt=new CompoundTag();
				if(v.containsKey(true)) pt.put("b",NbtUtils.writeBlockPos(v.get(true)));
				if(v.containsKey(false)) pt.put("o",NbtUtils.writeBlockPos(v.get(false)));
				t.put(k.toString(),pt);
			});
			return t;
		}

		public void set(UUID id,boolean b,BlockPos p){
			map.computeIfAbsent(id,k->new HashMap<>()).put(b,p);
			setDirty();
		}

		public BlockPos getPos(UUID id,boolean b){
			return map.getOrDefault(id,Map.of()).get(b);
		}
	}
}