package cz.maxtechnik.dif.init;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class DifModTabs{
    public static final DeferredRegister<CreativeModeTab>REGISTER=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,DifMod.MODID);
    public static final RegistryObject<CreativeModeTab>MAIN=REGISTER.register("main",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.main")).icon(()->new ItemStack(Blocks.STONE.asItem())).displayItems(((parameters,tabData)->{
        tabData.accept(DifModItems.CHERRY.get());
    })).build());
    public static final RegistryObject<CreativeModeTab>MUSIC=REGISTER.register("music",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.music")).icon(()->new ItemStack(DifModItems.REDSTONE.get())).withTabsBefore(MAIN.getKey()).displayItems(((parameters,tabData)->{
        tabData.accept(DifModItems.CLAIRDELUNE.get());
        tabData.accept(DifModItems.CREMEKA.get());
        tabData.accept(DifModItems.FURT_TA_STEJNA_HRA.get());
        tabData.accept(DifModItems.MATY_CREATE.get());
        tabData.accept(DifModItems.MATY_PADA_STREAM.get());
        tabData.accept(DifModItems.MAYONNAISE.get());
        tabData.accept(DifModItems.REDSTONE.get());
    })).build());

}
