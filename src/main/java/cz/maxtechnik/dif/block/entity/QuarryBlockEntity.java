package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.QuarryBlock;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
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
		this.miningPos = pos.below();
	}
	public static void tick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
		if (level.isClientSide) return;

		// Pokud miningPos ještě nebyl nastaven (je null nebo na výchozí pozici)
		if (be.miningPos == null || be.miningPos.equals(pos.offset(-5, -1, -5))) {
			be.resetMiningArea(state);

			// VÝPOČET STŘEDU PRO RÁM
			Direction facing = state.getValue(QuarryBlock.FACING);
			BlockPos center = pos.relative(facing, be.range + 1);

			// Teď už předáváme správný typ: Level a BlockPos
			be.buildFrame(level, center);
		}

		be.timer++;
		if (be.timer >= be.speed) {
			be.timer = 0;
			be.mineNextBlock(level);
		}
	}
	public int getRange() {
		return range;
	}
	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap,@Nullable Direction side) {
		if (cap == ForgeCapabilities.ENERGY) {
			return energyHolder.cast();
		}
		return super.getCapability(cap, side);
	}
	private void buildFrame(Level level, BlockPos center) {
		int y = worldPosition.getY();
		// Vytvoříme čtverec z QuarryFrame bloků po obvodu
		for (int x = center.getX() - range; x <= center.getX() + range; x++) {
			for (int z = center.getZ() - range; z <= center.getZ() + range; z++) {
				// Pouze okraje čtverce (X jsou kraje nebo Z jsou kraje)
				if (x == center.getX() - range || x == center.getX() + range ||
						z == center.getZ() - range || z == center.getZ() + range) {

					BlockPos framePos = new BlockPos(x, y, z);
					if (level.isEmptyBlock(framePos)) {
						// Tady se ujisti, že DifModBlocks.QUARRY_FRAME existuje v registru
						level.setBlock(framePos, DifModBlocks.QUARRY_FRAME.get().defaultBlockState(), 3);
					}
				}
			}
		}
	}
	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		energyHolder.invalidate();
	}
	// 1. Přidej tento getter kamkoliv do QuarryBlockEntity
	public BlockPos getMiningPos() {
		return miningPos;
	}
	// 2. Uprav metodu resetMiningArea, aby brala v úvahu směr (FACING)
	private void resetMiningArea(BlockState state) {
		Direction facing = state.getValue(QuarryBlock.FACING);
		// Posun o range+1 bloků DOPŘEDU
		BlockPos center = worldPosition.relative(facing, range + 1);

		// Nastavíme začátek těžby (v rohu pole o 1 níž než quarry)
		this.miningPos = new BlockPos(center.getX() - range, worldPosition.getY() - 1, center.getZ() - range);

		// Tady rovnou postavíme rám!
		buildFrame(level, center);
	}

	// 3. Uprav advanceMiningPos, aby se držela v definovaných mezích před strojem
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