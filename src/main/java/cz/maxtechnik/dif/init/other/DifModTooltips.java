package cz.maxtechnik.dif.init.other;

import com.simibubi.create.foundation.item.ItemDescription;
import cz.maxtechnik.dif.DifMod;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.function.Supplier;

import static cz.maxtechnik.dif.init.basic.DifModItems.*;
@EventBusSubscriber(modid=DifMod.MODID, value=Dist.CLIENT)
public class DifModTooltips{
	private static final List<Supplier<? extends ItemLike>> CREATE_TOOLTIP_ITEMS=List.of(
			PHANTOM_RING,
			MAGNET,
			FLUID_HATCH,
			FLUID_DRAIN,
			QUARRY,
			CHUNK_LOADER
	);
	@SubscribeEvent
	public static void onItemTooltip(ItemTooltipEvent event){
		ItemStack itemStack=event.getItemStack();
		boolean isRegistered=CREATE_TOOLTIP_ITEMS.stream().anyMatch(supplier->itemStack.is(supplier.get().asItem()));
		if(!isRegistered) return;
		Item item=itemStack.getItem();
		String baseKey=ItemDescription.getTooltipTranslationKey(item);
		String requiredSummaryKey=baseKey+".summary";
		if(!I18n.exists(requiredSummaryKey)){
			DifMod.LOGGER.error("TheDifferential: Error missing translation key: {}",requiredSummaryKey);
			return;
		}
		ItemDescription description=ItemDescription.create(item,FontHelper.Palette.STANDARD_CREATE);
		if(description!=null) event.getToolTip().addAll(1,description.getCurrentLines());
	}
}