package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ModifierEntry(String id, int lvl) {
    public static final Codec<ModifierEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(ModifierEntry::id),
            Codec.INT.fieldOf("lvl").forGetter(ModifierEntry::lvl)
    ).apply(instance, ModifierEntry::new));
}
