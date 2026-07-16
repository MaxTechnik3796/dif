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
import org.jetbrains.annotations.NotNull;

public class SilkwormMothModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("modid", "silkworm_moth"), "main");
	private final ModelPart all;
    private final ModelPart wing1;
	private final ModelPart wing2;
	private final ModelPart leg2;
	private final ModelPart leg1;

    public SilkwormMothModel(ModelPart root) {
		this.all = root.getChild("all");
        ModelPart body1 = this.all.getChild("body1");
        body1.getChild("bodycube1");
        ModelPart head = body1.getChild("head");
        head.getChild("ear1");
        head.getChild("ear2");
        this.wing1 = body1.getChild("wing1");
		this.wing2 = body1.getChild("wing2");
		this.leg2 = body1.getChild("leg2");
		this.leg1 = body1.getChild("leg1");
        ModelPart body2 = this.all.getChild("body2");
        body2.getChild("leg3");
        body2.getChild("leg4");
        body2.getChild("bodycube2");
        body2.getChild("leg5");
        body2.getChild("leg6");
        this.all.getChild("bone2");
    }

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition all = partdefinition.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.offset(-0.5F, 21.0F, -0.25F));

		PartDefinition body1 = all.addOrReplaceChild("body1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        body1.addOrReplaceChild("bodycube1", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, -2.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.25F))
                .texOffs(0, 21).addBox(-2.0F, -4.0F, -2.0F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 2.0F, -0.75F));

        PartDefinition head = body1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(21, 0).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 8).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.25F, 1.0F, -2.75F, 0.3054F, 0.0F, 0.0F));

		PartDefinition ear1 = head.addOrReplaceChild("ear1", CubeListBuilder.create(), PartPose.offset(-0.5F, -2.0F, -1.0F));

        ear1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(29, 1).addBox(-4.0F, -2.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.6981F, 0.0F, 0.0F));

        PartDefinition ear2 = head.addOrReplaceChild("ear2", CubeListBuilder.create(), PartPose.offset(0.25F, 0.0F, -0.25F));

        ear2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(25, 5).addBox(0.0F, -2.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -2.0F, -0.75F, -0.7418F, 0.0F, 0.0F));

        body1.addOrReplaceChild("wing1", CubeListBuilder.create().texOffs(8, 9).addBox(-9.0F, 0.0F, -5.0F, 9.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, -2.0F, -0.75F));

        body1.addOrReplaceChild("wing2", CubeListBuilder.create().texOffs(-9, 9).addBox(0.0F, 0.0F, -5.0F, 9.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -2.0F, -0.75F));

        body1.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(18, 34).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(30, 35).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-2.25F, 0.1F, -0.75F, 0.0F, -0.1745F, 0.1745F));

        body1.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(24, 35).addBox(0.0F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.25F))
                .texOffs(36, 34).addBox(0.0F, 0.0F, -1.0F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.75F, 0.1F, -0.75F, 0.0F, 0.1745F, -0.1745F));

        PartDefinition body2 = all.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(0.25F, 0.0F, 2.25F));

        body2.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(4, 37).addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 1.0F, 0.0F, 0.0F, 0.4363F, 0.0F));

        body2.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(6, 37).addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 1.0F, 0.0F, 0.0F, -0.4363F, 0.0F));

        body2.addOrReplaceChild("bodycube2", CubeListBuilder.create().texOffs(20, 23).addBox(-1.0F, -3.0F, -1.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(31, 20).addBox(-1.0F, -3.0F, -1.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-0.5F, 1.5F, 1.0F));

        body2.addOrReplaceChild("leg5", CubeListBuilder.create().texOffs(8, 37).addBox(0.001F, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 1.25F, 2.0F, 0.0F, 0.0F, -0.4363F));

        body2.addOrReplaceChild("leg6", CubeListBuilder.create().texOffs(10, 37).addBox(0.0F, 0.25F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 1.0F, 2.0F, 0.0F, 0.0F, 0.4363F));

        all.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Animace křídel
		float wingFlap = net.minecraft.util.Mth.cos(ageInTicks * 2.0F) * 0.6F;
		this.wing1.zRot = wingFlap;
		this.wing2.zRot = -wingFlap;

		// Animace nohou při chůzi
		this.leg1.xRot = net.minecraft.util.Mth.cos(limbSwing * 0.6662F) * 1.0F * limbSwingAmount;
		this.leg2.xRot = net.minecraft.util.Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.0F * limbSwingAmount;
	}

	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		all.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}