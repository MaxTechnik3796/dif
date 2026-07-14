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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class ModularReforgeTableRenderer implements BlockEntityRenderer<ModularReforgeTableBlockEntity>{
	private static final float IDLE_RADIUS=0.3F;
	private static final float IDLE_Y=0.3F;
	private static final float CENTER_Y_LIFT=0.30F;
	private static final float BOB_AMPLITUDE=0.035F;
	private static final float IDLE_ORBIT_SPEED=6F;   // stupňů/tick - společná rotace kolem středu stolu
	private static final float IDLE_SPIN_SPEED=-3.0F;  // stupňů/tick - rotace itemu kolem vlastní osy v klidu
	private static final float MERGE_MAX_SPIN_SPEED=5F; // stupňů/tick těsně před spojením
	private static final float ITEM_TILT_DEGREES=30F; // náklon místo plných 90° (stání místo ležení)
	private static final int FORCED_LIGHT=LightTexture.pack(14,14); // vynucené jasné nasvícení itemů

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
			poseStack.translate(0.5F,1.0201F,0.5F);
			poseStack.mulPose(Axis.XP.rotationDegrees(90F));
			switch(facing){
				case NORTH-> poseStack.mulPose(Axis.ZP.rotationDegrees(90F));
				case SOUTH->poseStack.mulPose(Axis.ZP.rotationDegrees(-90F));
				case EAST->poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
				default -> {}
			}
			poseStack.scale(0.5F,0.5F,0.5F);


			itemRenderer.renderStatic(tool,ItemDisplayContext.FIXED,FORCED_LIGHT,combinedOverlay,poseStack,buffer,null,0);
			poseStack.popPose();
		}
		if(!template.isEmpty()){
			poseStack.pushPose();

			switch(facing){
				case NORTH->{
					poseStack.translate(0.5F,1.02F,0.17F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(90F));
				}
				case SOUTH->{
					poseStack.translate(0.5F,1.02F,0.83F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(-90F));
				}
				case EAST->{
					poseStack.translate(0.83F,1.02F,0.5F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
				}
				case WEST->{
					poseStack.translate(0.17F,1.02F,0.5F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
				}
				default -> {}
			}

			poseStack.scale(0.3F,0.3F,0.3F);

			itemRenderer.renderStatic(template,ItemDisplayContext.FIXED,FORCED_LIGHT,combinedOverlay,poseStack,buffer,null,0);
			poseStack.popPose();
		}
		if(!catalyst.isEmpty()){
			poseStack.pushPose();

			switch(facing){
				case NORTH->{
					poseStack.translate(0.5F,1.02F,0.83F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(90F));
				}
				case SOUTH->{
					poseStack.translate(0.5F,1.02F,0.17F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(-90F));
				}
				case EAST->{
					poseStack.translate(0.17F,1.02F,0.5F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
					poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
				}
				case WEST->{
					poseStack.translate(0.83F,1.02F,0.5F);
					poseStack.mulPose(Axis.XP.rotationDegrees(90F));
				}
				default -> {}
			}
			poseStack.scale(0.3F,0.3F,0.3F);

			itemRenderer.renderStatic(catalyst,ItemDisplayContext.FIXED,FORCED_LIGHT,combinedOverlay,poseStack,buffer,null,0);
			poseStack.popPose();
		}








		/*List<Integer> activeSlots=new ArrayList<>();
		for(int i=0;i<ModularReforgeTableBlockEntity.SLOT_COUNT;i++){
			if(!blockEntity.getInventory().getStackInSlot(i).isEmpty()) activeSlots.add(i);
		}
		int count=activeSlots.size();
		if(count==0) return;

		float progress=blockEntity.isMerging()?Mth.clamp((blockEntity.getMergeProgress()+partialTick)/ModularReforgeTableBlockEntity.ANIMATION_DURATION,0F,1F):0F;
		float radialProgress=1F-(1F-progress)*(1F-progress);

		// Celková doba od začátku renderu (world time + partialTick) - používá se pro klidovou rotaci,
		// orbit a bobbing, aby to plynule běželo i bez ohledu na merge.
		float time=blockEntity.getLevel()!=null?(blockEntity.getLevel().getGameTime()%100000)+partialTick:partialTick;

		// Společný orbitální úhel - všechny itemy krouží kolem středu stolu jako "kolo".
		// Při merge se orbitální rychlost zvyšuje od začátku a itemy se už od prvních ticků stáčejí ke středu.
		float orbitSpeed=IDLE_ORBIT_SPEED + IDLE_ORBIT_SPEED*2F*progress;
		float orbitAngle=(time*orbitSpeed)%360F;

		for(int index=0;index<count;index++){
			int slot=activeSlots.get(index);
			ItemStack stack=blockEntity.getInventory().getStackInSlot(slot);
			poseStack.pushPose();

			// Úhel tohoto konkrétního itemu na orbitě: rovnoměrně rozmístěné po kruhu
			// (1 item = 0°, 2 = naproti sobě po 180°, 3 = trojúhelník po 120°) + společný orbitAngle.
			float slotAngleDeg=orbitAngle+(360F/count)*index;
			float slotAngleRad=slotAngleDeg*Mth.DEG_TO_RAD;

			// Klidová pozice na kruhu o poloměru IDLE_RADIUS, poloměr se od začátku merge zrychleně zmenšuje k 0.
			float radius=Mth.lerp(radialProgress,IDLE_RADIUS,0F);
			float idleX=Mth.sin(slotAngleRad)*radius;
			float idleZ=Mth.cos(slotAngleRad)*radius;

			// Bobbing - tlumí se s postupujícím mergem, aby item plynule splynul do jednoho bodu.
			float bobPhase=time*0.08F+slot*2.094F; // rozfázováno o 120°
			float bobOffset=Mth.sin(bobPhase)*BOB_AMPLITUDE*(1F-progress*0.7F);

			// Výška: v klidu IDLE_Y, s postupem mergu lehce stoupá k IDLE_Y + CENTER_Y_LIFT.
			float y=Mth.lerp(radialProgress,IDLE_Y,IDLE_Y+CENTER_Y_LIFT)+bobOffset;

			poseStack.translate(0.5D+idleX,1.0D+y,0.5D+idleZ);

			// Rotace kolem vlastní osy: zůstává konstantní a neovlivněná mergem.
			float spinAngle=(time*IDLE_SPIN_SPEED)%360F;
			poseStack.mulPose(Axis.YP.rotationDegrees(spinAngle));
			// Náklon - místo plných 90° (item naplocho) použijeme ITEM_TILT_DEGREES, aby item spíš "stál" nakloněný.
			poseStack.mulPose(Axis.XP.rotationDegrees(ITEM_TILT_DEGREES));

			float scale=0.5F*(1F-Math.min(0.55F,progress*0.55F));
			poseStack.scale(scale,scale,scale);

			// Vynucené jasné nasvícení, aby item nebyl tmavý bez ohledu na okolní světlo bloku.
			itemRenderer.renderStatic(stack,ItemDisplayContext.FIXED,FORCED_LIGHT,combinedOverlay,poseStack,buffer,null,0);
			poseStack.popPose();
		}*/
	}
}