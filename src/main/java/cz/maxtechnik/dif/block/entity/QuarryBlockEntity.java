package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.QuarryBlock;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuarryBlockEntity extends BlockEntity {
	private BlockPos miningPos;
	private int timer = 0;
	private final int speed = 10;
	private final int range = 5;

	public QuarryBlockEntity(BlockPos pos, BlockState state) {
		super(DifModBlockEntities.QUARRY.get(), pos, state);
	}

	public int getRange() { return range; }
	public BlockPos getMiningPos() { return miningPos; }

	public static void tick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
		if (level.isClientSide) return;

		// Inicializace při prvním ticku
		if (be.miningPos == null) {
			be.resetMiningArea(state);
			Direction facing = state.getValue(QuarryBlock.FACING);
			be.buildFrame(level, pos.relative(facing, be.range + 1));
		}

		be.timer++;
		if (be.timer >= be.speed) {
			be.timer = 0;
			be.mineNextBlock(level);
			// Pošle info klientovi o změně miningPos
			level.sendBlockUpdated(pos, state, state, 3);
		}
	}

	private void resetMiningArea(BlockState state) {
		Direction facing = state.getValue(QuarryBlock.FACING);
		BlockPos center = worldPosition.relative(facing, range + 1);
		this.miningPos = new BlockPos(center.getX() - range, worldPosition.getY() - 1, center.getZ() - range);
		setChanged();
	}

	private void mineNextBlock(Level level) {
		while (level.isEmptyBlock(miningPos) && miningPos.getY() > level.getMinBuildHeight()) {
			if (!advanceMiningPos()) break;
		}

		BlockState targetState = level.getBlockState(miningPos);
		if (!targetState.isAir() && targetState.getDestroySpeed(level, miningPos) >= 0) {
			if (level instanceof ServerLevel serverLevel) {
				List<ItemStack> drops = Block.getDrops(targetState, serverLevel, miningPos, level.getBlockEntity(miningPos));
				BlockEntity inventoryAbove = level.getBlockEntity(worldPosition.above());

				if (inventoryAbove != null) {
					IItemHandler handler = inventoryAbove.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
					if (handler != null) {
						for (ItemStack stack : drops) {
							ItemStack remaining = ItemHandlerHelper.insertItemStacked(handler, stack, false);
							if (!remaining.isEmpty()) Block.popResource(level, worldPosition.above(), remaining);
						}
					}
				}
				level.removeBlock(miningPos, false);
			}
		}
		advanceMiningPos();
	}

	private boolean advanceMiningPos() {
		Direction facing = getBlockState().getValue(QuarryBlock.FACING);
		BlockPos center = worldPosition.relative(facing, range + 1);

		int minX = center.getX() - range;
		int maxX = center.getX() + range;
		int minZ = center.getZ() - range;
		int maxZ = center.getZ() + range;

		int x = miningPos.getX();
		int y = miningPos.getY();
		int z = miningPos.getZ();

		x++;
		if (x > maxX) {
			x = minX;
			z++;
		}
		if (z > maxZ) {
			z = minZ;
			y--;
		}

		miningPos = new BlockPos(x, y, z);
		setChanged();
		return y > level.getMinBuildHeight();
	}

	private void buildFrame(Level level, BlockPos center) {
		int y = worldPosition.getY();
		for (int x = center.getX() - range; x <= center.getX() + range; x++) {
			for (int z = center.getZ() - range; z <= center.getZ() + range; z++) {
				if (x == center.getX() - range || x == center.getX() + range || z == center.getZ() - range || z == center.getZ() + range) {
					BlockPos framePos = new BlockPos(x, y, z);
					if (level.isEmptyBlock(framePos)) {
						level.setBlock(framePos, DifModBlocks.QUARRY_FRAME.get().defaultBlockState(), 3);
					}
				}
			}
		}
	}

	@Override
	public void load(@NotNull CompoundTag tag) {
		super.load(tag);
		if (tag.contains("MineX")) {
			this.miningPos = new BlockPos(tag.getInt("MineX"), tag.getInt("MineY"), tag.getInt("MineZ"));
		}
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		super.saveAdditional(tag);
		if (miningPos != null) {
			tag.putInt("MineX", miningPos.getX());
			tag.putInt("MineY", miningPos.getY());
			tag.putInt("MineZ", miningPos.getZ());
		}
	}

	@Override
	public CompoundTag getUpdateTag() {
		return saveWithoutMetadata();
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}