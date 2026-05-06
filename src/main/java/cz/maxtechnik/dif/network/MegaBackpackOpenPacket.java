package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.gui.menu.MegaBackpackMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MegaBackpackOpenPacket(int type, int pressedms) implements CustomPacketPayload {
	public static final Type<MegaBackpackOpenPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "mega_backpack_open"));
	public static final StreamCodec<FriendlyByteBuf, MegaBackpackOpenPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, MegaBackpackOpenPacket::type,
			ByteBufCodecs.INT, MegaBackpackOpenPacket::pressedms,
			MegaBackpackOpenPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext context) {
		context.enqueueWork(() -> pressAction(context.player(), type));
	}

	public static void pressAction(Player player, int type) {
		if (type == 0 && player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(new SimpleMenuProvider(
					(id, inventory, p) -> new MegaBackpackMenu(id, inventory),
					Component.literal("Mega Backpack")
			), buf -> {
				buf.writeInt(0);
			});
		}
	}
}