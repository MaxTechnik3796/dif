package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.events.JetpackHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
public record JetpackFlyMessage(int actionType) implements CustomPacketPayload {
	public static final Type<JetpackFlyMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "jetpack_fly"));
	public static final StreamCodec<FriendlyByteBuf, JetpackFlyMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, JetpackFlyMessage::actionType,
			JetpackFlyMessage::new
	);
	public static final int ACTION_FLY = 0;
	public static final int ACTION_TOGGLE_HOVER = 2;
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	public void handle(IPayloadContext context) {
		context.enqueueWork(() -> pressAction(context.player(), actionType));
	}
	public static void pressAction(Player player, int actionType) {
		if (!player.level().isLoaded(player.blockPosition())) return;
		if (actionType == ACTION_FLY) {
			JetpackHandler.fly(player);
		} else if (actionType == ACTION_TOGGLE_HOVER || actionType == 1) {
			JetpackHandler.toggleHover(player);
		}
	}
}