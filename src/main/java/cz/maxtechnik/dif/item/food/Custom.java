package cz.maxtechnik.dif.item.food;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
public class Custom extends Item{
	public Custom(int nutrition,float saturation){
		super(new Properties().food(new FoodProperties.Builder().nutrition(nutrition).saturationMod(saturation).alwaysEat().build()));
	}
}
