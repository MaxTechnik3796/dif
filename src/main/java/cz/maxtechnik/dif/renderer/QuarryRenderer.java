package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity.State;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
public class QuarryRenderer implements BlockEntityRenderer<QuarryBlockEntity>{
	// Box je posunut o půl bloku nahoru od základní Y pozice quarry
	private static final float BOX_Y_OFFSET=0.5f;
	private static final float BOX_HEIGHT=3.0f;
	@Override
	public void render(QuarryBlockEntity blockEntity,float partialTick,@NotNull PoseStack poseStack,
	                   @NotNull MultiBufferSource bufferSource,int light,int overlay){
		State quarryState=blockEntity.getQuarryState();
		// DONE → nic nevykresluj
		if(quarryState==State.DONE) return;
		Level level=Minecraft.getInstance().level;
		// Fyzický frame je kompletní → box je postaven, není třeba ho kreslit
		if(quarryState==State.MINING&&level!=null
				&&blockEntity.isFrameIntact(level,blockEntity.getBlockState())) return;
		// Chybí pouze energie a frame je ok → nic nezobrazuj
		if(quarryState==State.NO_ENERGY&&level!=null
				&&blockEntity.isFrameIntact(level,blockEntity.getBlockState())) return;
		BlockPos quarryPos=blockEntity.getBlockPos();
		BlockPos areaCenter=blockEntity.getAreaCenter();
		if(areaCenter==null) return;
		int halfX=blockEntity.getFrameHalfX();
		int halfZ=blockEntity.getFrameHalfZ();
		// Souřadnice relativní k pozici quarry
		float relCenterX=areaCenter.getX()-quarryPos.getX();
		float relCenterZ=areaCenter.getZ()-quarryPos.getZ();
		float minX=relCenterX-halfX+0.5f;
		float maxX=relCenterX+halfX+0.5f;
		float minZ=relCenterZ-halfZ+0.5f;
		float maxZ=relCenterZ+halfZ+0.5f;
		poseStack.pushPose();
		Matrix4f matrix=poseStack.last().pose();
		VertexConsumer vertexConsumer=bufferSource.getBuffer(RenderType.lines());
		float yBottom=BOX_Y_OFFSET;
		float yTop=BOX_Y_OFFSET+BOX_HEIGHT;
		// Dolní a horní obdélník framu
		rect(matrix,vertexConsumer,minX,yBottom,minZ,maxX,maxZ);
		rect(matrix,vertexConsumer,minX,yTop,minZ,maxX,maxZ);
		// Čtyři svislé sloupy rohů
		pillar(matrix,vertexConsumer,minX,minZ,yBottom,yTop);
		pillar(matrix,vertexConsumer,maxX,minZ,yBottom,yTop);
		pillar(matrix,vertexConsumer,minX,maxZ,yBottom,yTop);
		pillar(matrix,vertexConsumer,maxX,maxZ,yBottom,yTop);
		// Vrták – zobraz čáru k aktuálnímu těženému bloku
		if(quarryState==State.MINING){
			BlockPos miningPos=blockEntity.getMiningPos();
			if(miningPos!=null){
				float drillX=miningPos.getX()-quarryPos.getX()+0.5f;
				float drillZ=miningPos.getZ()-quarryPos.getZ()+0.5f;
				float drillY=miningPos.getY()-quarryPos.getY();
				line(matrix,vertexConsumer,drillX,yTop,drillZ,drillX,drillY,drillZ,255,255,200);
			}
		}
		poseStack.popPose();
	}
	private void rect(Matrix4f matrix,VertexConsumer vertexConsumer,
	                  float minX,float yLevel,float minZ,float maxX,float maxZ){
		line(matrix,vertexConsumer,minX,yLevel,minZ,maxX,yLevel,minZ,200,0,255);
		line(matrix,vertexConsumer,maxX,yLevel,minZ,maxX,yLevel,maxZ,200,0,255);
		line(matrix,vertexConsumer,maxX,yLevel,maxZ,minX,yLevel,maxZ,200,0,255);
		line(matrix,vertexConsumer,minX,yLevel,maxZ,minX,yLevel,minZ,200,0,255);
	}
	private void pillar(Matrix4f matrix,VertexConsumer vertexConsumer,
	                    float cornerX,float cornerZ,float yBottom,float yTop){
		line(matrix,vertexConsumer,cornerX,yBottom,cornerZ,cornerX,yTop,cornerZ,200,0,255);
	}
	private void line(Matrix4f matrix,VertexConsumer vertexConsumer,
	                  float x0,float y0,float z0,
	                  float x1,float y1,float z1,
	                  int colorG,int colorB,int colorA){
		float dx=x1-x0, dy=y1-y0, dz=z1-z0;
		float len=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);
		if(len<0.001f) return;
		float normX=dx/len, normY=dy/len, normZ=dz/len;
		vertexConsumer.vertex(matrix,x0,y0,z0).color(255,colorG,colorB,colorA).normal(normX,normY,normZ).endVertex();
		vertexConsumer.vertex(matrix,x1,y1,z1).color(255,colorG,colorB,colorA).normal(normX,normY,normZ).endVertex();
	}
	@Override
	public boolean shouldRenderOffScreen(@NotNull QuarryBlockEntity blockEntity){
		return true;
	}
	@Override
	public int getViewDistance(){
		return 128;
	}
}