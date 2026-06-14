package cz.maxtechnik.dif.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Logika pro samotné vytěžení bloku, spotřebu těžení a distribuci dropů.
 * Odděluje se tím "akční" část quarry od samotné správy stavů.
 */
public class QuarryMiningLogic {

	/**
	 * Provede těžební tick.
	 *
	 * @param be                QuarryBlockEntity, která operaci volá
	 * @param level             Svět
	 * @param miningProgressAcc Aktuální nastřádaný progres těžby
	 * @param progressStep      Kolik progresu přibylo tento tick
	 * @param hasSilkTouch      Zda má quarry silk touch
	 * @param hasLiquidRemover  Zda má quarry vylepšení na kapaliny
	 * @return Nový zůstatek nastřádaného progresu (miningProgressAcc)
	 */
	public static float doMiningTick(QuarryBlockEntity be, Level level, float miningProgressAcc, float progressStep, boolean hasSilkTouch, boolean hasLiquidRemover) {
		if (!(level instanceof ServerLevel sl)) return miningProgressAcc;

		QuarryAreaManager areaManager = be.getAreaManager();
		BlockPos miningPos = areaManager.getMiningPos();

		if (miningPos == null) {
			areaManager.resetMiningPos(be.getBlockPos().getY());
			miningPos = areaManager.getMiningPos();
			be.setChanged();
		}

		ItemStack tool = buildSimulatedTool(level, hasSilkTouch);
		miningProgressAcc += progressStep;

		int safety = 0;
		while (safety++ < 1000) {
			// Přeskočit prázdné bloky a bloky obsahující pouze kapalinu, pokud ji netěžíme
			while (level.isEmptyBlock(miningPos) && level.getBlockState(miningPos).getFluidState().isEmpty()) {
				if (!areaManager.advanceMiningPos(level)) {
					be.finishMining();
					return miningProgressAcc;
				}
				miningPos = areaManager.getMiningPos();
				be.setChanged();
			}

			BlockState target = level.getBlockState(miningPos);

			// Řešení kapalin
			if (!target.getFluidState().isEmpty()) {
				if (hasLiquidRemover) {
					float fluidCost = target.getFluidState().isSource() ? 5f : 1f;
					if (miningProgressAcc >= fluidCost) {
						miningProgressAcc -= fluidCost;
						level.setBlock(miningPos, Blocks.AIR.defaultBlockState(), 2);
						if (!areaManager.advanceMiningPos(level)) {
							be.finishMining();
							return miningProgressAcc;
						}
						miningPos = areaManager.getMiningPos();
						be.setChanged();
						continue;
					} else {
						return miningProgressAcc; // Nedostatek progresu na vysátí kapaliny
					}
				}
				
				// Pokud nemáme vysavač, kapalinu ignorujeme a jdeme dál
				if (!areaManager.advanceMiningPos(level)) {
					be.finishMining();
					return miningProgressAcc;
				}
				miningPos = areaManager.getMiningPos();
				be.setChanged();
				continue;
			}

			// Řešení nezničitelných bloků (Bedrock atd.)
			float hardness = target.getDestroySpeed(level, miningPos);
			if (hardness < 0) {
				miningProgressAcc = 0f; // Blok nelze zničit
				if (!areaManager.advanceMiningPos(level)) {
					be.finishMining();
					return miningProgressAcc;
				}
				miningPos = areaManager.getMiningPos();
				be.setChanged();
				continue;
			}

			// Vlastní těžení pevného bloku
			float required = Math.max(1f, hardness * 10f);
			if (miningProgressAcc < required) {
				return miningProgressAcc; // Nedostatek progresu pro zničení tohoto bloku, čekáme
			}

			miningProgressAcc -= required;
			List<ItemStack> drops = Block.getDrops(target, sl, miningPos, sl.getBlockEntity(miningPos), null, tool);
			level.removeBlock(miningPos, false);

			if (!drops.isEmpty()) {
				distributeDrops(be, level, drops);
			}

			if (!areaManager.advanceMiningPos(level)) {
				be.finishMining();
				return miningProgressAcc;
			}
			miningPos = areaManager.getMiningPos();
			be.setChanged();
		}

		return miningProgressAcc;
	}

	/**
	 * Nasimuluje virtuální nástroj, který quarry používá.
	 */
	private static ItemStack buildSimulatedTool(Level level, boolean hasSilkTouch) {
		ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
		if (hasSilkTouch && level != null) {
			var lookup = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
			tool.enchant(lookup.getOrThrow(Enchantments.SILK_TOUCH), 1);
		}
		return tool;
	}

	/**
	 * Rozešle vytěžené itemy do okolních inventářů. Pokud se nevejdou, vyhodí je nahoru.
	 */
	private static void distributeDrops(QuarryBlockEntity be, Level level, List<ItemStack> drops) {
		BlockPos pos = be.getBlockPos();
		List<IItemHandler> handlers = new ArrayList<>(6);

		for (Direction dir : Direction.values()) {
			IItemHandler h = level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(dir), dir.getOpposite());
			if (h != null) handlers.add(h);
		}

		for (ItemStack drop : drops) {
			if (drop.isEmpty()) continue;
			ItemStack rem = drop;
			for (IItemHandler h : handlers) {
				if (rem.isEmpty()) break;
				rem = ItemHandlerHelper.insertItemStacked(h, rem, false);
			}
			if (!rem.isEmpty()) {
				Block.popResource(level, pos.above(), rem);
			}
		}
	}
}
