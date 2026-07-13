package cz.maxtechnik.dif.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.dif.block.entity.ModularReforgeTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ModularReforgeTableRenderer implements BlockEntityRenderer<ModularReforgeTableBlockEntity>{
	/**
	 * Poloměr klidové orbity kolem středu stolu (na XZ rovině) a výška nad blokem.
	 * Skutečné pozice jednotlivých itemů se dopočítávají podle počtu aktivních slotů,
	 * takže 1 item = střed, 2 = naproti sobě, 3 = trojúhelník po 120°.
	 */
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
	public void render(@NotNull ModularReforgeTableBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int combinedLight, int combinedOverlay){
		ItemRenderer itemRenderer=Minecraft.getInstance().getItemRenderer();

		List<Integer> activeSlots=new ArrayList<>();
		for(int i=0;i<ModularReforgeTableBlockEntity.SLOT_COUNT;i++){
			if(!blockEntity.getInventory().getStackInSlot(i).isEmpty()) activeSlots.add(i);
		}
		int count=activeSlots.size();
		if(count==0) return;

		float progress=blockEntity.isMerging()?Mth.clamp((blockEntity.getMergeProgress()+partialTick)/ModularReforgeTableBlockEntity.ANIMATION_DURATION,0F,1F):0F;
		float linearProgress=progress;
		float radialProgress=1F-(1F-linearProgress)*(1F-linearProgress);

		// Celková doba od začátku renderu (world time + partialTick) - používá se pro klidovou rotaci,
		// orbit a bobbing, aby to plynule běželo i bez ohledu na merge.
		float time=blockEntity.getLevel()!=null?(blockEntity.getLevel().getGameTime()%100000)+partialTick:partialTick;

		// Společný orbitální úhel - všechny itemy krouží kolem středu stolu jako "kolo".
		// Při merge se orbitální rychlost zvyšuje od začátku a itemy se už od prvních ticků stáčejí ke středu.
		float orbitSpeed=IDLE_ORBIT_SPEED + IDLE_ORBIT_SPEED*2F*linearProgress;
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
			float spinSpeed=IDLE_SPIN_SPEED;
			float spinAngle=(time*spinSpeed)%360F;
			poseStack.mulPose(Axis.YP.rotationDegrees(spinAngle));
			// Náklon - místo plných 90° (item naplocho) použijeme ITEM_TILT_DEGREES, aby item spíš "stál" nakloněný.
			poseStack.mulPose(Axis.XP.rotationDegrees(ITEM_TILT_DEGREES));

			float scale=0.5F*(1F-Math.min(0.55F,progress*0.55F));
			poseStack.scale(scale,scale,scale);

			// Vynucené jasné nasvícení, aby item nebyl tmavý bez ohledu na okolní světlo bloku.
			itemRenderer.renderStatic(stack,ItemDisplayContext.FIXED,FORCED_LIGHT,combinedOverlay,poseStack,buffer,null,0);
			poseStack.popPose();
		}
	}

	private static float easeInCubic(float t){
		return t*t*t;
	}
}