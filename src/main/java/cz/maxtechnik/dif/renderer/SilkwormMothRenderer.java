package cz.maxtechnik.dif.renderer;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.creature.SilkwormMothEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SilkwormMothRenderer extends MobRenderer<SilkwormMothEntity, SilkwormMothModel<SilkwormMothEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "textures/entity/silkworm_moth.png");

    public SilkwormMothRenderer(EntityRendererProvider.Context context) {
        super(context, new SilkwormMothModel<>(context.bakeLayer(SilkwormMothModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(SilkwormMothEntity entity) {
        return TEXTURE;
    }
}