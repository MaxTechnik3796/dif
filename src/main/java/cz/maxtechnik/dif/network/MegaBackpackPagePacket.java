package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.gui.menu.MegaBackpackMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MegaBackpackPagePacket(int delta) implements CustomPacketPayload {
	public static final Type<MegaBackpackPagePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "mega_backpack_page"));
	public static final StreamCodec<FriendlyByteBuf, MegaBackpackPagePacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, MegaBackpackPagePacket::delta,
			MegaBackpackPagePacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player && player.containerMenu instanceof MegaBackpackMenu menu) {
				menu.changePage(delta);
			}
		});
	}
}