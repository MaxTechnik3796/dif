package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlock;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlock;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.entity.BrassLargeWaterWheelBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.catnip.render.SuperBufferFactory;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BrassWaterWheelRenderer extends KineticBlockEntityRenderer<WaterWheelBlockEntity>{
	public BrassWaterWheelRenderer(BlockEntityRendererProvider.Context context){
		super(context);
	}
	@Override
	protected void renderSafe(WaterWheelBlockEntity be,float partialTicks,PoseStack ms,MultiBufferSource buffer,int light,int overlay){
		BlockState state=be.getBlockState();
		if(be instanceof BrassLargeWaterWheelBlockEntity&&!state.hasProperty(LargeWaterWheelBlock.EXTENSION)) return;
		RenderType type=getRenderType(be,state);
		SuperByteBuffer model=getRotatedModel(be,state);
		standardKineticRotationTransform(model,be,light).renderInto(ms,buffer.getBuffer(type));
	}
	@Override
	protected SuperByteBuffer getRotatedModel(WaterWheelBlockEntity be,BlockState state){
		boolean large=be instanceof BrassLargeWaterWheelBlockEntity;
		PartialModel partial;
		if(large){
			boolean extension=state.getValue(LargeWaterWheelBlock.EXTENSION);
			partial=extension?PartialModel.of(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/brass_large_water_wheel_extension")):PartialModel.of(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/brass_large_water_wheel"));
		}else partial=PartialModel.of(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/brass_water_wheel_wheel"));
		PartialModel finalPartial=partial;
		return SuperByteBufferCache.getInstance().get(KineticBlockEntityRenderer.KINETIC_BLOCK,state,()->{
			net.minecraft.client.resources.model.BakedModel model=finalPartial.get();
			Direction dir;
			if(large) dir=Direction.fromAxisAndDirection(state.getValue(LargeWaterWheelBlock.AXIS),Direction.AxisDirection.POSITIVE);
			else dir=state.getValue(WaterWheelBlock.FACING);
			PoseStack transform=CachedBuffers.rotateToFaceVertical(dir).get();
			return SuperBufferFactory.getInstance().createForBlock(model,state,transform);
		});
	}
}