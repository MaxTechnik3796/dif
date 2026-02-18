package cz.maxtechnik.dif.model;// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
public class ModelElectroRunners<T extends Entity> extends EntityModel<T>{
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"modelel_ectro_runners"), "main");
	public final ModelPart RightLeg;
	public final ModelPart LeftLeg;

	public ModelElectroRunners(ModelPart root) {
		this.RightLeg = root.getChild("RightLeg");
		this.LeftLeg = root.getChild("LeftLeg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(1, 5).addBox(-0.6F, 9.0F, 2.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
		RightLeg.addOrReplaceChild("cube_r1",CubeListBuilder.create().texOffs(5,5).addBox(-1.0F,-1.5F,-0.5F,2.0F,3.0F,1.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(-0.1F,10.5F,-2.5F,0.0F,3.1416F,0.0F));
		RightLeg.addOrReplaceChild("cube_r2",CubeListBuilder.create().texOffs(0,0).addBox(-1.0F,2.0F,-1.0F,2.0F,5.0F,1.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(-0.1F,2.0F,2.0F,0.0F,3.1416F,0.0F));
		partdefinition.addOrReplaceChild("LeftLeg",CubeListBuilder.create().texOffs(0,0).addBox(-0.9F,4.0F,2.0F,2.0F,5.0F,1.0F,new CubeDeformation(0.0F))
				.texOffs(1,5).addBox(-0.4F,9.0F,2.0F,1.0F,3.0F,1.0F,new CubeDeformation(0.0F))
				.texOffs(5,5).addBox(-0.9F,9.0F,-3.0F,2.0F,3.0F,1.0F,new CubeDeformation(0.0F)),PartPose.offset(1.9F,12.0F,0.0F));
		return LayerDefinition.create(meshdefinition, 12, 10);
	}
	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack,@NotNull VertexConsumer vertexConsumer,int packedLight,int packedOverlay,float red,float green,float blue,float alpha) {
		RightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		LeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
	public void setupAnim(@NotNull T entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch) {
		this.LeftLeg.xRot = Mth.cos(limbSwing) * -1.0F * limbSwingAmount;
		this.RightLeg.xRot = Mth.cos(limbSwing) * 1.0F * limbSwingAmount;
	}
}