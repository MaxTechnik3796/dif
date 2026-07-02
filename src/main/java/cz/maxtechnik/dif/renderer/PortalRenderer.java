package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.portal.PortalEntity;
import cz.maxtechnik.dif.model.PortalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PortalRenderer extends EntityRenderer<PortalEntity> {
    private static final ResourceLocation TEX_BLUE = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "textures/entity/portal/portal_blue.png");
    private static final ResourceLocation TEX_BLUE_ACTIVE = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "textures/entity/portal/portal_blue_active.png");
    private static final ResourceLocation TEX_ORANGE = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "textures/entity/portal/portal_orange.png");
    private static final ResourceLocation TEX_ORANGE_ACTIVE = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "textures/entity/portal/portal_orange_active.png");

    private final PortalModel<PortalEntity> model;

    public PortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PortalModel<>(context.bakeLayer(PortalModel.LAYER_LOCATION));
    }

    @Override
    public void render(PortalEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        Direction facing = entity.getFacing();
        Direction up = entity.getUpDir();
        
        // Handle rotation based on facing and upDir
        if (facing.getAxis() == Direction.Axis.Y) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - up.toYRot()));
            if (facing == Direction.UP) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            } else {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            }
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        }

        // Translate 0.01 blocks along the normal (local Z) to prevent z-fighting / flickering on floors and walls
        poseStack.translate(0.0F, 0.0F, 0.01F);

        ResourceLocation texture = getTextureLocation(entity);
        RenderType renderType;
        int finalPackedLight;
        
        if (entity.isLinked()) {
            // Emissive rendering for active linked portal: unshaded, fullbright
            renderType = RenderType.entityTranslucentEmissive(texture);
            finalPackedLight = net.minecraft.client.renderer.LightTexture.FULL_BRIGHT;
        } else {
            // Normal cutout rendering for unlinked portal: shaded, but with a slight block light minimum (6)
            renderType = RenderType.entityCutoutNoCull(texture);
            int incomingBlock = (packedLight & 0xFFFF) >> 4;
            int incomingSky = ((packedLight >> 16) & 0xFFFF) >> 4;
            int block = Math.max(incomingBlock, 6);
            int sky = incomingSky;
            finalPackedLight = net.minecraft.client.renderer.LightTexture.pack(block, sky);
        }
        
        var vertexConsumer = buffer.getBuffer(renderType);

        // Setup anim and render
        this.model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        this.model.renderToBuffer(poseStack, vertexConsumer, finalPackedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PortalEntity entity) {
        if (entity.isBlue()) {
            return entity.isLinked() ? TEX_BLUE_ACTIVE : TEX_BLUE;
        } else {
            return entity.isLinked() ? TEX_ORANGE_ACTIVE : TEX_ORANGE;
        }
    }
}
