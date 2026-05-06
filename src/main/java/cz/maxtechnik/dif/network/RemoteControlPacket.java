package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.entity.vehicle.RemoteControlMinecart;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RemoteControlPacket(double push, boolean shouldFlip) implements CustomPacketPayload {
	public static final Type<RemoteControlPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "remote_control"));
	public static final StreamCodec<FriendlyByteBuf, RemoteControlPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.DOUBLE, RemoteControlPacket::push,
			ByteBufCodecs.BOOL, RemoteControlPacket::shouldFlip,
			RemoteControlPacket::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player) {
				ItemStack itemStack = player.getMainHandItem();
				CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
				if (itemStack.is(DifModItems.REMOTE_CONTROLLER.get()) && customData != null) {
					UUID cartUUID = customData.copyTag().getUUID("LinkedCart");
					ServerLevel level = player.serverLevel();
					Entity entity = level.getEntity(cartUUID);
					if (entity instanceof RemoteControlMinecart cart) {
						if (shouldFlip) {
							cart.flipDirection();
						}
						cart.setRemoteMovement(push);
					}
				}
			}
		});
	}
}