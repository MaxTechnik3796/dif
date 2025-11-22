package cz.maxtechnik.dif.item;

import net.minecraft.world.item.Item;
public class StackSize extends Item{
	public StackSize(int stackSize){
		super(new Properties().stacksTo(stackSize));
	}
}
