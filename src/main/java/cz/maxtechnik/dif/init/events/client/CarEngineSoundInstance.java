package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CarEngineSoundInstance extends AbstractTickableSoundInstance {
    private final BaseCarEntity c;
    public CarEngineSoundInstance(BaseCarEntity c) {
        super(c.getEngineSound(), SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.c = c; looping = true; delay = 0; volume = 0.55f; pitch = 0.9f; x = c.getX(); y = c.getY(); z = c.getZ();
    }
    public static void play(BaseCarEntity c) { Minecraft.getInstance().getSoundManager().play(new CarEngineSoundInstance(c)); }
    @Override public void tick() {
        if (c.isRemoved() || !c.isEngineOn()) { stop(); return; }
        x = c.getX(); y = c.getY(); z = c.getZ();
        float f = Math.max(0f, Math.min(1f, c.getRPM() / c.getMaxRPM()));
        pitch = 0.5f + f * 1.5f; volume = 0.55f + f * 0.45f;
    }
}