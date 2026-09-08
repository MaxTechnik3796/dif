package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.other.DifModMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

import java.awt.*;

@EventBusSubscriber(modid=DifMod.MODID, value=Dist.CLIENT)
public class ClientVisualEffectsRenderer{
	private static boolean wasExperienceActive=false;
	private static SoundInstance playingExperienceSound=null;
	private static float nukeFlashIntensity=0.0F;
	private static float nukeShakeIntensity=0.0F;

	public static void triggerNukeEffects(double x,double y,double z,float intensity){
		Minecraft mc=Minecraft.getInstance();
		Player player=mc.player;
		if(player==null) return;
		double dist=Math.sqrt(player.distanceToSqr(x,y,z));
		if(dist<320.0){
			float factor=(float)Math.max(0.0,1.0-(dist/320.0));
			nukeFlashIntensity=Math.clamp(Math.max(nukeFlashIntensity, factor * intensity * 1.5F), 0.0F, 1.0F);
			nukeShakeIntensity=Math.clamp(Math.max(nukeShakeIntensity, factor * intensity), 0.0F, 1.0F);
		}
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Pre event){
		Minecraft mc=Minecraft.getInstance();
		Player player=mc.player;
		if(player!=null){
			boolean isExperience=player.hasEffect(DifModMobEffects.EXPERIENCE);
			if(isExperience&&!wasExperienceActive){
				if(playingExperienceSound==null){
					playingExperienceSound=new SimpleSoundInstance(
							SoundEvents.MUSIC_DISC_OTHERSIDE.value().getLocation(),
							SoundSource.RECORDS,1.0F,1.0F,
							SoundInstance.createUnseededRandom(),false,0,
							SoundInstance.Attenuation.NONE,
							0.0,0.0,0.0,true
					);
					mc.getSoundManager().play(playingExperienceSound);
				}
			}else if(!isExperience&&wasExperienceActive){
				if(playingExperienceSound!=null){
					mc.getSoundManager().stop(playingExperienceSound);
					playingExperienceSound=null;
				}
			}
			wasExperienceActive=isExperience;
		}
		if(nukeFlashIntensity>0.0F){
			nukeFlashIntensity=Math.max(0.0F,nukeFlashIntensity-0.03F);
		}
		if(nukeShakeIntensity>0.0F){
			nukeShakeIntensity=Math.max(0.0F,nukeShakeIntensity-0.015F);
		}
	}

	@SubscribeEvent
	public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event){
		Minecraft mc=Minecraft.getInstance();
		Player player=mc.player;
		if(player==null) return;
		if(player.hasEffect(DifModMobEffects.EXPERIENCE)){
			float t=player.tickCount;
			float yaw=(t*5F)%360F;
			float pitch=(float)(Math.sin(t*0.125F)*45F)+(float)(Math.cos(t*0.1875F)*45F);
			float roll=(t*5F)%360F;
			event.setYaw(yaw);
			event.setPitch(pitch);
			event.setRoll(roll);
		}
		if(nukeShakeIntensity>0.0F){
			float shake=nukeShakeIntensity*6.5F;
			float t=player.tickCount;
			event.setPitch(event.getPitch()+(float)(Math.sin(t*1.8)*shake*0.7));
			event.setYaw(event.getYaw()+(float)(Math.cos(t*1.5)*shake*0.8));
			event.setRoll(event.getRoll()+(float)(Math.sin(t*2.3)*shake*0.5));
		}
	}

	@SubscribeEvent
	public static void onRenderGuiOverlay(RenderGuiEvent.Post event){
		Minecraft mc=Minecraft.getInstance();
		Player player=mc.player;
		if(player==null) return;
		GuiGraphics gg=event.getGuiGraphics();
		int w=mc.getWindow().getGuiScaledWidth();
		int h=mc.getWindow().getGuiScaledHeight();
		if(player.hasEffect(DifModMobEffects.EXPERIENCE)){
			// Oscillate between Yellow (hue ~0.15F) and Lime (hue ~0.30F)
			float cycle=(float)(Math.sin(player.tickCount*0.15F)+1.0F)*0.5F;
			float hue=0.15F+cycle*0.15F;
			int rgb=Color.HSBtoRGB(hue,0.95F,1.0F);
			float alpha=0.2F+(float)(Math.sin(player.tickCount*0.5F)+1.0F)/2.0F*0.35F;
			int color=((int)(alpha*255)<<24)|(rgb&0xFFFFFF);
			gg.fill(0,0,w,h,color);
		}
		if(player.hasEffect(DifModMobEffects.DRANK)){
			float time=player.tickCount*0.02F;
			float hue=0.3F+0.06F*(float)Math.sin(time);
			int rgb=Color.HSBtoRGB(hue,0.9F,0.95F);
			float pulse=(float)(Math.sin(player.tickCount*0.05F)+1F)*0.5F;
			float alpha=0.18F+pulse*0.25F;
			int color=((int)(alpha*255)<<24)|(rgb&0xFFFFFF);
			gg.fill(0,0,w,h,color);
		}
		if(nukeFlashIntensity>0.0F){
			int flashAlpha=(int)(Math.min(1.0F,nukeFlashIntensity*1.2F)*255.0F);
			if(flashAlpha>0){
				gg.fill(0,0,w,h,(flashAlpha<<24)|0xFFFFFF);
			}
		}
	}
}