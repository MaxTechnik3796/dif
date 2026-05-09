package cz.maxtechnik.dif.item.armor;

import cz.maxtechnik.dif.DifMod;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("removal")
public abstract class Jetpack extends ArmorItem {
	public Jetpack(ArmorItem.Type type, Item.Properties properties) {
		super(DifModTiers.ARMOR_MATERIAL_JETPACK, type, properties.stacksTo(1));
	}

	public static class Chestplate extends Jetpack {
		public Chestplate() {
			super(Type.CHESTPLATE, new Item.Properties().stacksTo(1));
		}

		@Override
		public ResourceLocation getArmorTexture(@NotNull ItemStack stack, @NotNull Entity entity, @NotNull EquipmentSlot slot, ArmorMaterial.@NotNull Layer layer, boolean innerModel) {
			return ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "textures/models/armor/jetpack.png");
		}

		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer) {
			consumer.accept(new IClientItemExtensions() {
				@Override
				@OnlyIn(Dist.CLIENT)
				public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity living, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> defaultModel) {
					HumanoidModel<?> armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(),
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

		// === Main storage (dlouhodobé) ===
		public static int getMain(ItemStack stack) {
			CustomData data = stack.get(DataComponents.CUSTOM_DATA);
			if (data == null || !data.copyTag().contains("Main")) return 0;
			return data.copyTag().getInt("Main");
		}
		public static void setMain(ItemStack stack, int value) {
			int max = DifModCommonConfig.jetpackMaxBasic;
			stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY,
					data -> data.update(tag -> tag.putInt("Main", Mth.clamp(value, 0, max))));
		}

		// === Thrust storage (letové) ===
		public static int getThrust(ItemStack stack) {
			CustomData data = stack.get(DataComponents.CUSTOM_DATA);
			if (data == null || !data.copyTag().contains("Thrust")) return 0;
			return data.copyTag().getInt("Thrust");
		}
		public static void setThrust(ItemStack stack, int value) {
			int max = DifModCommonConfig.jetpackMaxBasic;
			stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY,
					data -> data.update(tag -> tag.putInt("Thrust", Mth.clamp(value, 0, max))));
		}

		public static int getMax() {
			return DifModCommonConfig.jetpackMaxBasic;
		}

		public static boolean isFuel(ItemStack stack) {
			return stack.getItem().equals(DifModItems.JETPACK_FUEL.get());
		}

		public static int countFuelInInventory(net.minecraft.world.entity.player.Player player) {
			int count = 0;
			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				ItemStack s = player.getInventory().getItem(i);
				if (isFuel(s)) count += s.getCount();
			}
			return count;
		}

		// Bar ukazuje Thrust
		@Override
		public int getBarWidth(@NotNull ItemStack stack) {
			int max = getMax();
			if (max <= 0) return 0;
			return Math.round(13F * (float) getThrust(stack) / (float) max);
		}

		@Override
		public int getBarColor(@NotNull ItemStack stack) {
			float f = Math.max(0, (float) getThrust(stack) / (float) Math.max(1, getMax()));
			return Mth.hsvToRgb(f * 0.33F, 1F, 1F);
		}

		@Override
		public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext ctx, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
			int max = getMax();
			list.add(Component.literal("Main:   " + getMain(stack) + " / " + max).withStyle(ChatFormatting.YELLOW));
			list.add(Component.literal("Thrust: " + getThrust(stack) + " / " + max).withStyle(ChatFormatting.AQUA));
		}

		@Override
		public boolean isEnchantable(@NotNull ItemStack stack) { return false; }

		@Override
		public boolean isBarVisible(@NotNull ItemStack stack) { return true; }
	}
}