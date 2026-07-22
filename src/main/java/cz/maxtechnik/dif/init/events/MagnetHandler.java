package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.tool.Magnet;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = DifMod.MODID)
public class MagnetHandler {

    private static final double RANGE = 5.0;
    private static final double PULL_SPEED = 0.25;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) return;
        if (!hasActiveMagnet(player)) return;

        AABB area = player.getBoundingBox().inflate(RANGE);
        List<ItemEntity> items = player.level().getEntitiesOfClass(ItemEntity.class, area,
                itemEntity -> !itemEntity.hasPickUpDelay() && itemEntity.isAlive());

        for (ItemEntity itemEntity : items) {
            Vec3 toPlayer = player.position().add(0, 0.3, 0).subtract(itemEntity.position());
            double distance = toPlayer.length();

            if (distance < 0.7) continue;

            Vec3 motion = toPlayer.normalize().scale(PULL_SPEED);
            itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(motion).scale(0.9));
            itemEntity.hurtMarked = true;
        }
    }

    private static boolean hasActiveMagnet(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof Magnet && Magnet.isEnabled(stack)) return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (stack.getItem() instanceof Magnet && Magnet.isEnabled(stack)) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof Magnet && Magnet.isEnabled(stack)) return true;
        }
        return false;
    }
}