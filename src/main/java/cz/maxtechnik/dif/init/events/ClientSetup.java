package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

// Tato anotace zajistí, že se kód spustí pouze na straně klienta (hráče)
@Mod.EventBusSubscriber(modid = "dif", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // enqueueWork zajistí, že se registrace provede bezpečně v hlavním vlákně renderu
        event.enqueueWork(() -> {
            // Pro 1x1 variantu
            ItemBlockRenderTypes.setRenderLayer(DifModBlocks.CHUNK_LOADER_1X1.get(), RenderType.translucent());
            
            // Pro 3x3 variantu
            ItemBlockRenderTypes.setRenderLayer(DifModBlocks.CHUNK_LOADER_3X3.get(), RenderType.translucent());
        });
    }
}