package cz.maxtechnik.dif.item.tool;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
public class GodTotemItem extends Item{
	public GodTotemItem(Properties properties){
		super(properties.stacksTo(1).fireResistant().rarity(Rarity.EPIC));
	}
}