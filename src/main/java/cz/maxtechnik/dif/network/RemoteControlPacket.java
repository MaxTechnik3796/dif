package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.entity.vehicle.RemoteControlMinecart;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;
public class RemoteControlPacket{
	private final double push;
	private final boolean shouldFlip;
	public RemoteControlPacket(double push,boolean shouldFlip){
		this.push=push;
		this.shouldFlip=shouldFlip;
	}
	public static RemoteControlPacket decode(FriendlyByteBuf buffer){
		return new RemoteControlPacket(buffer.readDouble(),buffer.readBoolean());
	}
	public static void encode(RemoteControlPacket msg,FriendlyByteBuf buffer){
		buffer.writeDouble(msg.push);
		buffer.writeBoolean(msg.shouldFlip);
	}
	public static void handle(RemoteControlPacket msg,Supplier<NetworkEvent.Context> ctx){
		ctx.get().enqueueWork(()->{
			ServerPlayer player=ctx.get().getSender();
			if(player!=null){
				ItemStack itemStack=player.getMainHandItem();
				if(itemStack.is(DifModItems.REMOTE_CONTROLLER.get())&&itemStack.hasTag()){
					assert itemStack.getTag()!=null;
					UUID cartUUID=itemStack.getTag().getUUID("LinkedCart");
					ServerLevel level=player.serverLevel();
					Entity entity=level.getEntity(cartUUID);
					if(entity instanceof RemoteControlMinecart cart){
						if(msg.shouldFlip){
							cart.flipDirection();
						}
						cart.setRemoteMovement(msg.push);
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}