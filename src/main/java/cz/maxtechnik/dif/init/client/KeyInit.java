package cz.maxtechnik.dif.init.client;

import cz.maxtechnik.dif.DifMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = DifMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyInit {
	public static final KeyMapping JETPACK_FLY = new KeyMapping(
			"key.dif.jetpack_fly",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_SPACE, // Defaultní klávesa: Mezerník
			"key.categories.dif"
	);

	@SubscribeEvent
	public static void registerKeys(RegisterKeyMappingsEvent event) {
		event.register(JETPACK_FLY);
	}
}