package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.block.QuarryBlock;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class QuarryRenderer implements BlockEntityRenderer<QuarryBlockEntity> {
	@Override
	public void render(QuarryBlockEntity be, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		BlockState state = be.getBlockState();
		if (!(state.getBlock() instanceof QuarryBlock)) return;

		Direction facing = state.getValue(QuarryBlock.FACING);
		// Použijeme Lightning render type pro svítící efekt laseru
		VertexConsumer builder = buffer.getBuffer(RenderType.lines());

		poseStack.pushPose();

		// Posuneme se do středu oblasti před Quarry
		// Pokud je range 5, pak je oblast 11 bloků široká.
		// Střed oblasti je 6 bloků od stroje.
		poseStack.translate(facing.getStepX() * 6.0, -1.0, facing.getStepZ() * 6.0);

		Matrix4f matrix = poseStack.last().pose();

		// Vykreslení žlutého čtverce ohraničujícího oblast (11x11)
		float size = 5.5f; // Polovina z 11
		float r = 1.0f; float g = 1.0f; float b = 0.0f; float a = 1.0f;

		// Linky ohraničení
		this.drawLine(matrix, builder, -size, 0, -size,  size, 0, -size, r, g, b, a);
		this.drawLine(matrix, builder,  size, 0, -size,  size, 0,  size, r, g, b, a);
		this.drawLine(matrix, builder,  size, 0,  size, -size, 0,  size, r, g, b, a);
		this.drawLine(matrix, builder, -size, 0,  size, -size, 0, -size, r, g, b, a);

		poseStack.popPose();
	}

	private void drawLine(Matrix4f matrix, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
		builder.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(0, 1, 0).endVertex();
		builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(0, 1, 0).endVertex();
	}
}