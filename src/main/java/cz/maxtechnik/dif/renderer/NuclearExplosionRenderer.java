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
	private static final ItemStack CHARGING_ITEM=new ItemStack(Items.NETHER_STAR);
	private static final int FULL_BRIGHT_LIGHT=0xF000F0;
	private static final int BEAM_HEIGHT=350;
	private static final int OUTER_BEAM_COLOR=0xC89933FF;
	private static final int INNER_BEAM_COLOR=0xFFE0FFFF;
	private static final float SPHERE_DURATION_TICKS=25F;
	private static final float SPHERE_START_RADIUS=2F;
	private static final float SPHERE_EXPAND_RADIUS=28F;
	private static final float SPHERE_CENTER_Y=1.5F;
	private static final int SPHERE_HORIZONTAL_RINGS=10;
	private static final int SPHERE_VERTICAL_SEGMENTS=16;
	private static final float WAVE_START_TICK=5F;
	private static final float WAVE_END_TICK=70F;
	private static final float WAVE_DURATION_TICKS=WAVE_END_TICK-WAVE_START_TICK;
	private static final float WAVE_START_RADIUS=6F;
	private static final float WAVE_EXPAND_RADIUS=75F;
	private static final float WAVE_THICKNESS=12F;
	private static final float WAVE_MAX_HEIGHT=2.5F;
	private static final float WAVE_BOTTOM_Y=0.2F;
	private static final int WAVE_SEGMENTS=32;
	private record Point3D(float x,float y,float z){
	}
	private record ColorRGBA(int red,int green,int blue,int alpha){
	}
	private final ItemRenderer itemRenderer;
	public NuclearExplosionRenderer(EntityRendererProvider.Context context){
		super(context);
		this.itemRenderer=context.getItemRenderer();
	}
	@Override
	public void render(NuclearExplosionEntity entity,float entityYaw,float partialTick,@NotNull PoseStack poseStack,@NotNull MultiBufferSource buffer,int packedLight){
		int currentAge=entity.getAge();
		float exactTime=currentAge+partialTick;
		long gameTime=entity.level().getGameTime();
		if(currentAge<NuclearExplosionEntity.CHARGE_TICKS){
			float chargeProgress=exactTime/(float)NuclearExplosionEntity.CHARGE_TICKS;
			renderChargingStar(entity,poseStack,buffer,exactTime,chargeProgress);
			renderDoubleBeaconBeam(poseStack,buffer,partialTick,gameTime);
		}
		else{
			float blastTime=(currentAge-NuclearExplosionEntity.CHARGE_TICKS)+partialTick;
			poseStack.pushPose();
			Matrix4f matrix=poseStack.last().pose();
			VertexConsumer vertexBuilder=buffer.getBuffer(RenderType.lightning());
			if(blastTime<=SPHERE_DURATION_TICKS){
				float sphereProgress=blastTime/SPHERE_DURATION_TICKS;
				renderExplosionSphere(matrix,vertexBuilder,sphereProgress);
			}
			if(blastTime>=WAVE_START_TICK&&blastTime<=WAVE_END_TICK){
				float waveProgress=(blastTime-WAVE_START_TICK)/WAVE_DURATION_TICKS;
				renderShockwaveRing(matrix,vertexBuilder,waveProgress);
			}
			poseStack.popPose();
		}
		super.render(entity,entityYaw,partialTick,poseStack,buffer,packedLight);
	}
	private void renderChargingStar(NuclearExplosionEntity entity,PoseStack poseStack,MultiBufferSource buffer,float exactTime,float progress){
		poseStack.pushPose();
		poseStack.translate(0D,1.2D,0D);
		float rotationSpeed=25F+(progress*60F);
		float rotationDegrees=exactTime*rotationSpeed;
		poseStack.mulPose(Axis.YP.rotationDegrees(rotationDegrees));
		float scale=2F+progress;
		poseStack.scale(scale,scale,scale);
		this.itemRenderer.renderStatic(
				CHARGING_ITEM,
				ItemDisplayContext.FIXED,
				FULL_BRIGHT_LIGHT,
				OverlayTexture.NO_OVERLAY,
				poseStack,
				buffer,
				entity.level(),
				entity.getId()
		);
		poseStack.popPose();
	}
	private void renderDoubleBeaconBeam(PoseStack poseStack,MultiBufferSource buffer,float partialTick,long gameTime){
		poseStack.pushPose();
		poseStack.translate(-0.5D,1.2D,-0.5D);
		renderSingleBeam(poseStack,buffer,partialTick,gameTime,OUTER_BEAM_COLOR,0.25F,0.65F);
		renderSingleBeam(poseStack,buffer,partialTick,gameTime,INNER_BEAM_COLOR,0.70F,1.50F);
		poseStack.popPose();
	}
	private void renderSingleBeam(PoseStack poseStack,MultiBufferSource buffer,float partialTick,long gameTime,int color,float beamRadius,float glowRadius){
		BeaconRenderer.renderBeaconBeam(
				poseStack,buffer,BeaconRenderer.BEAM_LOCATION,
				partialTick,1.0F,gameTime,
				0,BEAM_HEIGHT,color,
				beamRadius,glowRadius
		);
	}
	private static void renderExplosionSphere(Matrix4f matrix,VertexConsumer builder,float progress){
		float sphereRadius=SPHERE_START_RADIUS+(SPHERE_EXPAND_RADIUS*progress);
		int alpha=(int)((1F-progress)*0.85F*255F);
		ColorRGBA sphereColor=new ColorRGBA(160,80,255,alpha);
		for(int ring=0;ring<SPHERE_HORIZONTAL_RINGS;ring++){
			float bottomPitch=(float)(-Math.PI/2.0+Math.PI*ring/SPHERE_HORIZONTAL_RINGS);
			float topPitch=(float)(-Math.PI/2.0+Math.PI*(ring+1)/SPHERE_HORIZONTAL_RINGS);
			float bottomY=SPHERE_CENTER_Y+sphereRadius*(float)Math.sin(bottomPitch);
			float topY=SPHERE_CENTER_Y+sphereRadius*(float)Math.sin(topPitch);
			float bottomRingRadius=sphereRadius*(float)Math.cos(bottomPitch);
			float topRingRadius=sphereRadius*(float)Math.cos(topPitch);
			for(int segment=0;segment<SPHERE_VERTICAL_SEGMENTS;segment++){
				float leftYaw=(float)(2*Math.PI*segment/SPHERE_VERTICAL_SEGMENTS);
				float rightYaw=(float)(2*Math.PI*(segment+1)/SPHERE_VERTICAL_SEGMENTS);
				Point3D bottomLeft=createCirclePoint(bottomRingRadius,leftYaw,bottomY);
				Point3D topLeft=createCirclePoint(topRingRadius,leftYaw,topY);
				Point3D topRight=createCirclePoint(topRingRadius,rightYaw,topY);
				Point3D bottomRight=createCirclePoint(bottomRingRadius,rightYaw,bottomY);
				drawDoubleSidedQuad(matrix,builder,bottomLeft,topLeft,topRight,bottomRight,sphereColor);
			}
		}
	}
	private static void renderShockwaveRing(Matrix4f matrix,VertexConsumer builder,float progress){
		float outerRadius=WAVE_START_RADIUS+(WAVE_EXPAND_RADIUS*progress);
		float innerRadius=Math.max(0.5F,outerRadius-WAVE_THICKNESS);
		float outerHeight=WAVE_MAX_HEIGHT*(1F-progress);
		int alpha=(int)((1F-progress)*200F);
		ColorRGBA waveColor=new ColorRGBA(0,220,255,alpha);
		for(int segment=0;segment<WAVE_SEGMENTS;segment++){
			float startAngle=(float)(2.0*Math.PI*segment/WAVE_SEGMENTS);
			float endAngle=(float)(2.0*Math.PI*(segment+1)/WAVE_SEGMENTS);
			Point3D innerStart=createCirclePoint(innerRadius,startAngle,WAVE_BOTTOM_Y);
			Point3D outerStart=createCirclePoint(outerRadius,startAngle,outerHeight);
			Point3D outerEnd=createCirclePoint(outerRadius,endAngle,outerHeight);
			Point3D innerEnd=createCirclePoint(innerRadius,endAngle,WAVE_BOTTOM_Y);
			drawDoubleSidedQuad(matrix,builder,innerStart,outerStart,outerEnd,innerEnd,waveColor);
		}
	}
	private static Point3D createCirclePoint(float radius,float angleRadians,float y){
		float x=radius*(float)Math.cos(angleRadians);
		float z=radius*(float)Math.sin(angleRadians);
		return new Point3D(x,y,z);
	}
	private static void drawDoubleSidedQuad(Matrix4f matrix,VertexConsumer builder,Point3D p1,Point3D p2,Point3D p3,Point3D p4,ColorRGBA color){
		addVertex(matrix,builder,p1,color);
		addVertex(matrix,builder,p2,color);
		addVertex(matrix,builder,p3,color);
		addVertex(matrix,builder,p4,color);
		addVertex(matrix,builder,p4,color);
		addVertex(matrix,builder,p3,color);
		addVertex(matrix,builder,p2,color);
		addVertex(matrix,builder,p1,color);
	}
	private static void addVertex(Matrix4f matrix,VertexConsumer builder,Point3D point,ColorRGBA color){
		builder.addVertex(matrix,point.x(),point.y(),point.z()).setColor(color.red(),color.green(),color.blue(),color.alpha());
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