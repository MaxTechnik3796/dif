package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.events.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
public record SyncCarPositionPacket(int entityId,double x,double y,double z,float yRot,float velocity) implements CustomPacketPayload{
	public static final Type<SyncCarPositionPacket> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"sync_car_pos"));
	public static final StreamCodec<FriendlyByteBuf,SyncCarPositionPacket> STREAM_CODEC=StreamCodec.composite(
			ByteBufCodecs.VAR_INT,SyncCarPositionPacket::entityId,
			ByteBufCodecs.DOUBLE,SyncCarPositionPacket::x,
			ByteBufCodecs.DOUBLE,SyncCarPositionPacket::y,
			ByteBufCodecs.DOUBLE,SyncCarPositionPacket::z,
			ByteBufCodecs.FLOAT,SyncCarPositionPacket::yRot,
			ByteBufCodecs.FLOAT,SyncCarPositionPacket::velocity,
			SyncCarPositionPacket::new
	);
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type(){
		return TYPE;
	}
	public void handle(IPayloadContext context){
		context.enqueueWork(()->ClientPacketHandler.handleSyncCarPosition(this));
	}
}