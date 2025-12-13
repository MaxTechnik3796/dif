package cz.maxtechnik.dif.item.random;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import java.util.List;
public class ExplosiveRam extends Item{
	public ExplosiveRam(){
		super(new Properties().stacksTo(16));
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemstack,Level level,@NotNull List<Component>list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemstack,level,list,flag);
		list.add(Component.literal("§7Use at your own risk!§r"));
	}
	@Override
	public @NotNull InteractionResult useOn(@NotNull UseOnContext context){
		super.useOn(context);
		context.getLevel().explode(context.getPlayer(),context.getClickedPos().getX(),context.getClickedPos().getY(),context.getClickedPos().getZ(),64,Level.ExplosionInteraction.TNT);
		return InteractionResult.SUCCESS;
	}
}
