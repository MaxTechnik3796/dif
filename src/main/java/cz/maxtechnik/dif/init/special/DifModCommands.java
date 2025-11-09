package cz.maxtechnik.dif.init.special;


import cz.maxtechnik.dif.DifModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;

@Mod.EventBusSubscriber
public class DifModCommands{
	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event){
		event.getDispatcher().register(Commands.literal("dif_config_reload").requires(s->s.hasPermission(4)).executes(arguments->{
			DifModConfig.load();
			return 0;
		}));
	}
}
