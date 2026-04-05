package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.block.entity.MonitorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;
public class CameraExitPacket{
	private final BlockPos monitorPos;
	public CameraExitPacket(BlockPos pos){
		this.monitorPos=pos;
	}
	public static void encode(CameraExitPacket msg,FriendlyByteBuf buffer){
		buffer.writeBlockPos(msg.monitorPos);
	}
	public static CameraExitPacket decode(FriendlyByteBuf buffer){
		return new CameraExitPacket(buffer.readBlockPos());
	}
	public static void handle(CameraExitPacket msg,Supplier<NetworkEvent.Context> ctx){
		ctx.get().enqueueWork(()->{
			if(ctx.get().getSender()!=null&&Objects.requireNonNull(ctx.get().getSender()).level().getBlockEntity(msg.monitorPos) instanceof MonitorBlockEntity monitor){
				monitor.setInactive();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}