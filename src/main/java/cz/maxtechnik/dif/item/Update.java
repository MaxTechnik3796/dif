package cz.maxtechnik.dif.item;

import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
public class Update extends Item{
	public Update(){
		super(new Properties().stacksTo(1));
	}
	@Override
	public void inventoryTick(@NotNull ItemStack itemStack,@NotNull Level world,@NotNull Entity entity,int slot,boolean selected){
		Item item=switch(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemStack.getItem())).toString()){
			case "dif:furt_ta_stejna_hra"->ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("random","furt_ta_stejna_hra"));
			case "dif:maty_create"->ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("random","maty_create"));
			case "dif:cremeka"-> DifModItems.DOG.get();
			case "dif:redstone"->ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("random","redstone"));
			default -> throw new IllegalStateException("Unexpected value: " +Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemStack.getItem())));
		};
		assert item!=null;
		itemStack=new ItemStack(item,itemStack.getCount());
		if(entity instanceof Player player) player.getInventory().setItem(slot,itemStack);
	}
}
