package cz.maxtechnik.dif.init.event;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.other.DifModKeys;
import cz.maxtechnik.dif.item.armor.Jetpack;
import cz.maxtechnik.dif.network.JetpackSyncMessage;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
@EventBusSubscriber(modid=DifMod.MODID)
public class JetpackHandler{
	private static final float FLY_SPEED=0.5F;
	private static final int FUEL_COST=1;
	private static final int FUEL_INTERVAL=4;
	private static final Map<UUID,Integer> lastFlyTick=new HashMap<>();
	@SubscribeEvent
	public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event){
		lastFlyTick.remove(event.getEntity().getUUID());
	}
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event){
		Player player=event.getEntity();
		if(player.isCreative()||player.isSpectator()||player.getAbilities().flying) return;
		ItemStack chest=player.getItemBySlot(EquipmentSlot.CHEST);
		if(chest.getItem() instanceof Jetpack) tickHover(player,chest);
	}
	public static void fly(Player player){
		ItemStack chest=player.getItemBySlot(EquipmentSlot.CHEST);
		if(!(chest.getItem() instanceof Jetpack)||Jetpack.Chestplate.isOff(chest)) return;
		if(Jetpack.Chestplate.getThrust(chest)<=0) return;
		lastFlyTick.put(player.getUUID(),player.tickCount);
		applyThrust(player,chest,player.isCrouching()?0:FLY_SPEED);
	}
	public static void toggleHover(Player player){
		ItemStack chest=player.getItemBySlot(EquipmentSlot.CHEST);
		if(!(chest.getItem() instanceof Jetpack)) return;
		int next=(Jetpack.Chestplate.getMode(chest)+1)%3;
		if(next!=2&&Jetpack.Chestplate.getThrust(chest)<=0) next=2;
		Jetpack.Chestplate.setMode(chest,next);
	}
	public static void tickHover(Player player,ItemStack chest){
		if(!Jetpack.Chestplate.isHovering(chest)||player.onGround()) return;
		if(Jetpack.Chestplate.getThrust(chest)<=0){
			Jetpack.Chestplate.setMode(chest,2);
			return;
		}
		if(isFlying(player)) return;
		applyThrust(player,chest,player.isCrouching()?-0.25:0);
	}
	private static void applyThrust(Player player,ItemStack chest,double yMotion){
		Vec3 motion=player.getDeltaMovement();
		player.setDeltaMovement(motion.x,yMotion,motion.z);
		player.resetFallDistance();
		if(player.tickCount%FUEL_INTERVAL==0&&!player.level().isClientSide()){
			int newFuel=Math.max(0,Jetpack.Chestplate.getThrust(chest)-FUEL_COST);
			Jetpack.Chestplate.setThrust(chest,newFuel);
			if(player instanceof ServerPlayer sp) PacketDistributor.sendToPlayer(sp,new JetpackSyncMessage(newFuel));
		}
		spawnParticles(player);
	}
	private static boolean isFlying(Player player){
		if(player.level().isClientSide()) return DifModKeys.JETPACK_FLY.isDown();
		return player.tickCount-lastFlyTick.getOrDefault(player.getUUID(),-99)<=1;
	}
	private static void spawnParticles(Player player){
		double angle=Math.toRadians(player.getYRot());
		double bx=player.getX()+Math.sin(angle)*0.3;
		double by=player.getY()+0.8;
		double bz=player.getZ()-Math.cos(angle)*0.3;
		double vx=Math.sin(angle)*0.05;
		double vz=-Math.cos(angle)*0.05;
		if(player.level().isClientSide()){
			player.level().addParticle(ParticleTypes.FLAME,bx,by,bz,vx,-0.1,vz);
		}else if(player.level() instanceof ServerLevel level&&player.tickCount%4==0){
			level.sendParticles(ParticleTypes.FLAME,bx,by,bz,1,vx,-0.1,vz,0.02);
		}
	}
}