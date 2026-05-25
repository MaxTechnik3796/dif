package cz.maxtechnik.dif.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
@OnlyIn(Dist.CLIENT)
public class FireballParticle extends TextureSheetParticle{
	private final SpriteSet spriteSet;
	protected FireballParticle(ClientLevel level,double x,double y,double z,
	                           double vx,double vy,double vz,SpriteSet sprites){
		super(level,x,y,z);
		this.spriteSet=sprites;
		this.pickSprite(sprites);         // nastav sprite ihned
		this.quadSize*=72.0F;
		this.lifetime=200;             // ~10 sekund
		this.gravity=-0.01F;           // lehce stoupá
		this.hasPhysics=false;
		this.xd=vx;
		this.yd=vy;
		this.zd=vz;
		this.alpha=1.0F;
	}
	@Override
	public void tick(){
		super.tick();
		// Animace – střídá framy podle věku
		this.setSpriteFromAge(spriteSet);
		// Mizení v poslední třetině
		if(this.age>this.lifetime*0.66f){
			float t=(this.age-this.lifetime*0.66f)/(this.lifetime*0.34f);
			this.alpha=(1.0F-t);
		}
	}
	@Override
	public @NotNull ParticleRenderType getRenderType(){
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}
	public static class Factory implements ParticleProvider<SimpleParticleType>{
		private final SpriteSet sprites;
		public Factory(SpriteSet sprites){
			this.sprites=sprites;
		}
		@Override
		public Particle createParticle(@NotNull SimpleParticleType type,@NotNull ClientLevel level,
		                               double x,double y,double z,
		                               double vx,double vy,double vz){
			return new FireballParticle(level,x,y,z,vx,vy,vz,sprites);
		}
	}
}