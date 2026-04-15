package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.gui.menu.MegaBackpackMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BackpackPagePacket {
	private final int delta;

	public BackpackPagePacket(int delta) {
		this.delta = delta;
	}

	public BackpackPagePacket(FriendlyByteBuf buf) {
		this.delta = buf.readInt();
	}

	public static void buffer(BackpackPagePacket message, FriendlyByteBuf buf) {
		buf.writeInt(message.delta);
	}

	public static void handler(BackpackPagePacket message, Supplier<NetworkEvent.Context> ctx) {
		NetworkEvent.Context context = ctx.get();
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null && player.containerMenu instanceof MegaBackpackMenu menu) {
				menu.changePage(message.delta);
			}
		});
		context.setPacketHandled(true);
	}

	// Registrace paketu (volá se z DifMod)
	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		DifMod.addNetworkMessage(BackpackPagePacket.class, BackpackPagePacket::buffer, BackpackPagePacket::new, BackpackPagePacket::handler);
	}
}