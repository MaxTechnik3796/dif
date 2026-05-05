package cz.maxtechnik.dif.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Properties;
public class MusicDiscDesc2 extends RecordItem{
	public MusicDiscDesc2(int comparatorValue,int lengthInTicks,String nameSpace,String path){
		super(comparatorValue,()->ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath(nameSpace,path)),new Item.Properties().stacksTo(1).rarity(Rarity.RARE),lengthInTicks);
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack itemStack,Item.TooltipContext context,List<Component> list,TooltipFlag flag){
		super.appendHoverText(itemStack,context,list,flag);
		list.add(Component.translatable("item.dif."+itemStack.getItem()+".desc2"));
	}
}
