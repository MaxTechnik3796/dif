package cz.maxtechnik.dif.init.events.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value=Dist.CLIENT)
public class VehicleCameraHandler {
    
    public static boolean vehicleCameraLocked = false;

    @SubscribeEvent
    public static void onComputeAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (vehicleCameraLocked && mc.player != null && mc.player.getVehicle() instanceof cz.maxtechnik.dif.entity.vehicle.BaseCarEntity car) {
            float pt = (float) event.getPartialTick();
            float lerpYaw = net.minecraft.util.Mth.lerp(pt, car.yRotO, car.getYRot());
            float lerpPitch = net.minecraft.util.Mth.lerp(pt, car.xRotO, car.getXRot());
            
            event.setYaw(lerpYaw);
            
            if (mc.options.getCameraType().isFirstPerson()) {
                event.setPitch(lerpPitch);
                // Nastaví tělo k řidiči
                mc.player.setYRot(lerpYaw);
                mc.player.setXRot(lerpPitch);
                mc.player.setYHeadRot(lerpYaw);
            } else {
                // V režimu F5 - kouká mírně ze shora na auto
                event.setPitch(lerpPitch + 25f);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (vehicleCameraLocked && mc.player != null && mc.player.getVehicle() instanceof cz.maxtechnik.dif.entity.vehicle.BaseCarEntity) {
            event.setCanceled(true);
        }
    }
}
