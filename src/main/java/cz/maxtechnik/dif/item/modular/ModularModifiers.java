package cz.maxtechnik.dif.item.modular;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record ModularModifiers(
		int maxModifiers,
		int efficiencyLevel,       // 0-3
		int efficiencyProgress,    // kolik modifikátorů bylo přidáno na aktuální level
		int fortuneLevel,          // 0-3
		int fortuneProgress,
		boolean silkTouch,
		boolean diamond,           // zvýší mining level na diamond tier
		boolean blazing            // auto-smelt
) {
	public static final Codec<ModularModifiers> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.INT.fieldOf("maxModifiers").forGetter(ModularModifiers::maxModifiers),
					Codec.INT.fieldOf("efficiencyLevel").forGetter(ModularModifiers::efficiencyLevel),
					Codec.INT.fieldOf("efficiencyProgress").forGetter(ModularModifiers::efficiencyProgress),
					Codec.INT.fieldOf("fortuneLevel").forGetter(ModularModifiers::fortuneLevel),
					Codec.INT.fieldOf("fortuneProgress").forGetter(ModularModifiers::fortuneProgress),
					Codec.BOOL.fieldOf("silkTouch").forGetter(ModularModifiers::silkTouch),
					Codec.BOOL.fieldOf("diamond").forGetter(ModularModifiers::diamond),
					Codec.BOOL.fieldOf("blazing").forGetter(ModularModifiers::blazing)
			).apply(instance, ModularModifiers::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModularModifiers> STREAM_CODEC =
			new StreamCodec<>() {
				@Override
				public @NotNull ModularModifiers decode(RegistryFriendlyByteBuf buf) {
					return new ModularModifiers(
							buf.readInt(),
							buf.readInt(),
							buf.readInt(),
							buf.readInt(),
							buf.readInt(),
							buf.readBoolean(),
							buf.readBoolean(),
							buf.readBoolean()
					);
				}
				@Override
				public void encode(RegistryFriendlyByteBuf buf, ModularModifiers val) {
					buf.writeInt(val.maxModifiers());
					buf.writeInt(val.efficiencyLevel());
					buf.writeInt(val.efficiencyProgress());
					buf.writeInt(val.fortuneLevel());
					buf.writeInt(val.fortuneProgress());
					buf.writeBoolean(val.silkTouch());
					buf.writeBoolean(val.diamond());
					buf.writeBoolean(val.blazing());
				}
			};

	// Výchozí stav pro nový nástroj
	public static ModularModifiers defaultModifiers(int maxModifiers) {
		return new ModularModifiers(maxModifiers, 0, 0, 0, 0, false, false, false);
	}

	public boolean hasSlots() {
		return maxModifiers > 0;
	}

	// Efektivní fortune level pro enchantment systém
	public int effectiveFortune() {
		return fortuneLevel;
	}

	// Vrátí nový record s přidaným efficiency progressem/levelem
	public ModularModifiers withEfficiencyProgress(int stage0, int stage1, int stage2) {
		int[] stages = {stage0, stage1, stage2};
		if (efficiencyLevel >= 3) return this;
		if (efficiencyProgress + 1 >= stages[efficiencyLevel]) {
			return new ModularModifiers(maxModifiers, efficiencyLevel + 1, 0,
					fortuneLevel, fortuneProgress, silkTouch, diamond, blazing);
		} else {
			int newMax = efficiencyProgress == 0 ? maxModifiers - 1 : maxModifiers;
			return new ModularModifiers(newMax, efficiencyLevel, efficiencyProgress + 1,
					fortuneLevel, fortuneProgress, silkTouch, diamond, blazing);
		}
	}

	public ModularModifiers withFortuneProgress(int stage0, int stage1, int stage2) {
		int[] stages = {stage0, stage1, stage2};
		if (fortuneLevel >= 3) return this;
		if (fortuneProgress + 1 >= stages[fortuneLevel]) {
			return new ModularModifiers(maxModifiers, efficiencyLevel, efficiencyProgress,
					fortuneLevel + 1, 0, silkTouch, diamond, blazing);
		} else {
			int newMax = fortuneProgress == 0 ? maxModifiers - 1 : maxModifiers;
			return new ModularModifiers(newMax, efficiencyLevel, efficiencyProgress,
					fortuneLevel, fortuneProgress + 1, silkTouch, diamond, blazing);
		}
	}

	public ModularModifiers withSilkTouch() {
		return new ModularModifiers(maxModifiers - 1, efficiencyLevel, efficiencyProgress,
				fortuneLevel, fortuneProgress, true, diamond, blazing);
	}

	public ModularModifiers withDiamond() {
		return new ModularModifiers(maxModifiers - 1, efficiencyLevel, efficiencyProgress,
				fortuneLevel, fortuneProgress, silkTouch, true, blazing);
	}

	public ModularModifiers withBlazing() {
		return new ModularModifiers(maxModifiers - 1, efficiencyLevel, efficiencyProgress,
				fortuneLevel, fortuneProgress, silkTouch, diamond, true);
	}
}