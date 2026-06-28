package cz.maxtechnik.dif.item.modular.v2;

import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
public class ModularTemplate extends Item{
	public ModularTemplate(Item.Properties properties){
		super(properties);
	}
	@Override
	public boolean isFoil(@NotNull ItemStack itemStack){
		return itemStack.getItem().equals(DifModItems.MODULAR_TEMPLATE_HYPER.get());
	}
}
