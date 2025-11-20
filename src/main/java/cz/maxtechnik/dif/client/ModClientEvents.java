package cz.maxtechnik.dif.client;

import cz.maxtechnik.dif.init.basic.DifModSounds;
import cz.maxtechnik.dif.init.special.DifModMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.Color;

@Mod.EventBusSubscriber(modid = "dif", value = Dist.CLIENT) // Tvoje MODID
public class ModClientEvents {

    private static boolean wasEffectActive = false;
    private static SoundInstance playingSound = null;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // Logika pro zvuk a stav (beze změny)
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) return;
            boolean isEffectActive = player.hasEffect(DifModMobEffects.DRANK.get());

            if (isEffectActive && !wasEffectActive) {
                if (playingSound == null) {
                    playingSound = new SimpleSoundInstance(
                            DifModSounds.FURT_TA_STEJNA_HRA.get().getLocation(),
                            SoundSource.PLAYERS,
                            1.0F, 1.0F,
                            player.getRandom(),
                            true, // Loop
                            0, SoundInstance.Attenuation.NONE,
                            0.0, 0.0, 0.0, true
                    );
                    mc.getSoundManager().play(playingSound);
                }
            }
            else if (!isEffectActive && wasEffectActive) {
                if (playingSound != null) {
                    mc.getSoundManager().stop(playingSound);
                    playingSound = null;
                }
            }
            wasEffectActive = isEffectActive;
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        var player = mc.player;

        if (player != null && player.hasEffect(DifModMobEffects.DRANK.get())) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();

            // <--- ZMĚNA: 2x pomalejší duha (7.5f / 2 = 3.75f)
            float hue = ((player.tickCount * 3.75f) % 100) / 100.0f;
            int rgbColor = Color.getHSBColor(hue, 1.0f, 1.0f).getRGB();

            // <--- ZMĚNA: 2x pomalejší blikání (1.5f / 2 = 0.75f)
            float alphaNormalized = (float) (Math.sin(player.tickCount * 0.75f) + 1.0f) / 2.0f;
            float alpha = 0.2f + (alphaNormalized * 0.5f);
            int alphaComponent = ((int) (alpha * 255.0f)) << 24;

            int finalColor = alphaComponent | (rgbColor & 0x00FFFFFF);
            guiGraphics.fill(0, 0, width, height, finalColor);
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        // Logika pro rotaci kamery (beze změny)
        Minecraft mc = Minecraft.getInstance();
        var player = mc.player;

        if (player != null && player.hasEffect(DifModMobEffects.DRANK.get())) {

            // Rychlost rotace kamery zůstává na 5.0f
            float orbitalYaw = (player.tickCount * 5.0f) % 360.0f;

            // Rychlost chaotického houpání zůstává
            float orbitalPitch = (float)(Math.sin(player.tickCount * 0.125f) * 45.0f) +
                    (float)(Math.cos(player.tickCount * 0.1875f) * 45.0f);

            // Rychlost rollu zůstává na 5.0f
            float orbitalRoll = (player.tickCount * 5.0f) % 360.0f;

            event.setYaw(orbitalYaw);
            event.setPitch(orbitalPitch);
            event.setRoll(orbitalRoll);
        }
    }
}