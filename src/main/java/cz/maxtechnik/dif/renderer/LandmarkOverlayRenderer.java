package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.block.entity.QuarryLandmarkBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
// Klientský overlay – formed landmarky vykreslí modrý obdélník v jejich rovině.
@EventBusSubscriber(value=Dist.CLIENT)
public class LandmarkOverlayRenderer{
	// Aktivní formed landmarky sledované rendererem
	private static final Map<BlockPos,QuarryLandmarkBlockEntity> FORMED_LANDMARKS=
			new ConcurrentHashMap<>();
	// Voláno z QuarryLandmarkBlockEntity na klientu po sync paketu (formed=true)
	public static void register(QuarryLandmarkBlockEntity landmark){
		FORMED_LANDMARKS.put(landmark.getBlockPos(),landmark);
	}
	// Voláno při ztrátě formace nebo zničení bloku
	public static void unregister(BlockPos pos){
		FORMED_LANDMARKS.remove(pos);
	}
	@SubscribeEvent
	public static void onRenderLevel(RenderLevelStageEvent event){
		if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
		if(FORMED_LANDMARKS.isEmpty()) return;
		Minecraft mc=Minecraft.getInstance();
		if(mc.level==null) return;
		var camPos=mc.gameRenderer.getMainCamera().getPosition();
		PoseStack poseStack=event.getPoseStack();
		MultiBufferSource.BufferSource bufferSource=mc.renderBuffers().bufferSource();
		poseStack.pushPose();
		poseStack.translate(-camPos.x,-camPos.y,-camPos.z);
		Matrix4f matrix=poseStack.last().pose();
		VertexConsumer vertexConsumer=bufferSource.getBuffer(RenderType.lines());
		for(QuarryLandmarkBlockEntity landmark: FORMED_LANDMARKS.values()){
			if(landmark.isFormed()) continue;
			BlockPos center=landmark.getFormedCenter();
			if(center==null) continue;
			int halfX=landmark.getFormedHalfX();
			int halfZ=landmark.getFormedHalfZ();
			float landmarkY=landmark.getBlockPos().getY()+0.5f;
			float minX=center.getX()-halfX+0.5f;
			float maxX=center.getX()+halfX+0.5f;
			float minZ=center.getZ()-halfZ+0.5f;
			float maxZ=center.getZ()+halfZ+0.5f;
			drawRect(matrix,vertexConsumer,minX,landmarkY,minZ,maxX,maxZ);
		}
		poseStack.popPose();
		bufferSource.endBatch(RenderType.lines());
	}
	private static void drawRect(Matrix4f matrix,VertexConsumer vertexConsumer,
	                             float minX,float yLevel,float minZ,
	                             float maxX,float maxZ){
		int colorR=50, colorG=120, colorB=255, colorA=220;
		line(matrix,vertexConsumer,minX,yLevel,minZ,maxX,yLevel,minZ,colorR,colorG,colorB,colorA);
		line(matrix,vertexConsumer,maxX,yLevel,minZ,maxX,yLevel,maxZ,colorR,colorG,colorB,colorA);
		line(matrix,vertexConsumer,maxX,yLevel,maxZ,minX,yLevel,maxZ,colorR,colorG,colorB,colorA);
		line(matrix,vertexConsumer,minX,yLevel,maxZ,minX,yLevel,minZ,colorR,colorG,colorB,colorA);
	}
	private static void line(Matrix4f matrix,VertexConsumer vertexConsumer,
	                         float x0,float y0,float z0,
	                         float x1,float y1,float z1,
	                         int colorR,int colorG,int colorB,int colorA){
		float dx=x1-x0, dy=y1-y0, dz=z1-z0;
		float len=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);
		if(len<0.0001f) return;
		float normX=dx/len, normY=dy/len, normZ=dz/len;
		vertexConsumer.addVertex(matrix,x0,y0,z0).setColor(colorR,colorG,colorB,colorA).setNormal(normX,normY,normZ);
		vertexConsumer.addVertex(matrix,x1,y1,z1).setColor(colorR,colorG,colorB,colorA).setNormal(normX,normY,normZ);
	}
}