package cz.maxtechnik.dif.block.generator.steam_generator;

import cz.maxtechnik.dif.block.generator.AbstractFluidGeneratorRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Renderer pro Steam Generator.
 *
 * Hřídel se renderuje automaticky přes základní třídu.
 *
 * Pro přidání animace (setrvačník, písty):
 * 1. Přidej PartialModel do PartialModels třídy.
 * 2. Vrať jeho ResourceLocation z {@link SteamGeneratorDefinition#overlayModel()}.
 * 3. Přepiš {@link #renderOverlay} zde pro aplikaci rotace.
 *
 * Příklad rotujícího overlay modelu:
 * <pre>{@code
 *   @Override
 *   protected void renderOverlay(SteamGeneratorBlockEntity be, SuperByteBuffer sbb,
 *                                float partialTicks, PoseStack ms, MultiBufferSource buffer,
 *                                int light, int overlay) {
 *       float angle = getShaftAngleDeg(be) % 360f;
 *       sbb.center().rotateYDegrees(angle).uncenter()
 *          .light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
 *   }
 * }</pre>
 */
public class SteamGeneratorRenderer extends AbstractFluidGeneratorRenderer<SteamGeneratorBlockEntity> {

    public SteamGeneratorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    // Žádné přepisy pro základní variantu – shaft se renderuje automaticky.
}
