package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.block.entity.ModularReforgeTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
public class ModularReforgeTableRenderer implements BlockEntityRenderer<ModularReforgeTableBlockEntity>{
	private static final int FORCED_LIGHT=LightTexture.pack(14,14);
	public ModularReforgeTableRenderer(){
	}
	@Override
	public void render(@NotNull ModularReforgeTableBlockEntity blockEntity,float partialTick,@NotNull PoseStack poseStack,@NotNull MultiBufferSource buffer,int combinedLight,int combinedOverlay){
		ItemRenderer itemRenderer=Minecraft.getInstance().getItemRenderer();
		BlockState blockState=blockEntity.getBlockState();
		Direction facing=blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
		ItemStack tool=blockEntity.getInventory().getStackInSlot(0);
		ItemStack template=blockEntity.getInventory().getStackInSlot(1);
		ItemStack catalyst=blockEntity.getInventory().getStackInSlot(2);
		if(!tool.isEmpty()){
			poseStack.pushPose();
			switch(facing){
				case NORTH -> {
					poseStack.translate(0.42F,1.0201F,0.52F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(90F));
				}
				case SOUTH -> {
					poseStack.translate(0.58F,1.0201F,0.48F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(-90F));
				}
				case EAST -> {
					poseStack.translate(0.48F,1.0201F,0.42F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
				}
				case WEST -> {
					poseStack.translate(0.52F,1.0201F,0.58F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
				}
				default -> {
				}
			}
			poseStack.scale(0.5F,0.5F,0.5F);
			itemRenderer.renderStatic(tool,ItemDisplayContext.FIXED,FORCED_LIGHT,combinedOverlay,poseStack,buffer,null,0);
			poseStack.popPose();
		}
		if(!template.isEmpty()){
			poseStack.pushPose();
			switch(facing){
				case NORTH -> {
					poseStack.translate(0.42F,1.02F,0.17F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(90F));
				}
				case SOUTH -> {
					poseStack.translate(0.58F,1.02F,0.83F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(-90F));
				}
				case EAST -> {
					poseStack.translate(0.83F,1.02F,0.42F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
				}
				case WEST -> {
					poseStack.translate(0.17F,1.02F,0.58F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
				}
				default -> {
				}
			}
			poseStack.scale(0.3F,0.3F,0.3F);
			itemRenderer.renderStatic(template,ItemDisplayContext.FIXED,FORCED_LIGHT,combinedOverlay,poseStack,buffer,null,0);
			poseStack.popPose();
		}
		if(!catalyst.isEmpty()){
			poseStack.pushPose();
			switch(facing){
				case NORTH -> {
					poseStack.translate(0.42F,1.02F,0.85F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(90F));
				}
				case SOUTH -> {
					poseStack.translate(0.58F,1.02F,0.15F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(-90F));
				}
				case EAST -> {
					poseStack.translate(0.15F,1.02F,0.42F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
				}
				case WEST -> {
					poseStack.translate(0.85F,1.02F,0.58F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
				}
				default -> {
				}
			}
			poseStack.scale(0.3F,0.3F,0.3F);
			itemRenderer.renderStatic(catalyst,ItemDisplayContext.FIXED,FORCED_LIGHT,combinedOverlay,poseStack,buffer,null,0);
			poseStack.popPose();
		}
	}
}