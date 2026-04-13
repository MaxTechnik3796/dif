package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.client.enitity.FormulaModel;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import cz.maxtechnik.dif.entity.vehicle.FormulaEntity;
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
    private final FormulaModel<T> model;
    private static final ResourceLocation TEX_BASE = ResourceLocation.fromNamespaceAndPath("dif", "textures/entity/f1_base.png");
    private static final ResourceLocation TEX_COLOR = ResourceLocation.fromNamespaceAndPath("dif", "textures/entity/f1_collored.png");

    public CarRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.7F;
        this.model = new FormulaModel<>(ctx.bakeLayer(FormulaModel.LAYER_LOCATION));
    }

    @Override
    public void render(@NotNull T e, float y, float pt, PoseStack ps, MultiBufferSource b, int l) {
        ps.pushPose();

        // Rotace modelu podle yaw
        ps.mulPose(Axis.YP.rotationDegrees(180F - y));

        // Snížili jsme hodnotu z 1.5D na cca 1.35D až 1.4D.
        // Tím se model posune dolů vzhledem k hráči a hitboxu.
        ps.translate(0D, 1.38D, 0D);

        // Flip modelu (Blockbench standard)
        ps.scale(-1F, -1F, 1F);

        // Animace kol
        float wheelSpin = (e.tickCount + pt) * e.getSpeedKmh() * 0.02f;
        model.setupAnim(e, wheelSpin, 0F, e.tickCount + pt, 0F, 0F);

        // Vykreslení modelu
        model.renderToBuffer(ps, b.getBuffer(model.renderType(TEX_BASE)), l, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);

        if (e instanceof FormulaEntity f) {
            int c = f.getColor();
            float r = (c >> 16 & 255) / 255F, g = (c >> 8 & 255) / 255F, bl = (c & 255) / 255F;
            model.renderToBuffer(ps, b.getBuffer(model.renderType(TEX_COLOR)), l, OverlayTexture.NO_OVERLAY, r, g, bl, 1F);
        }

        ps.popPose();
        super.render(e, y, pt, ps, b, l);
    }
    @Override public @NotNull ResourceLocation getTextureLocation(@NotNull T e) { return TEX_BASE; }

    @SubscribeEvent
    public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        if (event.getEntity().getVehicle() instanceof BaseCarEntity car) {
            float yaw = net.minecraft.util.Mth.lerp(event.getPartialTick(), car.yRotO, car.getYRot());
            event.getEntity().yBodyRot = yaw;
            event.getEntity().yBodyRotO = yaw;
            var m = event.getRenderer().getModel();
            m.rightLeg.yScale = m.leftLeg.yScale = m.rightPants.yScale = m.leftPants.yScale = 0.5F;
        }
    }

    @SubscribeEvent
    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        var m = event.getRenderer().getModel();
        m.rightLeg.yScale = m.leftLeg.yScale = m.rightPants.yScale = m.leftPants.yScale = 1.0F;
    }
}