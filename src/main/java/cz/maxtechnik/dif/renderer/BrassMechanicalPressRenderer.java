package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.simibubi.create.content.kinetics.press.MechanicalPressRenderer;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import cz.maxtechnik.dif.DifMod;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;
public class BrassMechanicalPressRenderer extends MechanicalPressRenderer{
	public BrassMechanicalPressRenderer(BlockEntityRendererProvider.Context context){
		super(context);
	}
	@Override
	protected void renderSafe(MechanicalPressBlockEntity blockEntity,float partialTicks,PoseStack poseStack,MultiBufferSource buffer,int light,int overlay){
		BlockState blockState=blockEntity.getBlockState();
		renderShaft(blockEntity,poseStack,buffer,light);
		PressingBehaviour pressingBehaviour=blockEntity.getPressingBehaviour();
		float renderedHeadOffset=pressingBehaviour.getRenderedHeadOffset(partialTicks)*pressingBehaviour.mode.headOffset;
		SuperByteBuffer headRender=CachedBuffers.partialFacing(DifMod.ClientModEvents.BRASS_PRESS_HEAD,blockState,blockState.getValue(HORIZONTAL_FACING));
		headRender.translate(0,-renderedHeadOffset,0).light(light).renderInto(poseStack,buffer.getBuffer(RenderType.solid()));
	}
	private void renderShaft(MechanicalPressBlockEntity be,PoseStack ms,MultiBufferSource buffer,int light){
		BlockState shaftState=getRenderedBlockState(be);
		SuperByteBuffer shaftRender=CachedBuffers.block(shaftState);
		standardKineticRotationTransform(shaftRender,be,light).renderInto(ms,buffer.getBuffer(RenderType.solid()));
	}
}
