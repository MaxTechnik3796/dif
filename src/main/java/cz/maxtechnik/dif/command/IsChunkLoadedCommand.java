package cz.maxtechnik.dif.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.BlockPos;
public class IsChunkLoadedCommand{
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
		dispatcher.register(Commands.literal("ischunkloaded")
				.executes(ctx->{
					CommandSourceStack source=ctx.getSource();
					ChunkPos cp=new ChunkPos(BlockPos.containing(source.getPosition()));
					boolean forced=isForced(source.getLevel(),cp);
					source.sendSuccess(()->Component.literal(forced?"§aChunk ":"§cChunk ")
							.append(Component.literal("["+cp.x+", "+cp.z+"]").withStyle(ChatFormatting.AQUA))
							.append(forced?" §ais permanently loaded.":" §cisn't permanently loaded."),false);
					return 1;
				})
		);
	}
	public static boolean isForced(ServerLevel level,ChunkPos pos){
		return level.getForcedChunks().contains(pos.toLong());
	}
}