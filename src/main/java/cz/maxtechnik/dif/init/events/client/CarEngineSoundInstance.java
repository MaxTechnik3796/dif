package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class CarEngineSoundInstance extends AbstractTickableSoundInstance{
	private final BaseCarEntity car;
	public CarEngineSoundInstance(BaseCarEntity car){
		super(car.getEngineSound(),SoundSource.NEUTRAL,SoundInstance.createUnseededRandom());
		this.car=car;
		looping=true;
		delay=0;
		volume=0.55F;
		pitch=0.9F;
		x=car.getX();
		y=car.getY();
		z=car.getZ();
	}
	public static void play(BaseCarEntity car){
		Minecraft.getInstance().getSoundManager().play(new CarEngineSoundInstance(car));
	}
	@Override
	public void tick(){
		if(car.isRemoved()||!car.isEngineOn()){
			stop();
			return;
		}
		x=car.getX();
		y=car.getY();
		z=car.getZ();
		float f=Math.clamp(car.getRPM()/car.getMaxRPM(),0F,1F);
		pitch=0.5F+f*1.5F;
		volume=0.55F+f*0.45F;
	}
}