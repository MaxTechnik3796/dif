package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModSounds;
import cz.maxtechnik.dif.init.other.DifModMobEffects;
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
@Mod.EventBusSubscriber(modid=DifMod.MODID,value=Dist.CLIENT)
public class DifMod_ModClientEvents{
	private static boolean wasDrankActive=false;
	private static SoundInstance playingDrankSound=null;
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event){
		if(event.phase!=TickEvent.Phase.START) return;
		Minecraft mc=Minecraft.getInstance();
		Player player=mc.player;
		if(player==null) return;
		boolean isDrank=player.hasEffect(DifModMobEffects.DRANK.get());
		// DRANK – spuštění/zastavení hudby
		if(isDrank&&!wasDrankActive){
			if(playingDrankSound==null){
				playingDrankSound=new SimpleSoundInstance(
						DifModSounds.FURT_TA_STEJNA_HRA.get().getLocation(),
						SoundSource.PLAYERS,1.0F,1.0F,
						player.getRandom(),true,0,SoundInstance.Attenuation.NONE,
						0.0,0.0,0.0,true
				);
				mc.getSoundManager().play(playingDrankSound);
			}
		}else if(!isDrank&&wasDrankActive){
			if(playingDrankSound!=null){
				mc.getSoundManager().stop(playingDrankSound);
				playingDrankSound=null;
			}
		}
		wasDrankActive=isDrank;
	}
	@SubscribeEvent
	public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event){
		Minecraft mc=Minecraft.getInstance();
		Player player=mc.player;
		if(player==null) return;
		// DRANK má absolutní přednost – úplně přepíše kameru
		if(player.hasEffect(DifModMobEffects.DRANK.get())){
			float t=player.tickCount;
			float yaw=(t*5.0f)%360.0f;
			float pitch=(float)(Math.sin(t*0.125f)*45.0f)+(float)(Math.cos(t*0.1875f)*45.0f);
			float roll=(t*5.0f)%360.0f;
			event.setYaw(yaw);
			event.setPitch(pitch);
			event.setRoll(roll);
		}
	}
	@SubscribeEvent
	public static void onRenderGuiOverlay(RenderGuiEvent.Post event){
		Minecraft mc=Minecraft.getInstance();
		Player player=mc.player;
		if(player==null) return;
		GuiGraphics gg=event.getGuiGraphics();
		int w=event.getWindow().getGuiScaledWidth();
		int h=event.getWindow().getGuiScaledHeight();
		// DRANK – duhový overlay
		if(player.hasEffect(DifModMobEffects.DRANK.get())){
			float hue=(player.tickCount*3.75f%100)/100.0f;
			int rgb=Color.getHSBColor(hue,1.0f,1.0f).getRGB();
			float alpha=0.2f+(float)(Math.sin(player.tickCount*0.75f)+1.0)/2.0f*0.5f;
			int color=((int)(alpha*255)<<24)|(rgb&0xFFFFFF);
			gg.fill(0,0,w,h,color);
		}
		// ZULENÍ – žluto-zelený pulzující overlay
		if(player.hasEffect(DifModMobEffects.ZULENI.get())){
			float time=player.tickCount*0.02f;
			float hue=0.30f+0.06f*(float)Math.sin(time); // plynule mezi žlutou a zelenou
			int rgb=Color.getHSBColor(hue,0.9f,0.95f).getRGB();
			float pulse=(float)(Math.sin(player.tickCount*0.05f)+1.0)*0.5f;
			float alpha=0.18f+pulse*0.25f; // od 18% do 43% průhlednosti
			int color=((int)(alpha*255)<<24)|(rgb&0xFFFFFF);
			gg.fill(0,0,w,h,color);
		}
	}




}