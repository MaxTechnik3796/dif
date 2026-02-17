package cz.maxtechnik.dif.model;// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
public class ModelJetpack<T extends Entity> extends EntityModel<T>{
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("dif", "dif"), "main");
	private final ModelPart Body;

	public ModelJetpack(ModelPart root) {
		this.Body = root.getChild("Body");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 7).addBox(-1.1F, 1.25F, 2.5F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(17, 0).addBox(-2.1F, 1.0F, 2.0F, 4.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = Body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -4.0F, 0.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9F, 5.25F, 2.5F, 0.0873F, 0.0F, -0.2182F));

		PartDefinition cube_r2 = Body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(1, 24).addBox(-1.5F, 2.0F, -1.0F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.9218F, 3.5936F, 3.9181F, 0.0873F, 0.0F, 0.2182F));

		PartDefinition cube_r3 = Body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(1, 24).addBox(-1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0048F, 5.0524F, 4.0488F, -0.0873F, 0.0F, 2.9234F));

		PartDefinition cube_r4 = Body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(1, 24).addBox(-1.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.2452F, 5.0524F, 4.0488F, 0.0873F, 0.0F, 0.2182F));

		PartDefinition cube_r5 = Body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 17).addBox(-4.0F, -4.0F, 0.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1F, 5.25F, 2.5F, 0.0873F, 0.0F, 0.2182F));

		PartDefinition cube_r6 = Body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(14, 15).addBox(-1.0F, -5.0F, 0.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1F, 12.0F, 3.5F, 0.4784F, -0.0403F, 0.0774F));

		PartDefinition cube_r7 = Body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(8, 7).addBox(-2.0F, -5.0F, 0.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.9F, 12.0F, 3.5F, 0.4784F, 0.0403F, -0.0774F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(@NotNull Entity entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch) {}
	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack,@NotNull VertexConsumer vertexConsumer,int packedLight,int packedOverlay,float red,float green,float blue,float alpha) {
		Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}