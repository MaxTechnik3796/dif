package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity.State;
import cz.maxtechnik.dif.block.entity.QuarryLandmarkBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@EventBusSubscriber(value=Dist.CLIENT)
public class QuarryRenderer extends KineticBlockEntityRenderer<QuarryBlockEntity>{
	public QuarryRenderer(BlockEntityRendererProvider.Context context){
		super(context);
	}
	// ── Barvy ───────────────────────────────────────────────────────────
	private static final int[] FRAME_COLOR={200,0,255,200};   // fialová
	private static final int[] LANDMARK_COLOR={50,120,255,220}; // modrá
	private static final int[] DRILL_COLOR={255,255,255,200}; // bílá
	// ══════════════════════════════════════════════════════════════════════
	// ── Quarry BER ──────────────────────────────────────────────────────
	// ══════════════════════════════════════════════════════════════════════
	@Override
	protected void renderSafe(QuarryBlockEntity blockEntity,float partialTick,@NotNull PoseStack poseStack,@NotNull MultiBufferSource buf,int light,int overlay){
		blockEntity.ensureAreaInitialized();
		State state=blockEntity.getQuarryState();
		if(state==State.DONE) return;
		Level level=Minecraft.getInstance().level;
		if(state==State.MINING&&level!=null&&blockEntity.isFrameIntact(level)) return;
		if(state==State.NO_ENERGY&&level!=null&&blockEntity.isFrameIntact(level)) return;
		BlockPos qPos=blockEntity.getBlockPos();
		// Střed bloků (+0.5F)
		float minX=blockEntity.getAreaMinX()-qPos.getX()+0.5F;
		float maxX=blockEntity.getAreaMaxX()-qPos.getX()+0.5F;
		float minZ=blockEntity.getAreaMinZ()-qPos.getZ()+0.5F;
		float maxZ=blockEntity.getAreaMaxZ()-qPos.getZ()+0.5F;
		poseStack.pushPose();
		Matrix4f m=poseStack.last().pose();
		VertexConsumer vc=buf.getBuffer(RenderType.lines());
		float yBot=0.5F, yTop=3.5F;
		wireRect(m,vc,minX,yBot,minZ,maxX,maxZ,FRAME_COLOR);
		wireRect(m,vc,minX,yTop,minZ,maxX,maxZ,FRAME_COLOR);
		wirePillar(m,vc,minX,minZ,yBot,yTop);
		wirePillar(m,vc,maxX,minZ,yBot,yTop);
		wirePillar(m,vc,minX,maxZ,yBot,yTop);
		wirePillar(m,vc,maxX,maxZ,yBot,yTop);
		if(state==State.MINING){
			BlockPos mp=blockEntity.getMiningPos();
			if(mp!=null){
				float dx=mp.getX()-qPos.getX()+0.5F;
				float dz=mp.getZ()-qPos.getZ()+0.5F;
				float dy=mp.getY()-qPos.getY();
				wireLine(m,vc,dx,yTop,dz,dx,dy,dz,DRILL_COLOR);
			}
		}
		poseStack.popPose();
	}
	@Override
	public boolean shouldRenderOffScreen(@NotNull QuarryBlockEntity be){
		return true;
	}
	@Override
	public int getViewDistance(){
		return Minecraft.getInstance().options.renderDistance().get()*16;
	}
	// ══════════════════════════════════════════════════════════════════════
	// ── Landmark overlay (level event) ──────────────────────────────────
	// ══════════════════════════════════════════════════════════════════════
	private static final Map<BlockPos,QuarryLandmarkBlockEntity> FORMED_LANDMARKS=new ConcurrentHashMap<>();
	public static void register(QuarryLandmarkBlockEntity lm){
		FORMED_LANDMARKS.put(lm.getBlockPos(),lm);
	}
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
		PoseStack ps=event.getPoseStack();
		MultiBufferSource.BufferSource buf=mc.renderBuffers().bufferSource();
		ps.pushPose();
		ps.translate(-camPos.x,-camPos.y,-camPos.z);
		Matrix4f m=ps.last().pose();
		VertexConsumer vc=buf.getBuffer(RenderType.lines());
		double renderDist=mc.options.renderDistance().get()*16;
		double renderDistSq=renderDist*renderDist;
		for(QuarryLandmarkBlockEntity lm: FORMED_LANDMARKS.values()){
			if(lm.getBlockPos().distToCenterSqr(camPos)>renderDistSq) continue;
			if(lm.isFormed()){
				var area=lm.getFormedArea();
				if(area==null) continue;
				float y=lm.getBlockPos().getY()+0.5F;
				// Střed bloků (+0.5F)
				float minX=area.minX()+0.5F, maxX=area.maxX()+0.5F;
				float minZ=area.minZ()+0.5F, maxZ=area.maxZ()+0.5F;
				wireRect(m,vc,minX,y,minZ,maxX,maxZ,LANDMARK_COLOR);
			}else{
				net.minecraft.world.level.block.state.BlockState state=lm.getBlockState();
				if(state.hasProperty(cz.maxtechnik.dif.block.QuarryLandmark.POWERED)&&state.getValue(cz.maxtechnik.dif.block.QuarryLandmark.POWERED)){
					float x=lm.getBlockPos().getX()+0.5F;
					float y=lm.getBlockPos().getY()+0.5F;
					float z=lm.getBlockPos().getZ()+0.5F;
					float length=cz.maxtechnik.dif.block.entity.QuarryLandmarkBlockEntity.MAX_SEARCH;
					wireLine(m,vc,x,y,z,x+length,y,z,LANDMARK_COLOR);
					wireLine(m,vc,x,y,z,x-length,y,z,LANDMARK_COLOR);
					wireLine(m,vc,x,y,z,x,y,z+length,LANDMARK_COLOR);
					wireLine(m,vc,x,y,z,x,y,z-length,LANDMARK_COLOR);
				}
			}
		}
		ps.popPose();
		buf.endBatch(RenderType.lines());
	}
	// ══════════════════════════════════════════════════════════════════════
	// ── Sdílené kreslicí utility ────────────────────────────────────────
	// ══════════════════════════════════════════════════════════════════════
	private static void wireRect(Matrix4f m,VertexConsumer vc,float minX,float y,float minZ,float maxX,float maxZ,int[] c){
		wireLine(m,vc,minX,y,minZ,maxX,y,minZ,c);
		wireLine(m,vc,maxX,y,minZ,maxX,y,maxZ,c);
		wireLine(m,vc,maxX,y,maxZ,minX,y,maxZ,c);
		wireLine(m,vc,minX,y,maxZ,minX,y,minZ,c);
	}
	private static void wirePillar(Matrix4f m,VertexConsumer vc,float x,float z,float yBot,float yTop){
		wireLine(m,vc,x,yBot,z,x,yTop,z,QuarryRenderer.FRAME_COLOR);
	}
	private static void wireLine(Matrix4f m,VertexConsumer vc,float x0,float y0,float z0,float x1,float y1,float z1,int[] c){
		float dx=x1-x0, dy=y1-y0, dz=z1-z0;
		float len=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);
		if(len<0.001F) return;
		float nx=dx/len, ny=dy/len, nz=dz/len;
		vc.addVertex(m,x0,y0,z0).setColor(c[0],c[1],c[2],c[3]).setNormal(nx,ny,nz);
		vc.addVertex(m,x1,y1,z1).setColor(c[0],c[1],c[2],c[3]).setNormal(nx,ny,nz);
	}
}