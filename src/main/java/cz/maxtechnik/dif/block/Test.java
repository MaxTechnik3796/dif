package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.gui.menu.SpecialCraftingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class Test extends Block {
	private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");
	public Test(){
        super(Properties.of());
    }
	public @NotNull InteractionResult use(@NotNull BlockState blockState,Level world,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,BlockHitResult hit) {
		if (world.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			player.openMenu(blockState.getMenuProvider(world,pos));
			player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
			return InteractionResult.CONSUME;
		}
	}
	public MenuProvider getMenuProvider(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos) {
		return new SimpleMenuProvider((p_52229_,p_52230_,p_52231_) ->new SpecialCraftingMenu(p_52229_, p_52230_, ContainerLevelAccess.create(world, pos)), CONTAINER_TITLE);
	}
}
