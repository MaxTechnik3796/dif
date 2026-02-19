package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Messages {
	private static SimpleChannel INSTANCE;
	private static int packetId = 0;
	private static int id() { return packetId++; }

	public static void register() {
		SimpleChannel net = NetworkRegistry.ChannelBuilder
				.named(new ResourceLocation(DifMod.MODID, "messages"))
				.networkProtocolVersion(() -> "1.0")
				.clientAcceptedVersions(s -> true)
				.serverAcceptedVersions(s -> true)
				.simpleChannel();

		INSTANCE = net;

		net.messageBuilder(PacketJetpackKey.class, id(), NetworkDirection.PLAY_TO_SERVER)
				.decoder(PacketJetpackKey::decode)
				.encoder(PacketJetpackKey::encode)
				.consumerMainThread(PacketJetpackKey::handle)
				.add();
	}

	public static <MSG> void sendToServer(MSG message) {
		INSTANCE.sendToServer(message);
	}
}