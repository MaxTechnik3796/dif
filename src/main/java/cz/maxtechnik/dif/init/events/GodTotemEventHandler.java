package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "dif")
public class GodTotemEventHandler {

    public static boolean isHoldingGodTotem(LivingEntity entity) {
        if (!(entity instanceof Player player)) return false;
        return player.getMainHandItem().getItem() == DifModItems.GOD_TOTEM.get() ||
                player.getOffhandItem().getItem() == DifModItems.GOD_TOTEM.get();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player target && isHoldingGodTotem(target)) {

            // Kontrola našich zbraní
            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                var item = attacker.getMainHandItem().getItem();
                if (item == DifModItems.GOD_SWORD.get() || item == DifModItems.BAN_HAMMER.get()) {
                    return; // Necháme projít, aby DivineDamageUtils mohl smazat totem
                }

                // ODRAZ (Thorns)
                if (attacker != target) {
                    attacker.hurt(target.damageSources().thorns(target), event.getAmount() * 2.0f);
                }
            }

            // TOTÁLNÍ IMUNITA (Zruší poškození od čehokoliv jiného)
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Player player = event.player;
        if (isHoldingGodTotem(player)) {
            // LÉTÁNÍ
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }

            // MAZÁNÍ NEGATIVNÍCH EFEKTŮ
            if (!player.getActiveEffects().isEmpty()) {
                player.removeAllEffects();
            }

            // FIXY (Oheň, Vzduch)
            if (player.isOnFire()) player.clearFire();
            if (player.getAirSupply() < player.getMaxAirSupply()) player.setAirSupply(player.getMaxAirSupply());

        } else if (!player.isCreative() && !player.isSpectator()) {
            // Vypnutí fly po schování totemu
            if (player.getAbilities().mayfly) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }
}