package cz.maxtechnik.dif.init.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class OrbitSkyRenderer {
	private static final ResourceLocation EARTH_TEXTURE = ResourceLocation.fromNamespaceAndPath("dif", "textures/environment/earth.png");
	private static final ResourceLocation MOON_TEXTURE = ResourceLocation.fromNamespaceAndPath("dif", "textures/environment/moon.png");

	public static void render(ClientLevel level, float partialTick, PoseStack poseStack, Matrix4f projectionMatrix) {
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();

		// --- 1. VYČIŠTĚNÍ OBLOHY (Odstranění vanilla slunce) ---
		// Vykreslíme černé stěny kolem hráče, které "přetřou" původní oblohu
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		float f = 150.0F; // Velikost černé bariéry

		for (int i = 0; i < 6; ++i) {
			poseStack.pushPose();
			if (i == 1) poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			if (i == 2) poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
			if (i == 3) poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			if (i == 4) poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
			if (i == 5) poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));

			Matrix4f m = poseStack.last().pose();
			bufferbuilder.vertex(m, -f, -f, -f).color(0, 0, 0, 255).endVertex();
			bufferbuilder.vertex(m, f, -f, -f).color(0, 0, 0, 255).endVertex();
			bufferbuilder.vertex(m, f, -f, f).color(0, 0, 0, 255).endVertex();
			bufferbuilder.vertex(m, -f, -f, f).color(0, 0, 0, 255).endVertex();
			poseStack.popPose();
		}
		tesselator.end();

		// --- 2. MĚSÍC (Nahoře) ---
		float skyAngle = level.getTimeOfDay(partialTick);
		poseStack.pushPose();
		poseStack.mulPose(Axis.XP.rotationDegrees(skyAngle * 360.0F));

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, MOON_TEXTURE);

		Matrix4f moonMatrix = poseStack.last().pose();
		int phase = level.getMoonPhase();
		float x1 = (float)(phase % 4) / 4.0F;
		float y1 = (float)(phase / 4) / 2.0F;
		float x2 = x1 + 0.25F;
		float y2 = y1 + 0.5F;

		float moonSize = 20.0F;
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferbuilder.vertex(moonMatrix, -moonSize, 100.0F, -moonSize).uv(x2, y2).endVertex();
		bufferbuilder.vertex(moonMatrix, moonSize, 100.0F, -moonSize).uv(x1, y2).endVertex();
		bufferbuilder.vertex(moonMatrix, moonSize, 100.0F, moonSize).uv(x1, y1).endVertex();
		bufferbuilder.vertex(moonMatrix, -moonSize, 100.0F, moonSize).uv(x2, y1).endVertex();
		tesselator.end();
		poseStack.popPose();

		// --- 3. PLANETA (Dole) ---
		poseStack.pushPose();
		RenderSystem.setShaderTexture(0, EARTH_TEXTURE);
		Matrix4f earthMatrix = poseStack.last().pose();

		float planetSize = 200.0F;

		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferbuilder.vertex(earthMatrix, -planetSize, -100.0F, planetSize).uv(0, 1).endVertex();
		bufferbuilder.vertex(earthMatrix, planetSize, -100.0F, planetSize).uv(1, 1).endVertex();
		bufferbuilder.vertex(earthMatrix, planetSize, -100.0F, -planetSize).uv(1, 0).endVertex();
		bufferbuilder.vertex(earthMatrix, -planetSize, -100.0F, -planetSize).uv(0, 0).endVertex();
		tesselator.end();
		poseStack.popPose();

		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.enableDepthTest();
	}
}