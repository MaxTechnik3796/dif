package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.maxtechnik.dif.block.ForgeFurnaceController;
import cz.maxtechnik.dif.block.ForgeGlass;
import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
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
public class ForgeGlassRenderer implements BlockEntityRenderer<ForgeControllerBlockEntity>{
	private static final float PAD=0.01F;
	public ForgeGlassRenderer(BlockEntityRendererProvider.Context ignoredCtx){
	}
	@Override
	public void render(ForgeControllerBlockEntity be,float partialTick,@NotNull PoseStack ps,@NotNull MultiBufferSource buf,int packedLight,int packedOverlay){
		Level level=be.getLevel();
		if(level==null) return;
		BlockState state=be.getBlockState();
		if(!state.hasProperty(ForgeFurnaceController.FORMED)||!state.getValue(ForgeFurnaceController.FORMED)) return;
		int glassLayers=be.getGlassLayers();
		if(glassLayers==0) return;
		int totalCapacity=ForgeMultiblockHelper.totalFluidCapacity(glassLayers);
		if(totalCapacity<=0) return;
		Direction facing=state.getValue(ForgeFurnaceController.FACING);
		Direction intoStr=facing.getOpposite();
		Direction right=intoStr.getClockWise();
		BlockPos ctrlPos=be.getBlockPos();
		int intactLayers=0;
		for(int layer=1;layer<=glassLayers;layer++){
			if(!isLayerIntact(level,ctrlPos,intoStr,right,layer)) break;
			intactLayers=layer;
		}
		if(intactLayers==0) return;
		int isX=intoStr.getStepX(), isZ=intoStr.getStepZ();
		int rX=right.getStepX(), rZ=right.getStepZ();
		int dx_nn=rX*(-1);
		int dz_nn=rZ*(-1);
		int dx_np=rX*(-1)+isX*2;
		int dz_np=rZ*(-1)+isZ*2;
		int dx_pp=rX+isX*2;
		int dz_pp=rZ+isZ*2;
		float xMin=Math.min(Math.min(dx_nn,dx_np),Math.min(rX,dx_pp))+PAD;
		float xMax=Math.max(Math.max(dx_nn,dx_np),Math.max(rX,dx_pp))+1-PAD;
		float zMin=Math.min(Math.min(dz_nn,dz_np),Math.min(rZ,dz_pp))+PAD;
		float zMax=Math.max(Math.max(dz_nn,dz_np),Math.max(rZ,dz_pp))+1-PAD;
		int[] renderOrder=be.getFluidRenderOrder();
		float currentYOffset=1F+PAD;
		float maxAllowedY=1F+intactLayers-PAD;
		int light=LevelRenderer.getLightColor(level,ctrlPos.above());
		for(int idx: renderOrder){
			if(idx<0||idx>=ForgeControllerBlockEntity.FLUID_TANK_COUNT) continue;
			FluidTank tank=be.fluidTanks[idx];
			if(tank==null||tank.isEmpty()||tank.getFluidAmount()<=0) continue;
			FluidStack stack=tank.getFluid();
			float fluidHeight=((float)tank.getFluidAmount()/totalCapacity)*glassLayers;
			if(fluidHeight<=0) continue;
			float yMin=currentYOffset;
			float yMax=currentYOffset+fluidHeight;
			float drawYMin=Math.min(yMin,maxAllowedY);
			float drawYMax=Math.min(yMax,maxAllowedY);
			if(drawYMax>drawYMin){
				NeoForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
						stack,
						xMin,drawYMin,zMin,
						xMax,drawYMax,zMax,
						buf,
						ps,
						light,
						false,
						true
				);
			}
			currentYOffset+=fluidHeight;
			if(currentYOffset>=maxAllowedY){
				break;
			}
		}
	}
	private static boolean isLayerIntact(Level level,BlockPos ctrlPos,Direction intoStr,Direction right,int layer){
		for(int z=0;z<3;z++){
			for(int x=-1;x<=1;x++){
				BlockPos gp=ctrlPos.relative(intoStr,z).relative(right,x).above(layer);
				if(!(level.getBlockState(gp).getBlock() instanceof ForgeGlass)) return false;
			}
		}
		return true;
	}
	@Override
	public int getViewDistance(){
		return 256;
	}
	@Override
	public boolean shouldRenderOffScreen(@NotNull ForgeControllerBlockEntity be){
		return true;
	}
	@Override
	public @NotNull AABB getRenderBoundingBox(ForgeControllerBlockEntity be){
		AABB base=new AABB(be.getBlockPos());
		int layers=Math.max(1,be.getGlassLayers());
		return base.inflate(3,0,3).expandTowards(0,layers+1,0).expandTowards(0,-1,0);
	}
}
