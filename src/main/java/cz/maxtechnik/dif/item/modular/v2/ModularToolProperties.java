package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ModularToolProperties(int maxDamage, float efficiency, float attackDamage, float attackSpeed, String toolType) {
    
    public static final Codec<ModularToolProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("max_damage").forGetter(ModularToolProperties::maxDamage),
            Codec.FLOAT.fieldOf("efficiency").forGetter(ModularToolProperties::efficiency),
            Codec.FLOAT.fieldOf("attack_damage").forGetter(ModularToolProperties::attackDamage),
            Codec.FLOAT.fieldOf("attack_speed").forGetter(ModularToolProperties::attackSpeed),
            Codec.STRING.fieldOf("tool_type").forGetter(ModularToolProperties::toolType)
    ).apply(instance, ModularToolProperties::new));

    // Výchozí hodnoty, pokud item komponentu nemá
    public static final ModularToolProperties DEFAULT = new ModularToolProperties(100, 1.0f, 1.0f, -2.4f, "none");
}