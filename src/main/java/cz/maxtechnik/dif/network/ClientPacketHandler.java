package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {
    public static void handleSyncCarPosition(ModNetworking.SyncCarPositionPacket p) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getEntity(p.entityId()) instanceof BaseCarEntity c) {
            if (mc.player == null || mc.player.getVehicle() != c) {
                c.lerpTo(p.x(), p.y(), p.z(), p.yRot(), c.getXRot(), 3, true);
            }
            c.setVelocityFromPacket(p.velocity());
        }
    }
}
