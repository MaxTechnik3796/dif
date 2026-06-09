package cz.maxtechnik.dif.init.other; // Uprav si package podle sebe

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.modular.v2.ModularPartProperties;
import cz.maxtechnik.dif.item.modular.v2.ModularToolProperties;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
public class DifModComponents{
	public static final DeferredRegister<DataComponentType<?>> REGISTRY=DeferredRegister.create(Registries.DATA_COMPONENT_TYPE,DifMod.MODID);
	public static final DeferredHolder<DataComponentType<?>,DataComponentType<ModularToolProperties>> MODULAR_TOOL_PROPERTIES=REGISTRY.register("modular_tool_properties",()->DataComponentType.<ModularToolProperties>builder().persistent(ModularToolProperties.CODEC).build());
	public static final DeferredHolder<DataComponentType<?>,DataComponentType<ModularPartProperties>> MODULAR_PART_PROPERTIES=REGISTRY.register("modular_part_properties",()->DataComponentType.<ModularPartProperties>builder().persistent(ModularPartProperties.CODEC).build());
}