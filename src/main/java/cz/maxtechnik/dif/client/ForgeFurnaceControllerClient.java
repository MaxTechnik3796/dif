package cz.maxtechnik.dif.client;

import cz.maxtechnik.dif.block.entity.ForgeControllerBlockEntity;
import cz.maxtechnik.dif.gui.screen.ForgeRadialScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ForgeFurnaceControllerClient {
    public static void openForgeRadialScreen(ForgeControllerBlockEntity be) {
        Minecraft.getInstance().setScreen(new ForgeRadialScreen(be));
    }
}
