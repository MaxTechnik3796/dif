package cz.maxtechnik.dif.item.armor;

import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.other.DifModTiers;
import cz.maxtechnik.dif.model.ModelJetpack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Jetpack extends ArmorItem {
	public Jetpack(ArmorItem.Type type, Item.Properties properties) {
		super(DifModTiers.ARMOR_MATERIAL_JETPACK, type, properties);
	}

	public static class Chestplate extends Jetpack {
		public Chestplate() {
			super(Type.CHESTPLATE, new Item.Properties().stacksTo(1));
		}

		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer) {
			consumer.accept(new IClientItemExtensions() {
				@Override
				@OnlyIn(Dist.CLIENT)
				public @NotNull HumanoidModel getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
					HumanoidModel armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(),
							Map.of("body", new ModelJetpack(Minecraft.getInstance().getEntityModels().bakeLayer(ModelJetpack.LAYER_LOCATION)).Body,
									"left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
					armorModel.crouching = living.isShiftKeyDown();
					armorModel.riding = defaultModel.riding;
					armorModel.young = living.isBaby();
					return armorModel;
				}
			});
		}

		public static int getMainFuel(ItemStack itemStack) {
			CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
			if (data == null || !data.copyTag().contains("MainFuel")) return 0;
			return data.copyTag().getInt("MainFuel");
		}

		public static void setMainFuel(ItemStack itemStack, int value) {
			itemStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putInt("MainFuel", value)));
		}

		public static int getThrustFuel(ItemStack itemStack) {
			CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
			if (data == null || !data.copyTag().contains("ThrustFuel")) return 0;
			return data.copyTag().getInt("ThrustFuel");
		}

		public static void setThrustFuel(ItemStack itemStack, int value) {
			itemStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putInt("ThrustFuel", value)));
		}

		public static boolean getTurbo(ItemStack itemStack) {
			CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
			if (data == null || !data.copyTag().contains("Turbo")) return false;
			return data.copyTag().getBoolean("Turbo");
		}

		public static void setTurbo(ItemStack itemStack, boolean value) {
			itemStack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putInt("Turbo", value ? 1 : 0)));
		}

		@Override
		public int getBarWidth(@NotNull ItemStack itemStack) {
			return Math.round(13F * (float) getMainFuel(itemStack) / (float) DifModCommonConfig.jetpackMaxBasic);
		}

		@Override
		public int getBarColor(@NotNull ItemStack itemStack) {
			float f = Math.max(0, (float) getMainFuel(itemStack) / (float) DifModCommonConfig.jetpackMaxBasic);
			return Mth.hsvToRgb(f * 0.33F, 1F, 1F);
		}

		@Override
		public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level world, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
			list.add(Component.literal("Main Storage: " + getMainFuel(itemStack) + " / " + DifModCommonConfig.jetpackMaxBasic).withStyle(ChatFormatting.GRAY));
			list.add(Component.literal("Thrust Tank: " + getThrustFuel(itemStack) + " / " + (getTurbo(itemStack) ? DifModCommonConfig.jetpackMaxTurbo : DifModCommonConfig.jetpackMaxThrust)).withStyle(ChatFormatting.AQUA));
			if (getTurbo(itemStack)) list.add(Component.literal("TURBO").withStyle(ChatFormatting.RED));
		}

		public static boolean isFuel(ItemStack itemStack) {
			return itemStack.getItem().equals(DifModItems.JETPACK_FUEL.get());
		}

		public static boolean isTurboFuel(ItemStack itemStack) {
			return itemStack.getItem().equals(DifModItems.JETPACK_TURBO_FUEL.get());
		}

		@Override
		public boolean isEnchantable(@NotNull ItemStack itemStack) {
			return false;
		}

		@Override
		public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
			return slotChanged || oldStack.getItem() != newStack.getItem();
		}

		@Override
		public boolean isBarVisible(@NotNull ItemStack itemStack) {
			return true;
		}
	}
}