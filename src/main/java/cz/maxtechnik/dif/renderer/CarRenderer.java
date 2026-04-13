package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
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
    private final cz.maxtechnik.dif.client.enitity.FormulaModel<T> model;
    private static final ResourceLocation TEX_BASE = ResourceLocation.fromNamespaceAndPath("dif", "textures/entity/f1_base.png");
    private static final ResourceLocation TEX_COLOR = ResourceLocation.fromNamespaceAndPath("dif", "textures/entity/f1_collored.png");

    public CarRenderer(EntityRendererProvider.Context ctx) {
        super(ctx); 
        this.shadowRadius = 0.7F; 
        this.model = new cz.maxtechnik.dif.client.enitity.FormulaModel<>(ctx.bakeLayer(cz.maxtechnik.dif.client.enitity.FormulaModel.LAYER_LOCATION));
    }

    @Override 
    public void render(@NotNull T e, float y, float pt, PoseStack ps, MultiBufferSource b, int l) {
        ps.pushPose(); 
        
        // --- POZICOVÁNÍ MODELU (Zde můžeš upravit) ---
        // Zde můžeš upravit rotaci a pozici modelu
        // Rotace auta: otáčí se podle Y rotace (yaw)
        ps.mulPose(Axis.YP.rotationDegrees(180F - y)); 
        // Posun modelu nahoru nebo dolů (např. 1.5D je blok a půl)
        // Můžeš upravit střední hodnotu (0.5D) např na 1.5D, abys ho posunul nahoru či dolů
        ps.translate(0D, 1.5D, 0D); 
        // Obracecí škálovaní (Blockbench exportuje vzhůru nohama)
        ps.scale(-1.0F, -1.0F, 1.0F);
        // ---------------------------------------------
        
        // Vypočítáme rotaci kol podle rychlosti (simulace pohybu)
        float wheelRotation = (e.tickCount + pt) * e.getSpeedKmh() * 0.02f;
        this.model.setupAnim(e, wheelRotation, 0.0F, e.tickCount + pt, e.getYRot(), e.getXRot());
        
        // 1. ZÁKLADNÍ TEXTURA (vždy se renderuje)
        VertexConsumer vertexConsumer = b.getBuffer(this.model.renderType(TEX_BASE));
        this.model.renderToBuffer(ps, vertexConsumer, l, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        // 2. BAREVNÁ TEXTURA (pouze pokud je auto obarvené tintou)
        if (e instanceof cz.maxtechnik.dif.entity.vehicle.FormulaEntity formula && formula.getColor() != -1) {
            int color = formula.getColor();
            float r = (float)(color >> 16 & 255) / 255.0F;
            float g = (float)(color >> 8 & 255) / 255.0F;
            float blue = (float)(color & 255) / 255.0F;
            
            VertexConsumer colorConsumer = b.getBuffer(this.model.renderType(TEX_COLOR));
            this.model.renderToBuffer(ps, colorConsumer, l, OverlayTexture.NO_OVERLAY, r, g, blue, 1.0F);
        }

        ps.popPose(); 
        super.render(e, y, pt, ps, b, l);
    }

    @Override public @NotNull ResourceLocation getTextureLocation(@NotNull T e) { return TEX_BASE; }

    @SubscribeEvent
    public static void onPlayerRenderPre(RenderPlayerEvent.Pre event) {
        if (event.getEntity().getVehicle() instanceof BaseCarEntity car) {
            float pt = event.getPartialTick();
            float lerpYaw = net.minecraft.util.Mth.lerp(pt, car.yRotO, car.getYRot());
            event.getEntity().yBodyRot = lerpYaw;
            event.getEntity().yBodyRotO = lerpYaw;
            event.getRenderer().getModel().rightLeg.yScale = 0.5F;
            event.getRenderer().getModel().leftLeg.yScale = 0.5F;
            
            event.getRenderer().getModel().rightPants.yScale = 0.5F;
            event.getRenderer().getModel().leftPants.yScale = 0.5F;
        }
    }

    @SubscribeEvent
    public static void onPlayerRenderPost(RenderPlayerEvent.Post event) {
        event.getRenderer().getModel().rightLeg.yScale = 1.0F;
        event.getRenderer().getModel().leftLeg.yScale = 1.0F;
        event.getRenderer().getModel().rightPants.yScale = 1.0F;
        event.getRenderer().getModel().leftPants.yScale = 1.0F;
    }
}