package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("removal")
public class ClientSkyHandler{
	@EventBusSubscriber(modid=DifMod.MODID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
	public static class ModBusEvents{
		@SubscribeEvent
		public static void onRegisterEffects(RegisterDimensionSpecialEffectsEvent event){
			DimensionSpecialEffects spaceEffects=new DimensionSpecialEffects(
					Float.NaN,false,DimensionSpecialEffects.SkyType.NORMAL,false,false){
				@Override
				public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor,float brightness){
					return Vec3.ZERO;
				}
				@Override
				public boolean isFoggyAt(int x,int z){
					return false;
				}
			};
			event.register(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"orbit"),spaceEffects);
			event.register(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"moon"),spaceEffects);
		}
	}

	@EventBusSubscriber(modid=DifMod.MODID,bus=EventBusSubscriber.Bus.GAME,value=Dist.CLIENT)
	public static class ForgeBusEvents{
		@SubscribeEvent
		public static void onRenderSky(RenderLevelStageEvent event){
			// AFTER_SKY – renderuje se po vanillovém nebi ale před bloky
			if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_SKY) return;
			Minecraft mc=Minecraft.getInstance();
			if(mc.level==null) return;
			String dim=mc.level.dimension().location().toString();
			if(dim.equals("dif:orbit")){
				SkyRenderer.renderCustomSky(mc.level,event.getPartialTick(),event.getPoseStack(),"orbit");
			}else if(dim.equals("dif:moon")){
				SkyRenderer.renderCustomSky(mc.level,event.getPartialTick(),event.getPoseStack(),"moon");
			}
		}
	}
}