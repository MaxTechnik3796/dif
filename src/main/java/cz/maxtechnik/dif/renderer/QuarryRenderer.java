package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class QuarryRenderer implements BlockEntityRenderer<QuarryBlockEntity> {

	// Box je posunut o 0.5 bloku (8px) nahoru oproti základní Y pozici
	private static final float BOX_Y_OFFSET = 0.5f;

	@Override
	public void render(QuarryBlockEntity be, float pt, @NotNull PoseStack ps,
	                   @NotNull MultiBufferSource buf, int light, int overlay) {

		QuarryBlockEntity.State state = be.getQuarryState();

		// DONE → nikdy nezobrazuj
		if (state == QuarryBlockEntity.State.DONE) return;

		Level level = Minecraft.getInstance().level;

		// Pokud se těží a frame je kompletní → box nepotřebujeme (je fyzicky postaven)
		if (state == QuarryBlockEntity.State.MINING && level != null
				&& be.isFrameIntact(level, be.getBlockState())) return;

		// NO_ENERGY: zobraz box jen pokud frame NENÍ kompletní (signalizuje problém)
		// Pokud frame je ok a jen chybí energie → nic nezobrazuj
		if (state == QuarryBlockEntity.State.NO_ENERGY && level != null
				&& be.isFrameIntact(level, be.getBlockState())) return;

		BlockPos qp = be.getBlockPos();
		BlockPos center = be.getAreaCenter();
		if (center == null) return;

		int hx = be.getFrameHalfX();
		int hz = be.getFrameHalfZ();

		float cx = center.getX() - qp.getX();
		float cz = center.getZ() - qp.getZ();
		float x0 = cx - hx + .5f, x1 = cx + hx + .5f;
		float z0 = cz - hz + .5f, z1 = cz + hz + .5f;

		ps.pushPose();
		Matrix4f m = ps.last().pose();
		VertexConsumer vc = buf.getBuffer(RenderType.lines());

		float yBot = BOX_Y_OFFSET;
		float yTop = BOX_Y_OFFSET + 3f;

		// Dolní a horní obdélník
		rect(m, vc, x0, yBot, z0, x1, z1);
		rect(m, vc, x0, yTop, z0, x1, z1);

		// 4 svislé sloupy
		pillar(m, vc, x0, z0, yBot, yTop);
		pillar(m, vc, x1, z0, yBot, yTop);
		pillar(m, vc, x0, z1, yBot, yTop);
		pillar(m, vc, x1, z1, yBot, yTop);

		// Vrták – jen při těžení
		if (state == QuarryBlockEntity.State.MINING) {
			BlockPos mp = be.getMiningPos();
			if (mp != null) {
				float dx = mp.getX() - qp.getX() + .5f;
				float dz = mp.getZ() - qp.getZ() + .5f;
				float dy = mp.getY() - qp.getY();
				line(m, vc, dx, yTop, dz, dx, dy, dz, 255, 255, 255, 200);
			}
		}

		ps.popPose();
	}

	private void rect(Matrix4f m, VertexConsumer vc, float x0, float y, float z0, float x1, float z1) {
		line(m, vc, x0, y, z0, x1, y, z0, 255, 200, 0, 255);
		line(m, vc, x1, y, z0, x1, y, z1, 255, 200, 0, 255);
		line(m, vc, x1, y, z1, x0, y, z1, 255, 200, 0, 255);
		line(m, vc, x0, y, z1, x0, y, z0, 255, 200, 0, 255);
	}

	private void pillar(Matrix4f m, VertexConsumer vc, float x, float z, float yBot, float yTop) {
		line(m, vc, x, yBot, z, x, yTop, z, 255, 200, 0, 255);
	}

	private void line(Matrix4f m, VertexConsumer vc,
	                  float x0, float y0, float z0,
	                  float x1, float y1, float z1,
	                  int r, int g, int b, int a) {
		float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
		float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (len < 0.001f) return;
		float nx = dx / len, ny = dy / len, nz = dz / len;
		vc.vertex(m, x0, y0, z0).color(r, g, b, a).normal(nx, ny, nz).endVertex();
		vc.vertex(m, x1, y1, z1).color(r, g, b, a).normal(nx, ny, nz).endVertex();
	}

	@Override public boolean shouldRenderOffScreen(@NotNull QuarryBlockEntity be) { return true; }
	@Override public int getViewDistance() { return 128; }
}