package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DifModParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, DifMod.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> HUGE_SMOKE =
            REGISTRY.register("huge_smoke", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIREBALL =
            REGISTRY.register("fire_ball", () -> new SimpleParticleType(false));
}