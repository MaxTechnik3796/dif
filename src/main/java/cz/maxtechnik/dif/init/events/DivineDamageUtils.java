package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class DivineDamageUtils {

    public static void applyDivineDamage(LivingEntity target, Player attacker, boolean isBanHammer) {
        if (target.level().isClientSide) return;

        // Vytvoření zdroje poškození (FELL_OUT_OF_WORLD obchází většinu ochran)
        DamageSource divineSource = new DamageSource(
                target.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD),
                attacker
        );

        // 1. LOGIKA PRO GOD TOTEM (Křupnutí u hráče)
        if (target instanceof Player targetPlayer) {
            ItemStack main = targetPlayer.getMainHandItem();
            ItemStack off = targetPlayer.getOffhandItem();

            if (main.getItem() == DifModItems.GOD_TOTEM.get() || off.getItem() == DifModItems.GOD_TOTEM.get()) {
                if (main.getItem() == DifModItems.GOD_TOTEM.get()) {
                    targetPlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                } else {
                    targetPlayer.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
                }
                targetPlayer.level().broadcastEntityEvent(targetPlayer, (byte) 35);
                return; // Totem zachránil cíl, končíme
            }
        }

        // 2. BAN LOGIKA (Pouze pro hráče přes Ban Hammer)
        if (isBanHammer && target instanceof Player targetPlayer) {
            MinecraftServer server = target.getServer();
            if (server != null) {
                String name = targetPlayer.getGameProfile().getName();
                server.getCommands().performPrefixedCommand(
                        server.createCommandSourceStack(),
                        "ban " + name + " Zabanován Božským Kladivem!"
                );
            }
        }

        // 3. ABSOLUTNÍ BYPASS (Oprava getAbilities a vynucení smrti)

        // Vypnutí nesmrtelnosti na úrovni základní entity
        target.setInvulnerable(false);

        // Pokud je cíl hráč, musíme vypnout i Player Abilities (zde byla ta chyba)
        if (target instanceof Player targetPlayer) {
            targetPlayer.getAbilities().invulnerable = false;
            targetPlayer.onUpdateAbilities();
        }

        // Pokus o standardní zabití (pro hroby/corpse)
        target.hurt(divineSource, Float.MAX_VALUE);

        // Finální bypass pro Avaritia / Lost Depths
        if (target.isAlive()) {
            target.setHealth(0.0f); // Přímé nastavení HP na 0
            target.die(divineSource); // Vynucení eventu smrti

            // Pokud po tom všem entita stále existuje (extrémní bugy/mody)
            if (target.isAlive()) {
                target.discard(); // Úplné smazání z paměti serveru
            }
        }
    }
}