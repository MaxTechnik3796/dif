package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.block.ChunkLoader;
import cz.maxtechnik.dif.block.entity.ChunkLoaderBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
public class ChunkLoaderRenderer implements BlockEntityRenderer<ChunkLoaderBlockEntity>{
	public ChunkLoaderRenderer(){
	}
	@Override
	public void render(ChunkLoaderBlockEntity be,float partialTicks,@NotNull PoseStack poseStack,@NotNull MultiBufferSource buffer,int combinedLight,int combinedOverlay){
		BlockState state=be.getBlockState();
		if(state.hasProperty(ChunkLoader.LIT)&&state.getValue(ChunkLoader.LIT)){
			BlockRenderDispatcher dispatcher=Minecraft.getInstance().getBlockRenderer();
			BakedModel model=dispatcher.getBlockModel(state);
			VertexConsumer glintBuffer=buffer.getBuffer(RenderType.glint());
			dispatcher.getModelRenderer().renderModel(
					poseStack.last(),
					glintBuffer,
					state,
					model,
					1.0F,1.0F,1.0F,
					combinedLight,
					combinedOverlay,
					ModelData.EMPTY,
					RenderType.glint()
			);
		}
	}
}