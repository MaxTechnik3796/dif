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
import org.jetbrains.annotations.NotNull;

public class CarRenderer<T extends BaseCarEntity> extends EntityRenderer<T> {
    private final MinecartModel<T> model;
    private static final ResourceLocation TEX = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/minecart.png");

    public CarRenderer(EntityRendererProvider.Context ctx) {
        super(ctx); this.shadowRadius = 0.7F; this.model = new MinecartModel<>(ctx.bakeLayer(ModelLayers.MINECART));
    }

    @Override public void render(@NotNull T e, float y, float pt, PoseStack ps, MultiBufferSource b, int l) {
        ps.pushPose(); ps.mulPose(Axis.YP.rotationDegrees(180F - y)); ps.translate(0D, 0.5D, 0D);
        model.renderToBuffer(ps, b.getBuffer(model.renderType(TEX)), l, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
        ps.popPose(); super.render(e, y, pt, ps, b, l);
    }
    @Override public @NotNull ResourceLocation getTextureLocation(@NotNull T e) { return TEX; }
}