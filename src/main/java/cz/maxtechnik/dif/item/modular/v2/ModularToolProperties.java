package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
public record ModularToolProperties(String toolType,String headMaterial,String bindingMaterial,String handleMaterial,String reforge){
	public static final Codec<ModularToolProperties> CODEC=RecordCodecBuilder.create(instance->instance.group(Codec.STRING.fieldOf("tool_type").forGetter(ModularToolProperties::toolType),Codec.STRING.fieldOf("head_material").forGetter(ModularToolProperties::headMaterial),Codec.STRING.fieldOf("binding_material").forGetter(ModularToolProperties::bindingMaterial),Codec.STRING.fieldOf("handle_material").forGetter(ModularToolProperties::handleMaterial),Codec.STRING.fieldOf("reforge").forGetter(ModularToolProperties::reforge)).apply(instance,ModularToolProperties::new));
	public static final ModularToolProperties DEFAULT=new ModularToolProperties(ModularTools.NONE.getName(),ModularMaterial.NONE.getName(),ModularMaterial.NONE.getName(),ModularMaterial.NONE.getName(),ModularReforge.NONE.getName());
}