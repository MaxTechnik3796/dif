package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.waterwheel.LargeWaterWheelBlock;
import cz.maxtechnik.dif.block.entity.IndustrialLargeWaterWheelBlockEntity;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import com.simibubi.create.content.kinetics.base.IRotate;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.catnip.render.SuperBufferFactory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IndustrialWaterWheelRenderer extends KineticBlockEntityRenderer<IndustrialLargeWaterWheelBlockEntity> {

    // PartialModel.of() registruje model do Flywheel/Create systému při game startu.
    // Model musí být zaregistrován i v ModelEvent.RegisterAdditional (viz DifMod.java).
    public static final PartialModel INDUSTRIAL_WHEEL =
            PartialModel.of(ResourceLocation.fromNamespaceAndPath("dif", "block/industrial_large_water_wheel/block"));
    public static final PartialModel INDUSTRIAL_WHEEL_EXTENSION =
            PartialModel.of(ResourceLocation.fromNamespaceAndPath("dif", "block/industrial_large_water_wheel/block_extension"));

    public IndustrialWaterWheelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * Přepsán přímo — voláme celý render pipeline sami.
     *
     * Proč: KineticBlockEntityRenderer.renderSafe() v Create 6.x v určitých případech
     * přeskočí renderování pokud Flywheel Visual systém je aktivní.
     * Přepsáním renderSafe() vynucujeme náš BER render vždy bez ohledu na Flywheel stav.
     */
    @Override
    protected void renderSafe(IndustrialLargeWaterWheelBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        if (!state.hasProperty(LargeWaterWheelBlock.EXTENSION)) return;

        RenderType type = getRenderType(be, state);
        SuperByteBuffer model = getRotatedModel(be, state);
        standardKineticRotationTransform(model, be, light)
                .renderInto(ms, buffer.getBuffer(type));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(IndustrialLargeWaterWheelBlockEntity be, BlockState state) {
        boolean extension = state.getValue(LargeWaterWheelBlock.EXTENSION);
        PartialModel partial = extension ? INDUSTRIAL_WHEEL_EXTENSION : INDUSTRIAL_WHEEL;

        return SuperByteBufferCache.getInstance().get(KineticBlockEntityRenderer.KINETIC_BLOCK, state, () -> {
            net.minecraft.client.resources.model.BakedModel model = partial.get();
            Direction dir = Direction.fromAxisAndDirection(state.getValue(LargeWaterWheelBlock.AXIS), Direction.AxisDirection.POSITIVE);
            PoseStack transform = CachedBuffers.rotateToFaceVertical(dir).get();
            return SuperBufferFactory.getInstance().createForBlock(model, state, transform);
        });
    }
}