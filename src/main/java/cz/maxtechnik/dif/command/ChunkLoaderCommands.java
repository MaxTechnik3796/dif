package cz.maxtechnik.dif.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import cz.maxtechnik.dif.init.events.ChunkLoaderData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.stream.Collectors;

public class ChunkLoaderCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("chunkloader")
                .executes(ctx -> {
                    showMainDashboard(ctx.getSource());
                    return 1;
                })
                .then(Commands.argument("player", StringArgumentType.string())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                ChunkLoaderData.get(ctx.getSource().getLevel()).loaders.stream().map(ChunkLoaderData.LoaderRecord::name).distinct(), builder))
                        .executes(ctx -> {
                            showPlayerDetail(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
                            return 1;
                        })
                )
        );
    }

    private static void showMainDashboard(CommandSourceStack source) {
        var loaders = ChunkLoaderData.get(source.getLevel()).loaders;
        source.sendSuccess(() -> Component.literal("§6§l=== CHUNKLOADERS ==="), false);

        if (loaders.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§8No chunkloaders found."), false);
            return;
        }

        long activeCount = loaders.stream().filter(ChunkLoaderData.LoaderRecord::active).count();
        source.sendSuccess(() -> Component.literal("§f (Total: " + loaders.size() + " §8|§aActive: " + activeCount + " §7| §cInactive: " + (loaders.size() - activeCount) + "§8)"), false);
        source.sendSuccess(() -> Component.literal("§7Player list:"), false);

        loaders.stream().collect(Collectors.groupingBy(ChunkLoaderData.LoaderRecord::name)).forEach((name, list) -> {
            long pActive = list.stream().filter(ChunkLoaderData.LoaderRecord::active).count();
            source.sendSuccess(() -> Component.literal(" §e• ")
                    .append(Component.literal("§f§n" + name).withStyle(s -> s
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chunkloader " + name))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Show player loaders " + name)))))
                    .append(" §7- " + list.size() + " ks §8(§a" + pActive + "§7/§c" + (list.size() - pActive) + "§8)"), false);
        });
    }

    private static void showPlayerDetail(CommandSourceStack source, String playerName) {
        var playerLoaders = ChunkLoaderData.get(source.getLevel()).loaders.stream()
                .filter(r -> r.name().equalsIgnoreCase(playerName)).toList();

        if (playerLoaders.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§cPlayer Records " + playerName + " do not exist."), false);
            return;
        }

        source.sendSuccess(() -> Component.literal("§6§l=== LOADERS: " + playerName + " ==="), false);

        var partitioned = playerLoaders.stream().collect(Collectors.partitioningBy(ChunkLoaderData.LoaderRecord::active));

        source.sendSuccess(() -> Component.literal("§aACTIVE:"), false);
        renderList(source, partitioned.get(true));

        source.sendSuccess(() -> Component.literal("\n§cINACTIVE:"), false);
        renderList(source, partitioned.get(false));
    }

    private static void renderList(CommandSourceStack source, List<ChunkLoaderData.LoaderRecord> list) {
        if (list.isEmpty()) {
            source.sendSuccess(() -> Component.literal(" §8- none"), false);
            return;
        }

        boolean isOp = source.hasPermission(2);

        for (var r : list) {
            var pos = r.pos(); // Ušetří spoustu místa při psaní .getX() atd.
            MutableComponent line = Component.literal(" §e• " + (r.is3x3() ? "§b[3x3]" : "§7[1x1]") + " §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " ");

            if (isOp) {
                line.append(Component.literal("§6[TP] ").withStyle(s -> s
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Teleport to the Chunkloader")))));

                line.append(Component.literal("§c[DEL]").withStyle(s -> s
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " minecraft:air"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Remove Chunkloader")))));
            }
            source.sendSuccess(() -> line, false);
        }
    }
}