package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.QuarryBlock;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
public class QuarryBlockEntity extends BlockEntity {
	private BlockPos miningPos;
	private int timer = 0;
	private final int speed = 10; // Rychlost: každých 10 ticků (0.5s) zničí blok
	private final int range = 5;  // Oblast 5 bloků na každou stranu (celkem 11x11)
	private final EnergyStorage energy = new EnergyStorage(100000, 1000);
	private final LazyOptional<IEnergyStorage> energyHolder = LazyOptional.of(() -> energy);
	public QuarryBlockEntity(BlockPos pos, BlockState state) {
		super(DifModBlockEntities.QUARRY.get(), pos, state);
		resetMiningArea(state);
	}
	private void resetMiningArea(BlockState state) {
		Direction facing = state.getValue(QuarryBlock.FACING);
		// Posuneme startovní bod před Quarry podle toho, kam se dívá
		// Oblast 11x11, kde střed je 6 bloků před strojem
		BlockPos centerOfArea = worldPosition.relative(facing, range + 1);
		this.miningPos = centerOfArea.offset(-range, -1, -range);
	}
	public static void tick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
		if (level.isClientSide) return;

// Spotřeba 500 FE za jeden vytěžený blok
		if (be.energy.getEnergyStored() >= 500) {
			be.timer++;
			if (be.timer >= be.speed) {
				be.timer = 0;
				if (be.mineNextBlock(level)) {
					be.energy.extractEnergy(500, false);
				}
			}
		}
	}
	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap,@Nullable Direction side) {
		if (cap == ForgeCapabilities.ENERGY) {
			return energyHolder.cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		energyHolder.invalidate();
	}
	private boolean advanceMiningPos() {
		int minX = worldPosition.getX() - range;
		int maxX = worldPosition.getX() + range;
		int minZ = worldPosition.getZ() - range;
		int maxZ = worldPosition.getZ() + range;

		int x = miningPos.getX();
		int y = miningPos.getY();
		int z = miningPos.getZ();

		x++; // Posun v řádku

		if (x > maxX) {
			x = minX;
			z++; // Posun na další řádek
		}

		if (z > maxZ) {
			z = minZ;
			y--; // Posun o patro níž
		}

		miningPos = new BlockPos(x, y, z);

		// Pokud jsme pod limitem světa, zastavíme stroj
		assert level!=null;
		return y > level.getMinBuildHeight();
	}

	// Ukládání pozice vrtáku, aby Quarry po restartu hry nepokračovala zase odshora
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
		tag.putInt("MineX", miningPos.getX());
		tag.putInt("MineY", miningPos.getY());
		tag.putInt("MineZ", miningPos.getZ());
	}
	private boolean mineNextBlock(Level level) {
		while (level.isEmptyBlock(miningPos) && miningPos.getY() > level.getMinBuildHeight()) {
			if (!advanceMiningPos()) return false;
		}

		BlockState targetState = level.getBlockState(miningPos);

		if (!targetState.isAir() && targetState.getDestroySpeed(level, miningPos) >= 0) {
			if (level instanceof ServerLevel serverLevel) {
				List<ItemStack> drops = Block.getDrops(targetState, serverLevel, miningPos, level.getBlockEntity(miningPos));
				BlockEntity inventoryAbove = level.getBlockEntity(worldPosition.above());

				if (inventoryAbove != null) {
					IItemHandler handler = inventoryAbove.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
					for(ItemStack stack: drops){
						ItemStack remaining=ItemHandlerHelper.insertItemStacked(handler,stack,false);
						if(!remaining.isEmpty()){
							Block.popResource(level,worldPosition.above(),remaining);
						}
					}
					level.removeBlock(miningPos, false);
					advanceMiningPos();
					return true; // Těžba proběhla
				}
			}
			level.destroyBlock(miningPos, true);
			advanceMiningPos();
			return true; // Těžba proběhla
		}

		advanceMiningPos();
		return false;
	}
}