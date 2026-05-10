package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.model.FormulaModel;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import cz.maxtechnik.dif.entity.vehicle.FormulaEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, value=Dist.CLIENT, bus=EventBusSubscriber.Bus.GAME)
public class CarRenderer<T extends BaseCarEntity> extends EntityRenderer<T>{
	private final FormulaModel<T> model;
	private static final ResourceLocation TEX_BASE=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/entity/f1_base.png");
	private static final ResourceLocation TEX_COLOR=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/entity/f1_collored.png");
	public CarRenderer(EntityRendererProvider.Context ctx){
		super(ctx);
		this.shadowRadius=0.7F;
		this.model=new FormulaModel<>(ctx.bakeLayer(FormulaModel.LAYER_LOCATION));
	}
	@Override
	public void render(@NotNull T entity,float y,float partialTick,PoseStack poseStack,MultiBufferSource buffer,int l){
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180F-y));
		poseStack.translate(0D,1.38D,0D);
		poseStack.scale(-1F,-1F,1F);
		float wheelSpin=(entity.tickCount+partialTick)*entity.getSpeedKmh()*0.02f;
		model.setupAnim(entity,wheelSpin,0F,entity.tickCount+partialTick,0F,0F);
		model.renderToBuffer(poseStack,buffer.getBuffer(model.renderType(TEX_BASE)),l,OverlayTexture.NO_OVERLAY,0xFFFFFFFF);
		if(entity instanceof FormulaEntity formula){
			int color=formula.getColor();
			int argbColor=FastColor.ARGB32.color(255,(color>>16&255),(color>>8&255),(color&255));
			model.renderToBuffer(poseStack,buffer.getBuffer(model.renderType(TEX_COLOR)),l,OverlayTexture.NO_OVERLAY,argbColor);
		}
		poseStack.popPose();
		super.render(entity,y,partialTick,poseStack,buffer,l);
	}
	@Override
	public @NotNull ResourceLocation getTextureLocation(@NotNull T e){
		return TEX_BASE;
	}
	@SubscribeEvent
	public static void onPlayerRenderPre(RenderPlayerEvent.Pre event){
		if(event.getEntity().getVehicle() instanceof BaseCarEntity car){
			float yaw=net.minecraft.util.Mth.lerp(event.getPartialTick(),car.yRotO,car.getYRot());
			event.getEntity().yBodyRot=yaw;
			event.getEntity().yBodyRotO=yaw;
			var model=event.getRenderer().getModel();
			model.rightLeg.yScale=model.leftLeg.yScale=model.rightPants.yScale=model.leftPants.yScale=0.5F;
		}
	}
	@SubscribeEvent
	public static void onPlayerRenderPost(RenderPlayerEvent.Post event){
		var model=event.getRenderer().getModel();
		model.rightLeg.yScale=model.leftLeg.yScale=model.rightPants.yScale=model.leftPants.yScale=1.0F;
	}
}