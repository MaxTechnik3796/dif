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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class QuarryBlockEntity extends BlockEntity {
	private BlockPos miningPos;
	private int timer = 0;
	private final int speed = 8;
	private final int range = 5;

	// Energie: Kapacita 100k, příjem 1k, spotřeba na blok 500 FE
	private final EnergyStorage energy = new EnergyStorage(100000, 1000, 1000);
	private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> energy);

	public QuarryBlockEntity(BlockPos pos, BlockState state) {
		super(DifModBlockEntities.QUARRY.get(), pos, state);
	}
	public static void tick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
		if (level.isClientSide) return;

		if (be.miningPos == null) {
			be.resetMiningArea(state);
			be.buildBuildCraftFrame(level, state);
		}

		be.timer++;
		if (be.timer >= be.speed) {
			// KONTROLA ENERGIE (500 FE na operaci)
			if (be.energy.getEnergyStored() >= 500) {
				be.timer = 0;
				be.mineNextBlock(level);
				be.energy.extractEnergy(500, false); // Odečtení energie
				level.sendBlockUpdated(pos, state, state, 3);
			}
		}
	}

	private void buildBuildCraftFrame(Level level, BlockState state) {
		Direction facing = state.getValue(QuarryBlock.FACING).getOpposite();
		// Střed těžební oblasti je posunutý před stroj o (range + 1)
		BlockPos center = worldPosition.relative(facing, range + 1);
		int yBase = worldPosition.getY();

		for (int x = center.getX() - range; x <= center.getX() + range; x++) {
			for (int z = center.getZ() - range; z <= center.getZ() + range; z++) {
				boolean isEdgeX = (x == center.getX() - range || x == center.getX() + range);
				boolean isEdgeZ = (z == center.getZ() - range || z == center.getZ() + range);

				if (isEdgeX || isEdgeZ) {
					// Spodní patro
					placeFrame(level, new BlockPos(x, yBase, z));
					// Horní patro s mezerou 2 bloky (Y, Y+1=vzduch, Y+2=vzduch, Y+3=frame)
					placeFrame(level, new BlockPos(x, yBase + 3, z));

					// Rohové sloupky
					if (isEdgeX && isEdgeZ) {
						placeFrame(level, new BlockPos(x, yBase + 1, z));
						placeFrame(level, new BlockPos(x, yBase + 2, z));
					}
				}
			}
		}
	}

	private void placeFrame(Level level, BlockPos pos) {
		if (level.isEmptyBlock(pos)) {
			level.setBlock(pos, DifModBlocks.QUARRY_FRAME.get().defaultBlockState(), 3);
		}
	}

	private void mineNextBlock(Level level) {
		while (level.isEmptyBlock(miningPos) && miningPos.getY() > level.getMinBuildHeight()) {
			if (!advanceMiningPos()) break;
		}

		BlockState targetState = level.getBlockState(miningPos);
		if (!targetState.isAir() && targetState.getDestroySpeed(level, miningPos) >= 0) {
			if (level instanceof ServerLevel serverLevel) {
				List<ItemStack> drops = Block.getDrops(targetState, serverLevel, miningPos, level.getBlockEntity(miningPos));
				BlockEntity invAbove = level.getBlockEntity(worldPosition.above());

				if (invAbove != null) {
					invAbove.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
						for (ItemStack stack : drops) {
							ItemStack rem = ItemHandlerHelper.insertItemStacked(handler, stack, false);
							if (!rem.isEmpty()) Block.popResource(level, worldPosition.above(), rem);
						}
					});
				}
				level.removeBlock(miningPos, false);
			}
		}
		advanceMiningPos();
	}

	private boolean advanceMiningPos() {
		Direction facing = getBlockState().getValue(QuarryBlock.FACING).getOpposite();
		// Musí být stejný výpočet jako v resetMiningArea!
		BlockPos center = worldPosition.relative(facing, range + 1);

		int minX = center.getX() - (range-1);
		int maxX = center.getX() + (range-1);
		int minZ = center.getZ() - (range-1);
		int maxZ = center.getZ() + (range-1);

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
		assert level!=null;
		return y > level.getMinBuildHeight();
	}

	private void resetMiningArea(BlockState state) {
		Direction facing = state.getValue(QuarryBlock.FACING);
		// Pokud to těží na opačné straně, změň .relative(facing, ...) na .relative(facing.getOpposite(), ...)
		BlockPos center = worldPosition.relative(facing.getOpposite(), range + 1);

		// Nastavíme miningPos do "nejmenšího" rohu oblasti (minX, minY, minZ)
		this.miningPos = new BlockPos(
				center.getX() - (range-1),
				worldPosition.getY()-1,
				center.getZ() - (range-1)
		);
		setChanged();
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public void load(@NotNull CompoundTag tag) {
		super.load(tag);
		energy.receiveEnergy(tag.getInt("Energy"), false);
		if (tag.contains("MineX")) {
			this.miningPos = new BlockPos(tag.getInt("MineX"), tag.getInt("MineY"), tag.getInt("MineZ"));
		}
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("Energy", energy.getEnergyStored());
		if (miningPos != null) {
			tag.putInt("MineX", miningPos.getX());
			tag.putInt("MineY", miningPos.getY());
			tag.putInt("MineZ", miningPos.getZ());
		}
	}

	public int getRange() { return range; }
	public BlockPos getMiningPos() { return miningPos; }
	@Override
	public @NotNull CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		if (miningPos != null) {
			tag.putInt("MineX", miningPos.getX());
			tag.putInt("MineY", miningPos.getY());
			tag.putInt("MineZ", miningPos.getZ());
		}
		return tag;
	}
	@Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}