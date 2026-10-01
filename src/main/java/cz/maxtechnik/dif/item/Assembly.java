package cz.maxtechnik.dif.item;

import com.simibubi.create.AllDataComponents;
import net.createmod.catnip.theme.Color;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
public class Assembly extends Item{
	public Assembly(Item.Properties properties){
		super(properties);
	}
	public float getProgress(ItemStack itemStack){
		return !itemStack.has(AllDataComponents.SEQUENCED_ASSEMBLY)?0F:Objects.requireNonNull(itemStack.get(AllDataComponents.SEQUENCED_ASSEMBLY)).progress();
	}
	public boolean isBarVisible(@NotNull ItemStack itemStack){
		return true;
	}
	public int getBarWidth(@NotNull ItemStack itemStack){
		return Math.round(this.getProgress(itemStack)*13.0F);
	}
	public int getBarColor(@NotNull ItemStack itemStack){
		return Color.mixColors(-16268,-12124192,this.getProgress(itemStack));
	}
}