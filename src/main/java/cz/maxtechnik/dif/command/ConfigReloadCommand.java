package cz.maxtechnik.dif.command;

import com.mojang.brigadier.CommandDispatcher;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModCommonConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Objects;
public class ConfigReloadCommand{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
		dispatcher.register(Commands.literal("dif_config_reload").requires(s->s.hasPermission(4)).executes(arguments->{
			MutableComponent message=Component.literal("");
			message.append(Component.literal("Configuration re-loaded!"));
			DifMod.sendMessageToPlayer(Objects.requireNonNull(arguments.getSource().getPlayer()),message);
			DifModCommonConfig.load();
			return 0;
		}));
	}
}
