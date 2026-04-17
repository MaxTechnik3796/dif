package cz.maxtechnik.dif.renderer;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlock;
import cz.maxtechnik.dif.block.entity.IndustrialLargeWaterWheelBlockEntity;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class IndustrialWaterWheelRenderer extends KineticBlockEntityRenderer<IndustrialLargeWaterWheelBlockEntity> {

    public static final PartialModel INDUSTRIAL_WHEEL = 
            new PartialModel(ResourceLocation.fromNamespaceAndPath("dif", "block/industrial_large_water_wheel/block"));
    public static final PartialModel INDUSTRIAL_WHEEL_EXTENSION = 
            new PartialModel(ResourceLocation.fromNamespaceAndPath("dif", "block/industrial_large_water_wheel/block_extension"));

    public IndustrialWaterWheelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(IndustrialLargeWaterWheelBlockEntity be, BlockState state) {
        boolean extension = state.getValue(LargeWaterWheelBlock.EXTENSION);
        PartialModel partial = extension ? INDUSTRIAL_WHEEL_EXTENSION : INDUSTRIAL_WHEEL;
        return CachedBuffers.partial(partial, state);
    }
}
