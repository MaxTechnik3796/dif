package cz.maxtechnik.dif.init.other;

import com.mojang.serialization.Codec;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DifModComponents {
    public static final DeferredRegister<DataComponentType<?>> REGISTRY =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, DifMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> MAGNET_ENABLED =
            REGISTRY.register("magnet_enabled", () ->
                    DataComponentType.<Boolean>builder()
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
                            .build());
}