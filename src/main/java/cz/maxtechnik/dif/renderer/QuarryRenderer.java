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
		if (be.getMiningPos() == null) return;

		// Vykreslíme pouze vrtnou tyč (Drill Rod)
		poseStack.pushPose();

		BlockPos mPos = be.getMiningPos();
		// Relativní pozice k bloku Quarry
		double dx = mPos.getX() - be.getBlockPos().getX() + 0.5;
		double dz = mPos.getZ() - be.getBlockPos().getZ() + 0.5;
		double dy = mPos.getY() - be.getBlockPos().getY();

		poseStack.translate(dx, 0, dz);

		// Použijeme DebugLineStrip pro jednoduchý "laserový" efekt tyče
		VertexConsumer drillBuilder = buffer.getBuffer(RenderType.debugLineStrip(4.0));
		Matrix4f matrix = poseStack.last().pose();

		// Čára odshora (z úrovně frame) až k bloku, co se těží
		// Začíná na Y=2 (horní frame)
		drillBuilder.vertex(matrix, 0, 2.0f, 0).color(255, 255, 255, 255).endVertex();
		drillBuilder.vertex(matrix, 0, (float)dy, 0).color(255, 255, 255, 255).endVertex();

		poseStack.popPose();
	}
}