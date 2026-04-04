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

public class RemoteControlPacket {
	private final double push;

	public RemoteControlPacket(double push) {
		this.push = push;
	}

	public static RemoteControlPacket decode(FriendlyByteBuf buffer) {
		return new RemoteControlPacket(buffer.readDouble());
	}

	public static void encode(RemoteControlPacket msg, FriendlyByteBuf buffer) {
		buffer.writeDouble(msg.push);
	}

	public static void handle(RemoteControlPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player != null) {
				ItemStack stack = player.getMainHandItem();
				if (stack.is(DifModItems.REMOTE_CONTROLLER.get())&&stack.hasTag()){
					assert stack.getTag()!=null;
					if(stack.getTag().hasUUID("LinkedCart")){
						UUID cartUUID=stack.getTag().getUUID("LinkedCart");
						ServerLevel level=player.serverLevel();
						Entity entity=level.getEntity(cartUUID);
						if(entity instanceof RemoteControlMinecart cart){
							cart.setRemoteMovement(msg.push);
						}
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}