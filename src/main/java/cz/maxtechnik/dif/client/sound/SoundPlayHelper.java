package cz.maxtechnik.dif.client.sound;

import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundPlayHelper {
    public static void playEngineSound(BaseCarEntity car) {
        Minecraft.getInstance().getSoundManager().play(new CarEngineSoundInstance(car, car.getEngineSound()));
    }
}