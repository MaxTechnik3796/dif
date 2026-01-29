package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.PortalBlock;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PortalBlockEntity extends BlockEntity {
	private static final Map<UUID, Map<Boolean, BlockPos>> PORTALS = new HashMap<>();
	private UUID owner;
	private boolean isBlue;
	private Direction facing;
	public long lastTeleportTime = 0;

	public PortalBlockEntity(BlockPos pos, BlockState state) { super(DifModBlockEntities.PORTAL.get(), pos, state); }

	public void setup(UUID owner, boolean isBlue, Direction facing) { this.owner = owner; this.isBlue = isBlue; this.facing = facing; this.setChanged(); }

	public static void tick(Level level, BlockPos pos, BlockState state, PortalBlockEntity be) {
		if (be.owner == null || level.getGameTime() - be.lastTeleportTime <= 20) return;
		AABB box = state.getShape(level, pos).bounds().move(pos);
		BlockPos extPos = pos.relative(state.getValue(PortalBlock.EXTENSION_DIR));
		if (level.getBlockState(extPos).is(state.getBlock())) box = box.minmax(level.getBlockState(extPos).getShape(level, extPos).bounds().move(extPos));
		for (Player p : level.getEntitiesOfClass(Player.class, box)) be.tryTeleport(p);
	}

	private void tryTeleport(Player p) {
		BlockPos target = getPortal(this.owner, !this.isBlue);
		if (target == null || !(Objects.requireNonNull(level).getBlockEntity(target) instanceof PortalBlockEntity other)) {
			p.displayClientMessage(Component.literal("§c[!] Linked portal not found"), true); return;
		}
		if (this.worldPosition.distSqr(target) > 65536) { // 256 block range
			p.displayClientMessage(Component.literal("§c[!] Portal too far away"), true); return;
		}

		// Offset 3 pixels from portal surface
		double tx = target.getX() + 0.5 - (other.facing.getStepX() * 0.1875);
		double ty = target.getY() + (other.facing == Direction.UP ? 0.1 : (other.facing == Direction.DOWN ? -2.0 : 0.0));
		double tz = target.getZ() + 0.5 - (other.facing.getStepZ() * 0.1875);

		p.teleportTo(tx, ty, tz);
		p.setYRot(other.facing.toYRot());

		// PŘIDANÉ ZVUKY TELEPORTU
		level.playSound(null, this.worldPosition, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.2F);
		level.playSound(null, target, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 1.2F);

		other.lastTeleportTime = this.lastTeleportTime = level.getGameTime();
	}

	public static void savePortal(UUID id, boolean b, BlockPos p) { PORTALS.computeIfAbsent(id, k -> new HashMap<>()).put(b, p); }
	public static BlockPos getPortal(UUID id, boolean b) { return PORTALS.getOrDefault(id, Map.of()).get(b); }
	public static void removeOldPortal(ServerLevel l, UUID id, boolean b) {
		BlockPos p = getPortal(id, b);
		if (p != null && l.isLoaded(p) && l.getBlockEntity(p) instanceof PortalBlockEntity) l.destroyBlock(p, false);
	}

	@Override public void load(@NotNull CompoundTag tag) { super.load(tag); if(tag.hasUUID("owner")) owner = tag.getUUID("owner"); isBlue = tag.getBoolean("b"); facing = Direction.byName(tag.getString("f")); }
	@Override protected void saveAdditional(@NotNull CompoundTag tag) { super.saveAdditional(tag); if(owner!=null) tag.putUUID("owner", owner); tag.putBoolean("b", isBlue); if(facing!=null) tag.putString("f", facing.getName()); }
}