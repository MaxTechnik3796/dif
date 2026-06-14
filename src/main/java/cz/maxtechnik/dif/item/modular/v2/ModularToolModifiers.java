package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.stream.Collectors;

public record ModularToolModifiers(String toolType, List<ModifierEntry> modifiers){
	
	public ModularToolModifiers {
		// Ochrana proti duplicitním modifierům (podle ID) - ponechá první nalezený
		modifiers = modifiers.stream()
				.collect(Collectors.toMap(
						ModifierEntry::id, 
						entry -> entry, 
						(existing, replacement) -> existing))
				.values().stream().toList();
	}

	public static final Codec<ModularToolModifiers> CODEC=RecordCodecBuilder.create(instance->instance.group(
			Codec.STRING.fieldOf("tool_type").forGetter(ModularToolModifiers::toolType),
			ModifierEntry.CODEC.listOf().fieldOf("modifiers").forGetter(ModularToolModifiers::modifiers)
	).apply(instance,ModularToolModifiers::new));
	public static final ModularToolModifiers DEFAULT=new ModularToolModifiers("none", List.of());
}