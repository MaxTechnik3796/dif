package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import cz.maxtechnik.dif.init.other.DifModKeys;
import cz.maxtechnik.dif.network.ModNetworking;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.GAME)
public class CarInputHandler{
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event){
		Minecraft mc=Minecraft.getInstance();
		if(mc.player==null||mc.screen!=null) return;
		if(!(mc.player.getVehicle() instanceof BaseCarEntity car)) return;
		while(DifModKeys.GEAR_UP.consumeClick()) handleShift(car,+1);
		while(DifModKeys.GEAR_DOWN.consumeClick()) handleShift(car,-1);
	}
	@SubscribeEvent
	public static void onMovementInput(MovementInputUpdateEvent event){
		if(event.getEntity().getVehicle() instanceof BaseCarEntity){
			boolean gas=DifModKeys.CAR_GAS.isDown();
			boolean brake=DifModKeys.CAR_BRAKE.isDown();
			net.minecraft.client.player.Input input=event.getInput();
			input.forwardImpulse=gas?1.0F:0.0F;
			input.jumping=brake;
		}
	}
	private static void handleShift(BaseCarEntity car,int direction){
		int current=car.getCurrentGear();
		int maxGear=car.getGearRatios().length;
		float speedKmh=car.getSpeedKmh();
		int newGear=current+direction;
		// Ohraničení sekvence: R(-1) až maxGear
		if(newGear<-1) newGear=-1;
		if(newGear>maxGear) newGear=maxGear;
		// R(-1): pouze stojíš A máš palivo
		if(newGear==-1&&(speedKmh>0.5f||car.getFuelMb()<=0.0f)) newGear=0; // fallback na N
		// Downshift rev-protection (jen pro 3↓2, 4↓3 atd.)
		if(direction<0&&current>1&&newGear>0){
			float[] ratios=car.getGearRatios();
			float newRatio=ratios[newGear-1];
			float rpmConv=car.getMaxRPM()/((car.getMaxSpeedKmh()/72.0f)*ratios[ratios.length-1]);
			float estRPM=(speedKmh/72.0f)*newRatio*rpmConv;
			if(estRPM>car.getRedlineRPM()*1.02f) return;
		}
		if(newGear!=current){
			// Okamžitá klientská predikce → HUD se okamžitě aktualizuje
			car.setCurrentGear(newGear);
			// Autoritativní potvrzení serveru
			PacketDistributor.sendToServer(new ModNetworking.ShiftGearPacket(direction));
		}
	}
}