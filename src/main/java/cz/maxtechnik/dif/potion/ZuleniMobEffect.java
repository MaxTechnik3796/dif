package cz.maxtechnik.dif.potion; // <-- Tvůj balíček

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class ZuleniMobEffect extends MobEffect {
    public ZuleniMobEffect() {
        // NEUTRAL kategorie, barva (třeba tmavě zelená)
        super(MobEffectCategory.NEUTRAL, 0x1A4D2E); 
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // Tento kód běží na serveru každý tick
        int duration = 60; // Doba trvání 3 sekundy (bude se neustále obnovovat)

        // 1. Vizuální zkreslení (Nausea)
        entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, duration, 0, false, false, true));
        
        // 2. Pomalost (těžké nohy)
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier, false, false, true));

        // 3. Hlad ("Munchies")
        entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, duration, amplifier, false, false, true));
        
        // 4. Trocha regenerace (relaxační efekt)
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 0, false, false, true));

        super.applyEffectTick(entity, amplifier);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Spustit každý tick
    }
}