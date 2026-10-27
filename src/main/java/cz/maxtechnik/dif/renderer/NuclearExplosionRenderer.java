package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.entity.bomb.NuclearExplosionEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
public class NuclearExplosionRenderer extends EntityRenderer<NuclearExplosionEntity>{
	private static final int OUTER_BEAM_COLOR=0xC89933FF;
	private static final int INNER_BEAM_COLOR=0xFFE0FFFF;
	private static final ItemStack NETHER_STAR_STACK=new ItemStack(Items.NETHER_STAR);
	private final ItemRenderer itemRenderer;
	public NuclearExplosionRenderer(EntityRendererProvider.Context context){
		super(context);
		this.itemRenderer=context.getItemRenderer();
	}
	@Override
	public void render(NuclearExplosionEntity entity,float entityYaw,float partialTick,@NotNull PoseStack poseStack,@NotNull MultiBufferSource buffer,int packedLight){
		int age=entity.getAge();
		float time=age+partialTick;
		long gameTime=entity.level().getGameTime();
		if(age<NuclearExplosionEntity.CHARGE_TICKS){
			float chargeProgress=time/(float)NuclearExplosionEntity.CHARGE_TICKS;
			renderChargingStar(entity,poseStack,buffer,time,chargeProgress);
			renderBeam(poseStack,buffer,partialTick,gameTime);
		}else{
			float blastTime=(age-NuclearExplosionEntity.CHARGE_TICKS)+partialTick;
			poseStack.pushPose();
			Matrix4f matrix=poseStack.last().pose();
			VertexConsumer builder=buffer.getBuffer(RenderType.lightning());
			if(blastTime<=25F){
				float sphereProgress=blastTime/25F;
				float radius=2F+28F*sphereProgress;
				float alpha=(1F-sphereProgress)*0.85F;
				renderExplosionSphere(matrix,builder,radius,alpha);
			}
			if(blastTime>=5F&&blastTime<=70F){
				float waveProgress=(blastTime-5F)/65F;
				renderShockwaveRing(matrix,builder,waveProgress);
			}
			poseStack.popPose();
		}
		super.render(entity,entityYaw,partialTick,poseStack,buffer,packedLight);
	}
	private void renderChargingStar(NuclearExplosionEntity entity,PoseStack poseStack,MultiBufferSource buffer,float time,float progress){
		poseStack.pushPose();
		poseStack.translate(0D,1.2D,0D);
		float rotation=time*(25F+progress*60F);
		poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
		float scale=2F+progress;
		poseStack.scale(scale,scale,scale);
		this.itemRenderer.renderStatic(
				NETHER_STAR_STACK,
				ItemDisplayContext.FIXED,
				0xF000F0,
				OverlayTexture.NO_OVERLAY,
				poseStack,
				buffer,
				entity.level(),
				entity.getId()
		);
		poseStack.popPose();
	}
	private void renderBeam(PoseStack poseStack,MultiBufferSource buffer,float partialTick,long gameTime){
		poseStack.pushPose();
		poseStack.translate(-0.5D,1.2,-0.5D);
		BeaconRenderer.renderBeaconBeam(
				poseStack,buffer,BeaconRenderer.BEAM_LOCATION,
				partialTick,1.0F,gameTime,
				0,350,OUTER_BEAM_COLOR,
				0.25F,0.65F
		);
		BeaconRenderer.renderBeaconBeam(
				poseStack,buffer,BeaconRenderer.BEAM_LOCATION,
				partialTick,1F,gameTime,
				0,350,INNER_BEAM_COLOR,
				0.7F,1.5F
		);
		poseStack.popPose();
	}
	private static void renderExplosionSphere(Matrix4f matrix,VertexConsumer builder,float radius,float alpha){
		int rings=10;
		int segments=16;
		int a=(int)(alpha*255);
		for(int i=0;i<rings;i++){
			float lat0=(float)(-Math.PI/2+Math.PI*i/rings);
			float lat1=(float)(-Math.PI/2+Math.PI*(i+1)/rings);
			float y0=(float)1.5+radius*(float)Math.sin(lat0);
			float y1=(float)1.5+radius*(float)Math.sin(lat1);
			float r0=radius*(float)Math.cos(lat0);
			float r1=radius*(float)Math.cos(lat1);
			for(int j=0;j<segments;j++){
				float lon0=(float)(2*Math.PI*j/segments);
				float lon1=(float)(2*Math.PI*(j+1)/segments);
				float x00=r0*(float)Math.cos(lon0), z00=r0*(float)Math.sin(lon0);
				float x01=r0*(float)Math.cos(lon1), z01=r0*(float)Math.sin(lon1);
				float x10=r1*(float)Math.cos(lon0), z10=r1*(float)Math.sin(lon0);
				float x11=r1*(float)Math.cos(lon1), z11=r1*(float)Math.sin(lon1);
				addQuad(matrix,builder,x00,y0,z00,x10,y1,z10,x11,y1,z11,x01,y0,z01,160,80,a);
			}
		}
	}
	private static void renderShockwaveRing(Matrix4f matrix,VertexConsumer builder,float progress){
		float outerRadius=6F+75F*progress;
		float innerRadius=Math.max(0.5F,outerRadius-12F);
		float height=2.5F*(1F-progress);
		int a=(int)((1F-progress)*200);
		int segments=32;
		for(int i=0;i<segments;i++){
			float angle0=(float)(2*Math.PI*i/segments);
			float angle1=(float)(2*Math.PI*(i+1)/segments);
			float cos0=(float)Math.cos(angle0), sin0=(float)Math.sin(angle0);
			float cos1=(float)Math.cos(angle1), sin1=(float)Math.sin(angle1);
			float inX0=innerRadius*cos0, inZ0=innerRadius*sin0;
			float inX1=innerRadius*cos1, inZ1=innerRadius*sin1;
			float outX0=outerRadius*cos0, outZ0=outerRadius*sin0;
			float outX1=outerRadius*cos1, outZ1=outerRadius*sin1;
			addQuad(matrix,builder,
					inX0,0.2F,inZ0,
					outX0,height,outZ0,
					outX1,height,outZ1,
					inX1,0.2F,inZ1,
					0,220,a);
		}
	}
	private static void addQuad(Matrix4f matrix,VertexConsumer builder,float x0,float y0,float z0,float x1,float y1,float z1,float x2,float y2,float z2,float x3,float y3,float z3,int r,int g,int a){
		builder.addVertex(matrix,x0,y0,z0).setColor(r,g,255,a);
		builder.addVertex(matrix,x1,y1,z1).setColor(r,g,255,a);
		builder.addVertex(matrix,x2,y2,z2).setColor(r,g,255,a);
		builder.addVertex(matrix,x3,y3,z3).setColor(r,g,255,a);
		builder.addVertex(matrix,x3,y3,z3).setColor(r,g,255,a);
		builder.addVertex(matrix,x2,y2,z2).setColor(r,g,255,a);
		builder.addVertex(matrix,x1,y1,z1).setColor(r,g,255,a);
		builder.addVertex(matrix,x0,y0,z0).setColor(r,g,255,a);
	}
	@Override
	public boolean shouldRender(@NotNull NuclearExplosionEntity entity,@NotNull Frustum camera,double camX,double camY,double camZ){
		return true;
	}
	@Override
	public @NotNull ResourceLocation getTextureLocation(@NotNull NuclearExplosionEntity entity){
		return BeaconRenderer.BEAM_LOCATION;
	}
}