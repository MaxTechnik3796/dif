package cz.maxtechnik.dif.item.quarry;

import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
public class DrillHeadItem extends Item{
	public final int dpReq;
	public DrillHeadItem(Properties properties,int dpReq){
		super(properties);
		this.dpReq=dpReq;
	}
	@Override
	public void appendHoverText(@NotNull ItemStack stack,@Nullable Level level,List<Component> tooltips,@NotNull TooltipFlag flag){
		tooltips.add(Component.literal("§7Drill Power Required: §c"+dpReq+" DP"));
		super.appendHoverText(stack,level,tooltips,flag);
	}
}