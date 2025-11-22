package cz.maxtechnik.dif.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class MusicDiscDesc2 extends RecordItem{
	public MusicDiscDesc2(int comparatorValue,int lengthInTicks,String nameSpace,String path){
		super(comparatorValue,()->ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath(nameSpace,path)),new Properties().stacksTo(1).rarity(Rarity.RARE),lengthInTicks);
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemstack,Level level,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemstack,level,list,flag);
		list.add(Component.translatable("item.dif."+itemstack.getItem()+".desc2"));
	}
}
