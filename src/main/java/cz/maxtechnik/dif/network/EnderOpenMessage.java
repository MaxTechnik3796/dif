package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
@SuppressWarnings("deprecation")
public record EnderOpenMessage(int actionType,int pressedms) implements CustomPacketPayload{
	public static final Type<EnderOpenMessage> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"ender_open"));
	public static final StreamCodec<FriendlyByteBuf,EnderOpenMessage> STREAM_CODEC=StreamCodec.composite(
			ByteBufCodecs.INT,EnderOpenMessage::actionType,
			ByteBufCodecs.INT,EnderOpenMessage::pressedms,
			EnderOpenMessage::new
	);
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type(){
		return TYPE;
	}
	public void handle(IPayloadContext context){
		context.enqueueWork(()->pressAction(context.player(),actionType));
	}
	public static void pressAction(Player player,int actionType){
		Level world=player.level();
		if(!world.hasChunkAt(player.blockPosition())) return;
		if(actionType==0&&player instanceof ServerPlayer serverPlayer){
			CuriosApi.getCuriosInventory(serverPlayer).flatMap(handler->handler.findFirstCurio(stack->stack.getItem()==Items.ENDER_CHEST)).ifPresent(slotResult->{
				PlayerEnderChestContainer enderChestInventory=serverPlayer.getEnderChestInventory();
				SimpleMenuProvider menuProvider=new SimpleMenuProvider(
						(containerId,playerInventory,playerEntity)->new ChestMenu(MenuType.GENERIC_9x3,containerId,playerInventory,enderChestInventory,3){
							@Override
							public void removed(@NotNull Player pPlayer){
								super.removed(pPlayer);
								pPlayer.level().playSound(null,pPlayer.blockPosition(),SoundEvents.ENDER_CHEST_CLOSE,SoundSource.PLAYERS,0.5F,pPlayer.level().random.nextFloat()*0.1F+0.9F);
							}
						},
						Component.translatable("container.enderchest")
				);
				serverPlayer.openMenu(menuProvider);
				serverPlayer.level().playSound(null,serverPlayer.blockPosition(),SoundEvents.ENDER_CHEST_OPEN,SoundSource.PLAYERS,0.5F,serverPlayer.level().random.nextFloat()*0.1F+0.9F);
			});
		}
	}
}