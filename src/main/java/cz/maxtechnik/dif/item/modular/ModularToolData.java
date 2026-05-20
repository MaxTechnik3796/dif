package cz.maxtechnik.dif.item.modular;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ModularToolData(
		String headMaterial,
		String bindingMaterial,
		String handleMaterial,
		int headDurability,
		int bindingDurability,
		int handleDurability
) {
	public static final Codec<ModularToolData> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.fieldOf("headMaterial").forGetter(ModularToolData::headMaterial),
					Codec.STRING.fieldOf("bindingMaterial").forGetter(ModularToolData::bindingMaterial),
					Codec.STRING.fieldOf("handleMaterial").forGetter(ModularToolData::handleMaterial),
					Codec.INT.fieldOf("headDurability").forGetter(ModularToolData::headDurability),
					Codec.INT.fieldOf("bindingDurability").forGetter(ModularToolData::bindingDurability),
					Codec.INT.fieldOf("handleDurability").forGetter(ModularToolData::handleDurability)
			).apply(instance, ModularToolData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ModularToolData> STREAM_CODEC =
			StreamCodec.composite(
					ByteBufCodecs.STRING_UTF8, ModularToolData::headMaterial,
					ByteBufCodecs.STRING_UTF8, ModularToolData::bindingMaterial,
					ByteBufCodecs.STRING_UTF8, ModularToolData::handleMaterial,
					ByteBufCodecs.INT, ModularToolData::headDurability,
					ByteBufCodecs.INT, ModularToolData::bindingDurability,
					ByteBufCodecs.INT, ModularToolData::handleDurability,
					ModularToolData::new);

	// Celková durabilita = součet všech tří částí + 1
	public int totalDurability() {
		return headDurability + bindingDurability + handleDurability + 1;
	}

	public ToolMaterial head() {
		return ToolMaterial.fromName(headMaterial);
	}

	public ToolMaterial binding() {
		return ToolMaterial.fromName(bindingMaterial);
	}

	public ToolMaterial handle() {
		return ToolMaterial.fromName(handleMaterial);
	}

	public boolean containsMaterial(ToolMaterial material) {
		return head() == material || binding() == material || handle() == material;
	}

	// Vytvoří nový ModularToolData ze tří materiálů
	public static ModularToolData of(ToolMaterial head, ToolMaterial binding, ToolMaterial handle) {
		return new ModularToolData(
				head.name,
				binding.name,
				handle.name,
				head.headDurability(),
				binding.bindingDurability(),
				handle.handleDurability()
		);
	}

	// Výměna hlavy
	public ModularToolData withHead(ToolMaterial newHead) {
		return new ModularToolData(newHead.name, bindingMaterial, handleMaterial,
				newHead.headDurability(), bindingDurability, handleDurability);
	}

	// Výměna bindingu
	public ModularToolData withBinding(ToolMaterial newBinding) {
		return new ModularToolData(headMaterial, newBinding.name, handleMaterial,
				headDurability, newBinding.bindingDurability(), handleDurability);
	}

	// Výměna handle
	public ModularToolData withHandle(ToolMaterial newHandle) {
		return new ModularToolData(headMaterial, bindingMaterial, newHandle.name,
				headDurability, bindingDurability, newHandle.handleDurability());
	}
}