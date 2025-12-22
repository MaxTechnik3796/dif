package cz.maxtechnik.dif.item.random;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
		if(context.getLevel().isClientSide())return InteractionResult.SUCCESS;
		context.getLevel().explode(context.getPlayer(),context.getClickedPos().getX(),context.getClickedPos().getY(),context.getClickedPos().getZ(),64,Level.ExplosionInteraction.TNT);
		assert context.getPlayer()!=null;
		if(context.getPlayer()instanceof ServerPlayer player){
			if(!DifMod.playerGameModeIsCreativeCategory(player))context.getItemInHand().shrink(1);
		}
		return InteractionResult.SUCCESS;
	}
}
