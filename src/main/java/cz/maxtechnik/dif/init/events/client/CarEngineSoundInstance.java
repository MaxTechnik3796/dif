package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class CarEngineSoundInstance extends AbstractTickableSoundInstance{
	private final BaseCarEntity c;
	public CarEngineSoundInstance(BaseCarEntity c){
		super(c.getEngineSound(),SoundSource.NEUTRAL,SoundInstance.createUnseededRandom());
		this.c=c;
		looping=true;
		delay=0;
		volume=0.55F;
		pitch=0.9F;
		x=c.getX();
		y=c.getY();
		z=c.getZ();
	}
	public static void play(BaseCarEntity c){
		Minecraft.getInstance().getSoundManager().play(new CarEngineSoundInstance(c));
	}
	@Override
	public void tick(){
		if(c.isRemoved()||!c.isEngineOn()){
			stop();
			return;
		}
		x=c.getX();
		y=c.getY();
		z=c.getZ();
		float f=Math.max(0F,Math.min(1F,c.getRPM()/c.getMaxRPM()));
		pitch=0.5F+f*1.5F;
		volume=0.55F+f*0.45F;
	}
}