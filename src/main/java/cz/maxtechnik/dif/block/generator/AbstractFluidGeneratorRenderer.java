package cz.maxtechnik.dif.block.generator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Abstraktní renderer pro všechny generátory na kapalinu.
 *
 * Rozšiřuje Create's {@link ShaftRenderer} – rotující hřídel je automatický.
 * Konkrétní renderery mohou přepsat {@link #renderOverlay} nebo {@link #renderExtra}
 * pro animované modely (písty, setrvačník, atd.).
 */
public abstract class AbstractFluidGeneratorRenderer<T extends AbstractFluidGeneratorBlockEntity>
        extends ShaftRenderer<T> {

    protected AbstractFluidGeneratorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(T be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {

        BlockState state = be.getBlockState();
        GeneratorDefinition def = be.definition();

        if (def != null) {
            @Nullable ResourceLocation overlayModel = def.overlayModel();
            if (overlayModel != null) {
                SuperByteBuffer sbb = net.createmod.catnip.render.CachedBuffers.partial(
                        dev.engine_room.flywheel.lib.model.baked.PartialModel.of(overlayModel),
                        state);
                renderOverlay(be, sbb, partialTicks, ms, buffer, light, overlay);
            }
            renderExtra(be, partialTicks, ms, buffer, light, overlay);
        }

        if (def == null || def.renderShaft()) {
            super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        }
    }

    /**
     * Volá se po načtení overlay modelu.
     * Přepiš pro aplikaci rotace/transformací.
     * Výchozí implementace renderuje buffer as-is.
     */
    protected void renderOverlay(T be, SuperByteBuffer sbb, float partialTicks,
                                  PoseStack ms, MultiBufferSource buffer,
                                  int light, int overlay) {
        sbb.light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    /**
     * Volá se po overlay průchodu.
     * Přepiš pro dodatečné modely (písty s frame animací atd.).
     */
    protected void renderExtra(T be, float partialTicks, PoseStack ms,
                                MultiBufferSource buffer, int light, int overlay) { }

    /**
     * Helper: aktuální úhel hřídele ve stupních.
     * Užitečné pro výběr animačního frame.
     */
    protected float getShaftAngleDeg(T be) {
        return (float) Math.toDegrees(Math.abs(
                KineticBlockEntityRenderer.getAngleForBe(
                        be, be.getBlockPos(),
                        KineticBlockEntityRenderer.getRotationAxisOf(be))));
    }
}
