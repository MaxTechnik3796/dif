package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
public record ModularPartProperties(String partType,String material){
	public static final Codec<ModularPartProperties> CODEC=RecordCodecBuilder.create(instance->instance.group(
			Codec.STRING.fieldOf("part_type").forGetter(ModularPartProperties::partType),
			Codec.STRING.fieldOf("material").forGetter(ModularPartProperties::material)
	).apply(instance,ModularPartProperties::new));
	public static final ModularPartProperties DEFAULT=new ModularPartProperties("none","wood");
}