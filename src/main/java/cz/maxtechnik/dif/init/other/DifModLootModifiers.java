package cz.maxtechnik.dif.init.other;

import com.mojang.serialization.MapCodec;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.loot.AddMeatLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
public class DifModLootModifiers{
	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTRY=
			DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,DifMod.MODID);
	public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>,MapCodec<AddMeatLootModifier>> ADD_MEAT=
			REGISTRY.register("add_meat",()->AddMeatLootModifier.CODEC);
}