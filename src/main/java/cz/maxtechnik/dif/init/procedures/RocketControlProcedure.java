package cz.maxtechnik.dif.init.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

public class RocketControlProcedure {

    public static void planet(LevelAccessor world, int x, int y, int z, Player entity, int buttonId) {
        int scroll = getNBT(world, x, y, z, "scroll");
        int target = scroll + buttonId;
        // Zde doplň logiku teleportace pro planetu s indexem 'target'
        entity.displayClientMessage(net.minecraft.network.chat.Component.literal("Traveling to planet ID: " + target), true);
    }

    public static void arrow(LevelAccessor world, int x, int y, int z, Player entity, int buttonId) {
        int scroll = getNBT(world, x, y, z, "scroll");
        if (buttonId == 4 && scroll > 0) setNBT(world, x, y, z, "scroll", scroll - 1);
        if (buttonId == 5 && scroll < 12) setNBT(world, x, y, z, "scroll", scroll + 1);
    }

    public static int getNBT(LevelAccessor world, double x, double y, double z, String tag) {
        BlockEntity be = world.getBlockEntity(BlockPos.containing(x, y, z));
        if (be != null) return be.getPersistentData().getInt(tag);
        return 0;
    }

    public static void setNBT(LevelAccessor world, double x, double y, double z, String tag, int value) {
        BlockEntity be = world.getBlockEntity(BlockPos.containing(x, y, z));
        if (be != null) {
            be.getPersistentData().putInt(tag, value);
            be.setChanged();
        }
    }
}