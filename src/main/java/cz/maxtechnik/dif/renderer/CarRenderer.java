package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer pro všechna auta (placeholder – zatím Minecart model).
 * Až budeš mít vlastní OBJ/BBMODEL model, vyměň MinecartModel za BakedModel nebo EntityModel.
 */
public class CarRenderer<T extends BaseCarEntity> extends EntityRenderer<T> {

    private final MinecartModel<T> model;
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/minecart.png");

    public CarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.7F;
        this.model = new MinecartModel<>(context.bakeLayer(ModelLayers.MINECART));
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Otočení dle fyziky auta
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));

        // Model vozíku je zabořený – zvedneme ho
        poseStack.translate(0.0D, 0.5D, 0.0D);

        this.model.renderToBuffer(
                poseStack,
                buffer.getBuffer(this.model.renderType(TEXTURE)),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}