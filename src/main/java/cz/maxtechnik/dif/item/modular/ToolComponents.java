package cz.maxtechnik.dif.item.modular;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ToolComponents {
	public static final DeferredRegister<DataComponentType<?>> REGISTER =
			DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, DifMod.MODID);

	// Hlavní data nástroje — materiály všech tří částí
	public static final Supplier<DataComponentType<ModularToolData>> TOOL_DATA =
			REGISTER.register("tool_data", () ->
					DataComponentType.<ModularToolData>builder()
							.persistent(ModularToolData.CODEC)
							.networkSynchronized(ModularToolData.STREAM_CODEC)
							.build());

	// Modifikátory nástroje
	public static final Supplier<DataComponentType<ModularModifiers>> MODIFIERS =
			REGISTER.register("modifiers", () ->
					DataComponentType.<ModularModifiers>builder()
							.persistent(ModularModifiers.CODEC)
							.networkSynchronized(ModularModifiers.STREAM_CODEC)
							.build());

	// Broken stav — samostatná komponenta pro jednoduchost
	public static final Supplier<DataComponentType<Boolean>> BROKEN =
			REGISTER.register("broken", () ->
					DataComponentType.<Boolean>builder()
							.persistent(com.mojang.serialization.Codec.BOOL)
							.networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL)
							.build());
}