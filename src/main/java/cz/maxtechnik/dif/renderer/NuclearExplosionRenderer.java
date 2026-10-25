package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.maxtechnik.dif.entity.bomb.NuclearExplosionEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
public class NuclearExplosionRenderer extends EntityRenderer<NuclearExplosionEntity>{
	private static final int PURPLE_BEAM_COLOR=0xFFB838FF;
	public NuclearExplosionRenderer(EntityRendererProvider.Context context){
		super(context);
	}
	@Override
	public void render(NuclearExplosionEntity entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight){
		if(entity.getAge()<NuclearExplosionEntity.IMPLOSION_TICKS) return;
		poseStack.pushPose();
		poseStack.translate(-0.5D,0.0D,-0.5D);
		BeaconRenderer.renderBeaconBeam(
				poseStack,buffer,BeaconRenderer.BEAM_LOCATION,
				partialTick,1.0F,entity.level().getGameTime(),
				-64,320,PURPLE_BEAM_COLOR,0.65F,1.35F
		);
		poseStack.popPose();
		super.render(entity,entityYaw,partialTick,poseStack,buffer,packedLight);
	}
	@Override
	public boolean shouldRender(@NotNull NuclearExplosionEntity entity,@NotNull Frustum camera,double camX,double camY,double camZ){
		return true;
	}
	@Override
	public @NotNull ResourceLocation getTextureLocation(@NotNull NuclearExplosionEntity entity){
		return BeaconRenderer.BEAM_LOCATION;
	}
}
