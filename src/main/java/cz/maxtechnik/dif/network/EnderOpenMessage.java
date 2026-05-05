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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.MenuType;

import java.util.Objects;
import java.util.function.Supplier;
@SuppressWarnings("removal")
@EventBusSubscriber(bus=EventBusSubscriber.Bus.MOD)
public class EnderOpenMessage{
	int type, pressedms;
	public EnderOpenMessage(int type,int pressedms){
		this.type=type;
		this.pressedms=pressedms;
	}
	public EnderOpenMessage(FriendlyByteBuf buffer){
		this.type=buffer.readInt();
		this.pressedms=buffer.readInt();
	}
	public static void buffer(EnderOpenMessage message,FriendlyByteBuf buffer){
		buffer.writeInt(message.type);
		buffer.writeInt(message.pressedms);
	}
	public static void handler(EnderOpenMessage message,Supplier<NetworkEvent.Context> contextSupplier){
		NetworkEvent.Context context=contextSupplier.get();
		context.enqueueWork(()->pressAction(Objects.requireNonNull(context.getSender()),message.type));
		context.setPacketHandled(true);
	}
	public static void pressAction(Player player,int type){
		Level world=player.level();
		if(!world.hasChunkAt(player.blockPosition())) return;
		if(type==0&&player instanceof ServerPlayer serverPlayer){
			CuriosApi.getCuriosInventory(serverPlayer).ifPresent(handler->handler.findFirstCurio(stack->stack.getItem()==Items.ENDER_CHEST).ifPresent(slotResult->{
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
			}));
		}
	}
	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event){
		DifMod.addNetworkMessage(EnderOpenMessage.class,EnderOpenMessage::buffer,EnderOpenMessage::new,EnderOpenMessage::handler);
	}
}