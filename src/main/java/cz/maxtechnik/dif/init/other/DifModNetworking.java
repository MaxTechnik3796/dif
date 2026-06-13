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
		registrar.playToServer(MegaBackpackOpenPacket.TYPE,MegaBackpackOpenPacket.STREAM_CODEC,MegaBackpackOpenPacket::handle);
		registrar.playToServer(MegaBackpackPagePacket.TYPE,MegaBackpackPagePacket.STREAM_CODEC,MegaBackpackPagePacket::handle);
		registrar.playToServer(SpaceshipScreenButtonMessage.TYPE,SpaceshipScreenButtonMessage.STREAM_CODEC,SpaceshipScreenButtonMessage::handle);
		registrar.playToServer(ShiftGearPacket.TYPE,ShiftGearPacket.STREAM_CODEC,ShiftGearPacket::handle);
		registrar.playToServer(CameraExitPacket.TYPE,CameraExitPacket.STREAM_CODEC,CameraExitPacket::handle);
		registrar.playToClient(JetpackSyncMessage.TYPE,JetpackSyncMessage.STREAM_CODEC,JetpackSyncMessage::handle);
		// Client-bound packets (Server -> Client)
		registrar.playToClient(SyncCarPositionPacket.TYPE,SyncCarPositionPacket.STREAM_CODEC,SyncCarPositionPacket::handle);
		registrar.playToServer(ForgeSelectFluidPacket.TYPE,ForgeSelectFluidPacket.STREAM_CODEC,ForgeSelectFluidPacket::handle);
	}
}