package cz.maxtechnik.dif.model;// Made with Blockbench 5.1.2
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
public class FormulaModel<T extends BaseCarEntity> extends EntityModel<T>{
	public static final ModelLayerLocation LAYER_LOCATION=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("modid","model"),"main");
	private final ModelPart body;
	private final ModelPart under;
	private final ModelPart front_wing;
	private final ModelPart wheel_handle;
	private final ModelPart spoil;
	private final ModelPart back_wing;
	private final ModelPart W1;
	private final ModelPart W2;
	private final ModelPart w3;
	private final ModelPart W4;
	public FormulaModel(ModelPart root){
		this.body=root.getChild("body");
		this.under=root.getChild("under");
		this.front_wing=root.getChild("front_wing");
		this.wheel_handle=root.getChild("wheel_handle");
		this.spoil=root.getChild("spoil");
		this.back_wing=root.getChild("back_wing");
		this.W1=root.getChild("W1");
		this.W2=root.getChild("W2");
		this.w3=root.getChild("w3");
		this.W4=root.getChild("W4");
	}
	public static LayerDefinition createBodyLayer(){
		MeshDefinition meshdefinition=new MeshDefinition();
		PartDefinition partdefinition=meshdefinition.getRoot();
		// Celý model posunut o 1px výš (Y: 9.4412 → 8.4412)
		PartDefinition body=partdefinition.addOrReplaceChild("body",CubeListBuilder.create(),PartPose.offset(0.0F,8.4412F,-4.6097F));
		body.addOrReplaceChild("cube_r1",CubeListBuilder.create().texOffs(171,55).addBox(-4.0F,-23.5181F,-37.5315F,8.0F,8.0F,26.0F,new CubeDeformation(-0.01F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-2.7053F,0.0F,-3.1416F));
		body.addOrReplaceChild("cube_r2",CubeListBuilder.create().texOffs(150,116).addBox(-4.0F,-16.4412F,-20.3903F,8.0F,8.0F,21.0F,new CubeDeformation(0.0F))
				.texOffs(204,59).addBox(-5.5F,1.5588F,25.6097F,11.0F,6.0F,15.0F,new CubeDeformation(0.0F))
				.texOffs(190,129).addBox(-7.0F,-2.4412F,10.6097F,14.0F,11.0F,18.0F,new CubeDeformation(0.01F))
				.texOffs(85,57).addBox(-9.0F,-8.4412F,-24.3903F,18.0F,18.0F,25.0F,new CubeDeformation(0.0F))
				.texOffs(0,109).addBox(-9.0F,-3.4412F,-49.3903F,18.0F,13.0F,25.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,0.0F,3.1416F));
		body.addOrReplaceChild("cube_r3",CubeListBuilder.create().texOffs(85,51).addBox(-8.802F,-7.2644F,-4.7306F,2.0F,5.0F,11.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,2.5303F,-0.0357F,-3.1166F));
		body.addOrReplaceChild("cube_r4",CubeListBuilder.create().texOffs(85,51).addBox(6.802F,-7.2644F,-4.7306F,2.0F,5.0F,11.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,2.5303F,0.0357F,3.1166F));
		body.addOrReplaceChild("cube_r5",CubeListBuilder.create().texOffs(179,90).addBox(7.4669F,-2.4412F,-0.1092F,2.0F,11.0F,28.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,0.0873F,3.1416F));
		body.addOrReplaceChild("cube_r6",CubeListBuilder.create().texOffs(179,90).addBox(-9.4669F,-2.4412F,-0.1092F,2.0F,11.0F,28.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,-0.0873F,3.1416F));
		body.addOrReplaceChild("cube_r7",CubeListBuilder.create().texOffs(96,105).addBox(5.4423F,-7.0F,-18.1732F,7.0F,14.0F,40.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.2434F,3.5588F,15.7248F,3.1416F,-0.1745F,-3.1416F));
		body.addOrReplaceChild("cube_r8",CubeListBuilder.create().texOffs(0,147).addBox(-9.0F,-12.5483F,-47.5539F,18.0F,5.0F,25.0F,new CubeDeformation(-0.01F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-2.9671F,0.0F,-3.1416F));
		body.addOrReplaceChild("cube_r9",CubeListBuilder.create().texOffs(96,105).addBox(-12.3567F,-7.0F,-18.1883F,7.0F,14.0F,40.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(-0.2566F,3.5588F,15.7248F,-3.1416F,0.1745F,3.1416F));
		PartDefinition under=partdefinition.addOrReplaceChild("under",CubeListBuilder.create(),PartPose.offset(0.0F,8.4412F,-4.6097F));
		under.addOrReplaceChild("cube_r10",CubeListBuilder.create().texOffs(0,51).addBox(-9.0F,8.5588F,-28.3903F,18.0F,5.0F,49.0F,new CubeDeformation(0.0F))
				.texOffs(0,0).addBox(-16.0F,9.5588F,-45.3903F,32.0F,2.0F,49.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,0.0F,3.1416F));
		PartDefinition front_wing=partdefinition.addOrReplaceChild("front_wing",CubeListBuilder.create(),PartPose.offset(0.0F,8.4412F,-4.6097F));
		front_wing.addOrReplaceChild("cube_r11",CubeListBuilder.create().texOffs(17,13).addBox(-4.0F,7.5588F,57.1097F,8.0F,3.0F,2.0F,new CubeDeformation(0.0F))
				.texOffs(0,51).addBox(-26.5F,6.5588F,45.1097F,1.0F,4.0F,14.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,0.0F,3.1416F));
		front_wing.addOrReplaceChild("cube_r12",CubeListBuilder.create().texOffs(55,128).addBox(-9.2875F,-10.9313F,25.8404F,3.0F,4.0F,32.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,2.8351F,-0.0832F,-3.1153F));
		front_wing.addOrReplaceChild("cube_r13",CubeListBuilder.create().texOffs(113,0).addBox(-4.0F,-10.9313F,26.5515F,8.0F,4.0F,32.0F,new CubeDeformation(0.01F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,2.8362F,0.0F,-3.1416F));
		front_wing.addOrReplaceChild("cube_r14",CubeListBuilder.create().texOffs(184,5).addBox(6.2875F,-10.9313F,25.8404F,3.0F,4.0F,32.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,2.8351F,0.0832F,3.1153F));
		front_wing.addOrReplaceChild("cube_r15",CubeListBuilder.create().texOffs(0,51).addBox(-26.5F,6.5588F,45.1097F,1.0F,4.0F,14.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(-52.0F,0.0F,0.0F,-3.1416F,0.0F,3.1416F));
		front_wing.addOrReplaceChild("cube_r16",CubeListBuilder.create().texOffs(0,4).addBox(-11.0F,-0.5F,-1.5F,22.0F,1.0F,3.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(15.0637F,8.0588F,-50.5675F,3.1416F,-0.0873F,0.0F));
		front_wing.addOrReplaceChild("cube_r17",CubeListBuilder.create().texOffs(0,8).addBox(-11.0F,-0.5F,-1.5F,22.0F,1.0F,3.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(15.0637F,9.0588F,-53.5675F,3.1416F,-0.0873F,0.0F));
		front_wing.addOrReplaceChild("cube_r18",CubeListBuilder.create().texOffs(0,0).addBox(-11.0F,-0.5F,-1.5F,22.0F,1.0F,3.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(14.9765F,10.0588F,-56.5713F,3.1416F,-0.0873F,0.0F));
		front_wing.addOrReplaceChild("cube_r19",CubeListBuilder.create().texOffs(0,0).addBox(-1.1016F,9.5588F,56.2363F,22.0F,1.0F,3.0F,new CubeDeformation(0.0F))
				.texOffs(0,8).addBox(-0.753F,8.5588F,53.2515F,22.0F,1.0F,3.0F,new CubeDeformation(0.0F))
				.texOffs(0,4).addBox(-0.4915F,7.5588F,50.2629F,22.0F,1.0F,3.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,-0.0873F,3.1416F));
		PartDefinition wheel_handle=partdefinition.addOrReplaceChild("wheel_handle",CubeListBuilder.create(),PartPose.offset(0.0F,8.4412F,-4.6097F));
		wheel_handle.addOrReplaceChild("cube_r20",CubeListBuilder.create().texOffs(113,37).addBox(-8.0F,-1.0F,-10.5F,16.0F,2.0F,10.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,2.5588F,48.8903F,3.0107F,0.0F,3.1416F));
		wheel_handle.addOrReplaceChild("cube_r21",CubeListBuilder.create().texOffs(113,37).addBox(-8.0F,-1.0F,-10.5F,16.0F,2.0F,10.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,8.5588F,48.8903F,3.0107F,0.0F,3.1416F));
		wheel_handle.addOrReplaceChild("cube_r22",CubeListBuilder.create().texOffs(11,105).addBox(-13.0F,6.5588F,-56.3903F,26.0F,2.0F,2.0F,new CubeDeformation(0.0F))
				.texOffs(0,105).addBox(-19.0F,6.5588F,32.6097F,38.0F,2.0F,2.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,0.0F,3.1416F));
		wheel_handle.addOrReplaceChild("cube_r23",CubeListBuilder.create().texOffs(22,105).addBox(17.9709F,6.5588F,17.2531F,16.0F,2.0F,2.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,0.5672F,3.1416F));
		wheel_handle.addOrReplaceChild("cube_r24",CubeListBuilder.create().texOffs(20,105).addBox(-34.0829F,6.5588F,17.1375F,17.0F,2.0F,2.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,-0.5672F,3.1416F));
		PartDefinition spoil=partdefinition.addOrReplaceChild("spoil",CubeListBuilder.create().texOffs(20,59).addBox(-14.0F,-7.0F,61.0F,4.0F,7.0F,10.0F,new CubeDeformation(0.0F))
				.texOffs(86,68).addBox(-13.0F,-11.0F,61.0F,2.0F,4.0F,9.0F,new CubeDeformation(0.0F)),PartPose.offset(12.0F,16.0F,-16.0F));
		spoil.addOrReplaceChild("cube_r25",CubeListBuilder.create().texOffs(0,12).addBox(-2.5F,-0.5F,-3.5F,5.0F,1.0F,7.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(-22.5F,-0.4056F,-0.5792F,-0.1745F,0.0F,-3.1416F));
		spoil.addOrReplaceChild("cube_r26",CubeListBuilder.create().texOffs(0,12).addBox(-4.0F,-1.0F,-4.0F,5.0F,1.0F,7.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.1745F,0.0F,0.0F));
		PartDefinition back_wing=partdefinition.addOrReplaceChild("back_wing",CubeListBuilder.create(),PartPose.offset(0.0F,8.4412F,-4.6097F));
		back_wing.addOrReplaceChild("cube_r27",CubeListBuilder.create().texOffs(128,66).addBox(-13.0F,-7.0968F,-58.1123F,26.0F,1.0F,9.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,3.0543F,0.0F,3.1416F));
		back_wing.addOrReplaceChild("cube_r28",CubeListBuilder.create().texOffs(0,20).addBox(8.0F,-6.4412F,-59.3903F,1.0F,9.0F,11.0F,new CubeDeformation(-0.02F))
				.texOffs(24,31).addBox(13.0F,-15.4412F,-59.3903F,1.0F,7.0F,11.0F,new CubeDeformation(0.0F))
				.texOffs(24,31).addBox(-14.0F,-15.4412F,-59.3903F,1.0F,7.0F,11.0F,new CubeDeformation(0.0F))
				.texOffs(0,20).addBox(-9.0F,-6.4412F,-59.3903F,1.0F,9.0F,11.0F,new CubeDeformation(-0.02F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,0.0F,3.1416F));
		back_wing.addOrReplaceChild("cube_r29",CubeListBuilder.create().texOffs(24,12).addBox(-1.1341F,-16.208F,-59.3903F,1.0F,6.0F,11.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,0.0F,2.138F));
		back_wing.addOrReplaceChild("cube_r30",CubeListBuilder.create().texOffs(133,76).addBox(-13.0F,46.6464F,-38.3935F,26.0F,1.0F,4.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,2.0071F,0.0F,3.1416F));
		back_wing.addOrReplaceChild("cube_r31",CubeListBuilder.create().texOffs(24,12).addBox(0.0948F,-16.2573F,-59.3903F,1.0F,6.0F,11.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,3.1416F,0.0F,-2.138F));
		// Přední kola – Y: 16 → 15
		PartDefinition W1=partdefinition.addOrReplaceChild("W1",CubeListBuilder.create(),PartPose.offset(23.0F,15.0F,-38.0F));
		W1.addOrReplaceChild("cube_r32",CubeListBuilder.create().texOffs(151,39).addBox(-4.0F,-3.0F,-7.5F,8.0F,6.0F,15.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.7854F,0.0F,3.1416F));
		W1.addOrReplaceChild("cube_r33",CubeListBuilder.create().texOffs(151,39).addBox(-4.0F,-3.0F,-7.5F,8.0F,6.0F,15.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,2.3562F,0.0F,3.1416F));
		W1.addOrReplaceChild("cube_r34",CubeListBuilder.create().texOffs(114,0).addBox(-4.0F,-7.5F,-3.0F,8.0F,15.0F,6.0F,new CubeDeformation(0.11F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,0.0F,3.1416F));
		W1.addOrReplaceChild("cube_r35",CubeListBuilder.create().texOffs(151,39).addBox(-4.0F,-3.0F,-7.5F,8.0F,6.0F,15.0F,new CubeDeformation(0.1F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,3.1416F,0.0F,3.1416F));
		PartDefinition W2=partdefinition.addOrReplaceChild("W2",CubeListBuilder.create(),PartPose.offset(-23.0F,15.0F,-38.0F));
		W2.addOrReplaceChild("cube_r36",CubeListBuilder.create().texOffs(151,39).addBox(-4.0F,-3.0F,-7.5F,8.0F,6.0F,15.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-2.3562F,0.0F,0.0F));
		W2.addOrReplaceChild("cube_r37",CubeListBuilder.create().texOffs(151,39).addBox(-4.0F,-3.0F,-7.5F,8.0F,6.0F,15.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-0.7854F,0.0F,0.0F));
		W2.addOrReplaceChild("cube_r36_2",CubeListBuilder.create().texOffs(114,0).addBox(-4.0F,-7.5F,-3.0F,8.0F,15.0F,6.0F,new CubeDeformation(0.11F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
		W2.addOrReplaceChild("cube_r36_3",CubeListBuilder.create().texOffs(151,39).addBox(-4.0F,-3.0F,-7.5F,8.0F,6.0F,15.0F,new CubeDeformation(0.1F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
		// Zadní kola – Y: 16 → 15, X: 19 → 18 a -19 → -18
		PartDefinition w3=partdefinition.addOrReplaceChild("w3",CubeListBuilder.create(),PartPose.offset(18.0F,15.0F,50.0F));
		w3.addOrReplaceChild("cube_r38",CubeListBuilder.create().texOffs(85,106).addBox(-5.0F,-3.0F,-7.5F,10.0F,6.0F,15.0F,new CubeDeformation(0.1F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,3.1416F,0.0F,3.1416F));
		w3.addOrReplaceChild("cube_r39",CubeListBuilder.create().texOffs(174,90).addBox(-5.0F,-7.5F,-3.0F,10.0F,15.0F,6.0F,new CubeDeformation(0.11F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-3.1416F,0.0F,3.1416F));
		w3.addOrReplaceChild("cube_r40",CubeListBuilder.create().texOffs(85,106).addBox(-5.0F,-3.0F,-7.5F,10.0F,6.0F,15.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,2.3562F,0.0F,3.1416F));
		w3.addOrReplaceChild("cube_r41",CubeListBuilder.create().texOffs(85,106).addBox(-5.0F,-3.0F,-7.5F,10.0F,6.0F,15.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.7854F,0.0F,3.1416F));
		PartDefinition W4=partdefinition.addOrReplaceChild("W4",CubeListBuilder.create(),PartPose.offset(-18.0F,15.0F,50.0F));
		W4.addOrReplaceChild("cube_r42",CubeListBuilder.create().texOffs(85,106).addBox(-5.0F,-3.0F,-7.5F,10.0F,6.0F,15.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-2.3562F,0.0F,0.0F));
		W4.addOrReplaceChild("cube_r43",CubeListBuilder.create().texOffs(85,106).addBox(-5.0F,-3.0F,-7.5F,10.0F,6.0F,15.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,-0.7854F,0.0F,0.0F));
		W4.addOrReplaceChild("cube_r42_2",CubeListBuilder.create().texOffs(85,106).addBox(-5.0F,-3.0F,-7.5F,10.0F,6.0F,15.0F,new CubeDeformation(0.1F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
		W4.addOrReplaceChild("cube_r42_3",CubeListBuilder.create().texOffs(174,90).addBox(-5.0F,-7.5F,-3.0F,10.0F,15.0F,6.0F,new CubeDeformation(0.11F)),PartPose.offsetAndRotation(0.0F,0.0F,0.0F,0.0F,0.0F,0.0F));
		return LayerDefinition.create(meshdefinition,256,256);
	}
	@Override
	public void setupAnim(T entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch){
		this.W1.xRot=limbSwing;
		this.W2.xRot=limbSwing;
		this.w3.xRot=limbSwing;
		this.W4.xRot=limbSwing;
		float steer=entity.getCurrentSteering()*0.4363F;
		this.W1.yRot=steer;
		this.W2.yRot=steer;
		this.w3.yRot=0;
		this.W4.yRot=0;
	}
	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack,@NotNull VertexConsumer vertexConsumer,int packedLight,int packedOverlay,int color){
		body.render(poseStack,vertexConsumer,packedLight,packedOverlay,color);
		under.render(poseStack,vertexConsumer,packedLight,packedOverlay,color);
		front_wing.render(poseStack,vertexConsumer,packedLight,packedOverlay,color);
		wheel_handle.render(poseStack,vertexConsumer,packedLight,packedOverlay,color);
		spoil.render(poseStack,vertexConsumer,packedLight,packedOverlay,color);
		back_wing.render(poseStack,vertexConsumer,packedLight,packedOverlay,color);
		W1.render(poseStack,vertexConsumer,packedLight,packedOverlay,color);
		W2.render(poseStack,vertexConsumer,packedLight,packedOverlay,color);
		w3.render(poseStack,vertexConsumer,packedLight,packedOverlay,color);
		W4.render(poseStack,vertexConsumer,packedLight,packedOverlay,color);
	}
}