package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = DifMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(DifMod.MODID).versioned("1");

        // Server-bound packets (Client -> Server)
        registrar.playToServer(
                JetpackFlyMessage.TYPE,
                JetpackFlyMessage.STREAM_CODEC,
                JetpackFlyMessage::handle
        );
        registrar.playToServer(
                EnderOpenMessage.TYPE,
                EnderOpenMessage.STREAM_CODEC,
                EnderOpenMessage::handle
        );
        registrar.playToServer(
                RemoteControlPacket.TYPE,
                RemoteControlPacket.STREAM_CODEC,
                RemoteControlPacket::handle
        );
        registrar.playToServer(
                MegaBackpackOpenPacket.TYPE,
                MegaBackpackOpenPacket.STREAM_CODEC,
                MegaBackpackOpenPacket::handle
        );
        registrar.playToServer(
                ShiftGearPacket.TYPE,
                ShiftGearPacket.STREAM_CODEC,
                ShiftGearPacket::handle
        );
        registrar.playToServer(
                CameraExitPacket.TYPE,
                CameraExitPacket.STREAM_CODEC,
                CameraExitPacket::handle
        );

        // Client-bound packets (Server -> Client)
        registrar.playToClient(
                SyncCarPositionPacket.TYPE,
                SyncCarPositionPacket.STREAM_CODEC,
                SyncCarPositionPacket::handle
        );
    }
}