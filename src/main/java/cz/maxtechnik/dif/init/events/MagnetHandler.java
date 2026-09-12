package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.tool.Magnet;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
@EventBusSubscriber(modid=DifMod.MODID)
public class MagnetHandler{
	private static final double RANGE=5.0;
	private static final double PULL_SPEED=0.25;
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event){
		Player player=event.getEntity();
		if(player.level().isClientSide) return;
		if(!hasActiveMagnet(player)) return;
		AABB area=player.getBoundingBox().inflate(RANGE);
		List<ItemEntity> items=player.level().getEntitiesOfClass(ItemEntity.class,area,
				itemEntity->!itemEntity.hasPickUpDelay()&&itemEntity.isAlive());
		if(items.isEmpty()) return;
		Vec3 playerTarget=player.position().add(0,0.3,0);
		for(ItemEntity itemEntity: items){
			Vec3 toPlayer=playerTarget.subtract(itemEntity.position());
			double distSqr=toPlayer.lengthSqr();
			if(distSqr<0.49) continue;
			double distance=Math.sqrt(distSqr);
			Vec3 motion=toPlayer.scale(PULL_SPEED/distance);
			itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(motion).scale(0.9));
			itemEntity.hurtMarked=true;
		}
	}
	private static boolean hasActiveMagnet(Player player){
		if(isMagnetEnabled(player.getMainHandItem())) return true;
		if(isMagnetEnabled(player.getOffhandItem())) return true;
		for(ItemStack stack: player.getInventory().items){
			if(isMagnetEnabled(stack)) return true;
		}
		for(ItemStack stack: player.getInventory().armor){
			if(isMagnetEnabled(stack)) return true;
		}
		return false;
	}
	private static boolean isMagnetEnabled(ItemStack stack){
		return !stack.isEmpty()&&stack.getItem() instanceof Magnet&&Magnet.isEnabled(stack);
	}
}