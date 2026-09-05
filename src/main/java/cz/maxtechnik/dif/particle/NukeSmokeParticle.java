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
	private final float initialR, initialG, initialB;
	private final boolean isGlowing;

	protected NukeSmokeParticle(ClientLevel level,double x,double y,double z,
	                            double vx,double vy,double vz,SpriteSet sprites){
		super(level,x,y,z,0.0,0.0,0.0);
		this.sprites=sprites;
		this.setSpriteFromAge(sprites);

		// vx: Packed 24-bit RGB barva
		int rgb=(int)vx;
		if(rgb!=0){
			this.initialR=((rgb>>16)&0xFF)/255.0F;
			this.initialG=((rgb>>8)&0xFF)/255.0F;
			this.initialB=(rgb&0xFF)/255.0F;
		}else{
			this.initialR=0.25F;
			this.initialG=0.25F;
			this.initialB=0.25F;
		}
		this.rCol=this.initialR;
		this.gCol=this.initialG;
		this.bCol=this.initialB;

		// Je částice svítivá/žhnoucí? (Žlutá, oranžová, červená nebo azurová/barevná)
		this.isGlowing=(this.initialR>0.45F||this.initialG>0.35F||this.initialB>0.45F);

		// vy: Velikost částice
		float size=(float)Math.abs(vy);
		this.baseSize=(size>0.1F)?size:3.5F;
		this.quadSize=this.baseSize;

		// vz: Životnost v ticích
		int life=(int)Math.abs(vz);
		this.lifetime=(life>0)?life:120;

		this.gravity=-0.0015F;
		this.hasPhysics=false;
		this.xd=(this.random.nextFloat()-0.5F)*0.01F;
		this.yd=this.isGlowing ? (0.015F+this.random.nextFloat()*0.015F) : (0.006F+this.random.nextFloat()*0.008F);
		this.zd=(this.random.nextFloat()-0.5F)*0.01F;
		this.alpha=0.92F;
	}

	@Override
	public int getLightColor(float partialTick){
		// Žhnoucí částice (žlutá/oranžová/červená) září plným jasem i v noci
		if(this.isGlowing){
			float progress=(float)this.age/(float)this.lifetime;
			if(progress<0.55F){
				return 0xF000F0; // Plné světlo (emissive glow)
			}
		}
		return super.getLightColor(partialTick);
	}

	@Override
	public void tick(){
		super.tick();
		this.setSpriteFromAge(this.sprites);

		float progress=(float)this.age/(float)this.lifetime;

		// Plynulá expanze o 55 %
		this.quadSize=this.baseSize*(1.0F+progress*0.55F);

		// Postupné chladnutí ohnivých barev do popelavé šedé (0.28, 0.28, 0.28)
		if(this.isGlowing&&progress>0.20F){
			float cool=Math.min(1.0F,(progress-0.20F)/0.45F);
			this.rCol=this.initialR*(1.0F-cool)+0.28F*cool;
			this.gCol=this.initialG*(1.0F-cool)+0.28F*cool;
			this.bCol=this.initialB*(1.0F-cool)+0.28F*cool;
		}

		// Plynulé mizení v závěru života (od 65 %)
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
