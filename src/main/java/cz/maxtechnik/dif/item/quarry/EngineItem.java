package cz.maxtechnik.dif.item.quarry;

import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class EngineItem extends Item{
	public final int dpGen;
	public final int feCost;
	public EngineItem(Properties properties,int dpGen,int feCost){
		super(properties);
		this.dpGen=dpGen;
		this.feCost=feCost;
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@NotNull ItemStack itemStack,Item.@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemStack,context,list,flag);
		list.add(Component.literal("§7Drill Power Generated: §a+"+dpGen+" DP"));
		list.add(Component.literal("§7Energy Consumption: §e-"+feCost+" FE/t"));
	}
}