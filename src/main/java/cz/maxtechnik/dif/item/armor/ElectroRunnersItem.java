package cz.maxtechnik.dif.item.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ElectroRunnersItem extends ArmorItem {
	public static final int MAX_ENERGY = 1000;
	private static final UUID SPEED_MOD = UUID.fromString("a4e29252-1234-4567-890a-1234567890ab");
	private static final UUID STEP_MOD = UUID.fromString("b5f39363-1234-4567-890a-1234567890ac");

	public ElectroRunnersItem(ArmorMaterial material, Properties properties) {
		// Durability 0 a setNoRepair pro technologický charakter
		super(material, Type.BOOTS, properties.defaultDurability(0).setNoRepair());
	}

	// Zakázání enchantů
	@Override
	public boolean isEnchantable(ItemStack stack) { return false; }

	@Override
	public int getEnchantmentValue() { return 0; }

	// Atributy: Rychlost +20% a plynulé vycházení bloků
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.putAll(super.getAttributeModifiers(slot, stack));

		if (slot == EquipmentSlot.FEET && getEnergyStored(stack) > 0) {
			builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(SPEED_MOD, "Electro Speed", 0.20, AttributeModifier.Operation.MULTIPLY_TOTAL));
			builder.put(ForgeMod.STEP_HEIGHT_ADDITION.get(), new AttributeModifier(STEP_MOD, "Electro Step", 1.0, AttributeModifier.Operation.ADDITION));
		}
		return builder.build();
	}

	// Energy Bar (ukazatel energie místo durability)
	@Override
	public boolean isBarVisible(ItemStack stack) { return true; }
	@Override
	public int getBarWidth(ItemStack stack) { return Math.round(13.0F * (float)getEnergyStored(stack) / MAX_ENERGY); }
	@Override
	public int getBarColor(ItemStack stack) { return 0x00FFFF; }

	public static int getEnergyStored(ItemStack stack) {
		return stack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
	}

	public static void extractEnergy(ItemStack stack, int amount) {
		stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(e -> e.extractEnergy(amount, false));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.literal("Energie: " + getEnergyStored(stack) + " / " + MAX_ENERGY + " FE").withStyle(ChatFormatting.AQUA));
		tooltip.add(Component.literal("Nelze očarovat").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC));
	}

	@Override
	public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new ElectroEnergyProvider(stack);
	}

	private static class ElectroEnergyProvider implements ICapabilityProvider {
		private final ItemStack stack;
		private final LazyOptional<IEnergyStorage> holder;

		public ElectroEnergyProvider(ItemStack stack) {
			this.stack = stack;
			IEnergyStorage storage = new EnergyStorage(MAX_ENERGY, 1000, 1000) {
				@Override public int getEnergyStored() { return stack.getOrCreateTag().getInt("Energy"); }
				@Override public int extractEnergy(int max, boolean sim) {
					int energy = getEnergyStored();
					int ext = Math.min(energy, max);
					if (!sim) stack.getOrCreateTag().putInt("Energy", energy - ext);
					return ext;
				}
				@Override public int receiveEnergy(int max, boolean sim) {
					int energy = getEnergyStored();
					int rec = Math.min(MAX_ENERGY - energy, max);
					if (!sim) stack.getOrCreateTag().putInt("Energy", energy + rec);
					return rec;
				}
				@Override public int getMaxEnergyStored() { return MAX_ENERGY; }
				@Override public boolean canExtract() { return true; }
				@Override public boolean canReceive() { return true; }
			};
			this.holder = LazyOptional.of(() -> storage);
		}

		@Override
		public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
			return cap == ForgeCapabilities.ENERGY ? holder.cast() : LazyOptional.empty();
		}
	}
}