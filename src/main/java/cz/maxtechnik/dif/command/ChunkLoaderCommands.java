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

import java.util.*;

public class ChunkLoaderCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("chunkloader")
                // Hlavní příkaz bez argumentů
                .executes(ctx -> {
                    showMainDashboard(ctx.getSource());
                    return 1;
                })
                // Detail hráče přes /chunkloader [jméno]
                .then(Commands.argument("player", StringArgumentType.string())
                        .suggests((ctx, builder) -> {
                            var names = ChunkLoaderData.get(ctx.getSource().getLevel()).loaders.stream()
                                    .map(ChunkLoaderData.LoaderRecord::name).distinct();
                            return SharedSuggestionProvider.suggest(names, builder);
                        })
                        .executes(ctx -> {
                            showPlayerDetail(ctx.getSource(), StringArgumentType.getString(ctx, "player"));
                            return 1;
                        })
                )
        );
    }

    private static void showMainDashboard(CommandSourceStack source) {
        ChunkLoaderData data = ChunkLoaderData.get(source.getLevel());
        source.sendSuccess(() -> Component.literal("§6§l=== CHUNKLOADERS ==="), false);

        if (data.loaders.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§8No chunkloaders found."), false);
            return;
        }

        // Výpočet celkových statistik (vzaté z ModCommands)
        long total = data.loaders.size();
        long activeCount = data.loaders.stream().filter(ChunkLoaderData.LoaderRecord::active).count();
        long offCount = total - activeCount;

        source.sendSuccess(() -> Component.literal("§f (Total: " + total + " §8|§aActive: " + activeCount + " §7| §cInactive: " + offCount + "§8)"), false);
        source.sendSuccess(() -> Component.literal("§7Player list:"), false);

        Map<String, List<ChunkLoaderData.LoaderRecord>> grouped = new HashMap<>();
        for (var r : data.loaders) {
            grouped.computeIfAbsent(r.name(), k -> new ArrayList<>()).add(r);
        }

        // Výpis hráčů s podtrženým jménem jako tlačítkem
        grouped.forEach((name, list) -> {
            long pActive = list.stream().filter(ChunkLoaderData.LoaderRecord::active).count();

            MutableComponent playerLine = Component.literal(" §e• ")
                    .append(Component.literal("§f§n" + name)
                            .withStyle(s -> s
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chunkloader " + name))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Show player loaders " + name)))))
                    .append(Component.literal(" §7- " + list.size() + " ks §8(§a" + pActive + "§7/§c" + (list.size() - pActive) + "§8)"));
            source.sendSuccess(() -> playerLine, false);
        });
    }

    private static void showPlayerDetail(CommandSourceStack source, String playerName) {
        ChunkLoaderData data = ChunkLoaderData.get(source.getLevel());
        List<ChunkLoaderData.LoaderRecord> all = data.loaders.stream()
                .filter(r -> r.name().equalsIgnoreCase(playerName)).toList();

        if (all.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§cPlayer Records " + playerName + " do not exist."), false);
            return;
        }

        source.sendSuccess(() -> Component.literal("§6§l=== LOADERS: " + playerName + " ==="), false);

        // Sekce ACTIVE
        source.sendSuccess(() -> Component.literal("§aACTIVE:"), false);
        renderList(source, all.stream().filter(ChunkLoaderData.LoaderRecord::active).toList());

        // Sekce DEACTIVE
        source.sendSuccess(() -> Component.literal("\n§cINACTIVE:"), false);
        renderList(source, all.stream().filter(r -> !r.active()).toList());
    }

    private static void renderList(CommandSourceStack source, List<ChunkLoaderData.LoaderRecord> list) {
        if (list.isEmpty()) {
            source.sendSuccess(() -> Component.literal(" §8- none"), false);
            return;
        }

        boolean isOp = source.hasPermission(2);

        for (var r : list) {
            String type = r.is3x3() ? "§b[3x3]" : "§7[1x1]";
            MutableComponent line = Component.literal(" §e• " + type + " §f" + r.pos().getX() + ", " + r.pos().getY() + ", " + r.pos().getZ() + " ");

            if (isOp) {
                // TP tlačítko
                line.append(Component.literal("§6[TP] ").withStyle(s -> s
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + r.pos().getX() + " " + r.pos().getY() + " " + r.pos().getZ()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Teleport to the block")))));

                // DELETE tlačítko
                String setblockCmd = "/setblock " + r.pos().getX() + " " + r.pos().getY() + " " + r.pos().getZ() + " minecraft:air";
                line.append(Component.literal("§c[DEL]").withStyle(s -> s
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, setblockCmd))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Setnlock Chunkloader")))));
            }
            source.sendSuccess(() -> line, false);
        }
    }
}