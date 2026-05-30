package cz.maxtechnik.dif.init.other; // Uprav si package podle sebe

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.modular.v2.ModularToolProperties;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
public class DifModComponents{
	public static final DeferredRegister<DataComponentType<?>> REGISTRY=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,DifMod.MODID);
	public static final DeferredHolder<DataComponentType<?>,DataComponentType<ModularToolProperties>> MODULAR_PROPERTIES=REGISTRY.register("modular_properties",()->DataComponentType.<ModularToolProperties>builder().persistent(ModularToolProperties.CODEC).build());
}