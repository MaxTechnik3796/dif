package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.block.entity.QuarryLandmarkBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side overlay: pokud je landmark ve stavu "formed", vykreslí modrý rám.
 *
 * Tracking: QuarryLandmarkBlockEntity volá register/unregister při každé změně
 * stavu (sync paket, onLoad, setRemoved). Renderer pak pouze iteruje malý set
 * – žádné skenování světa ani iterace přes blockEntityList.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class LandmarkOverlayRenderer {

    private static final Map<BlockPos, QuarryLandmarkBlockEntity> FORMED =
            new ConcurrentHashMap<>();

    /** Voláno z QuarryLandmarkBlockEntity na klientu po sync paketu (formed=true). */
    public static void register(QuarryLandmarkBlockEntity lbe) {
        FORMED.put(lbe.getBlockPos(), lbe);
    }

    /** Voláno při ztrátě formace nebo zničení bloku. */
    public static void unregister(BlockPos pos) {
        FORMED.remove(pos);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (FORMED.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        var camPos = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack ps = event.getPoseStack();
        MultiBufferSource.BufferSource buf = mc.renderBuffers().bufferSource();

        ps.pushPose();
        ps.translate(-camPos.x, -camPos.y, -camPos.z);

        Matrix4f m  = ps.last().pose();
        VertexConsumer vc = buf.getBuffer(RenderType.lines());

        for (QuarryLandmarkBlockEntity lbe : FORMED.values()) {
            if (!lbe.isFormed()) continue;
            BlockPos center = lbe.getFormedCenter();
            if (center == null) continue;

            int hx = lbe.getFormedHalfX();
            int hz = lbe.getFormedHalfZ();
            int y  = lbe.getBlockPos().getY();

            float x0 = center.getX() - hx + 0.5f;
            float x1 = center.getX() + hx + 0.5f;
            float z0 = center.getZ() - hz + 0.5f;
            float z1 = center.getZ() + hz + 0.5f;
            float yf = y + 0.5f;

            drawRect(m, vc, x0, yf, z0, x1, z1);
        }

        ps.popPose();
        buf.endBatch(RenderType.lines());
    }

    private static void drawRect(Matrix4f m, VertexConsumer vc,
                                 float x0, float y, float z0,
                                 float x1, float z1) {
        int r = 50, g = 120, b = 255, a = 220;
        line(m, vc, x0, y, z0, x1, y, z0, r, g, b, a);
        line(m, vc, x1, y, z0, x1, y, z1, r, g, b, a);
        line(m, vc, x1, y, z1, x0, y, z1, r, g, b, a);
        line(m, vc, x0, y, z1, x0, y, z0, r, g, b, a);
    }

    private static void line(Matrix4f m, VertexConsumer vc,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             int r, int g, int b, int a) {
        float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.0001f) return;
        float nx = dx / len, ny = dy / len, nz = dz / len;
        vc.vertex(m, x0, y0, z0).color(r, g, b, a).normal(nx, ny, nz).endVertex();
        vc.vertex(m, x1, y1, z1).color(r, g, b, a).normal(nx, ny, nz).endVertex();
    }
}