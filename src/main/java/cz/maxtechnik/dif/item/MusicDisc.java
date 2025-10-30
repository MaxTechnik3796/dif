package cz.maxtechnik.dif.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.registries.ForgeRegistries;

public class MusicDisc extends RecordItem{
    public MusicDisc(int comparatorValue,int lengthInTicks,String nameSpace,String path){
        super(comparatorValue,()->ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath(nameSpace,path)),new Properties().stacksTo(1).rarity(Rarity.RARE),lengthInTicks);
    }

}
