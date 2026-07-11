package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, value=Dist.CLIENT, bus=EventBusSubscriber.Bus.GAME)
public class VehicleCameraHandler{
	public static boolean vehicleCameraLocked=false;
	@SubscribeEvent
	public static void onComputeAngles(ViewportEvent.ComputeCameraAngles event){
		Minecraft mc=Minecraft.getInstance();
		if(vehicleCameraLocked&&mc.player!=null&&mc.player.getVehicle() instanceof BaseCarEntity car){
			float partialTick=(float)event.getPartialTick();
			float lerpYaw=Mth.lerp(partialTick,car.yRotO,car.getYRot());
			float lerpPitch=Mth.lerp(partialTick,car.xRotO,car.getXRot());
			event.setYaw(lerpYaw);
			event.setPitch(lerpPitch);
			// Lock the player's head as well so their body visually matches
			mc.player.setYRot(lerpYaw);
			mc.player.setXRot(lerpPitch);
			mc.player.setYHeadRot(lerpYaw);
		}
	}
	@SubscribeEvent
	public static void onRenderHand(RenderHandEvent event){
		Minecraft mc=Minecraft.getInstance();
		if(vehicleCameraLocked&&mc.player!=null&&mc.player.getVehicle() instanceof BaseCarEntity) event.setCanceled(true);
	}
}
