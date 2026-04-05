package cz.maxtechnik.dif.item;

import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
public class Update extends Item{
	public Update(){
		super(new Properties().stacksTo(1));
	}
	@Override
	public void inventoryTick(@NotNull ItemStack itemStack,@NotNull Level world,@NotNull Entity entity,int slot,boolean selected){
		itemStack=new ItemStack(DifModItems.DOG.get(),itemStack.getCount());
		if(entity instanceof Player player){
			player.getInventory().setItem(slot,itemStack);
		}
	}
}
