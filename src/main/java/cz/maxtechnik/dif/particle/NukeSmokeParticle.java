package cz.maxtechnik.dif.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class NukeSmokeParticle extends TextureSheetParticle{
	private final SpriteSet sprites;
	private final float baseSize;
	private final boolean isFire;

	protected NukeSmokeParticle(ClientLevel level,double x,double y,double z,
	                            double vx,double vy,double vz,SpriteSet sprites){
		super(level,x,y,z,vx,vy,vz);
		this.sprites=sprites;
		this.setSpriteFromAge(sprites);

		this.lifetime=160+this.random.nextInt(60); // 8 - 11 sekund
		this.gravity=-0.003F; // jemné stoupání kouře
		this.hasPhysics=false;
		this.xd=vx;
		this.yd=vy;
		this.zd=vz;

		// Zvětšená velikost částice (cca 6 - 9 bloků při zrodu)
		this.baseSize=6.5F+this.random.nextFloat()*3.5F;
		this.quadSize=this.baseSize;

		// Pokud částice stoupá rychle nebo má příznak ohně, začíná jako žhnoucí plamen
		this.isFire=(vy>0.15||this.random.nextFloat()<0.35F);
		if(this.isFire){
			this.rCol=1.0F;
			this.gCol=0.55F+this.random.nextFloat()*0.25F;
			this.bCol=0.12F;
		}else{
			float shade=0.18F+this.random.nextFloat()*0.14F;
			this.rCol=shade;
			this.gCol=shade*0.96F;
			this.bCol=shade*0.92F;
		}
		this.alpha=0.92F;
	}

	@Override
	public void tick(){
		super.tick();
		this.setSpriteFromAge(this.sprites);

		float progress=(float)this.age/(float)this.lifetime;

		// Plynulé zvětšování kouře s rostoucím časem (expanze oblaku)
		this.quadSize=this.baseSize*(1.0F+progress*1.4F);

		// Pokud začala jako oheň, plynule chladne do hustého tmavého dýmu
		if(this.isFire&&progress>0.12F){
			float cool=Math.min(1.0F,(progress-0.12F)/0.35F);
			this.rCol=1.0F*(1.0F-cool)+0.22F*cool;
			this.gCol=0.6F*(1.0F-cool)+0.20F*cool;
			this.bCol=0.12F*(1.0F-cool)+0.20F*cool;
		}

		// Plynulé slábnutí v závěru života
		if(progress>0.65F){
			float fade=(progress-0.65F)/0.35F;
			this.alpha=0.92F*(1.0F-fade);
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
			return new NukeSmokeParticle(level,x,y,z,vx,vy,vz,sprites);
		}
	}
}
