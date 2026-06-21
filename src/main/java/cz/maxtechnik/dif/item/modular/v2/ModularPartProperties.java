package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
public record ModularPartProperties(String partType,String material,boolean castMold){
	public static final Codec<ModularPartProperties> CODEC=RecordCodecBuilder.create(instance->instance.group(
			Codec.STRING.fieldOf("part_type").forGetter(ModularPartProperties::partType),
			Codec.STRING.fieldOf("material").forGetter(ModularPartProperties::material),
			Codec.BOOL.fieldOf("cast_mold").forGetter(ModularPartProperties::castMold)
	).apply(instance,ModularPartProperties::new));
	public static final ModularPartProperties DEFAULT=new ModularPartProperties("none","wood",false);
}