package cz.maxtechnik.dif.init.events.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
public class SkyRenderer{
	private static final ResourceLocation EARTH_TEXTURE=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/environment/earth.png");

	public static void renderCustomSky(ClientLevel level,DeltaTracker partialTick,PoseStack poseStack,String mode){
		Minecraft mc=Minecraft.getInstance();

		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthMask(false);

		// Klíčový fix: použijeme fresh PoseStack BEZ camera rotace
		// poseStack z eventu obsahuje camera transform, takže geometrie by se otáčela s pohledem
		// Místo toho vytvoříme nový stack a aplikujeme jen rotaci kamery naopak
		PoseStack skyStack=new PoseStack();

		// Získej rotaci kamery a aplikuj ji na sky stack (stejně jako vanilla sky renderer)
		float pitch=mc.gameRenderer.getMainCamera().getXRot();
		float yaw=mc.gameRenderer.getMainCamera().getYRot();
		skyStack.mulPose(Axis.XP.rotationDegrees(pitch));
		skyStack.mulPose(Axis.YP.rotationDegrees(yaw+180F));

		Tesselator tesselator=Tesselator.getInstance();

		if(mode.equals("orbit")){
			// Země pevně dole – Y záporné = pod hráčem
			skyStack.pushPose();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0,EARTH_TEXTURE);
			Matrix4f matrix=skyStack.last().pose();
			float size=200F;

			BufferBuilder buf=tesselator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
			// Plochý quad pod hráčem (Y=-80 = daleko dole)
			buf.addVertex(matrix,-size,-80F, size).setUv(0,1);
			buf.addVertex(matrix, size,-80F, size).setUv(1,1);
			buf.addVertex(matrix, size,-80F,-size).setUv(1,0);
			buf.addVertex(matrix,-size,-80F,-size).setUv(0,0);
			BufferUploader.drawWithShader(buf.buildOrThrow());
			skyStack.popPose();

		}else if(mode.equals("moon")){
			// Černá maska přes vanillový měsíc
			skyStack.pushPose();
			float moonAngle=level.getTimeOfDay(partialTick.getGameTimeDeltaPartialTick(true))+0.5F;
			skyStack.mulPose(Axis.ZP.rotationDegrees(moonAngle*360F));
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			Matrix4f maskMatrix=skyStack.last().pose();
			float maskSize=16F;

			BufferBuilder maskBuf=tesselator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_COLOR);
			maskBuf.addVertex(maskMatrix,-maskSize,98F,-maskSize).setColor(0,0,0,255);
			maskBuf.addVertex(maskMatrix, maskSize,98F,-maskSize).setColor(0,0,0,255);
			maskBuf.addVertex(maskMatrix, maskSize,98F, maskSize).setColor(0,0,0,255);
			maskBuf.addVertex(maskMatrix,-maskSize,98F, maskSize).setColor(0,0,0,255);
			BufferUploader.drawWithShader(maskBuf.buildOrThrow());
			skyStack.popPose();

			// Země na obloze (fixně na severu)
			skyStack.pushPose();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0,EARTH_TEXTURE);
			skyStack.mulPose(Axis.ZP.rotationDegrees(10F));
			skyStack.mulPose(Axis.XP.rotationDegrees(-15F));
			Matrix4f earthMatrix=skyStack.last().pose();
			float earthSize=10F;

			BufferBuilder earthBuf=tesselator.begin(VertexFormat.Mode.QUADS,DefaultVertexFormat.POSITION_TEX);
			earthBuf.addVertex(earthMatrix,-earthSize,100F,-earthSize).setUv(0,1);
			earthBuf.addVertex(earthMatrix, earthSize,100F,-earthSize).setUv(1,1);
			earthBuf.addVertex(earthMatrix, earthSize,100F, earthSize).setUv(1,0);
			earthBuf.addVertex(earthMatrix,-earthSize,100F, earthSize).setUv(0,0);
			BufferUploader.drawWithShader(earthBuf.buildOrThrow());
			skyStack.popPose();
		}

		RenderSystem.depthMask(true);
		RenderSystem.disableBlend();
		RenderSystem.enableDepthTest();
	}
}