package cz.maxtechnik.dif.init.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "dif", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientSkyHandler {

	@SubscribeEvent
	public static void onRenderSky(RenderLevelStageEvent event) {
		// Musíme kreslit až po obloze, abychom ji mohli přikrýt
		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.level != null) {
				String dimLocation = mc.level.dimension().location().toString();

				if (dimLocation.equals("dif:orbit")) {
					OrbitSkyRenderer.render(
							mc.level,
							event.getPartialTick(),
							event.getPoseStack(),
							event.getProjectionMatrix()
					);
				}
			}
		}
	}
}