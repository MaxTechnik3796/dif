package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.network.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD)
public class DifModNetworking{
	@SubscribeEvent
	public static void register(final RegisterPayloadHandlersEvent event){
		final PayloadRegistrar registrar=event.registrar(DifMod.MODID).versioned("1");
		// Server-bound packets (Client -> Server)
		registrar.playToServer(JetpackFlyMessage.TYPE,JetpackFlyMessage.STREAM_CODEC,JetpackFlyMessage::handle);
		registrar.playToServer(EnderOpenMessage.TYPE,EnderOpenMessage.STREAM_CODEC,EnderOpenMessage::handle);
		registrar.playToServer(SpaceshipScreenButtonMessage.TYPE,SpaceshipScreenButtonMessage.STREAM_CODEC,SpaceshipScreenButtonMessage::handle);
		registrar.playToServer(ShiftGearPacket.TYPE,ShiftGearPacket.STREAM_CODEC,ShiftGearPacket::handle);
		registrar.playToClient(JetpackSyncMessage.TYPE,JetpackSyncMessage.STREAM_CODEC,JetpackSyncMessage::handle);
		// Client-bound packets (Server -> Client)
		registrar.playToClient(SyncCarPositionPacket.TYPE,SyncCarPositionPacket.STREAM_CODEC,SyncCarPositionPacket::handle);
	}
}