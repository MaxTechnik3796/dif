package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.maxtechnik.dif.block.ForgeFurnaceController;
import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
import cz.maxtechnik.dif.block.entity.ForgeGlassBlockEntity;
import cz.maxtechnik.dif.util.ForgeMultiblockHelper;
import net.createmod.catnip.platform.NeoForgeCatnipServices;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
public class ForgeGlassBlockEntityRenderer implements BlockEntityRenderer<ForgeGlassBlockEntity>{
	private static final float PAD=0.01F;
	public ForgeGlassBlockEntityRenderer(BlockEntityRendererProvider.Context ignoredCtx){
	}
	@Override
	public void render(ForgeGlassBlockEntity be,float partialTick,@NotNull PoseStack ps,@NotNull MultiBufferSource buf,int packedLight,int packedOverlay){
		BlockPos ctrlPos=be.getControllerPos();
		if(ctrlPos==null) return;
		Level level=be.getLevel();
		if(level==null) return;
		if(!(level.getBlockEntity(ctrlPos) instanceof ForgeControllerBlockEntity ctrl)) return;
		BlockState ctrlState=ctrl.getBlockState();
		if(!ctrlState.hasProperty(ForgeFurnaceController.FORMED)
				||!ctrlState.getValue(ForgeFurnaceController.FORMED)) return;
		int glassLayers=ctrl.getGlassLayers();
		if(glassLayers==0) return;
		int totalCapacity=ForgeMultiblockHelper.totalFluidCapacity(glassLayers);
		if(totalCapacity<=0) return;
		int myLayer=be.getBlockPos().getY()-ctrlPos.getY();
		if(myLayer<1||myLayer>glassLayers) return;
		float layerFloor=myLayer-1F;
		Direction facing=ctrlState.getValue(ForgeFurnaceController.FACING);
		Direction intoStr=facing.getOpposite();
		Direction right=intoStr.getClockWise();
		BlockPos glassPos=be.getBlockPos();
		int isX=intoStr.getStepX(), isZ=intoStr.getStepZ();
		int rX=right.getStepX(), rZ=right.getStepZ();
		int dx_nn=rX*(-1);
		int dz_nn=rZ*(-1);
		int dx_np=rX*(-1)+isX*2;
		int dz_np=rZ*(-1)+isZ*2;
		int dx_pp=rX+isX*2;
		int dz_pp=rZ+isZ*2;
		int dx=ctrlPos.getX()-glassPos.getX();
		int dz=ctrlPos.getZ()-glassPos.getZ();
		float xMin=dx+Math.min(Math.min(dx_nn,dx_np),Math.min(rX,dx_pp))+PAD;
		float xMax=dx+Math.max(Math.max(dx_nn,dx_np),Math.max(rX,dx_pp))+1-PAD;
		float zMin=dz+Math.min(Math.min(dz_nn,dz_np),Math.min(rZ,dz_pp))+PAD;
		float zMax=dz+Math.max(Math.max(dz_nn,dz_np),Math.max(rZ,dz_pp))+1-PAD;
		int light=LevelRenderer.getLightColor(level,glassPos);
		int[] renderOrder=ctrl.getFluidRenderOrder();
		float globalY=0F;
		for(int idx: renderOrder){
			if(idx<0||idx>=ForgeControllerBlockEntity.FLUID_TANK_COUNT) continue;
			FluidTank tank=ctrl.fluidTanks[idx];
			if(tank==null||tank.isEmpty()) continue;
			float fluidHeight=((float)tank.getFluidAmount()/totalCapacity)*glassLayers;
			if(fluidHeight<=0) continue;
			float fluidBottom=globalY;
			float fluidTop=globalY+fluidHeight;
			float drawBottom=Math.max(fluidBottom,layerFloor);
			float drawTop=Math.min(fluidTop,(float)myLayer);
			if(drawTop>drawBottom){
				float localYMin=(drawBottom-layerFloor)+PAD;
				float localYMax=(drawTop-layerFloor)-PAD;
				if(localYMax>localYMin){
					FluidStack stack=tank.getFluid();
					NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
							stack,
							xMin,localYMin,zMin,
							xMax,localYMax,zMax,
							buf,ps,light,false,true
					);
				}
			}
			globalY+=fluidHeight;
			if(globalY>=glassLayers) break;
		}
	}
	@Override
	public @NotNull AABB getRenderBoundingBox(ForgeGlassBlockEntity be){
		return new AABB(be.getBlockPos());
	}
	@Override
	public int getViewDistance(){
		return 256;
	}
}
