package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.item.armor.Jetpack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=DifMod.MODID)
public class JetpackHandler{
	// Vertikální rychlost per hráče pro plynulou fyziku
	private static final Map<UUID,Float> verticalVelocity=new HashMap<>();
	private static final float MAX_VELOCITY_NORMAL=0.5F;
	private static final float MAX_VELOCITY_TURBO=0.75F;
	private static final float ACCEL_TICKS=20F;
	private static final float DECEL_TICKS=20F;

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event){
		Player player=event.getEntity();
		ItemStack chest=player.getItemBySlot(EquipmentSlot.CHEST);
		if(!(chest.getItem() instanceof Jetpack)) return;

		// Refuel cooldown — každých 5 ticků kontroluj palivo
		if(player.tickCount%5==0&&!player.level().isClientSide()){
			int fuel=Jetpack.Chestplate.getFuel(chest);
			if(fuel<=0) tryRefuel(player,chest);
		}

		// Overlay na klientu
		if(player.level().isClientSide()){
			showOverlay(player,chest);
		}
	}

	private static void tryRefuel(Player player,ItemStack chest){
		// Upřednostni normální palivo
		for(int i=0;i<player.getInventory().getContainerSize();i++){
			ItemStack s=player.getInventory().getItem(i);
			if(Jetpack.Chestplate.isFuel(s)){
				s.shrink(1);
				Jetpack.Chestplate.setFuel(chest,DifModCommonConfig.jetpackMaxBasic);
				Jetpack.Chestplate.setTurbo(chest,false);
				return;
			}
		}
		// Pak turbo palivo
		for(int i=0;i<player.getInventory().getContainerSize();i++){
			ItemStack s=player.getInventory().getItem(i);
			if(Jetpack.Chestplate.isTurboFuel(s)){
				s.shrink(1);
				Jetpack.Chestplate.setFuel(chest,DifModCommonConfig.jetpackMaxTurbo);
				Jetpack.Chestplate.setTurbo(chest,true);
				return;
			}
		}
	}

	public static void fly(Player player){
		ItemStack chest=player.getItemBySlot(EquipmentSlot.CHEST);
		if(!(chest.getItem() instanceof Jetpack)) return;

		int fuel=Jetpack.Chestplate.getFuel(chest);
		boolean turbo=Jetpack.Chestplate.isTurbo(chest);
		if(fuel<=0) return;

		UUID uid=player.getUUID();
		float maxVel=turbo?MAX_VELOCITY_TURBO:MAX_VELOCITY_NORMAL;
		float accel=maxVel/ACCEL_TICKS;

		// Zrychlení vertikální rychlosti
		float curVel=verticalVelocity.getOrDefault(uid,0F);
		curVel=Math.min(curVel+accel,maxVel);
		verticalVelocity.put(uid,curVel);

		// Aplikuj pohyb
		Vec3 motion=player.getDeltaMovement();
		// Turbo: 50% rychlejší horizontálně
		double hMult=turbo?1.5:1.0;
		player.setDeltaMovement(motion.x*hMult,curVel,motion.z*hMult);
		player.fallDistance=0;

		// Spotřeba paliva
		if(!player.level().isClientSide()){
			Jetpack.Chestplate.setFuel(chest,fuel-1);
		}

		// Particles ohně na zádech — pouze server posílá klientům
		if(player.level() instanceof ServerLevel sl){
			double angle=Math.toRadians(player.getYRot());
			double bx=player.getX()-Math.sin(angle)*0.3;
			double by=player.getY()+0.8;
			double bz=player.getZ()+Math.cos(angle)*0.3;
			sl.sendParticles(ParticleTypes.FLAME,bx,by,bz,2,-Math.sin(angle)*0.05,- 0.1,Math.cos(angle)*0.05,0.02);
		}
	}

	// Zpomalení když nepřetím space
	public static void decelerate(Player player){
		UUID uid=player.getUUID();
		float curVel=verticalVelocity.getOrDefault(uid,0F);
		if(curVel<=0){
			verticalVelocity.remove(uid);
			return;
		}
		ItemStack chest=player.getItemBySlot(EquipmentSlot.CHEST);
		boolean turbo=Jetpack.Chestplate.isTurbo(chest);
		float maxVel=turbo?MAX_VELOCITY_TURBO:MAX_VELOCITY_NORMAL;
		float decel=maxVel/DECEL_TICKS;
		curVel=Math.max(0,curVel-decel);
		verticalVelocity.put(uid,curVel);
		if(curVel>0){
			Vec3 motion=player.getDeltaMovement();
			player.setDeltaMovement(motion.x,curVel,motion.z);
			player.fallDistance=0;
		}
	}

	@OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
	private static void showOverlay(Player player,ItemStack chest){
		boolean turbo=Jetpack.Chestplate.isTurbo(chest);
		int fuel=Jetpack.Chestplate.getFuel(chest);
		int max=Jetpack.Chestplate.getMaxFuel(chest);
		int pct=(max>0?(fuel*100)/max:0);

		// Počet paliva v inventáři
		// Pokud turbo dojde a zbývá turbo → ukaž červeně
        int invCount=Jetpack.Chestplate.countFuelInInventory(player, turbo);
		String invStr=invCount>99?"99+":String.valueOf(invCount);

		// Kostky progress baru — 10 kostek
		int filled=(fuel*10)/Math.max(1,max);
		StringBuilder bar= new StringBuilder("[");
		for(int i=0;i<10;i++) bar.append(i < filled ? "■" : "□");
		bar.append("]");

		// Barva
		ChatFormatting color=turbo?ChatFormatting.RED:ChatFormatting.YELLOW;

		// Formát: 🚀 {počet} {bar} {pct}%
		Component msg=Component.literal("🚀 ")
				.append(Component.literal(invStr+" ").withStyle(fuel<=0?ChatFormatting.RED:color))
				.append(Component.literal(bar+" ").withStyle(color))
				.append(Component.literal(pct+"%").withStyle(color));

		player.displayClientMessage(msg,true);
	}
}