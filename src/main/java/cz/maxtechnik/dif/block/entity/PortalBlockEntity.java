package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.PortalBlock;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PortalBlockEntity extends BlockEntity {
	private UUID owner;
	private boolean isBlue;
	private Direction facing;
	public long lastTeleportTime = 0;

	public PortalBlockEntity(BlockPos pos, BlockState state) { super(DifModBlockEntities.PORTAL.get(), pos, state); }

	public void setup(UUID owner, boolean isBlue, Direction facing) { this.owner = owner; this.isBlue = isBlue; this.facing = facing; this.setChanged(); }

	public static void tick(Level level, BlockPos pos, BlockState state, PortalBlockEntity be) {
		if (be.owner == null || level.getGameTime() - be.lastTeleportTime <= 20) return;
		if (level instanceof ServerLevel sl && !pos.equals(getPortal(sl, be.owner, be.isBlue))) return; // Validace pozice

		AABB box = state.getShape(level, pos).bounds().move(pos);
		BlockPos extPos = pos.relative(state.getValue(PortalBlock.EXTENSION_DIR));
		if (level.getBlockState(extPos).is(state.getBlock())) box = box.minmax(level.getBlockState(extPos).getShape(level, extPos).bounds().move(extPos));
		for (Player p : level.getEntitiesOfClass(Player.class, box)) be.tryTeleport(p);
	}

	private void tryTeleport(Player p) {
		if(!(level instanceof ServerLevel sl)) return;
		BlockPos target = getPortal(sl, this.owner, !this.isBlue);
		if (target == null || !(Objects.requireNonNull(level).getBlockEntity(target) instanceof PortalBlockEntity other)) {
			p.displayClientMessage(Component.literal("§c[!] Linked portal not found"), true); return;
		}
		if (this.worldPosition.distSqr(target) > 65536) {
			p.displayClientMessage(Component.literal("§c[!] Portal too far away"), true); return;
		}

		double tx = target.getX() + 0.5 - (other.facing.getStepX() * 0.1875);
		double ty = target.getY() + (other.facing == Direction.UP ? 0.1 : (other.facing == Direction.DOWN ? -2.0 : 0.0));
		double tz = target.getZ() + 0.5 - (other.facing.getStepZ() * 0.1875);

		p.teleportTo(tx, ty, tz);
		p.setYRot(other.facing.toYRot());
		level.playSound(null, this.worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.2F);
		level.playSound(null, target, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.2F);
		other.lastTeleportTime = this.lastTeleportTime = level.getGameTime();
	}

	public static void savePortal(ServerLevel l, UUID id, boolean b, BlockPos p) { PortalData.get(l).set(id, b, p); }
	public static BlockPos getPortal(ServerLevel l, UUID id, boolean b) { return PortalData.get(l).getPos(id, b); }
	public static void removeOldPortal(ServerLevel l, UUID id, boolean b) {
		BlockPos p = getPortal(l, id, b);
		if (p != null && l.isLoaded(p) && l.getBlockEntity(p) instanceof PortalBlockEntity) l.destroyBlock(p, false);
	}

	@Override public void load(@NotNull CompoundTag tag) { super.load(tag); if(tag.hasUUID("owner")) owner = tag.getUUID("owner"); isBlue = tag.getBoolean("b"); facing = Direction.byName(tag.getString("f")); }
	@Override protected void saveAdditional(@NotNull CompoundTag tag) { super.saveAdditional(tag); if(owner!=null) tag.putUUID("owner", owner); tag.putBoolean("b", isBlue); if(facing!=null) tag.putString("f", facing.getName()); }

	public static class PortalData extends SavedData {
		private final Map<UUID, Map<Boolean, BlockPos>> map = new HashMap<>();
		public static PortalData get(ServerLevel l) { return l.getDataStorage().computeIfAbsent(PortalData::load, PortalData::new, "dif_portals"); }
		public static PortalData load(CompoundTag t) {
			PortalData d = new PortalData();
			t.getAllKeys().forEach(k -> {
				CompoundTag pt = t.getCompound(k);
				Map<Boolean, BlockPos> m = new HashMap<>();
				if(pt.contains("b")) m.put(true, NbtUtils.readBlockPos(pt.getCompound("b")));
				if(pt.contains("o")) m.put(false, NbtUtils.readBlockPos(pt.getCompound("o")));
				d.map.put(UUID.fromString(k), m);
			});
			return d;
		}
		@Override public @NotNull CompoundTag save(@NotNull CompoundTag t) {
			map.forEach((k, v) -> {
				CompoundTag pt = new CompoundTag();
				if(v.containsKey(true)) pt.put("b", NbtUtils.writeBlockPos(v.get(true)));
				if(v.containsKey(false)) pt.put("o", NbtUtils.writeBlockPos(v.get(false)));
				t.put(k.toString(), pt);
			});
			return t;
		}
		public void set(UUID id, boolean b, BlockPos p) { map.computeIfAbsent(id, k -> new HashMap<>()).put(b, p); setDirty(); }
		public BlockPos getPos(UUID id, boolean b) { return map.getOrDefault(id, Map.of()).get(b); }
	}
}