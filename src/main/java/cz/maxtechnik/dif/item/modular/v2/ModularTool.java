package cz.maxtechnik.dif.item.modular.v2;

import cz.maxtechnik.dif.init.other.DifModComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;       // OPRAVENÝ IMPORT
import net.neoforged.neoforge.common.ItemAbilities;     // OPRAVENÝ IMPORT
import org.jetbrains.annotations.NotNull;

public class ModularTool extends DiggerItem {
	public ModularTool() {
		super(new Tier() {
			@Override public int getUses() { return 100; }
			@Override public float getSpeed() { return 1.0f; }
			@Override public float getAttackDamageBonus() { return 1.0f; }
			@Override public @NotNull net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() { return BlockTags.MINEABLE_WITH_PICKAXE; }
			@Override public int getEnchantmentValue() { return 0; }
			@Override public @NotNull Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
		}, BlockTags.MINEABLE_WITH_PICKAXE, new Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
	}

	private ModularToolProperties getProps(ItemStack stack) {
		ModularToolProperties props = stack.get(DifModComponents.MODULAR_PROPERTIES.get());
		return props != null ? props : ModularToolProperties.DEFAULT;
	}

	// ====================================================================
	// DYNAMICKÉ VLASTNOSTI NÁSTROJE PODLE KOMPONENTY
	// ====================================================================

	@Override
	public int getMaxDamage(@NotNull ItemStack stack) {
		return getProps(stack).maxDamage();
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
		ModularToolProperties props = getProps(stack);
		String type = props.toolType().toLowerCase();

		if (type.equals("pickaxe") && state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return props.efficiency();
		if (type.equals("axe") && state.is(BlockTags.MINEABLE_WITH_AXE)) return props.efficiency();
		if (type.equals("shovel") && state.is(BlockTags.MINEABLE_WITH_SHOVEL)) return props.efficiency();
		if (type.equals("hoe") && state.is(BlockTags.MINEABLE_WITH_HOE)) return props.efficiency();
		if (type.equals("sword") && state.is(BlockTags.SWORD_EFFICIENT)) return props.efficiency();

		return 1.0f;
	}

	@Override
	public boolean isCorrectToolForDrops(@NotNull ItemStack stack, @NotNull BlockState state) {
		String type = getProps(stack).toolType().toLowerCase();

		if (type.equals("pickaxe") && state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return true;
		if (type.equals("axe") && state.is(BlockTags.MINEABLE_WITH_AXE)) return true;
		if (type.equals("shovel") && state.is(BlockTags.MINEABLE_WITH_SHOVEL)) return true;
		if (type.equals("hoe") && state.is(BlockTags.MINEABLE_WITH_HOE)) return true;

		return super.isCorrectToolForDrops(stack, state);
	}

	// ====================================================================
	// FIX 1.21.1: canPerformAction nyní využívá ItemAbility namísto ToolAction
	// ====================================================================
	@Override
	public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility itemAbility) {
		String type = getProps(stack).toolType().toLowerCase();

		// Kontrola schopností nástroje pomocí globálního registru ItemAbilities
		if (type.equals("pickaxe") && itemAbility == ItemAbilities.PICKAXE_DIG) return true;
		if (type.equals("axe") && (itemAbility == ItemAbilities.AXE_DIG || itemAbility == ItemAbilities.AXE_STRIP)) return true;
		if (type.equals("shovel") && (itemAbility == ItemAbilities.SHOVEL_DIG || itemAbility == ItemAbilities.SHOVEL_FLATTEN)) return true;
		if (type.equals("hoe") && (itemAbility == ItemAbilities.HOE_DIG || itemAbility == ItemAbilities.HOE_TILL)) return true;
		if (type.equals("sword") && itemAbility == ItemAbilities.SWORD_DIG) return true;

		return false;
	}

	@Override
	public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
		ModularToolProperties props = getProps(stack);
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

		builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
				ResourceLocation.fromNamespaceAndPath("dif", "modular_attack_damage"),
				props.attackDamage(),
				AttributeModifier.Operation.ADD_VALUE
		), EquipmentSlotGroup.MAINHAND);

		builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(
				ResourceLocation.fromNamespaceAndPath("dif", "modular_attack_speed"),
				props.attackSpeed(),
				AttributeModifier.Operation.ADD_VALUE
		), EquipmentSlotGroup.MAINHAND);

		return builder.build();
	}

	// ====================================================================
	// ANTI-ENCHANT A ANTI-REPAIR BLOKACE
	// ====================================================================

	@Override
	public boolean isEnchantable(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public int getEnchantmentValue(@NotNull ItemStack stack) {
		return 0;
	}

	@Override
	public boolean isRepairable(@NotNull ItemStack stack) {
		return false;
	}

	@Override
	public boolean isValidRepairItem(@NotNull ItemStack stack, @NotNull ItemStack repair) {
		return false;
	}
}