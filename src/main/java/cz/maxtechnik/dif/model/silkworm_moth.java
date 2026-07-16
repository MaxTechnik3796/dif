package cz.maxtechnik.dif.model;// Made with Blockbench 5.1.3
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

public class silkworm_moth<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("modid", "silkworm_moth"), "main");
	private final ModelPart all;
	private final ModelPart body1;
	private final ModelPart bodycube1;
	private final ModelPart head;
	private final ModelPart ear1;
	private final ModelPart ear2;
	private final ModelPart wing1;
	private final ModelPart wing2;
	private final ModelPart leg2;
	private final ModelPart leg1;
	private final ModelPart body2;
	private final ModelPart leg3;
	private final ModelPart leg4;
	private final ModelPart bodycube2;
	private final ModelPart leg5;
	private final ModelPart leg6;
	private final ModelPart bone2;

	public silkworm_moth(ModelPart root) {
		this.all = root.getChild("all");
		this.body1 = this.all.getChild("body1");
		this.bodycube1 = this.body1.getChild("bodycube1");
		this.head = this.body1.getChild("head");
		this.ear1 = this.head.getChild("ear1");
		this.ear2 = this.head.getChild("ear2");
		this.wing1 = this.body1.getChild("wing1");
		this.wing2 = this.body1.getChild("wing2");
		this.leg2 = this.body1.getChild("leg2");
		this.leg1 = this.body1.getChild("leg1");
		this.body2 = this.all.getChild("body2");
		this.leg3 = this.body2.getChild("leg3");
		this.leg4 = this.body2.getChild("leg4");
		this.bodycube2 = this.body2.getChild("bodycube2");
		this.leg5 = this.body2.getChild("leg5");
		this.leg6 = this.body2.getChild("leg6");
		this.bone2 = this.all.getChild("bone2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition all = partdefinition.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.offset(-0.5F, 21.0F, -0.25F));

		PartDefinition body1 = all.addOrReplaceChild("body1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bodycube1 = body1.addOrReplaceChild("bodycube1", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, -2.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.25F))
		.texOffs(0, 21).addBox(-2.0F, -4.0F, -2.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 2.0F, -0.75F));

		PartDefinition head = body1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(21, 0).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 8).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.25F, 1.0F, -2.75F, 0.3054F, 0.0F, 0.0F));

		PartDefinition ear1 = head.addOrReplaceChild("ear1", CubeListBuilder.create(), PartPose.offset(-0.5F, -2.0F, -1.0F));

		PartDefinition cube_r1 = ear1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(29, 1).addBox(-4.0F, -2.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6981F, 0.0F, 0.0F));

		PartDefinition ear2 = head.addOrReplaceChild("ear2", CubeListBuilder.create(), PartPose.offset(0.25F, 0.0F, -0.25F));

		PartDefinition cube_r2 = ear2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(25, 5).addBox(0.0F, -2.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -2.0F, -0.75F, -0.7418F, 0.0F, 0.0F));

		PartDefinition wing1 = body1.addOrReplaceChild("wing1", CubeListBuilder.create().texOffs(8, 9).addBox(-9.0F, 0.0F, -5.0F, 9.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, -2.0F, -0.75F));

		PartDefinition wing2 = body1.addOrReplaceChild("wing2", CubeListBuilder.create().texOffs(-9, 9).addBox(0.0F, 0.0F, -5.0F, 9.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -2.0F, -0.75F));

		PartDefinition leg2 = body1.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(18, 34).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(30, 35).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-2.25F, 0.1F, -0.75F, 0.0F, -0.1745F, 0.1745F));

		PartDefinition leg1 = body1.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(24, 35).addBox(0.0F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.25F))
		.texOffs(36, 34).addBox(0.0F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.75F, 0.1F, -0.75F, 0.0F, 0.1745F, -0.1745F));

		PartDefinition body2 = all.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(0.25F, 0.0F, 2.25F));

		PartDefinition leg3 = body2.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(4, 37).addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 1.0F, 0.0F, 0.0F, 0.4363F, 0.0F));

		PartDefinition leg4 = body2.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(6, 37).addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 1.0F, 0.0F, 0.0F, -0.4363F, 0.0F));

		PartDefinition bodycube2 = body2.addOrReplaceChild("bodycube2", CubeListBuilder.create().texOffs(20, 23).addBox(-1.0F, -3.0F, -1.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(31, 20).addBox(-1.0F, -3.0F, -1.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-0.5F, 1.5F, 1.0F));

		PartDefinition leg5 = body2.addOrReplaceChild("leg5", CubeListBuilder.create().texOffs(8, 37).addBox(0.001F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 1.25F, 2.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition leg6 = body2.addOrReplaceChild("leg6", CubeListBuilder.create().texOffs(10, 37).addBox(0.0F, 0.25F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 1.0F, 2.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition bone2 = all.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		all.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int i1, int i2) {

	}
}