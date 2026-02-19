package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.events.JetpackHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Objects;
import java.util.function.Supplier;
@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class JetpackFlyMessage{
	int type, pressedms;
	public JetpackFlyMessage(int type,int pressedms){
		this.type=type;
		this.pressedms=pressedms;
	}
	public JetpackFlyMessage(FriendlyByteBuf buffer){
		this.type=buffer.readInt();
		this.pressedms=buffer.readInt();
	}
	public static void buffer(JetpackFlyMessage message,FriendlyByteBuf buffer){
		buffer.writeInt(message.type);
		buffer.writeInt(message.pressedms);
	}
	public static void handler(JetpackFlyMessage message,Supplier<NetworkEvent.Context> contextSupplier){
		NetworkEvent.Context context=contextSupplier.get();
		context.enqueueWork(()->pressAction(Objects.requireNonNull(context.getSender()),message.type));
		context.setPacketHandled(true);
	}
	public static void pressAction(Player player,int type){
		Level world=player.level();
		if(!world.hasChunkAt(player.blockPosition())) return;
		if(type==0){
			JetpackHandler.fly(player);
		}
	}
	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event){
		DifMod.addNetworkMessage(JetpackFlyMessage.class,JetpackFlyMessage::buffer,JetpackFlyMessage::new,JetpackFlyMessage::handler);
	}
}
