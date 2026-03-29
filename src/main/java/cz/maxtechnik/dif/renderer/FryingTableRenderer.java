package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.block.entity.FryingTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
public class FryingTableRenderer implements BlockEntityRenderer<FryingTableBlockEntity>{
	public FryingTableRenderer(BlockEntityRendererProvider.Context context){
	}
	@Override
	public void render(FryingTableBlockEntity blockEntity,float partialTick,@NotNull PoseStack poseStack,@NotNull MultiBufferSource buffer,int combinedLight,int combinedOverlay){
		blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler->{
			ItemRenderer itemRenderer=Minecraft.getInstance().getItemRenderer();
			ItemStack inputStack=handler.getStackInSlot(FryingTableBlockEntity.INPUT_SLOT);
			if(!inputStack.isEmpty()){
				poseStack.pushPose();
				poseStack.translate(0.5D,0.75D,0.5D);
				poseStack.scale(0.6F,0.6F,0.6F);
				renderItemStack(inputStack,poseStack,buffer,combinedLight,combinedOverlay,itemRenderer,false);
				poseStack.popPose();
			}
			ItemStack outputStack=handler.getStackInSlot(FryingTableBlockEntity.OUTPUT_SLOT);
			if(!outputStack.isEmpty()){
				poseStack.pushPose();
				poseStack.translate(0.75D,0.72D,0.7D);
				poseStack.scale(0.4F,0.4F,0.4F);
				renderItemStack(outputStack,poseStack,buffer,combinedLight,combinedOverlay,itemRenderer,true);
				poseStack.popPose();
			}
		});
	}
	/**
	 * Pomocná metoda pro renderování hromádky itemů
	 */
	private void renderItemStack(ItemStack stack,PoseStack poseStack,MultiBufferSource buffer,int combinedLight,int combinedOverlay,ItemRenderer itemRenderer,boolean isOutput){
		int count=stack.getCount();
		int layers=Math.min(4,1+(count/16));
		for(int i=0;i<layers;i++){
			poseStack.pushPose();
			double stackShift=isOutput?-0.015D:0;
			poseStack.translate(stackShift*i,i*0.1D,stackShift*i);
			poseStack.mulPose(Axis.YP.rotationDegrees(i*10F+(count%10)));
			poseStack.mulPose(Axis.XP.rotationDegrees(90F));
			itemRenderer.renderStatic(stack,ItemDisplayContext.FIXED,combinedLight,combinedOverlay,poseStack,buffer,null,0);
			poseStack.popPose();
		}
	}
}