package cz.maxtechnik.dif.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.portal.PortalEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
public class PortalModel<T extends PortalEntity> extends EntityModel<T>{
	public static final ModelLayerLocation LAYER_LOCATION=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"portal"),"main");
	private final ModelPart main;
	public PortalModel(ModelPart root){
		this.main=root.getChild("main");
	}
	public static LayerDefinition createBodyLayer(){
		MeshDefinition meshdefinition=new MeshDefinition();
		PartDefinition partdefinition=meshdefinition.getRoot();
		// 16x32 texture, covering a 16x32 face
		// We use a thin box so it has some presence, but the main texture maps to the front face.
		// We set the texture size to 16x32.
		partdefinition.addOrReplaceChild("main",CubeListBuilder.create()
						.texOffs(0,0)
						.addBox(-8.0F,-16.0F,0.0F,16.0F,32.0F,0.01F,new CubeDeformation(0.0F)),
				PartPose.ZERO);
		return LayerDefinition.create(meshdefinition,16,32);
	}
	@Override
	public void setupAnim(@NotNull T entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch){
		// No animation needed
	}
	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack,@NotNull VertexConsumer vertexConsumer,int packedLight,int packedOverlay,int color){
		main.render(poseStack,vertexConsumer,packedLight,packedOverlay,color);
	}
}
