package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record ModularToolModifiers(List<String> modifiers){
	public ModularToolModifiers{
		// Ochrana proti duplicitním modifierům
		modifiers=modifiers.stream()
				.distinct()
				.toList();
	}
	public static final Codec<ModularToolModifiers> CODEC=RecordCodecBuilder.create(instance->instance.group(
			Codec.STRING.listOf().fieldOf("list").forGetter(ModularToolModifiers::modifiers)
	).apply(instance,ModularToolModifiers::new));
	public static final ModularToolModifiers DEFAULT=new ModularToolModifiers(List.of());
}