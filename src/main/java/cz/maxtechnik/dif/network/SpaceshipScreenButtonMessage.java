package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.init.events.SpaceshipControl;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SpaceshipScreenButtonMessage(int buttonID, int x, int y, int z) implements CustomPacketPayload {
	public static final Type<SpaceshipScreenButtonMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "spaceship_button"));
	public static final StreamCodec<FriendlyByteBuf, SpaceshipScreenButtonMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, SpaceshipScreenButtonMessage::buttonID,
			ByteBufCodecs.INT, SpaceshipScreenButtonMessage::x,
			ByteBufCodecs.INT, SpaceshipScreenButtonMessage::y,
			ByteBufCodecs.INT, SpaceshipScreenButtonMessage::z,
			SpaceshipScreenButtonMessage::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext context) {
		context.enqueueWork(() -> {
			Player entity = context.player();
			LevelAccessor world = entity.level();
			if (!world.hasChunkAt(new BlockPos(x, y, z))) return;
			if (buttonID >= 0 && buttonID <= 3) {
				SpaceshipControl.planet(world, x, y, z, entity, buttonID);
			} else if (buttonID == 4 || buttonID == 5) {
				SpaceshipControl.arrow(world, x, y, z, buttonID);
			}
		});
	}
}