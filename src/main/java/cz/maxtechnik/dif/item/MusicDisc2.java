package cz.maxtechnik.dif.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraftforge.registries.ForgeRegistries;

public class MusicDisc2 extends RecordItem{
    public MusicDisc2(){
        super(5,()->ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath("dif","clairdelune")),new Properties().stacksTo(1).rarity(Rarity.RARE),6320);
    }

}
