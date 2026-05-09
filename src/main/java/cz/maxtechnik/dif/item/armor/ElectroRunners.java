package cz.maxtechnik.dif.item.armor;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.other.DifModTiers;
import cz.maxtechnik.dif.model.ModelElectroRunners;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("removal")
public abstract class ElectroRunners extends ArmorItem {
	public ElectroRunners(Type type, Properties properties) {
		super(DifModTiers.ARMOR_MATERIAL_ELECTRO, type, properties.stacksTo(1));
	}

	public static class Boots extends ElectroRunners {
		public static final int MAX = 1000;
		private static final ResourceLocation S_MOD = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "electro_runners_speed");
		private static final ResourceLocation H_MOD = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "electro_runners_step");
		private static final ResourceLocation A_MOD = ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "electro_runners_armor");

		public Boots() {
			super(Type.BOOTS, new Properties().stacksTo(1));
		}

		// ── Energie: čtení a zápis přes CustomData NBT ──────────────────────

		public static int getEnergy(ItemStack stack) {
			CustomData data = stack.get(DataComponents.CUSTOM_DATA);
			if (data == null) return 0;
			return data.copyTag().getInt("Energy");
		}

		public static void setEnergy(ItemStack stack, int amount) {
			int clamped = Math.max(0, Math.min(MAX, amount));
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			tag.putInt("Energy", clamped);
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}

		public static void extract(ItemStack stack, int amount) {
			setEnergy(stack, getEnergy(stack) - amount);
		}

		// ── Capability registrace — volej v RegisterCapabilitiesEvent ────────
		// Přidej do svého capability event handleru:
		//
		//   ElectroRunners.Boots.registerCapability(event, DifItems.ELECTRO_RUNNERS_BOOTS.get());
		//
		public static void registerCapability(RegisterCapabilitiesEvent event, Item item) {
			event.registerItem(
					Capabilities.EnergyStorage.ITEM,
					(stack, ctx) -> new IEnergyStorage() {
						@Override
						public int receiveEnergy(int maxReceive, boolean simulate) {
							int stored = getEnergy(stack);
							int toReceive = Math.min(maxReceive, MAX - stored);
							if (!simulate && toReceive > 0) {
								setEnergy(stack, stored + toReceive);
							}
							return toReceive;
						}

						@Override
						public int extractEnergy(int maxExtract, boolean simulate) {
							int stored = getEnergy(stack);
							int toExtract = Math.min(maxExtract, stored);
							if (!simulate && toExtract > 0) {
								setEnergy(stack, stored - toExtract);
							}
							return toExtract;
						}

						@Override
						public int getEnergyStored() {
							return getEnergy(stack);
						}

						@Override
						public int getMaxEnergyStored() {
							return MAX;
						}

						@Override
						public boolean canExtract() {
							return false; // tesla může jen nabíjet, ne vybíjet přes capability
						}

						@Override
						public boolean canReceive() {
							return true;
						}
					},
					item
			);
		}

		// ── Render ───────────────────────────────────────────────────────────

		@Override
		public ResourceLocation getArmorTexture(@NotNull ItemStack stack, @NotNull Entity entity, @NotNull EquipmentSlot slot, ArmorMaterial.@NotNull Layer layer, boolean innerModel) {
			return ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/models/armor/electro_runners.png");
		}
		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer) {
			consumer.accept(new IClientItemExtensions() {
				@Override
				@OnlyIn(Dist.CLIENT)
				public @NotNull HumanoidModel<?> getHumanoidArmorModel(
						@NotNull LivingEntity living, @NotNull ItemStack stack,
						@NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> defaultModel) {
					HumanoidModel<?> armorModel = new HumanoidModel<>(new ModelPart(
							Collections.emptyList(),
							Map.of(
									"left_leg", new ModelElectroRunners(Minecraft.getInstance()
											.getEntityModels().bakeLayer(ModelElectroRunners.LAYER_LOCATION)).LeftLeg,
									"right_leg", new ModelElectroRunners(Minecraft.getInstance()
											.getEntityModels().bakeLayer(ModelElectroRunners.LAYER_LOCATION)).RightLeg,
									"head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"body", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap())
							)));
					armorModel.crouching = living.isShiftKeyDown();
					armorModel.riding = defaultModel.riding;
					armorModel.young = living.isBaby();
					return armorModel;
				}
			});
		}

		// ── Item vlastnosti ──────────────────────────────────────────────────

		@Override
		public boolean isEnchantable(@NotNull ItemStack stack) { return false; }

		@Override
		public int getEnchantmentValue() { return 0; }

		@Override
		public boolean isBarVisible(@NotNull ItemStack stack) { return true; }

		@Override
		public int getBarWidth(@NotNull ItemStack stack) {
			return Math.round(13F * getEnergy(stack) / MAX);
		}

		@Override
		public int getBarColor(@NotNull ItemStack stack) { return 0x00FFFF; }

		@Override
		public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers() {
			return ItemAttributeModifiers.builder()
					.add(Attributes.ARMOR,
							new AttributeModifier(A_MOD, 2, AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.bySlot(EquipmentSlot.FEET))
					.add(Attributes.MOVEMENT_SPEED,
							new AttributeModifier(S_MOD, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
							EquipmentSlotGroup.bySlot(EquipmentSlot.FEET))
					.add(Attributes.STEP_HEIGHT,
							new AttributeModifier(H_MOD, 1, AttributeModifier.Operation.ADD_VALUE),
							EquipmentSlotGroup.bySlot(EquipmentSlot.FEET))
					.build();
		}

		@Override
		public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext ctx,
		                            @NotNull List<Component> list, @NotNull TooltipFlag flag) {
			list.add(Component.literal(getEnergy(stack) + " / " + MAX + " FE")
					.withStyle(ChatFormatting.AQUA));
		}
	}
}