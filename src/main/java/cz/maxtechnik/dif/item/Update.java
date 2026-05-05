package cz.maxtechnik.dif.item;

import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
public class Update extends Item{
	public Update(){
		super(new Item.Properties().stacksTo(1));
	}
	@Override
	public void inventoryTick(@NotNull ItemStack itemStack,@NotNull Level world,@NotNull Entity entity,int slot,boolean selected){
		Item item=switch(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(itemStack.getItem())).toString()){
			case "dif:furt_ta_stejna_hra" ->
					BuiltInRegistries.ITEM.get(ResourceLocation.parse("random:furt_ta_stejna_hra"));
			case "dif:maty_create" -> BuiltInRegistries.ITEM.get(ResourceLocation.parse("random:maty_create"));
			case "dif:cremeka" -> DifModItems.DOG.get();
			case "dif:redstone" -> BuiltInRegistries.ITEM.get(ResourceLocation.parse("random:redstone"));
			default ->
					throw new IllegalStateException("Unexpected value: "+Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(itemStack.getItem())));
		};
		itemStack=new ItemStack(item,itemStack.getCount());
		if(entity instanceof Player player) player.getInventory().setItem(slot,itemStack);
	}
}
