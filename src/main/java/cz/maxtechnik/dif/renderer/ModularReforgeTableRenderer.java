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
	private static final float IDLE_RADIUS=0.22F;
	private static final float IDLE_Y=0.10F;
	private static final float BOB_AMPLITUDE=0.035F;
	private static final float IDLE_ORBIT_SPEED=6F;   // stupňů/tick - společná rotace kolem středu stolu v klidu
	// Rychlost rotace itemu kolem vlastní osy. Záměrně NENÍ celočíselný násobek/podíl IDLE_ORBIT_SPEED,
	// aby při 3 itemech (120° rozestup) nedocházelo k opticky "zamrzlé" synchronizaci mezi orbitem a spinem.
	private static final float IDLE_SPIN_SPEED=4.7F;  // stupňů/tick - rotace itemu kolem vlastní osy v klidu
	private static final float ITEM_TILT_DEGREES=70F; // náklon místo plných 90° (stání místo ležení)
	private static final int FORCED_LIGHT=LightTexture.pack(14,14); // vynucené jasné nasvícení itemů
	// Od jaké hodnoty progressu (0-1) začne item mizet (scale na 0), aby "zmizel" přesně v okamžiku craftění.
	private static final float VANISH_START_PROGRESS=0.85F;
	// Základní (klidová) škála itemů - zvětšeno oproti původním 0.5F.
	private static final float BASE_SCALE=0.85F;

	public ModularReforgeTableRenderer(){
	}

	@Override
	public void render(ModularReforgeTableBlockEntity blockEntity,float partialTick,@NotNull PoseStack poseStack,@NotNull MultiBufferSource buffer,int combinedLight,int combinedOverlay){
		ItemRenderer itemRenderer=Minecraft.getInstance().getItemRenderer();

		List<Integer> activeSlots=new ArrayList<>();
		for(int i=0;i<ModularReforgeTableBlockEntity.SLOT_COUNT;i++){
			if(!blockEntity.getInventory().getStackInSlot(i).isEmpty()) activeSlots.add(i);
		}
		int count=activeSlots.size();
		if(count==0) return;

		boolean merging=blockEntity.isMerging();
		float progress=merging?Mth.clamp((blockEntity.getMergeProgress()+partialTick)/ModularReforgeTableBlockEntity.ANIMATION_DURATION,0F,1F):0F;
		// Ease-in na POZICI (let do středu zrychluje).
		float flightProgress=easeInCubic(progress);

		// Celková doba od začátku renderu (world time + partialTick) - používá se pro klidovou rotaci,
		// orbit a bobbing KDYŽ NEBĚŽÍ MERGE. Během mergu se čas pro tyto účely nepoužívá - vše stojí/letí rovně.
		float time=blockEntity.getLevel()!=null?(blockEntity.getLevel().getGameTime()%100000)+partialTick:partialTick;

		// Společný orbitální úhel - itemy krouží kolem středu stolu jako "kolo".
		// IDLE_ORBIT_SPEED je konstantní a NEZÁVISÍ na počtu itemů (count) - se 2 i se 3 itemy
		// se celá formace otáčí stejnou úhlovou rychlostí, jen je mezi itemy jiný úhlový rozestup.
		// Během mergu se orbit úplně zastaví (item letí přímou dráhou, nekrouží).
		float orbitAngle=merging?0F:(time*IDLE_ORBIT_SPEED)%360F;

		// Spin úhel - společný pro všechny itemy, počítaný nezávisle na slotu/indexu,
		// aby rotace kolem vlastní osy byla vždy viditelná bez ohledu na počet aktivních slotů.
		float spinAngle=merging?0F:(time*IDLE_SPIN_SPEED)%360F;

		for(int index=0;index<count;index++){
			int slot=activeSlots.get(index);
			ItemStack stack=blockEntity.getInventory().getStackInSlot(slot);

			// Pro jediný item žádný úhel/offset - má být přesně v bodě orbitu (střed).
			float idleX,idleZ;
			if(count==1){
				idleX=0F;
				idleZ=0F;
			}else{
				float slotAngleDeg=orbitAngle+(360F/count)*index;
				float slotAngleRad=slotAngleDeg*Mth.DEG_TO_RAD;
				idleX=Mth.sin(slotAngleRad)*IDLE_RADIUS;
				idleZ=Mth.cos(slotAngleRad)*IDLE_RADIUS;
			}

			// Bobbing - jen v klidu (ne během mergu), aby let do středu byl čistá přímka.
			float bobOffset=0F;
			if(!merging){
				float bobPhase=time*0.08F+slot*2.094F; // rozfázováno o 120°
				bobOffset=Mth.sin(bobPhase)*BOB_AMPLITUDE;
			}

			// Přímý let: interpolace z klidové pozice (idleX,IDLE_Y,idleZ) do středu (0,IDLE_Y,0).
			float x=Mth.lerp(flightProgress,idleX,0F);
			float y=IDLE_Y+bobOffset;
			float z=Mth.lerp(flightProgress,idleZ,0F);

			// Škála: v klidu plná (BASE_SCALE), na konci letu (těsně před craftěním) rychle zmizí na 0.
			float scale;
			if(progress<VANISH_START_PROGRESS){
				scale=BASE_SCALE;
			}else{
				float vanishT=(progress-VANISH_START_PROGRESS)/(1F-VANISH_START_PROGRESS);
				scale=BASE_SCALE*(1F-vanishT);
			}
			if(scale<=0F) continue; // item už zmizel, nemá smysl ho renderovat

			poseStack.pushPose();
			poseStack.translate(0.5D+x,1.0D+y,0.5D+z);

			// Rotace kolem vlastní osy - POUZE v klidu. Během mergu žádná rotace, jen přímý let do středu.
			poseStack.mulPose(Axis.YP.rotationDegrees(spinAngle));
			// Náklon - místo plných 90° (item naplocho) použijeme ITEM_TILT_DEGREES, aby item spíš "stál" nakloněný.
			poseStack.mulPose(Axis.XP.rotationDegrees(ITEM_TILT_DEGREES));

			poseStack.scale(scale,scale,scale);

			// GROUND kontext = vzhled jako u dropnutého ItemEntity ve světě.
			// Vynucené jasné nasvícení, aby item nebyl tmavý bez ohledu na okolní světlo bloku.
			itemRenderer.renderStatic(stack,ItemDisplayContext.GROUND,FORCED_LIGHT,combinedOverlay,poseStack,buffer,null,0);
			poseStack.popPose();
		}
	}

	private static float easeInCubic(float t){
		return t*t*t;
	}
}