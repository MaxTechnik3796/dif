package cz.maxtechnik.dif.init;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.BasicBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DifModBlocks {
    public static final DeferredRegister<Block>REGISTRY=DeferredRegister.create(ForgeRegistries.BLOCKS,DifMod.MODID);
    public static final RegistryObject<Block>EXAMPLE_BLOCK=REGISTRY.register("example_block", BasicBlock::new);
}
