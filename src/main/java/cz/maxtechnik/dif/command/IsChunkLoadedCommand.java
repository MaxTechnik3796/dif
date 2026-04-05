package cz.maxtechnik.dif.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.ChatFormatting;

public class IsChunkLoadedCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ischunkloaded")
            .executes(context -> {
                CommandSourceStack source = context.getSource();
                ServerLevel level = source.getLevel();
                ChunkPos chunkPos = new ChunkPos(BlockPos.containing(source.getPosition()));
                boolean isForced = level.getForcedChunks().contains(chunkPos.toLong());
                if (isForced) {
                    source.sendSuccess(() -> Component.literal("This chunk ")
                        .append(Component.literal("[" + chunkPos.x + ", " + chunkPos.z + "]").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" is permanently loaded.").withStyle(ChatFormatting.GREEN)), false);
                } else {
                    source.sendFailure(Component.literal("This chunk ")
                        .append(Component.literal("[" + chunkPos.x + ", " + chunkPos.z + "]").withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" isn't permanently loaded.")));
                }
                return 1;
            })
        );
    }
}