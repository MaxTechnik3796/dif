package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler{
	public static void handleSyncCarPosition(ModNetworking.SyncCarPositionPacket packet){
		Minecraft mc=Minecraft.getInstance();
		if(mc.level!=null&&mc.level.getEntity(packet.entityId()) instanceof BaseCarEntity entity){
			if(mc.player==null||mc.player.getVehicle()!=entity) entity.lerpTo(packet.x(),packet.y(),packet.z(),packet.yRot(),entity.getXRot(),3);
			entity.setVelocityFromPacket(packet.velocity());
		}
	}
}
