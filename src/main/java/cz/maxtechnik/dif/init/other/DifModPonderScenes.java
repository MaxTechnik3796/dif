package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.ponder.DistillationTankScenes;
import cz.maxtechnik.dif.ponder.MegaTorchScenes;
import cz.maxtechnik.dif.ponder.ModularEngine;
import cz.maxtechnik.dif.ponder.PortableEngine;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static cz.maxtechnik.dif.DifMod.MODID;
public class DifModPonderScenes implements PonderPlugin{
	@Override
	public @NotNull String getModId(){
		return MODID;
	}
	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper){
		helper.forComponents(ResourceLocation.fromNamespaceAndPath(MODID,"distillation_tank")).addStoryBoard("distillation_tank",DistillationTankScenes::intro);
		helper.forComponents(ResourceLocation.fromNamespaceAndPath(MODID,"mega_torch")).addStoryBoard("mega_torch",MegaTorchScenes::intro);
		helper.forComponents(ResourceLocation.fromNamespaceAndPath(MODID,"engine_extender")).addStoryBoard("modular_engine",ModularEngine::intro);
		helper.forComponents(ResourceLocation.fromNamespaceAndPath(MODID,"engine_base")).addStoryBoard("modular_engine",ModularEngine::intro);
		helper.forComponents(ResourceLocation.fromNamespaceAndPath(MODID,"engine_portable")).addStoryBoard("portable_engine",PortableEngine::intro);
	}
}
