package cz.maxtechnik.dif.block.generator.steam_generator;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
public class SteamGeneratorRenderer extends KineticBlockEntityRenderer<SteamGeneratorBlockEntity>{
	public SteamGeneratorRenderer(BlockEntityRendererProvider.Context context){
		super(context);
	}
	// Žádný extra kód není potřeba – hřídel se renderuje plně automaticky díky rodičovské třídě!
}