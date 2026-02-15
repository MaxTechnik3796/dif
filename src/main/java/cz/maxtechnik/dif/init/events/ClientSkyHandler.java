package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
public class ClientSkyHandler{
	@Mod.EventBusSubscriber(modid=DifMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
	public static class ModBusEvents{
		@SubscribeEvent
		public static void onRegisterEffects(RegisterDimensionSpecialEffectsEvent event){
			DimensionSpecialEffects spaceEffects=new DimensionSpecialEffects(
					Float.NaN,
					false,
					DimensionSpecialEffects.SkyType.NORMAL,
					false,
					false){
				@Override
				public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 fogColor,float brightness){
					return Vec3.ZERO;
				}
				@Override
				public boolean isFoggyAt(int x,int z){return false;}
			};
			event.register(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"orbit"),spaceEffects);
			event.register(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"moon"),spaceEffects);
		}
	}
	@Mod.EventBusSubscriber(modid=DifMod.MODID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
	public static class ForgeBusEvents{
		@SubscribeEvent
		public static void onRenderSky(RenderLevelStageEvent event){
			if(event.getStage()==RenderLevelStageEvent.Stage.AFTER_SKY){
				Minecraft mc=Minecraft.getInstance();
				if(mc.level!=null){
					String dim=mc.level.dimension().location().toString();
					if(dim.equals("dif:orbit")){
						OrbitSkyRenderer.renderCustomSky(mc.level,event.getPartialTick(),event.getPoseStack(),"orbit");
					}else if(dim.equals("dif:moon")){
						OrbitSkyRenderer.renderCustomSky(mc.level,event.getPartialTick(),event.getPoseStack(),"moon");
					}
				}
			}
		}
	}
}