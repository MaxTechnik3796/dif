package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class QuarryRenderer implements BlockEntityRenderer<QuarryBlockEntity> {
	@Override
	public void render(QuarryBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		BlockPos mPos = be.getMiningPos();
		if (mPos == null) return;

		poseStack.pushPose();

		// Relativní pozice k bloku Quarry (v metrech/blocích)
		double dx = mPos.getX() - be.getBlockPos().getX() + 0.5;
		double dz = mPos.getZ() - be.getBlockPos().getZ() + 0.5;
		double dy = mPos.getY() - be.getBlockPos().getY();

		poseStack.translate(dx, 0, dz);

		// Použijeme tlustší čáru pro vrták
		VertexConsumer drillBuilder = buffer.getBuffer(RenderType.debugLineStrip(6.0));
		Matrix4f matrix = poseStack.last().pose();

		// Začátek vrtáku (v úrovni horního rámu Y+3)
		float startY = 3.0f;

		// Vykreslíme svislou tyč
		drillBuilder.vertex(matrix, 0, startY, 0).color(255, 255, 255, 255).endVertex();
		drillBuilder.vertex(matrix, 0, (float)dy, 0).color(255, 255, 255, 255).endVertex();

		poseStack.popPose();
	}
}