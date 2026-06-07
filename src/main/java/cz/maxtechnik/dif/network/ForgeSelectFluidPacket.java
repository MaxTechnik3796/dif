package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Klient → Server.
 * Odesílá se když hráč vybere kapalinu v radial menu.
 * Server nastaví preferredOutputTank a aktualizuje render order.
 */
public record ForgeSelectFluidPacket(BlockPos pos, int tankIndex) implements CustomPacketPayload {

    public static final Type<ForgeSelectFluidPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "forge_select_fluid"));

    public static final StreamCodec<FriendlyByteBuf, ForgeSelectFluidPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.fromCodec(BlockPos.CODEC), ForgeSelectFluidPacket::pos,
                    ByteBufCodecs.INT, ForgeSelectFluidPacket::tankIndex,
                    ForgeSelectFluidPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            if (player.level().getBlockEntity(pos) instanceof ForgeControllerBlockEntity be) {
                be.setPreferredOutputTank(tankIndex, player);
            }
        });
    }
}