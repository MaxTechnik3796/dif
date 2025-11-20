package cz.maxtechnik.dif.client; // <-- Můžeš si upravit balíček

// Důležité importy pro klienta
import cz.maxtechnik.dif.init.special.DifModMobEffects; // <--- UPRAV CESTU ke své registraci efektů
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.Color; // Použijeme java.awt.Color pro snadné HSB (duhové) barvy

/**
 * Tato třída se automaticky zaregistruje jako "event listener"
 * DŮLEŽITÉ: 'modid = "dif"' musí odpovídat tvému MODID v souboru mods.toml
 * 'value = Dist.CLIENT' zajistí, že se tento kód nikdy nespustí na serveru.
 */
@Mod.EventBusSubscriber(modid = "dif", value = Dist.CLIENT) // <--- ZMĚŇ "dif" na TVOJE MODID
public class ModClientEvents {

    /**
     * Tato metoda se zavolá každý frame po vykreslení HUDu.
     */
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        var player = mc.player;

        // 1. Zjistíme, jestli má hráč náš efekt
        // Musíš mít efekt někde registrovaný, např. ve třídě ModEffects
        // UPRAV "ModEffects.DRANK.get()" tak, aby to odpovídalo tvému kódu!
        DifModMobEffects ModEffects = null;
        if (player != null && player.hasEffect(DifModMobEffects.DRANK.get())) {

            // 2. Získáme GuiGraphics a rozměry obrazovky
            GuiGraphics guiGraphics = event.getGuiGraphics();
            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();

            // 3. Vypočítáme barvu (cyklení duhy)
            // player.tickCount je herní čas. Dělením 100f získáme cyklus každých 5 sekund.
            float hue = (player.tickCount % 100) / 100.0f;
            // Vytvoříme barvu pomocí HSB (Hue, Saturation, Brightness)
            int rgbColor = Color.getHSBColor(hue, 1.0f, 1.0f).getRGB(); // Čistá barva

            // 4. Vypočítáme blikání (měnící se průhlednost)
            // Použijeme sinusovku pro plynulé "dýchání" / pulzování
            // (Math.sin(...) + 1.0) / 2.0 převede rozsah (-1, 1) na (0, 1)
            float alphaNormalized = (float) (Math.sin(player.tickCount * 0.2f) + 1.0f) / 2.0f;

            // Chceme, aby průhlednost byla mezi 20% (0.2) a 70% (0.7)
            float alpha = 0.2f + (alphaNormalized * 0.5f); // Výsledná průhlednost 0.2 až 0.7

            // Převedeme alpha (0.0-1.0) na ARGB komponentu (0-255) a posuneme bity
            int alphaComponent = ((int) (alpha * 255.0f)) << 24;

            // 5. Spojíme barvu (RGB) a průhlednost (A)
            int finalColor = alphaComponent | (rgbColor & 0x00FFFFFF);

            // 6. Vykreslíme poloprůhledný obdélník přes celou obrazovku
            guiGraphics.fill(0, 0, width, height, finalColor);
        }
    }
}