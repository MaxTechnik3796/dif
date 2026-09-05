package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.events.client.ClientVisualEffectsRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record NukeDetonationMessage(double x, double y, double z, float intensity) implements CustomPacketPayload {
	public static final Type<NukeDetonationMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "nuke_detonation"));
	public static final StreamCodec<FriendlyByteBuf, NukeDetonationMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE, NukeDetonationMessage::x,
			ByteBufCodecs.DOUBLE, NukeDetonationMessage::y,
			ByteBufCodecs.DOUBLE, NukeDetonationMessage::z,
			ByteBufCodecs.FLOAT, NukeDetonationMessage::intensity,
			NukeDetonationMessage::new
	);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext context) {
		context.enqueueWork(() -> ClientVisualEffectsRenderer.triggerNukeEffects(x, y, z, intensity));
	}
}
