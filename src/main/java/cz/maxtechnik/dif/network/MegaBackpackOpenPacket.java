package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.gui.menu.MegaBackpackMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Objects;
import java.util.function.Supplier;
@SuppressWarnings("removal")
@EventBusSubscriber(bus=EventBusSubscriber.Bus.MOD)
public class MegaBackpackOpenPacket{
	int type, pressedms;
	public MegaBackpackOpenPacket(int type,int pressedms){
		this.type=type;
		this.pressedms=pressedms;
	}
	public MegaBackpackOpenPacket(FriendlyByteBuf buffer){
		this.type=buffer.readInt();
		this.pressedms=buffer.readInt();
	}
	public static void buffer(MegaBackpackOpenPacket message,FriendlyByteBuf buffer){
		buffer.writeInt(message.type);
		buffer.writeInt(message.pressedms);
	}
	public static void handler(MegaBackpackOpenPacket message,Supplier<NetworkEvent.Context> contextSupplier){
		NetworkEvent.Context context=contextSupplier.get();
		context.enqueueWork(()->pressAction(Objects.requireNonNull(context.getSender()),message.type));
		context.setPacketHandled(true);
	}
	public static void pressAction(Player player,int type){
		if(type==0&&player instanceof ServerPlayer serverPlayer){
			// Tady je ten hlavní rozdíl oproti Enderce:
			// Musíme použít NetworkHooks.openScreen, abychom uspokojili IForgeMenuType
			NetworkHooks.openScreen(serverPlayer,new SimpleMenuProvider(
					(id,inventory,p)->new MegaBackpackMenu(id,inventory),
					Component.literal("Mega Backpack")
			),buf->{
				// Zapíšeme jeden int, aby klient (IForgeMenuType) věděl, že data přišla
				buf.writeInt(0);
			});
		}
	}
	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event){
		DifMod.addNetworkMessage(MegaBackpackOpenPacket.class,MegaBackpackOpenPacket::buffer,MegaBackpackOpenPacket::new,MegaBackpackOpenPacket::handler);
	}
}