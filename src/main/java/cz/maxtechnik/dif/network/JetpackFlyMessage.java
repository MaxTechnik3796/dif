package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.events.JetpackHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record JetpackFlyMessage(int type, int pressedms) implements CustomPacketPayload {
	public static final Type<JetpackFlyMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "jetpack_fly"));
	public static final StreamCodec<FriendlyByteBuf, JetpackFlyMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, JetpackFlyMessage::type,
			ByteBufCodecs.INT, JetpackFlyMessage::pressedms,
			JetpackFlyMessage::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext context) {
		context.enqueueWork(() -> pressAction(context.player(), type));
	}

	public static void pressAction(Player player, int type) {
		Level world = player.level();
		if (!world.hasChunkAt(player.blockPosition())) return;
		if (type == 0) {
			JetpackHandler.fly(player);
		}
	}
}
