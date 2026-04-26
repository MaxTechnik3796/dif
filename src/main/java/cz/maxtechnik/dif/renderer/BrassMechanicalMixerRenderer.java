package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerRenderer;
import cz.maxtechnik.dif.DifMod;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
public class BrassMechanicalMixerRenderer extends MechanicalMixerRenderer{
	public BrassMechanicalMixerRenderer(BlockEntityRendererProvider.Context context){
		super(context);
	}
	@Override
	protected void renderSafe(MechanicalMixerBlockEntity blockEntity,float partialTicks,PoseStack poseStack,MultiBufferSource buffer,int light,int overlay){
		BlockState blockState=blockEntity.getBlockState();
		VertexConsumer vb=buffer.getBuffer(RenderType.solid());
		SuperByteBuffer superBuffer=CachedBuffers.partial(AllPartialModels.SHAFTLESS_COGWHEEL,blockState);
		standardKineticRotationTransform(superBuffer,blockEntity,light).renderInto(poseStack,vb);
		float renderedHeadOffset=blockEntity.getRenderedHeadOffset(partialTicks);
		float speed=blockEntity.getRenderedHeadRotationSpeed(partialTicks);
		float time=AnimationTickHolder.getRenderTime(blockEntity.getLevel());
		float angle=((time*speed*6/10f)%360)/180*(float)Math.PI;
		SuperByteBuffer poleRender=CachedBuffers.partial(DifMod.ClientModEvents.BRASS_MIXER_POLE,blockState);
		poleRender.translate(0,-renderedHeadOffset,0).light(light).renderInto(poseStack,vb);
		VertexConsumer vbCutout=buffer.getBuffer(RenderType.cutoutMipped());
		SuperByteBuffer headRender=CachedBuffers.partial(DifMod.ClientModEvents.BRASS_MIXER_HEAD,blockState);
		headRender.rotateCentered(angle,Direction.UP).translate(0,-renderedHeadOffset,0).light(light).renderInto(poseStack,vbCutout);
	}
}
