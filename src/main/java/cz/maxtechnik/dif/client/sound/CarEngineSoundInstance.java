package cz.maxtechnik.dif.client.sound;

import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CarEngineSoundInstance extends AbstractTickableSoundInstance {
    private final BaseCarEntity car;

    public CarEngineSoundInstance(BaseCarEntity car, SoundEvent soundEvent) {
        super(soundEvent, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.car = car;
        this.looping = true; // Chceme, aby zvuk jel ve smyčce
        this.delay = 0;
        this.volume = 0.5f; // Počáteční hlasitost
        this.x = car.getX();
        this.y = car.getY();
        this.z = car.getZ();
    }

    @Override
    public void tick() {
        // Pokud auto zmizí nebo se vypne motor, zvuk se ukončí
        if (this.car.isRemoved() || !this.car.isEngineOn()) {
            this.stop();
            return;
        }

        // Zvuk se musí hýbat s autem
        this.x = this.car.getX();
        this.y = this.car.getY();
        this.z = this.car.getZ();

        // Dynamická změna zvuku podle tvých RPM
        float rpm = this.car.getRPM();
        float maxRpm = this.car.getMaxRPM();
        float idleRpm = this.car.getIdleRPM();

        // Spočítáme procento otáček od idle do max (0.0 až 1.0)
        float rpmFraction = (rpm - idleRpm) / (maxRpm - idleRpm);
        rpmFraction = Math.max(0.0f, Math.min(1.0f, rpmFraction));

        // ZDE MŮŽEŠ LADIT: pitch (výška) půjde od 0.8 (hlubší idle) po 1.8 (maximální rychlost)
        this.pitch = 0.8f + (rpmFraction * 0.9f);

        // ZDE MŮŽEŠ LADIT: hlasitost půjde od 0.6 do 1.0
        this.volume = 0.6f + (rpmFraction * 0.4f);
    }
}