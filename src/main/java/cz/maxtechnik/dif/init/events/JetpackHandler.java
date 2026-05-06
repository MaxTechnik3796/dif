package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.item.armor.Jetpack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid=DifMod.MODID)
public class JetpackHandler{
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!(chest.getItem() instanceof Jetpack)) return;
		int main = Jetpack.Chestplate.getMainFuel(chest);
		int thrust = Jetpack.Chestplate.getThrustFuel(chest);
		boolean turbo = Jetpack.Chestplate.getTurbo(chest);
		if (main <= 0 && !player.level().isClientSide()) {
			handleRefuel(player, chest);
		}
		if (player.onGround()) {
			handleCharging(player, chest, main, thrust, turbo);
		}
	}
	private static void handleRefuel(Player player,ItemStack chest){
		for(int i=0;i<player.getInventory().getContainerSize();i++){
			ItemStack fuelStack=player.getInventory().getItem(i);
			if(Jetpack.Chestplate.isTurboFuel(fuelStack)){
				fuelStack.shrink(1);
				player.addItem(new ItemStack(DifModItems.JETPACK_CANISTER.get()));
				Jetpack.Chestplate.setMainFuel(chest,DifModCommonConfig.jetpackMaxBasic);
				Jetpack.Chestplate.setTurbo(chest,true);
				player.displayClientMessage(Component.literal("Jetpack refueled with TURBO!").withStyle(ChatFormatting.RED),true);
				break;
			}else if(Jetpack.Chestplate.isFuel(fuelStack)){
				fuelStack.shrink(1);
				player.addItem(new ItemStack(DifModItems.JETPACK_CANISTER.get()));
				Jetpack.Chestplate.setMainFuel(chest,DifModCommonConfig.jetpackMaxBasic);
				Jetpack.Chestplate.setTurbo(chest,false);
				player.displayClientMessage(Component.literal("Jetpack refueled!").withStyle(ChatFormatting.GREEN),true);
				break;
			}
		}
	}
	private static void handleCharging(Player player,ItemStack chest,int main,int thrust,boolean turbo){
		int maxThrust=turbo?DifModCommonConfig.jetpackMaxTurbo:DifModCommonConfig.jetpackMaxThrust;
		if(thrust<maxThrust&&main>0){
			int toAdd=Math.min(1,Math.min(main,maxThrust-thrust));
			if(!player.level().isClientSide()){
				Jetpack.Chestplate.setMainFuel(chest,main-toAdd);
				Jetpack.Chestplate.setThrustFuel(chest,thrust+toAdd);
			}
			showOverlay(player,thrust+toAdd,true,turbo);
		}
	}
	private static void showOverlay(Player player,int thrust,boolean charging,boolean turbo){
		if(player.level().isClientSide()){
			String icon=charging?"⚡ ":"🚀 ";
			ChatFormatting color=charging?(turbo?ChatFormatting.RED:ChatFormatting.YELLOW):ChatFormatting.AQUA;
			int max=turbo?DifModCommonConfig.jetpackMaxTurbo:DifModCommonConfig.jetpackMaxThrust;
			int filled=(thrust*10)/max;
			String bar="■".repeat(Math.max(0,filled))+"□".repeat(Math.max(0,10-filled));
			player.displayClientMessage(
					Component.literal(icon+"THRUST: ["+bar+"] "+((thrust*100)/max)+"%").withStyle(color),
					true
			);
		}
	}
	public static void fly(Player player){
		ItemStack chest=player.getItemBySlot(EquipmentSlot.CHEST);
		int thrust=Jetpack.Chestplate.getThrustFuel(chest);
		boolean turbo=Jetpack.Chestplate.getTurbo(chest);
		if(!player.onGround()){
			if(thrust>0&&!player.getAbilities().flying){
				player.setDeltaMovement(player.getDeltaMovement().x,0.45,player.getDeltaMovement().z);
				player.fallDistance=0;
				if(!player.level().isClientSide()) Jetpack.Chestplate.setThrustFuel(chest,thrust-1);
				showOverlay(player,thrust-1,false,turbo);
			}
		}
	}
}