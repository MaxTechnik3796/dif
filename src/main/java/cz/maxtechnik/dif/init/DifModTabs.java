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
    public static final RegistryObject<CreativeModeTab>MAIN=REGISTER.register("main",()->CreativeModeTab.builder().title(Component.translatable("creative_tab.dif.main")).icon(()->new ItemStack(Blocks.STONE.asItem())).displayItems(((parameters, tabData)->{
        tabData.accept(Blocks.STONE.asItem());
        tabData.accept(DifModBlocks.EXAMPLE_BLOCK.get().asItem());
        tabData.accept(DifModItems.EXAMPLE_ITEM.get());
    })).build());
}
