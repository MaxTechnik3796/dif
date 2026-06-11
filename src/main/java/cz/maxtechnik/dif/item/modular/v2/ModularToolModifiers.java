package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
public record ModularToolModifiers(String toolType){
	public static final Codec<ModularToolModifiers> CODEC=RecordCodecBuilder.create(instance->instance.group(
			Codec.STRING.fieldOf("tool_type").forGetter(ModularToolModifiers::toolType)//,
			//Codec.list().fieldOf("list").forGetter(ModularToolModifiers::toolType)
	).apply(instance,ModularToolModifiers::new));
	public static final ModularToolModifiers DEFAULT=new ModularToolModifiers("d");
}