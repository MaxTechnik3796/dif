package cz.maxtechnik.dif.init.events;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
public class SkyRenderer{
	private static final ResourceLocation EARTH_TEXTURE=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/environment/earth.png");
	public static void renderCustomSky(ClientLevel level,float partialTick,PoseStack poseStack,String mode){
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);
		Tesselator tesselator=Tesselator.getInstance();
		BufferBuilder bufferbuilder=tesselator.getBuilder();
		if(mode.equals("orbit")){
			// REŽIM ORBITA: Země pod nohama
			poseStack.pushPose();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0,EARTH_TEXTURE);
			Matrix4f matrix=poseStack.last().pose();
			float planetSize=250F;
			bufferbuilder.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
			bufferbuilder.vertex(matrix,-planetSize,-100F,planetSize).uv(0,1).endVertex();
			bufferbuilder.vertex(matrix,planetSize,-100F,planetSize).uv(1,1).endVertex();
			bufferbuilder.vertex(matrix,planetSize,-100F,-planetSize).uv(1,0).endVertex();
			bufferbuilder.vertex(matrix,-planetSize,-100F,-planetSize).uv(0,0).endVertex();
			tesselator.end();
			poseStack.popPose();
		}else if(mode.equals("moon")){
			// --- 1. POHYBLIVÁ ČERNÁ MASKA (Zakrytí měsíce) ---
			poseStack.pushPose();
			// Synchronizace s měsícem (+ 0.5F otočí masku proti slunci)
			float moonAngle=level.getTimeOfDay(partialTick)+0.5F;
			poseStack.mulPose(Axis.ZP.rotationDegrees(moonAngle*360F));
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			bufferbuilder.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR);
			Matrix4f maskMatrix=poseStack.last().pose();
			float maskSize=16F;
			// Výška 98.0F (blíže k hráči než nebe)
			bufferbuilder.vertex(maskMatrix,-maskSize,98F,-maskSize).color(0,0,0,255).endVertex();
			bufferbuilder.vertex(maskMatrix,maskSize,98F,-maskSize).color(0,0,0,255).endVertex();
			bufferbuilder.vertex(maskMatrix,maskSize,98F,maskSize).color(0,0,0,255).endVertex();
			bufferbuilder.vertex(maskMatrix,-maskSize,98F,maskSize).color(0,0,0,255).endVertex();
			tesselator.end();
			poseStack.popPose();
			// --- 2. STATICKÁ ZEMĚ NA SEVERU ---
			poseStack.pushPose();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0,EARTH_TEXTURE);
			// Polohování: 15° na sever (osa Z) a -20° sklon (osa X)
			poseStack.mulPose(Axis.ZP.rotationDegrees(10F));
			poseStack.mulPose(Axis.XP.rotationDegrees(-15F));
			Matrix4f earthMatrix=poseStack.last().pose();
			float earthSize=10F;
			bufferbuilder.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
			bufferbuilder.vertex(earthMatrix,-earthSize,100F,-earthSize).uv(0,1).endVertex();
			bufferbuilder.vertex(earthMatrix,earthSize,100F,-earthSize).uv(1,1).endVertex();
			bufferbuilder.vertex(earthMatrix,earthSize,100F,earthSize).uv(1,0).endVertex();
			bufferbuilder.vertex(earthMatrix,-earthSize,100F,earthSize).uv(0,0).endVertex();
			tesselator.end();
			poseStack.popPose();
		}
		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.enableDepthTest();
	}
}