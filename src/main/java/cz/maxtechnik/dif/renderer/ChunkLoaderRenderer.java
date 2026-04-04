package cz.maxtechnik.dif.renderer;

import cz.maxtechnik.dif.block.ChunkLoader;
import cz.maxtechnik.dif.block.entity.ChunkLoaderBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
public class ChunkLoaderRenderer implements BlockEntityRenderer<ChunkLoaderBlockEntity>{
	public ChunkLoaderRenderer(){
	}
	@Override
	public void render(ChunkLoaderBlockEntity be,float partialTicks,@NotNull PoseStack poseStack,@NotNull MultiBufferSource buffer,int combinedLight,int combinedOverlay){
		BlockState state=be.getBlockState();
		// Vykreslíme glint pouze pokud je blok zapnutý (LIT = true)
		if(state.hasProperty(ChunkLoader.LIT)&&state.getValue(ChunkLoader.LIT)){
			BlockRenderDispatcher dispatcher=Minecraft.getInstance().getBlockRenderer();
			BakedModel model=dispatcher.getBlockModel(state);
			// TADY JE TO KOUZLO:
			// Získáme speciální buffer pro "Glint" (ten fialový svit)
			// Použijeme buď glint() nebo armorGlint() pro jinou intenzitu
			VertexConsumer glintBuffer=buffer.getBuffer(RenderType.glint());
			// Vykreslíme model znovu přes tento buffer
			// Tím se na stávající blok "přilepí" ta animovaná fialová vrstva
			dispatcher.getModelRenderer().renderModel(
					poseStack.last(),
					glintBuffer,
					state,
					model,
					1.0F,1.0F,1.0F, // Barva (RGB)
					combinedLight,
					combinedOverlay,
					ModelData.EMPTY,
					RenderType.glint()
			);
		}
	}
}