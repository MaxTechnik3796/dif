package cz.maxtechnik.dif.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class HugeSmoke extends TextureSheetParticle {

    protected HugeSmoke(ClientLevel level, double x, double y, double z,
                        double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz);
        this.setSpriteFromAge(sprites);
        this.quadSize *= 24.0F;
        this.lifetime = 800;
        this.gravity = -0.01F;
        this.alpha = 0.85F;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new HugeSmoke(level, x, y, z, vx, vy, vz, sprites);
        }
    }
}