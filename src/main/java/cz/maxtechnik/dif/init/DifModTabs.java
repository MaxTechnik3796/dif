package cz.maxtechnik.dif.init;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class DifModTabs{
    public static final DeferredRegister<CreativeModeTab>REGISTER=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,DifMod.MODID);
    public static final RegistryObject<CreativeModeTab>MAIN=REGISTER.register("main",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.main")).icon(()->new ItemStack(DifModBlocks.THE_DIFFERENTIAL.get().asItem())).displayItems(((parameters,tabData)->{
		tabData.accept(DifModBlocks.THE_DIFFERENTIAL.get().asItem());
		tabData.accept(DifModBlocks.PEDROCK.get().asItem());
        tabData.accept(DifModBlocks.DEEPSLATED_ARROW.get().asItem());
        tabData.accept(DifModBlocks.STONED_ARROW.get().asItem());
        tabData.accept(DifModBlocks.WOODED_ARROW.get().asItem());
		tabData.accept(DifModBlocks.ANDESITE_LATTICE.get().asItem());
		tabData.accept(DifModBlocks.ANDESITE_WINDOW.get().asItem());
        tabData.accept(DifModItems.CPU_SINGULARITY.get());
		tabData.accept(DifModItems.HEAVY_PLATE.get());
		tabData.accept(DifModItems.TRESNOVICE.get());
		tabData.accept(DifModItems.CHERRY.get());
		tabData.accept(DifModItems.MATY_DRINK.get());
		tabData.accept(DifModItems.MATA.get());
		tabData.accept(DifModBlocks.MATA_PLANT.get().asItem());
		tabData.accept(DifModBlocks.MATY_BLOCK.get().asItem());
		tabData.accept(DifModItems.BOTTLE_OF_MOLOTOVUV_KOKTEJL.get());
		tabData.accept(DifModItems.BOTTLE_OF_URANOVEJ_KOKTEJL.get());

    })).build());
    public static final RegistryObject<CreativeModeTab>MUSIC=REGISTER.register("music",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.music")).icon(()->new ItemStack(DifModItems.REDSTONE.get())).withTabsBefore(MAIN.getKey()).displayItems(((parameters,tabData)->{
		tabData.accept(DifModItems.CREMEKA.get());
		tabData.accept(DifModItems.MATY_CREATE.get());
		tabData.accept(DifModItems.MAYONNAISE.get());
		tabData.accept(DifModItems.REDSTONE.get());
		tabData.accept(DifModItems.MATY_PADA_STREAM.get());
		tabData.accept(DifModItems.FURT_TA_STEJNA_HRA.get());
		tabData.accept(DifModItems.CLAIRDELUNE.get());
    })).build());
    public static final RegistryObject<CreativeModeTab>RANDOM=REGISTER.register("random",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.random")).icon(()->new ItemStack(DifModItems.QUESTION_MARK.get())).withTabsBefore(MUSIC.getKey()).displayItems(((parameters,tabData)->{
        tabData.accept(DifModItems.QUESTION_MARK.get());
        tabData.accept(DifModBlocks.EVENT_BUS.get().asItem());
        tabData.accept(DifModBlocks.GENERATOR.get().asItem());
		tabData.accept(DifModBlocks.HOSPITAL_HANDLE.get().asItem());
		tabData.accept(DifModBlocks.SINGULARITATOR.get().asItem());
		tabData.accept(DifModItems.MASTICKA.get());
		tabData.accept(DifModItems.BLUE_PLATE.get());


        tabData.accept(DifModItems.ROTTEN_BELT.get());
        tabData.accept(DifModItems.ROTTEN_APPLE.get());
        tabData.accept(DifModItems.INCOMPLETE_CPU_SINGULARITY.get());
    })).build());
}
