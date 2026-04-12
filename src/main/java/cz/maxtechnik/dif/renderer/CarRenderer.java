package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
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

    @SubscribeEvent
    public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        if (event.getEntity().getVehicle() instanceof BaseCarEntity car) {
            float pt = event.getPartialTick();
            float lerpYaw = net.minecraft.util.Mth.lerp(pt, car.yRotO, car.getYRot());
            
            // Fix the player's body and legs to face the same direction as the formula
            event.getEntity().yBodyRot = lerpYaw;
            event.getEntity().yBodyRotO = lerpYaw;
            
            // Move legs into the torso (approx 6 pixels up) by shrinking their vertical scale
            // Normal leg is 12 pixels. Scale 0.5 means it's 6 pixels tall, shifting the bottom 6 pixels up.
            event.getRenderer().getModel().rightLeg.yScale = 0.5F;
            event.getRenderer().getModel().leftLeg.yScale = 0.5F;
            
            event.getRenderer().getModel().rightPants.yScale = 0.5F;
            event.getRenderer().getModel().leftPants.yScale = 0.5F;
        }
    }

    @SubscribeEvent
    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        // Always reset the scales to 1.0F to prevent impacting players outside the formula
        event.getRenderer().getModel().rightLeg.yScale = 1.0F;
        event.getRenderer().getModel().leftLeg.yScale = 1.0F;
        
        event.getRenderer().getModel().rightPants.yScale = 1.0F;
        event.getRenderer().getModel().leftPants.yScale = 1.0F;
    }
}