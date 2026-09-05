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

	public static final DeferredHolder<ParticleType<?>, SimpleParticleType> NUKE_SMOKE =
			REGISTRY.register("nuke_smoke", () -> new SimpleParticleType(false));
}
