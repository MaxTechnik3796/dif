package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
	private final int speed = 10; // Rychlost: každých 10 ticků (0.5s) zničí blok
	private final int range = 5;  // Oblast 5 bloků na každou stranu (celkem 11x11)

	public QuarryBlockEntity(BlockPos pos, BlockState state) {
		super(DifModBlockEntities.QUARRY.get(), pos, state);
		// Výchozí pozice začíná v rohu oblasti o jedno patro níž
		this.miningPos = pos.offset(-range, -1, -range);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, QuarryBlockEntity be) {
		if (level.isClientSide) return;

		be.timer++;
		if (be.timer >= be.speed) {
			be.timer = 0;
			be.mineNextBlock(level);
		}
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
	private void mineNextBlock(Level level) {
		while (level.isEmptyBlock(miningPos) && miningPos.getY() > level.getMinBuildHeight()) {
			if (!advanceMiningPos()) break;
		}

		BlockState targetState = level.getBlockState(miningPos);

		if (!targetState.isAir() && targetState.getDestroySpeed(level, miningPos) >= 0) {
			// Získáme dropy bloku
			List<ItemStack> drops = Block.getDrops(targetState, (ServerLevel) level, miningPos, level.getBlockEntity(miningPos));

			// Zkusíme najít bednu/inventář NAD Quarry
			BlockEntity inventoryAbove = level.getBlockEntity(worldPosition.above());

			if (inventoryAbove != null) {
				IItemHandler handler = inventoryAbove.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
				for(ItemStack stack: drops){
					// Zkusíme vložit item do bedny
					ItemStack remaining=ItemHandlerHelper.insertItemStacked(handler,stack,false);
					// Pokud se nevejde všechno, zbytek hodíme na zem
					if(!remaining.isEmpty()){
						Block.popResource(level,worldPosition.above(),remaining);
					}
				}
				level.removeBlock(miningPos, false); // Odstraníme blok bez dropu na zem
			} else {
				level.destroyBlock(miningPos, true); // Není bedna, dropujeme postaru
			}
		}
		advanceMiningPos();
	}
}