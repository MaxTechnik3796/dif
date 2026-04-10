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
        this.car      = car;
        this.looping  = true;
        this.delay    = 0;
        this.volume   = 0.55f;
        this.pitch    = 0.9f;
        this.x        = car.getX();
        this.y        = car.getY();
        this.z        = car.getZ();
    }

    @Override
    public void tick() {
        // Zastavit zvuk pokud auto zmizelo nebo je motor vypnut
        if (this.car.isRemoved() || !this.car.isEngineOn()) {
            this.stop();
            return;
        }

        // Zvuk sleduje pohyb auta
        this.x = this.car.getX();
        this.y = this.car.getY();
        this.z = this.car.getZ();

        float rpm    = this.car.getRPM();
        float maxRpm = this.car.getMaxRPM();

        // Procento otáček 0.0 (idle) → 1.0 (redline)
        float rpmFraction = rpm / maxRpm;
        rpmFraction = Math.max(0.0f, Math.min(1.0f, rpmFraction));

        // Pitch: 0.5 (úplné minimum) → 2.0 (plný výkon)
        // Vzorec odpovídá doporučení v zadání, upravený pro realističtější idle
        this.pitch = 0.5f + rpmFraction * 1.5f;

        // Hlasitost: tišší na volnoběh, hlasitá na plný výkon
        this.volume = 0.55f + rpmFraction * 0.45f;
    }
}