package cz.maxtechnik.dif.init;

import cz.maxtechnik.dif.block.entity.PortalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PortalStorage {
    // Mapa: Hráč -> (JeModrý -> Pozice)
    private static final Map<UUID, Map<Boolean, BlockPos>> portals = new HashMap<>();

    public static void savePortal(UUID player, boolean isBlue, BlockPos pos) {
        portals.computeIfAbsent(player, k -> new HashMap<>()).put(isBlue, pos);
    }

    public static BlockPos getPortal(UUID player, boolean isBlue) {
        if (portals.containsKey(player)) {
            return portals.get(player).get(isBlue);
        }
        return null;
    }

    public static void removeOldPortal(ServerLevel level, UUID player, boolean isBlue) {
        BlockPos oldPos = getPortal(player, isBlue);
        if (oldPos != null && level.isLoaded(oldPos)) {
            // Pokud tam ten portál stále fyzicky je, zničíme ho
            if (level.getBlockEntity(oldPos) instanceof PortalBlockEntity) {
                level.destroyBlock(oldPos, false); // false = bez dropu
                // Pokud je portál vysoký 2 bloky, Minecraft automaticky zničí i druhou půlku díky PortalBlock.java
            }
        }
    }
}