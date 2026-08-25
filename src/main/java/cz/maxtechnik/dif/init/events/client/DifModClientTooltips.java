package cz.maxtechnik.dif.init.events.client;

import com.simibubi.create.foundation.item.ItemDescription;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
@EventBusSubscriber(modid = DifMod.MODID, value = Dist.CLIENT)
public class DifModClientTooltips{

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        // Kontrola tvého itemu / bloku
        if (event.getItemStack().is(DifModItems.FLUID_HATCH.get())) {
            
            // Zjistíme přesný klíč, který Create v lang souboru očekává
            String baseKey = ItemDescription.getTooltipTranslationKey(event.getItemStack().getItem());
            String requiredSummaryKey = baseKey + ".summary";

            // DEBUG KONTROLA: Pokud klíč neexistuje, vypíše ti přesný název do konzole
            if (!I18n.exists(requiredSummaryKey)) {
                System.out.println("[DIF Tooltip Error] Chybí překlad pro klíč: " + requiredSummaryKey);
                return;
            }

            // Vytvoření Create popisu s paletou
            ItemDescription description = ItemDescription.create(
                    event.getItemStack().getItem(),
                    FontHelper.Palette.STANDARD_CREATE
            );

            if (description != null) {
                // Přidá Create řádky (včetně Hold Shift / Ctrl) na začátek tooltipu
                event.getToolTip().addAll(1, description.getCurrentLines());
            }
        }
    }
}