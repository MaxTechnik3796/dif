package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.init.procedures.RocketControlProcedure;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Rocketg00ButtonMessage {
	private final int buttonID, x, y, z;

	public Rocketg00ButtonMessage(int buttonID, int x, int y, int z) {
		this.buttonID = buttonID;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Rocketg00ButtonMessage(FriendlyByteBuf buffer) {
		this.buttonID = buffer.readInt();
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
	}

	public static void buffer(Rocketg00ButtonMessage message, FriendlyByteBuf buffer) {
		buffer.writeInt(message.buttonID);
		buffer.writeInt(message.x);
		buffer.writeInt(message.y);
		buffer.writeInt(message.z);
	}

	public static void handler(Rocketg00ButtonMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			Player entity = context.getSender();
			if (entity != null) {
				LevelAccessor world = entity.level();
				if (!world.hasChunkAt(new BlockPos(message.x, message.y, message.z))) return;

				if (message.buttonID >= 0 && message.buttonID <= 3) {
					RocketControlProcedure.planet(world, message.x, message.y, message.z, entity, message.buttonID);
				} else if (message.buttonID == 4 || message.buttonID == 5) {
					RocketControlProcedure.arrow(world, message.x, message.y, message.z, entity, message.buttonID);
				}
			}
		});
		context.setPacketHandled(true);
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		DifMod.addNetworkMessage(Rocketg00ButtonMessage.class, Rocketg00ButtonMessage::buffer, Rocketg00ButtonMessage::new, Rocketg00ButtonMessage::handler);
	}
}