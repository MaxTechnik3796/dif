package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;

import java.util.Objects;
@Mod.EventBusSubscriber
public class DifModCommands{
	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event){
		event.getDispatcher().register(Commands.literal("dif_config_reload").requires(s->s.hasPermission(4)).executes(arguments->{
			MutableComponent message=Component.literal("");
			message.append(Component.literal("Configuration re-loaded!"));
			DifMod.sendMessageToPlayer(Objects.requireNonNull(arguments.getSource().getPlayer()),message);
			DifModConfig.load();
			return 0;
		}));
	}
}
