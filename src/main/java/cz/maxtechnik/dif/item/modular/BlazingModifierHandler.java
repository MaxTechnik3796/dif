package cz.maxtechnik.dif.item.modular;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = DifMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class BlazingModifierHandler {
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		Player player = event.getPlayer();
		ItemStack stack = player.getMainHandItem();
		Level world = (Level) event.getLevel();
		BlockPos pos = event.getPos();
		BlockState blockState = event.getState();

		if (!(stack.getItem() instanceof ModularBase)) return;
		ModularModifiers mods = ModularBase.getModifiers(stack);
		if (!mods.blazing()) return;
		if (!stack.isCorrectToolForDrops(blockState)) return;

		List<ItemStack> drops = Block.getDrops(blockState, (ServerLevel) world, pos,
				world.getBlockEntity(pos), player, stack);
		boolean smeltedAny = false;

		for (ItemStack drop : drops) {
			Optional<net.minecraft.world.item.crafting.RecipeHolder<SmeltingRecipe>> recipeHolder = world.getRecipeManager()
					.getRecipeFor(RecipeType.SMELTING, new net.minecraft.world.item.crafting.SingleRecipeInput(drop), world);
			if (recipeHolder.isPresent() && !player.getAbilities().instabuild) {
				ItemStack result = recipeHolder.get().value().getResultItem(world.registryAccess()).copy();
				result.setCount(drop.getCount());
				Block.popResource(world, pos, result);
				smeltedAny = true;
			}
		}

		if (smeltedAny) {
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			event.setCanceled(true);
			world.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.5F, 1.2F);
			((ServerLevel) world).sendParticles(ParticleTypes.FLAME,
					pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					5, 0.2, 0.2, 0.2, 0.05);
		}
	}
}