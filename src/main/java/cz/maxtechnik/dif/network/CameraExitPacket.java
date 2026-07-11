package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.entity.CameraMonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
public record CameraExitPacket(BlockPos monitorPos) implements CustomPacketPayload{
	public static final Type<CameraExitPacket> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"camera_exit"));
	public static final StreamCodec<FriendlyByteBuf,CameraExitPacket> STREAM_CODEC=StreamCodec.composite(
			BlockPos.STREAM_CODEC,CameraExitPacket::monitorPos,
			CameraExitPacket::new
	);
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type(){
		return TYPE;
	}
	public void handle(IPayloadContext context){
		context.enqueueWork(()->{
			if(context.player() instanceof ServerPlayer player){
				if(player.level().getBlockEntity(monitorPos) instanceof CameraMonitorBlockEntity monitor){
					monitor.setInactive();
				}
			}
		});
	}
}