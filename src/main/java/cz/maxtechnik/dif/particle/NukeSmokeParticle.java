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
public class NukeSmokeParticle extends TextureSheetParticle{
	private static final int FULL_BRIGHT=0xF000F0;
	private static final float START_R=1.00F, START_G=0.75F, START_B=0.98F;
	private static final float MID_R=0.82F, MID_G=0.20F, MID_B=0.80F;
	private static final float END_R=0.24F, END_G=0.24F, END_B=0.26F;
	private final SpriteSet sprites;
	private final float baseSize;
	protected NukeSmokeParticle(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites){
		super(level,x,y,z);
		this.sprites=sprites;
		this.xd=vx;
		this.yd=vy;
		this.zd=vz;
		this.friction=0.95F;
		this.gravity=0.0F;
		this.hasPhysics=false;
		this.baseSize=8.5F+this.random.nextFloat()*3.0F;
		this.quadSize=this.baseSize;
		this.lifetime=55+this.random.nextInt(20);
		this.rCol=START_R;
		this.gCol=START_G;
		this.bCol=START_B;
		this.setSpriteFromAge(sprites);
	}
	@Override
	public int getLightColor(float partialTick){
		return FULL_BRIGHT;
	}
	@Override
	public void tick(){
		super.tick();
		if(this.removed) return;
		this.setSpriteFromAge(this.sprites);
		float progress=(float)this.age/(float)this.lifetime;
		if(progress>0.75F){
			float fade=(progress-0.75F)/0.25F;
			this.quadSize=this.baseSize*(1.0F-fade*0.70F);
		}
		if(progress<0.40F){
			float t=progress/0.40F;
			this.rCol=Mth.lerp(t,START_R,MID_R);
			this.gCol=Mth.lerp(t,START_G,MID_G);
			this.bCol=Mth.lerp(t,START_B,MID_B);
		}else{
			float t=(progress-0.40F)/0.60F;
			this.rCol=Mth.lerp(t,MID_R,END_R);
			this.gCol=Mth.lerp(t,MID_G,END_G);
			this.bCol=Mth.lerp(t,MID_B,END_B);
		}
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
			return new NukeSmokeParticle(level,x,y,z,vx,vy,vz,this.sprites);
		}
	}
}
