package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.block.QuarryBlock;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
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

		// 1. Vykreslení žlutého rámu na zemi
		poseStack.pushPose();
		VertexConsumer lineBuilder = buffer.getBuffer(RenderType.lines());
		double offsetX = facing.getStepX() * (be.getRange() + 1);
		double offsetZ = facing.getStepZ() * (be.getRange() + 1);
		poseStack.translate(offsetX + 0.5, -0.95, offsetZ + 0.5);

		Matrix4f matrix = poseStack.last().pose();
		float size = be.getRange() + 0.5f;
		this.drawLine(matrix, lineBuilder, -size, 0, -size, size, 0, -size, 1, 1, 0, 1);
		this.drawLine(matrix, lineBuilder, size, 0, -size, size, 0, size, 1, 1, 0, 1);
		this.drawLine(matrix, lineBuilder, size, 0, size, -size, 0, size, 1, 1, 0, 1);
		this.drawLine(matrix, lineBuilder, -size, 0, size, -size, 0, -size, 1, 1, 0, 1);
		poseStack.popPose();

		// 2. Vykreslení vrtné tyče (Drill)
		poseStack.pushPose();
		BlockPos mPos = be.getMiningPos();
		// Relativní pozice vůči bloku Quarry
		double dx = mPos.getX() - be.getBlockPos().getX() + 0.5;
		double dz = mPos.getZ() - be.getBlockPos().getZ() + 0.5;
		double dy = mPos.getY() - be.getBlockPos().getY();

		poseStack.translate(dx, 0, dz);
		// Použijeme solid render pro "hmatatelnou" tyč
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
		// Vykreslí tyč od stroje dolů k bloku
		builder.vertex(matrix, 0, 0, 0).color(0.7f, 0.7f, 0.7f, 1.0f).normal(0, 1, 0).endVertex();
		builder.vertex(matrix, 0, (float)targetY, 0).color(0.3f, 0.3f, 0.3f, 1.0f).normal(0, 1, 0).endVertex();
	}
}