package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.stream.Collectors;

public record ModularToolModifiers(List<entry> modifiers){
	public ModularToolModifiers{
		// Ochrana proti duplicitním modifierům (podle ID) - pokud by se nějak dostaly duplikáty, sečteme levely
		modifiers=modifiers.stream()
				.collect(Collectors.toMap(
						entry::id,
						e->e,
						(existing,replacement)->new entry(existing.id(),existing.lvl()+replacement.lvl())))
				.values().stream().toList();
	}
	public static final Codec<ModularToolModifiers> CODEC=RecordCodecBuilder.create(instance->instance.group(
			entry.CODEC.listOf().fieldOf("modifiers").forGetter(ModularToolModifiers::modifiers)
	).apply(instance,ModularToolModifiers::new));
	public static final ModularToolModifiers DEFAULT=new ModularToolModifiers(List.of());
	public record entry(String id,int lvl){
		public static final Codec<entry> CODEC=RecordCodecBuilder.create(instance->instance.group(
				Codec.STRING.fieldOf("id").forGetter(entry::id),
				Codec.INT.fieldOf("lvl").forGetter(entry::lvl)
		).apply(instance,entry::new));
	}
}