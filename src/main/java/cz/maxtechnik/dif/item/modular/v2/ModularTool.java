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
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
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
		int miningLevel = props.miningLevel();

		boolean matches = false;
		if (type.equals("pickaxe") && state.is(BlockTags.MINEABLE_WITH_PICKAXE)) matches = true;
		else if (type.equals("axe") && state.is(BlockTags.MINEABLE_WITH_AXE)) matches = true;
		else if (type.equals("shovel") && state.is(BlockTags.MINEABLE_WITH_SHOVEL)) matches = true;
		else if (type.equals("hoe") && state.is(BlockTags.MINEABLE_WITH_HOE)) matches = true;
		else if (type.equals("sword") && state.is(BlockTags.SWORD_EFFICIENT)) return props.efficiency();

		if (matches) {
			if (state.is(BlockTags.NEEDS_DIAMOND_TOOL) && miningLevel < 3) return 1.0f;
			if (state.is(BlockTags.NEEDS_IRON_TOOL) && miningLevel < 2) return 1.0f;
			if (state.is(BlockTags.NEEDS_STONE_TOOL) && miningLevel < 1) return 1.0f;

			return props.efficiency();
		}

		return 1.0f;
	}

	@Override
	public boolean isCorrectToolForDrops(@NotNull ItemStack stack, @NotNull BlockState state) {
		ModularToolProperties props = getProps(stack);
		String type = props.toolType().toLowerCase();
		int miningLevel = props.miningLevel();

		boolean isCorrectType = false;
		if (type.equals("pickaxe") && state.is(BlockTags.MINEABLE_WITH_PICKAXE)) isCorrectType = true;
		else if (type.equals("axe") && state.is(BlockTags.MINEABLE_WITH_AXE)) isCorrectType = true;
		else if (type.equals("shovel") && state.is(BlockTags.MINEABLE_WITH_SHOVEL)) isCorrectType = true;
		else if (type.equals("hoe") && state.is(BlockTags.MINEABLE_WITH_HOE)) isCorrectType = true;

		if (isCorrectType) {
			if (state.is(BlockTags.NEEDS_DIAMOND_TOOL) && miningLevel < 3) return false;
			if (state.is(BlockTags.NEEDS_IRON_TOOL) && miningLevel < 2) return false;
			if (state.is(BlockTags.NEEDS_STONE_TOOL) && miningLevel < 1) return false;

			return true;
		}

		return super.isCorrectToolForDrops(stack, state);
	}

	@Override
	public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility itemAbility) {
		String type = getProps(stack).toolType().toLowerCase();

		if (type.equals("pickaxe") && itemAbility == ItemAbilities.PICKAXE_DIG) return true;

		// OPRAVA: Přejmenováno z AXE_UNWAX na AXE_WAX_OFF
		if (type.equals("axe") && (itemAbility == ItemAbilities.AXE_DIG || itemAbility == ItemAbilities.AXE_STRIP || itemAbility == ItemAbilities.AXE_SCRAPE || itemAbility == ItemAbilities.AXE_WAX_OFF)) return true;
		if (type.equals("shovel") && (itemAbility == ItemAbilities.SHOVEL_DIG || itemAbility == ItemAbilities.SHOVEL_FLATTEN || itemAbility == ItemAbilities.SHOVEL_DOUSE)) return true;
		if (type.equals("hoe") && (itemAbility == ItemAbilities.HOE_DIG || itemAbility == ItemAbilities.HOE_TILL)) return true;

		if (type.equals("sword") && itemAbility == ItemAbilities.SWORD_DIG) return true;

		return false;
	}

	// ====================================================================
	// INTERAKCE PRAVÝM TLAČÍTKEM (Sided Logika pro 1.21.1)
	// ====================================================================
	@Override
	public @NotNull net.minecraft.world.InteractionResult useOn(@NotNull net.minecraft.world.item.context.UseOnContext context) {
		net.minecraft.world.level.Level level = context.getLevel();
		net.minecraft.core.BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		ItemStack stack = context.getItemInHand();
		String type = getProps(stack).toolType().toLowerCase();

		BlockState modified = null;
		net.minecraft.sounds.SoundEvent sound = null;

		// 1. CHOVÁNÍ PRO SEKERU (Odloupnutí kůry, seškrabávání mědi, sundání vosku)
		switch(type){
			case "axe" -> {
				modified=state.getToolModifiedState(context,ItemAbilities.AXE_STRIP,false);
				if(modified!=null){
					sound=net.minecraft.sounds.SoundEvents.AXE_STRIP;
				}else{
					modified=state.getToolModifiedState(context,ItemAbilities.AXE_SCRAPE,false);
					if(modified!=null){
						sound=net.minecraft.sounds.SoundEvents.AXE_SCRAPE;
					}else{
						// OPRAVA: Přejmenováno z AXE_UNWAX na AXE_WAX_OFF
						modified=state.getToolModifiedState(context,ItemAbilities.AXE_WAX_OFF,false);
						if(modified!=null){
							sound=net.minecraft.sounds.SoundEvents.AXE_WAX_OFF;
						}
					}
				}
				// 2. CHOVÁNÍ PRO MOTYKU (Orání hlíny / Farmland)
			}
			case "hoe" -> {
				modified=state.getToolModifiedState(context,ItemAbilities.HOE_TILL,false);
				if(modified!=null){
					sound=net.minecraft.sounds.SoundEvents.HOE_TILL;
				}
				// 3. CHOVÁNÍ PRO LOPATU (Tvorba travnatých cestiček, hašení ohnišť)
			}
			case "shovel" -> {
				modified=state.getToolModifiedState(context,ItemAbilities.SHOVEL_FLATTEN,false);
				if(modified!=null){
					sound=net.minecraft.sounds.SoundEvents.SHOVEL_FLATTEN;
				}else{
					modified=state.getToolModifiedState(context,ItemAbilities.SHOVEL_DOUSE,false);
					if(modified!=null){
						sound=net.minecraft.sounds.SoundEvents.FIRE_EXTINGUISH;
					}
				}
			}
		}

		// Pokud NeoForge našel platnou transformaci bloku, provedeme ji
		if (modified!=null) {
			net.minecraft.world.entity.player.Player player = context.getPlayer();
			level.playSound(player, pos, sound, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
			if (!level.isClientSide) {
				level.setBlock(pos, modified, 11);
				if (player != null) {
					stack.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
				}
			}
			return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
		}

		return super.useOn(context);
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

	@Override public boolean isEnchantable(@NotNull ItemStack stack) { return false; }
	@Override public int getEnchantmentValue(@NotNull ItemStack stack) { return 0; }
	@Override public boolean isRepairable(@NotNull ItemStack stack) { return false; }
	@Override public boolean isValidRepairItem(@NotNull ItemStack stack, @NotNull ItemStack repair) { return false; }
}