package cz.maxtechnik.dif.init.events.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class SkyRenderer {
	private static final ResourceLocation EARTH_TEXTURE = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "textures/environment/earth.png");

	public static void renderCustomSky(ClientLevel level, DeltaTracker partialTick, PoseStack poseStack, String mode) {
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);

		Tesselator tesselator = Tesselator.getInstance();

		if (mode.equals("orbit")) {
			poseStack.pushPose();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, EARTH_TEXTURE);
			Matrix4f matrix = poseStack.last().pose();
			float planetSize = 250F;

			BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			bufferbuilder.addVertex(matrix, -planetSize, -100F,  planetSize).setUv(0, 1);
			bufferbuilder.addVertex(matrix,  planetSize, -100F,  planetSize).setUv(1, 1);
			bufferbuilder.addVertex(matrix,  planetSize, -100F, -planetSize).setUv(1, 0);
			bufferbuilder.addVertex(matrix, -planetSize, -100F, -planetSize).setUv(0, 0);
			BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

			poseStack.popPose();

		} else if (mode.equals("moon")) {
			// --- 1. ČERNÁ MASKA ---
			poseStack.pushPose();
			float moonAngle = level.getTimeOfDay(partialTick.getGameTimeDeltaPartialTick(true)) + 0.5F;
			poseStack.mulPose(Axis.ZP.rotationDegrees(moonAngle * 360F));
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			Matrix4f maskMatrix = poseStack.last().pose();
			float maskSize = 16F;

			BufferBuilder maskBuffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			maskBuffer.addVertex(maskMatrix, -maskSize, 98F, -maskSize).setColor(0, 0, 0, 255);
			maskBuffer.addVertex(maskMatrix,  maskSize, 98F, -maskSize).setColor(0, 0, 0, 255);
			maskBuffer.addVertex(maskMatrix,  maskSize, 98F,  maskSize).setColor(0, 0, 0, 255);
			maskBuffer.addVertex(maskMatrix, -maskSize, 98F,  maskSize).setColor(0, 0, 0, 255);
			BufferUploader.drawWithShader(maskBuffer.buildOrThrow());

			poseStack.popPose();

			// --- 2. ZEMĚ ---
			poseStack.pushPose();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, EARTH_TEXTURE);
			poseStack.mulPose(Axis.ZP.rotationDegrees(10F));
			poseStack.mulPose(Axis.XP.rotationDegrees(-15F));
			Matrix4f earthMatrix = poseStack.last().pose();
			float earthSize = 10F;

			BufferBuilder earthBuffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			earthBuffer.addVertex(earthMatrix, -earthSize, 100F, -earthSize).setUv(0, 1);
			earthBuffer.addVertex(earthMatrix,  earthSize, 100F, -earthSize).setUv(1, 1);
			earthBuffer.addVertex(earthMatrix,  earthSize, 100F,  earthSize).setUv(1, 0);
			earthBuffer.addVertex(earthMatrix, -earthSize, 100F,  earthSize).setUv(0, 0);
			BufferUploader.drawWithShader(earthBuffer.buildOrThrow());

			poseStack.popPose();
		}

		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.enableDepthTest();
	}
}