package cz.maxtechnik.dif.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
public class NukeBlastParticle extends TextureSheetParticle{
	private static final int FULL_BRIGHT=0xF000F0;
	private final SpriteSet sprites;
	protected NukeBlastParticle(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites){
		super(level,x,y,z);
		this.sprites=sprites;
		this.xd=vx;
		this.yd=vy;
		this.zd=vz;
		this.friction=0.90F;
		this.gravity=0.0F;
		this.hasPhysics=false;
		this.quadSize=2.5F+this.random.nextFloat()*1.5F;
		this.lifetime=14+this.random.nextInt(8);
		this.rCol=1.0F;
		this.gCol=0.75F;
		this.bCol=0.95F;
		this.setSpriteFromAge(sprites);
	}
	@Override
	public void tick(){
		super.tick();
		if(this.removed) return;
		this.setSpriteFromAge(this.sprites);
		float progress=(float)this.age/(float)this.lifetime;
		this.rCol=Mth.lerp(progress,1.0F,0.35F);
		this.gCol=Mth.lerp(progress,0.75F,0.35F);
		this.bCol=Mth.lerp(progress,0.95F,0.38F);
	}
	@Override
	public int getLightColor(float partialTick){
		return FULL_BRIGHT;
	}
	@Override
	public @NotNull ParticleRenderType getRenderType(){
		return ParticleRenderType.PARTICLE_SHEET_LIT;
	}
	public static class Provider implements ParticleProvider<SimpleParticleType>{
		private final SpriteSet sprites;
		public Provider(SpriteSet sprites){
			this.sprites=sprites;
		}
		@Override
		public Particle createParticle(@NotNull SimpleParticleType type,@NotNull ClientLevel level,double x,double y,double z,double vx,double vy,double vz){
			return new NukeBlastParticle(level,x,y,z,vx,vy,vz,this.sprites);
		}
	}
}
