package cz.maxtechnik.dif.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
public class ModelSpaceHelmet<T extends Entity> extends EntityModel<T>{
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"model_space_helmet"),"main");
	public final ModelPart Head;
	public ModelSpaceHelmet(ModelPart root){
		this.Head=root.getChild("Head");
	}
	public static LayerDefinition createBodyLayer(){
		MeshDefinition meshdefinition=new MeshDefinition();
		PartDefinition partdefinition=meshdefinition.getRoot();
		PartDefinition Head=partdefinition.addOrReplaceChild("Head",
				CubeListBuilder.create().texOffs(0,0).addBox(-4.0F,-8.0F,-4.0F,8.0F,8.0F,8.0F,new CubeDeformation(0.501F)).texOffs(32,0).addBox(-4.0F,-8.0F,-4.0F,8.0F,8.0F,8.0F,new CubeDeformation(0.801F)),
				PartPose.offset(0.0F,0.0F,0.0F));
		return LayerDefinition.create(meshdefinition,88,88);
	}
	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack,@NotNull VertexConsumer vertexConsumer,int packedLight,int packedOverlay,float red,float green,float blue,float alpha){
		Head.render(poseStack,vertexConsumer,packedLight,packedOverlay,red,green,blue,alpha);
	}
	public void setupAnim(@NotNull T entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch){
		this.Head.yRot=netHeadYaw/(180F/(float)Math.PI);
		this.Head.xRot=headPitch/(180F/(float)Math.PI);
	}
}
