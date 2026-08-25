package cz.maxtechnik.dif.item;

import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import static com.simibubi.create.foundation.item.TooltipHelper.holdShift;
public class FluidHatch extends BlockItem{
	public FluidHatch(Supplier<? extends Block> block,Properties properties){
		super(block.get(),properties);
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@NotNull ItemStack itemStack,Item.@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemStack,context,list,flag);
		list.add(holdShift(FontHelper.Palette.ALL_GRAY,Screen.hasShiftDown()));
	}
}
