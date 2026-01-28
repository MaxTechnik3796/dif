package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.PortalBlock;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.init.PortalStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PortalBlockEntity extends BlockEntity {
	private UUID owner;
	private boolean isBlue;
	private Direction facing;
	public long lastTeleportTime = 0;

	public PortalBlockEntity(BlockPos pos, BlockState state) {
		super(DifModBlockEntities.PORTAL.get(), pos, state);
	}

	public void setup(UUID owner, boolean isBlue, Direction facing) {
		this.owner = owner;
		this.isBlue = isBlue;
		this.facing = facing;
		this.setChanged();
	}

	public static void tick(Level level, BlockPos pos, BlockState state, PortalBlockEntity be) {
		if (be.owner == null) return;

		// Sjednocení hitboxů obou částí portálu
		AABB lowerBox = state.getShape(level, pos).bounds().move(pos);
		Direction extDir = state.getValue(PortalBlock.EXTENSION_DIR);
		BlockPos extPos = pos.relative(extDir);
		BlockState extState = level.getBlockState(extPos);

		AABB combinedBox = lowerBox;
		if (extState.is(state.getBlock())) {
			combinedBox = lowerBox.minmax(extState.getShape(level, extPos).bounds().move(extPos));
		}

		level.getEntitiesOfClass(Player.class, combinedBox).forEach(player -> {
			if (level.getGameTime() - be.lastTeleportTime > 20) {
				be.tryTeleport(player);
			}
		});
	}

	private void tryTeleport(Player player) {
		BlockPos targetPos = PortalStorage.getPortal(this.owner, !this.isBlue);
		if (targetPos == null) return;
		assert level!=null;
		if (level.getBlockEntity(targetPos) instanceof PortalBlockEntity other) {
			double tx = targetPos.getX() + 0.5 + (other.facing.getStepX() * 0.8);
			double ty = targetPos.getY() + (other.facing == Direction.UP ? 0.2 : 0.0);
			double tz = targetPos.getZ() + 0.5 + (other.facing.getStepZ() * 0.8);

			player.teleportTo(tx, ty, tz);
			player.setYRot(other.facing.toYRot());
			other.lastTeleportTime = level.getGameTime();
			this.lastTeleportTime = level.getGameTime();
		}
	}

	@Override public void load(@NotNull CompoundTag nbt) { super.load(nbt); if(nbt.hasUUID("owner")) owner = nbt.getUUID("owner"); isBlue = nbt.getBoolean("isBlue"); facing = Direction.byName(nbt.getString("f")); }
	@Override protected void saveAdditional(@NotNull CompoundTag nbt) { super.saveAdditional(nbt); if(owner != null) nbt.putUUID("owner", owner); nbt.putBoolean("isBlue", isBlue); if(facing != null) nbt.putString("f", facing.getName()); }
}