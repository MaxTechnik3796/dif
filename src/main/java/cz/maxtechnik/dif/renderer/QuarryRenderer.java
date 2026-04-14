package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.block.QuarryBlock;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
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
		if (!(state.getBlock() instanceof QuarryBlock)) return;

		Direction facing = state.getValue(QuarryBlock.FACING);
		VertexConsumer builder = buffer.getBuffer(RenderType.lines());

		poseStack.pushPose();

		// Posuneme se o 6 bloků ve směru, kam Quarry kouká
		// Přidáme 0.5, abychom byli ve středu bloku, a -1.0 na Y, aby byl laser na zemi
		double offsetX = facing.getStepX() * (be.getRange() + 1);
		double offsetZ = facing.getStepZ() * (be.getRange() + 1);

		poseStack.translate(offsetX + 0.5, -0.95, offsetZ + 0.5);

		Matrix4f matrix = poseStack.last().pose();
		float size = (float) be.getRange() + 0.5f; // Aby to obalilo celých 11 bloků

		float r = 1.0f; float g = 1.0f; float b = 0.0f; float a = 1.0f;

		// Čtverec na zemi
		this.drawLine(matrix, builder, -size, 0, -size,  size, 0, -size, r, g, b, a);
		this.drawLine(matrix, builder,  size, 0, -size,  size, 0,  size, r, g, b, a);
		this.drawLine(matrix, builder,  size, 0,  size, -size, 0,  size, r, g, b, a);
		this.drawLine(matrix, builder, -size, 0,  size, -size, 0, -size, r, g, b, a);

		poseStack.popPose();



		// Vykreslení Drillu (vrtné tyče)
		poseStack.pushPose();
// Vypočítáme relativní pozici vrtáku vůči Quarry
		BlockPos mPos = be.getMiningPos();
		double drillX = mPos.getX() - be.getBlockPos().getX();
		double drillZ = mPos.getZ() - be.getBlockPos().getZ();
		double drillY = mPos.getY() - be.getBlockPos().getY();

// Posuneme se na souřadnice, kde se právě těží
		poseStack.translate(drillX + 0.5, 0, drillZ + 0.5);

// Vykreslíme svislou tyč odshora až k místu těžení
		VertexConsumer drillBuilder = buffer.getBuffer(RenderType.solid());
		renderDrillRod(poseStack, drillBuilder, combinedLight, drillY);

		poseStack.popPose();
	}

	private void drawLine(Matrix4f matrix, VertexConsumer builder, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
		// Normal (0, 1, 0) směřuje nahoru
		builder.vertex(matrix, x1, y1, z1).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
		builder.vertex(matrix, x2, y2, z2).color(r, g, b, a).normal(0.0f, 1.0f, 0.0f).endVertex();
	}
	private void renderDrillRod(PoseStack poseStack, VertexConsumer builder, int light, double targetY) {
		Matrix4f matrix = poseStack.last().pose();

		// Použijeme bílou barvu a nulové UV souřadnice, aby to nespadlo
		// Vykreslíme to jako tlustší "čáru" pomocí dvou bodů
		// Pozor: RenderType.solid() očekává trojúhelníky/čtverce,
		// pro čáry je lepší v render() metodě změnit typ na RenderType.lines()

		builder.vertex(matrix, 0, 0, 0)
				.color(0.5f, 0.5f, 0.5f, 1.0f)
				.uv(0, 0) // Chybějící UV
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(light) // Světlo
				.normal(0, 1, 0)
				.endVertex();

		builder.vertex(matrix, 0, (float)targetY, 0)
				.color(0.5f, 0.5f, 0.5f, 1.0f)
				.uv(0, 0) // Chybějící UV
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(light) // Světlo
				.normal(0, 1, 0)
				.endVertex();
	}
}