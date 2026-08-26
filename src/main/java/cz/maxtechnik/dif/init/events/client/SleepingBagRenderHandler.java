package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.SleepingBag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber(modid = DifMod.MODID, value = Dist.CLIENT)
public class SleepingBagRenderHandler {
	// Standard bed height is 9/16 (0.5625) blocks, sleeping bag is 2/16 (0.125) blocks.
	// Player model sits at -0.25D (-4/16 blocks) to rest perfectly on the sleeping bag surface.
	private static final double SLEEPING_BAG_Y_OFFSET = 2.0D / 16.0D;

	@SubscribeEvent
	public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
		LivingEntity entity = event.getEntity();
		if (entity.isSleeping()) {
			entity.getSleepingPos().ifPresent(pos -> {
				if (isSleepingBagAt(entity, pos)) {
					event.getPoseStack().pushPose();
					event.getPoseStack().translate(0.0D, SLEEPING_BAG_Y_OFFSET, 0.0D);
				}
			});
		}
	}

	@SubscribeEvent
	public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
		LivingEntity entity = event.getEntity();
		if (entity.isSleeping()) {
			entity.getSleepingPos().ifPresent(pos -> {
				if (isSleepingBagAt(entity, pos)) {
					event.getPoseStack().popPose();
				}
			});
		}
	}

	private static boolean isSleepingBagAt(LivingEntity entity, BlockPos pos) {
        BlockState state = entity.level().getBlockState(pos);
		return state.getBlock() instanceof SleepingBag;
	}
}
