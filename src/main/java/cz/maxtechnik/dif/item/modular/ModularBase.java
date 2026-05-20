package cz.maxtechnik.dif.item.modular;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import static cz.maxtechnik.dif.DifModCommonConfig.*;

public abstract class ModularBase extends Item {
	protected abstract TagKey<Block> getMineableTag();
	protected abstract float baseAttackDamage(ToolMaterial material);
	protected abstract float baseAttackSpeed(ToolMaterial material);

	private static final ResourceLocation BASE_ATTACK_DAMAGE_ID =
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "modular_attack_damage");
	private static final ResourceLocation BASE_ATTACK_SPEED_ID =
			ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "modular_attack_speed");

	public ModularBase() {
		super(new Properties()
				.stacksTo(1)
				.fireResistant()
				.rarity(Rarity.EPIC)
				.component(ToolComponents.TOOL_DATA.get(),
						ModularToolData.of(ToolMaterial.WOOD, ToolMaterial.WOOD, ToolMaterial.WOOD))
				.component(ToolComponents.MODIFIERS.get(),
						ModularModifiers.defaultModifiers(3))
				.component(ToolComponents.BROKEN.get(), false));
	}

	// -----------------------------------------------------------------------
	// Gettery komponent
	// -----------------------------------------------------------------------
	public static ModularToolData getToolData(ItemStack stack) {
		ModularToolData data = stack.get(ToolComponents.TOOL_DATA.get());
		return data != null ? data : ModularToolData.of(ToolMaterial.WOOD, ToolMaterial.WOOD, ToolMaterial.WOOD);
	}

	public static ModularModifiers getModifiers(ItemStack stack) {
		ModularModifiers mods = stack.get(ToolComponents.MODIFIERS.get());
		return mods != null ? mods : ModularModifiers.defaultModifiers(MODULAR_TOOLS_DEFAULT_MAX_MODIFIERS.get());
	}

	public static boolean isBroken(ItemStack stack) {
		Boolean broken = stack.get(ToolComponents.BROKEN.get());
		return broken != null && broken;
	}

	// -----------------------------------------------------------------------
	// Durabilita
	// -----------------------------------------------------------------------
	@Override
	public int getMaxDamage(@NotNull ItemStack stack) {
		return getToolData(stack).totalDurability();
	}

	@Override
	public boolean isRepairable(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public boolean isValidRepairItem(@NotNull ItemStack stack, @NotNull ItemStack repair) {
		return false;
	}

	// -----------------------------------------------------------------------
	// Těžení
	// -----------------------------------------------------------------------
	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
		if (isBroken(stack)) return 1.0F;
		if (!state.is(getMineableTag())) return 1.0F;
		ModularToolData data = getToolData(stack);
		ModularModifiers mods = getModifiers(stack);
		ToolMaterial head = data.head();
		int speed = head.efficiency + mods.efficiencyLevel() * MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_0.get();
		if (data.containsMaterial(ToolMaterial.GOLD)) speed += 8;
		return speed;
	}

	@Override
	public boolean isCorrectToolForDrops(@NotNull ItemStack stack, @NotNull BlockState state) {
		if (isBroken(stack)) return false;
		if (!state.is(getMineableTag())) return false;
		ModularToolData data = getToolData(stack);
		ModularModifiers mods = getModifiers(stack);
		int level = mods.diamond() ? 3 : data.head().miningLevel;
		if (state.is(BlockTags.NEEDS_DIAMOND_TOOL) && level < 3) return false;
		if (state.is(BlockTags.NEEDS_IRON_TOOL) && level < 2) return false;
        return !state.is(BlockTags.NEEDS_STONE_TOOL) || level >= 1;
    }

	// -----------------------------------------------------------------------
	// Atributy — attack damage a speed
	// V 1.21.1 se dynamické atributy řeší přes getDefaultAttributeModifiers
	// a přepsání ItemAttributeModifiers komponenty v inventoryTick
	// -----------------------------------------------------------------------
	@Override
	public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers() {
		return ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 1.0F, AttributeModifier.Operation.ADD_VALUE),
						net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED,
						new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.8F, AttributeModifier.Operation.ADD_VALUE),
						net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
				.build();
	}

	// Aktualizuje atributy podle aktuálního materiálu hlavy
	private void updateAttributes(ItemStack stack) {
		ModularToolData data = getToolData(stack);
		ToolMaterial head = data.head();
		if (isBroken(stack)) {
			stack.set(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
					ItemAttributeModifiers.builder().build());
			return;
		}
		stack.set(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
				ItemAttributeModifiers.builder()
						.add(Attributes.ATTACK_DAMAGE,
								new AttributeModifier(BASE_ATTACK_DAMAGE_ID, baseAttackDamage(head), AttributeModifier.Operation.ADD_VALUE),
								net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
						.add(Attributes.ATTACK_SPEED,
								new AttributeModifier(BASE_ATTACK_SPEED_ID, baseAttackSpeed(head), AttributeModifier.Operation.ADD_VALUE),
								net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
						.build());
	}

	// -----------------------------------------------------------------------
	// Enchantmenty
	// -----------------------------------------------------------------------
	@Override
	public boolean isEnchantable(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public boolean isFoil(@NotNull ItemStack stack) {
		return false;
	}

	// -----------------------------------------------------------------------
	// Inventory tick — pasivní efekty materiálů + broken check
	// -----------------------------------------------------------------------
	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world,
	                          @NotNull Entity entity, int slot, boolean selected) {
		if (world.isClientSide()) return;
		ModularToolData data = getToolData(stack);

		// Wood — náhoda 1/500 samooprava o 1
		if (data.containsMaterial(ToolMaterial.WOOD)) {
			boolean inMainHand = entity instanceof Player p && p.getMainHandItem() == stack;
			if (DifMod.rouletteBoolean(500) && !inMainHand) {
				stack.setDamageValue(Math.max(0, stack.getDamageValue() - 1));
			}
		}

		// Iron — magnet na itemy v okruhu 4 bloků
		if (data.containsMaterial(ToolMaterial.IRON) && entity instanceof Player player) {
			boolean held = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
			if (held) {
				List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class,
						player.getBoundingBox().inflate(4.0));
				for (ItemEntity itemEntity : items) {
					if (itemEntity.isRemoved() || !itemEntity.isAlive()) continue;
					Vec3 dir = player.position().subtract(itemEntity.position());
					if (dir.lengthSqr() < 0.4) {
						itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().multiply(0.8, 1, 0.8));
						continue;
					}
					itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(dir.normalize().scale(0.1)));
				}
			}
		}

		// Broken check
		boolean shouldBeBroken = stack.getDamageValue() >= stack.getMaxDamage() - 1;
		boolean currentlyBroken = isBroken(stack);
		if (shouldBeBroken != currentlyBroken) {
			stack.set(ToolComponents.BROKEN.get(), shouldBeBroken);
			updateAttributes(stack);
		} else {
			updateAttributes(stack);
		}
	}

	// -----------------------------------------------------------------------
	// Tooltip
	// -----------------------------------------------------------------------
	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
	                            @NotNull List<Component> list, @NotNull TooltipFlag flag) {
		ModularToolData data = getToolData(stack);
		ModularModifiers mods = getModifiers(stack);
		boolean broken = isBroken(stack);

		if (Screen.hasShiftDown()) {
			list.add(Component.literal("Remaining Modifiers: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(String.valueOf(mods.maxModifiers())).withStyle(ChatFormatting.YELLOW)));
			if (mods.efficiencyLevel() > 0 || mods.efficiencyProgress() > 0)
				list.add(Component.literal("Efficiency: ").withStyle(ChatFormatting.RED)
						.append(Component.literal(String.valueOf(mods.efficiencyLevel()))));
			if (mods.fortuneLevel() > 0 || mods.fortuneProgress() > 0)
				list.add(Component.literal("Fortune: ").withStyle(ChatFormatting.BLUE)
						.append(Component.literal(String.valueOf(mods.fortuneLevel()))));
			if (mods.silkTouch())
				list.add(Component.literal("Silk Touch").withStyle(Style.EMPTY.withColor(0xFDFD96)));
			if (mods.diamond())
				list.add(Component.literal("Diamond").withStyle(Style.EMPTY.withColor(ToolMaterial.DIAMOND.color)));
			if (mods.blazing())
				list.add(Component.literal("Blazing").withStyle(Style.EMPTY.withColor(0xFF5A00)));
			list.add(CommonComponents.EMPTY);
			for (ToolMaterial m : ToolMaterial.values())
				if (data.containsMaterial(m) && !m.passiveKey().isEmpty())
					list.add(Component.literal(capitalize(m.passiveKey()))
							.withStyle(Style.EMPTY.withColor(m.color)));

		} else if (Screen.hasControlDown()) {
			list.add(Component.literal("Head: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(data.headMaterial())
							.withStyle(Style.EMPTY.withColor(data.head().color))));
			list.add(Component.literal("  Durability: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(String.valueOf(data.headDurability()))));
			list.add(Component.literal("Binding: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(data.bindingMaterial())
							.withStyle(Style.EMPTY.withColor(data.binding().color))));
			list.add(Component.literal("  Durability: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(String.valueOf(data.bindingDurability()))));
			list.add(Component.literal("Handle: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(data.handleMaterial())
							.withStyle(Style.EMPTY.withColor(data.handle().color))));
			list.add(Component.literal("  Durability: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(String.valueOf(data.handleDurability()))));

		} else {
			ToolMaterial head = data.head();
			int miningLevel = mods.diamond() ? 3 : head.miningLevel;
			list.add(Component.literal("Mining Level: ").withStyle(ChatFormatting.WHITE)
					.append(Component.translatable("dif.mining_level." + miningLevel)
							.withStyle(Style.EMPTY.withColor(miningLevelColor(miningLevel)))));

			int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue() - 1);
			int max = stack.getMaxDamage() - 1;
			float ratio = max > 0 ? (float) remaining / max : 0;
			int red = (int)(255 * (1 - ratio));
			int green = (int)(255 * ratio);
			int durColor = (red << 16) | (green << 8);
			list.add(Component.literal("Durability: ")
					.append(Component.literal(String.valueOf(remaining))
							.withStyle(Style.EMPTY.withColor(durColor)))
					.append(Component.literal(" / " + max).withStyle(ChatFormatting.GRAY)));

			int speed = head.efficiency + mods.efficiencyLevel() * MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_0.get();
			if (data.containsMaterial(ToolMaterial.GOLD)) speed += 8;
			list.add(Component.literal("Efficiency: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(String.valueOf(speed)).withStyle(ChatFormatting.GREEN)));

			list.add(Component.literal("Attack Damage: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(String.format(Locale.ROOT, "%.1f", 1.0F + baseAttackDamage(head)))
							.withStyle(ChatFormatting.RED)));
			list.add(Component.literal("Attack Speed: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(String.format(Locale.ROOT, "%.1f", 4.0F + baseAttackSpeed(head)))
							.withStyle(ChatFormatting.YELLOW)));

			if (broken)
				list.add(Component.literal("Broken")
						.withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED).withBold(true)));

			list.add(CommonComponents.EMPTY);
			list.add(Component.literal("Press ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal("Shift").withStyle(ChatFormatting.AQUA))
					.append(Component.literal(" for modifiers info.").withStyle(ChatFormatting.GRAY)));
			list.add(Component.literal("Press ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal("Ctrl").withStyle(ChatFormatting.YELLOW))
					.append(Component.literal(" for parts info.").withStyle(ChatFormatting.GRAY)));
		}
	}

	// -----------------------------------------------------------------------
	// Helpers
	// -----------------------------------------------------------------------
	private static int miningLevelColor(int level) {
		int[] colors = {0x745631, 0x838383, 0xDCDCDC, 0x6DEDE4, 0x433F41};
		return level < colors.length ? colors[level] : 0xFFFFFF;
	}

	private static String capitalize(String s) {
		if (s == null || s.isEmpty()) return s;
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	public static boolean isHead(ItemStack stack) {
		return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "modular_tools_parts/head")));
	}

	public static boolean isBinding(ItemStack stack) {
		return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "modular_tools_parts/binding")));
	}

	public static boolean isHandle(ItemStack stack) {
		return stack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(DifMod.MODID, "modular_tools_parts/handle")));
	}

	public static ItemStack newSingleMaterialPreFab(Item toolItem, String material) {
		ToolMaterial mat = ToolMaterial.fromName(material);
		return createTool(toolItem, mat, mat, mat);
	}

	public static ItemStack newToolFromMaterials(Item toolItem, String head, String binding, String handle) {
		return createTool(toolItem, ToolMaterial.fromName(head), ToolMaterial.fromName(binding), ToolMaterial.fromName(handle));
	}

	public static ItemStack newPartFromMaterial(Item partItem, String material) {
		// Části nástroje zatím nemají Data Components — vrátí plain ItemStack
		// Bude přepsáno až v ModularPart přepisu
		return new ItemStack(partItem);
	}

	public static ItemStack createTool(Item toolItem, ToolMaterial head, ToolMaterial binding, ToolMaterial handle) {
		ItemStack stack = new ItemStack(toolItem);
		stack.set(ToolComponents.TOOL_DATA.get(), ModularToolData.of(head, binding, handle));
		stack.set(ToolComponents.MODIFIERS.get(), ModularModifiers.defaultModifiers(MODULAR_TOOLS_DEFAULT_MAX_MODIFIERS.get()));
		stack.set(ToolComponents.BROKEN.get(), false);
		return stack;
	}
}