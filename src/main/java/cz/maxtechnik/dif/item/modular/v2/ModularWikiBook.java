package cz.maxtechnik.dif.item.modular.v2;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
public class ModularWikiBook extends Item{
	public ModularWikiBook(Properties properties){
		super(properties);
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level,@NotNull Player player,@NotNull InteractionHand hand){
		if(level.isClientSide) execute(player);
		return super.use(level,player,hand);
	}

	@Override
	public @NotNull InteractionResult useOn(@NotNull UseOnContext context){
		super.useOn(context);
		if(context.getLevel().isClientSide) execute(context.getPlayer());
		return InteractionResult.SUCCESS;
	}

	private void execute(Player player){
		MutableComponent list=Component.empty();
		list.append(Component.literal("Open ModularTools Wiki: ")
				.append(Component.literal("HERE").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withUnderlined(true).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,Component.literal("Open Wiki"))) .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,"https://maxtechnik3796.github.io/dif/modular_tools_wiki.html"))))
		);
		DifMod.sendMessageToPlayer(player,list);
	}
}
