package cz.maxtechnik.dif.item.armor;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.other.DifModTiers;
import cz.maxtechnik.dif.model.ModelElectroRunners;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ElectroRunners extends ArmorItem {
	public ElectroRunners(Type type, Properties properties) {
		super(DifModTiers.ARMOR_MATERIAL_ELECTRO, type, properties);
	}

	public static class Boots extends ElectroRunners {
		public static final int MAX = 1000;
		private static final ResourceLocation S_MOD = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "electro_runners_speed");
		private static final ResourceLocation H_MOD = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "electro_runners_step");
		private static final ResourceLocation A_MOD = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "electro_runners_armor");

		public Boots() {
			super(Type.BOOTS, new Properties().stacksTo(1));
		}

		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer) {
			consumer.accept(new IClientItemExtensions() {
				@Override
				@OnlyIn(Dist.CLIENT)
				public @NotNull HumanoidModel getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
					HumanoidModel armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(),
							Map.of("left_leg", new ModelElectroRunners(Minecraft.getInstance().getEntityModels().bakeLayer(ModelElectroRunners.LAYER_LOCATION)).LeftLeg,
									"right_leg", new ModelElectroRunners(Minecraft.getInstance().getEntityModels().bakeLayer(ModelElectroRunners.LAYER_LOCATION)).RightLeg,
									"head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"body", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
					armorModel.crouching = living.isShiftKeyDown();
					armorModel.riding = defaultModel.riding;
					armorModel.young = living.isBaby();
					return armorModel;
				}
			});
		}

		@Override
		public boolean isEnchantable(@NotNull ItemStack itemStack) {
			return false;
		}

		@Override
		public int getEnchantmentValue() {
			return 0;
		}

		@Override
		public boolean isBarVisible(@NotNull ItemStack itemStack) {
			return true;
		}

		@Override
		public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
			com.google.common.collect.ImmutableMultimap.Builder<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> b = com.google.common.collect.ImmutableMultimap.builder();
			if (slot == EquipmentSlot.FEET && getEnergy(stack) > 0) {
				b.put(Attributes.ARMOR, new AttributeModifier(A_MOD, 2.0, AttributeModifier.Operation.ADD_VALUE));
				b.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(S_MOD, 0.20, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
				b.put(Attributes.STEP_HEIGHT, new AttributeModifier(H_MOD, 1.0, AttributeModifier.Operation.ADD_VALUE));
			}
			return b.build();
		}

		@Override
		public int getBarWidth(@NotNull ItemStack itemStack) {
			return Math.round(13.0F * getEnergy(itemStack) / MAX);
		}

		@Override
		public int getBarColor(@NotNull ItemStack itemStack) {
			return 0x00FFFF;
		}

		public static int getEnergy(ItemStack itemStack) {
			IEnergyStorage energy = itemStack.getCapability(Capabilities.EnergyStorage.ITEM);
			return energy != null ? energy.getEnergyStored() : 0;
		}

		public static void extract(ItemStack itemStack, int amount) {
			IEnergyStorage energy = itemStack.getCapability(Capabilities.EnergyStorage.ITEM);
			if (energy != null) energy.extractEnergy(amount, false);
		}

		@Override
		public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level world, List<Component> list, @NotNull TooltipFlag flag) {
			list.add(Component.literal(getEnergy(itemStack) + " / " + MAX + " FE").withStyle(ChatFormatting.AQUA));
		}
	}
}