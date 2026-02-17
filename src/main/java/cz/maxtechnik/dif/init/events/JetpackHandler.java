package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.item.armor.Jetpack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
@Mod.EventBusSubscriber(modid=DifMod.MODID)
public class JetpackHandler{
	private static final Field JUMPING_FIELD=ObfuscationReflectionHelper.findField(LivingEntity.class,"f_20899_"); // SRG název pro 'jumping'
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event){
		if(event.phase!=TickEvent.Phase.END) return;
		Player player=event.player;
		ItemStack chest=player.getItemBySlot(EquipmentSlot.CHEST);
		if(!(chest.getItem() instanceof Jetpack)) return;
		int main=Jetpack.Chestplate.getMainFuel(chest);
		int thrust=Jetpack.Chestplate.getThrustFuel(chest);
		boolean turbo=Jetpack.Chestplate.getTurbo(chest);
		// --- 1. REFUEL (Main z Inventáře) ---
		if(main<=0){//player.getInventory().getContainerSize()
			for(int i=0;i<9;i++){
				ItemStack fuelStack=player.getInventory().getItem(i);
				if(!player.level().isClientSide){
					if(Jetpack.Chestplate.isTurboFuel(fuelStack)){
						fuelStack.shrink(1);
						Jetpack.Chestplate.setMainFuel(chest,DifModCommonConfig.jetpackMaxBasic);
						Jetpack.Chestplate.setTurbo(chest,true);
						player.displayClientMessage(Component.literal("Jetpack refueled with TURBO!").withStyle(ChatFormatting.RED),true);
						main=DifModCommonConfig.jetpackMaxBasic;
						turbo=true;
						break;
					}else if(Jetpack.Chestplate.isFuel(fuelStack)){
						fuelStack.shrink(1);
						Jetpack.Chestplate.setMainFuel(chest,DifModCommonConfig.jetpackMaxBasic);
						Jetpack.Chestplate.setTurbo(chest,false);
						player.displayClientMessage(Component.literal("Jetpack refueled!").withStyle(ChatFormatting.GREEN),true);
						main=DifModCommonConfig.jetpackMaxBasic;
						turbo=false;
						break;
					}
				}
			}
		}
		// --- 2. DOBÍJENÍ THRUSTU (Na zemi) ---
		if(player.onGround()){
			if(thrust<(turbo?DifModCommonConfig.jetpackMaxTurbo:DifModCommonConfig.jetpackMaxThrust)&&main>0){
				int toAdd=Math.min(1,Math.min(main,(turbo?DifModCommonConfig.jetpackMaxTurbo:DifModCommonConfig.jetpackMaxThrust)-thrust));
				Jetpack.Chestplate.setMainFuel(chest,main-toAdd);
				Jetpack.Chestplate.setThrustFuel(chest,thrust+toAdd);
				showOverlay(player,thrust+toAdd,true,turbo);
			}
		}
		// --- 3. LET (Ve vzduchu) ---
		else{
			boolean isJumping=false;
			try{
				isJumping=JUMPING_FIELD.getBoolean(player);
			}catch(IllegalAccessException ignored){
			}
			if(isJumping&&thrust>0&&!player.getAbilities().flying){
				player.setDeltaMovement(player.getDeltaMovement().x,0.45,player.getDeltaMovement().z);
				player.fallDistance=0;
				Jetpack.Chestplate.setThrustFuel(chest,thrust-1);
				showOverlay(player,thrust-1,false,turbo);
			}
		}
	}
	private static void showOverlay(Player player,int thrust,boolean charging,boolean turbo){
		String icon=charging?"⚡ ":"🚀 ";
		ChatFormatting color=charging?turbo?ChatFormatting.RED:ChatFormatting.YELLOW:ChatFormatting.AQUA;
		int filled=thrust/(turbo?10:5);
		String bar="■".repeat(Math.max(0,filled))+"□".repeat(Math.max(0,10-filled));
		player.displayClientMessage(
				Component.literal(icon+"THRUST: ["+bar+"] "+(thrust*2)+"%").withStyle(color),
				true
		);
	}
}