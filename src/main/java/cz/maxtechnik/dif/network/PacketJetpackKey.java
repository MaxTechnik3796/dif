package cz.maxtechnik.dif.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class PacketJetpackKey {
	private final boolean isPressed;

	public PacketJetpackKey(boolean isPressed) {
		this.isPressed = isPressed;
	}

	public static void encode(PacketJetpackKey msg, FriendlyByteBuf buffer) {
		buffer.writeBoolean(msg.isPressed);
	}

	public static PacketJetpackKey decode(FriendlyByteBuf buffer) {
		return new PacketJetpackKey(buffer.readBoolean());
	}

	public static void handle(PacketJetpackKey msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player != null) {
				// Uložíme informaci do dočasných dat hráče na serveru
				player.getPersistentData().putBoolean("isJetpackKeyPressed", msg.isPressed);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}