package cz.maxtechnik.dif.init.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientSkyHandler {

	// --- ČÁST PRO MOD BUS (Vypnutí mraků a vanilla oblohy) ---
	@Mod.EventBusSubscriber(modid = "dif", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ModBusEvents {
		@SubscribeEvent
		public static void onRegisterEffects(RegisterDimensionSpecialEffectsEvent event) {
			// Vytvoříme vesmírné efekty
			DimensionSpecialEffects spaceEffects = new DimensionSpecialEffects(
					Float.NaN, // cloudLevel: NaN = TOTÁLNÍ ABSENCE MRAKŮ
					false,     // hasGround: vypne mlhu pod horizontem
					DimensionSpecialEffects.SkyType.NONE, // skyType: NONE = vypne vanilla slunce/měsíc
					false,
					false
			) {
				@Override
				public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
					return Vec3.ZERO; // Černá mlha
				}

				@Override
				public boolean isFoggyAt(int x, int z) {
					return false;
				}
			};

			// Zaregistrujeme je pod ID, která jsme napsali do JSONů
			event.register(ResourceLocation.fromNamespaceAndPath("dif", "orbit"), spaceEffects);
			event.register(ResourceLocation.fromNamespaceAndPath("dif", "moon"), spaceEffects);
		}
	}

	// --- ČÁST PRO FORGE BUS (Vykreslení tvé Země) ---
	@Mod.EventBusSubscriber(modid = "dif", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
	public static class ForgeBusEvents {
		@SubscribeEvent
		public static void onRenderSky(RenderLevelStageEvent event) {
			// Kreslíme pouze ve fázi oblohy
			if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
				Minecraft mc = Minecraft.getInstance();
				if (mc.level != null) {
					String dim = mc.level.dimension().location().toString();

					if (dim.equals("dif:orbit")) {
						OrbitSkyRenderer.renderCustomSky(mc.level, event.getPartialTick(), event.getPoseStack(), "orbit");
					}
					else if (dim.equals("dif:moon")) {
						OrbitSkyRenderer.renderCustomSky(mc.level, event.getPartialTick(), event.getPoseStack(), "moon");
					}
				}
			}
		}
	}
}