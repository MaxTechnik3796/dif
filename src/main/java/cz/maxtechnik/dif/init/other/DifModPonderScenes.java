package cz.maxtechnik.dif.init.other;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.ponder.DistillationTankScenes;
import cz.maxtechnik.dif.ponder.MegaTorchScenes;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
public class DifModPonderScenes implements PonderPlugin{
	@Override
	public @NotNull String getModId(){
		return DifMod.MODID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper){
		helper.forComponents(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"distillation_tank")).addStoryBoard("distillation_tank",DistillationTankScenes::intro);
		helper.forComponents(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"mega_torch")).addStoryBoard("mega_torch",MegaTorchScenes::intro);
	}
}
