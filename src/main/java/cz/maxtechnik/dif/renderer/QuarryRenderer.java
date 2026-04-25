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

	@Override
	public void render(QuarryBlockEntity be, float pt, @NotNull PoseStack ps,
	                   @NotNull MultiBufferSource buf, int light, int overlay) {
		QuarryBlockEntity.State state = be.getQuarryState();
		// Obrys se skryje jen ve stavu DONE, nebo pokud je frame kompletní (MINING).
		// Ve všech ostatních stavech (i NO_ENERGY!) se zobrazuje.
		if (state == QuarryBlockEntity.State.DONE) return;

		Level level = Minecraft.getInstance().level;
		boolean frameIntact = (state == QuarryBlockEntity.State.MINING)
				&& level != null
				&& be.isFrameIntact(level, be.getBlockState());
		if (frameIntact) return; // frame kompletní – obrys schovat

		BlockPos qp = be.getBlockPos(), center = be.getAreaCenter();
		int range = be.getRange();
		float cx = center.getX() - qp.getX(), cz = center.getZ() - qp.getZ();
		float x0 = cx - range + .5f, x1 = cx + range + .5f;
		float z0 = cz - range + .5f, z1 = cz + range + .5f;

		ps.pushPose();
		Matrix4f m = ps.last().pose();
		VertexConsumer vc = buf.getBuffer(RenderType.lines());

		// Dolní a horní obdélník + 4 sloupy
		for (float y : new float[]{0f, 3f}) rect(m, vc, x0, y, z0, x1, z1);
		pillar(m, vc, x0, z0); pillar(m, vc, x1, z0);
		pillar(m, vc, x0, z1); pillar(m, vc, x1, z1);

		// Vrták jen při těžbě
		if (state == QuarryBlockEntity.State.MINING) {
			BlockPos mp = be.getMiningPos();
			if (mp != null) {
				float dx = mp.getX() - qp.getX() + .5f, dz = mp.getZ() - qp.getZ() + .5f;
				float dy = mp.getY() - qp.getY();
				line(m, vc, dx, 3f, dz, dx, dy, dz, 255, 255, 255, 200);
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

	private void pillar(Matrix4f m, VertexConsumer vc, float x, float z) {
		line(m, vc, x, 0, z, x, 3, z, 255, 200, 0, 255);
	}

	private void line(Matrix4f m, VertexConsumer vc,
	                  float x0, float y0, float z0,
	                  float x1, float y1, float z1,
	                  int r, int g, int b, int a) {
		float dx = x1-x0, dy = y1-y0, dz = z1-z0;
		float len = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
		if (len < 0.001f) return;
		float nx = dx/len, ny = dy/len, nz = dz/len;
		vc.vertex(m, x0, y0, z0).color(r, g, b, a).normal(nx, ny, nz).endVertex();
		vc.vertex(m, x1, y1, z1).color(r, g, b, a).normal(nx, ny, nz).endVertex();
	}

	@Override public boolean shouldRenderOffScreen(@NotNull QuarryBlockEntity be) { return true; }
	@Override public int getViewDistance() { return 128; }
}