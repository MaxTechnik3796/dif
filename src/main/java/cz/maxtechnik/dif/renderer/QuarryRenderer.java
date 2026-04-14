package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.block.QuarryBlock;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class QuarryRenderer implements BlockEntityRenderer<QuarryBlockEntity> {
	@Override
	public void render(QuarryBlockEntity be, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		BlockState state = be.getBlockState();
		if (!(state.getBlock() instanceof QuarryBlock) || be.getMiningPos() == null) return;

		Direction facing = state.getValue(QuarryBlock.FACING);
		int range = be.getRange();

		// 1. ŽLUTÝ RÁM (Laser) - vykreslíme dvě patra
		poseStack.pushPose();
		// Správný posun středu před Quarry
		double tx = facing.getStepX() * (range + 1) + 0.5;
		double tz = facing.getStepZ() * (range + 1) + 0.5;
		poseStack.translate(tx, 0, tz);

		VertexConsumer lineBuilder = buffer.getBuffer(RenderType.lines());
		Matrix4f matrix = poseStack.last().pose();
		float s = range + 0.51f; // Mírně větší, aby neproblikávalo skrze bloky

		// Spodní čtverec (y=0) a horní čtverec (y=1)
		for (float y : new float[]{0.05f, 1.05f}) {
			drawLine(matrix, lineBuilder, -s, y, -s,  s, y, -s, 1, 1, 0, 1);
			drawLine(matrix, lineBuilder,  s, y, -s,  s, y,  s, 1, 1, 0, 1);
			drawLine(matrix, lineBuilder,  s, y,  s, -s, y,  s, 1, 1, 0, 1);
			drawLine(matrix, lineBuilder, -s, y,  s, -s, y, -s, 1, 1, 0, 1);
		}
		// Svislé linky v rozích
		drawLine(matrix, lineBuilder, -s, 0, -s, -s, 1, -s, 1, 1, 0, 1);
		drawLine(matrix, lineBuilder,  s, 0, -s,  s, 1, -s, 1, 1, 0, 1);
		drawLine(matrix, lineBuilder,  s, 0,  s,  s, 1,  s, 1, 1, 0, 1);
		drawLine(matrix, lineBuilder, -s, 0,  s, -s, 1,  s, 1, 1, 0, 1);

		poseStack.popPose();

		// 2. VRTÁK (Drill rod)
		poseStack.pushPose();
		BlockPos mPos = be.getMiningPos();
		double dx = mPos.getX() - be.getBlockPos().getX() + 0.5;
		double dz = mPos.getZ() - be.getBlockPos().getZ() + 0.5;
		double dy = mPos.getY() - be.getBlockPos().getY();

		poseStack.translate(dx, 0, dz);
		VertexConsumer drillBuilder = buffer.getBuffer(RenderType.debugLineStrip(2.0));
		renderDrillRod(poseStack, drillBuilder, combinedLight, dy);
		poseStack.popPose();
	}

	private void drawLine(Matrix4f matrix, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
		builder.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(0, 1, 0).endVertex();
		builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(0, 1, 0).endVertex();
	}

	private void renderDrillRod(PoseStack poseStack, VertexConsumer builder, int light, double targetY) {
		Matrix4f matrix = poseStack.last().pose();
		builder.vertex(matrix, 0, 0, 0).color(0.5f, 0.5f, 0.5f, 1.0f).normal(0, 1, 0).endVertex();
		builder.vertex(matrix, 0, (float)targetY, 0).color(0.5f, 0.5f, 0.5f, 1.0f).normal(0, 1, 0).endVertex();
	}
}