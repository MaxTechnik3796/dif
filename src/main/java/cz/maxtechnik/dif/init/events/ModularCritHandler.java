package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.other.DifModComponents;
import cz.maxtechnik.dif.item.modular.v2.ModularTool;
import cz.maxtechnik.dif.item.modular.v2.ModularToolProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

@EventBusSubscriber(modid = DifMod.MODID)
public class ModularCritHandler {

    /**
     * Upravuje násobič kritického zásahu pro modulární nástroje.
     *
     * Logika:
     *   critMultiplier = 1.0 → vanilla násobič (obvykle 1.5x) beze změny
     *   critMultiplier = 2.0 → 2× větší krit než vanilla (nasobí vanillu * 2.0)
     *   critMultiplier = 0.5 → poloviční krit
     *
     * Vzorec: výsledný násobič = event.getDamageModifier() * critMultiplier
     * Pokud není krit (isCriticalHit() == false), event se ignoruje.
     */
    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        ItemStack weapon = event.getEntity().getMainHandItem();

        // Zkontrolujeme, zda jde o modulární nástroj
        if (!(weapon.getItem() instanceof ModularTool)) return;

        // Pokud to není kritický zásah, nic neděláme
        if (!event.isCriticalHit()) return;

        // Načteme vlastnosti nástroje
        ModularToolProperties props = weapon.get(DifModComponents.MODULAR_TOOL_PROPERTIES.get());
        if (props == null) return;

        float critMultiplier = props.critMultiplier();

        // critMultiplier == 1.0 → žádná změna (vanilla chování)
        if (critMultiplier == 1.0F) return;

        // Lineární škálování: nový násobič = stávající násobič * critMultiplier
        float newModifier = event.getDamageMultiplier() * critMultiplier;
        event.setDamageMultiplier(newModifier);
    }
}
