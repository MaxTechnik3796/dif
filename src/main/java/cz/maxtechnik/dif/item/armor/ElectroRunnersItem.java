package cz.maxtechnik.dif.item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ElectroRunnersItem extends ArmorItem {
	public static final int MAX = 1000;
	private static final UUID S_MOD = UUID.fromString("a4e29252-1234-4567-890a-1234567890ab");
	private static final UUID H_MOD = UUID.fromString("b5f39363-1234-4567-890a-1234567890ac");
	private static final UUID A_MOD = UUID.fromString("c6d49474-1234-4567-890a-1234567890ad");
	public ElectroRunnersItem(ArmorMaterial mat, Properties props) {
		super(mat, Type.BOOTS, props.defaultDurability(0).setNoRepair());
	}
	@Override public boolean isEnchantable(ItemStack s) { return false; }
	@Override public int getEnchantmentValue() { return 0; }
	// Základní hodnota je 0, bonus se přidává dynamicky přes modifikátor níže
	@Override public int getDefense() { return 0; }
	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		ImmutableMultimap.Builder<Attribute, AttributeModifier> b = ImmutableMultimap.builder();
		if (slot == EquipmentSlot.FEET && getEnergy(stack) > 0) {
			b.putAll(super.getAttributeModifiers(slot, stack));
			b.put(Attributes.ARMOR, new AttributeModifier(A_MOD, "E-Armor", 2.0, AttributeModifier.Operation.ADDITION));
			b.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(S_MOD, "E-Speed", 0.20, AttributeModifier.Operation.MULTIPLY_TOTAL));
			b.put(ForgeMod.STEP_HEIGHT_ADDITION.get(), new AttributeModifier(H_MOD, "E-Step", 1.0, AttributeModifier.Operation.ADDITION));
		}
		return b.build();
	}
	@Override public boolean isBarVisible(ItemStack s) { return true; }
	@Override public int getBarWidth(ItemStack s) { return Math.round(13.0F * getEnergy(s) / MAX); }
	@Override public int getBarColor(ItemStack s) { return 0x00FFFF; }

	public static int getEnergy(ItemStack s) {
		return s.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
	}
	public static void extract(ItemStack s, int a) {
		s.getCapability(ForgeCapabilities.ENERGY).ifPresent(e -> e.extractEnergy(a, false));
	}
	@Override
	public void appendHoverText(ItemStack s, @Nullable Level l, List<Component> t, TooltipFlag f) {
		t.add(Component.literal(getEnergy(s) + " / " + MAX + " FE").withStyle(ChatFormatting.AQUA));
	}
	@Override
	public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new ICapabilityProvider() {
			private final LazyOptional<IEnergyStorage> h = LazyOptional.of(() -> new EnergyStorage(MAX) {
				@Override public int getEnergyStored() { return stack.getOrCreateTag().getInt("Energy"); }
				@Override public int extractEnergy(int max, boolean sim) {
					int e = getEnergyStored(), ext = Math.min(e, max);
					if (!sim) stack.getOrCreateTag().putInt("Energy", e - ext);
					return ext;
				}
				@Override public int receiveEnergy(int max, boolean sim) {
					int e = getEnergyStored(), rec = Math.min(MAX - e, max);
					if (!sim) stack.getOrCreateTag().putInt("Energy", e + rec);
					return rec;
				}
			});
			@Override public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> c, @Nullable Direction s) {
				return c == ForgeCapabilities.ENERGY ? h.cast() : LazyOptional.empty();
			}
		};
	}
}