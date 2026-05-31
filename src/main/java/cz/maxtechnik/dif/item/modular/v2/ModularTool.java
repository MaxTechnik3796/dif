package cz.maxtechnik.dif.item.modular.v2;

import cz.maxtechnik.dif.init.other.DifModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
public class ModularTool extends DiggerItem{
	int[] miningLevelColor={0x745631,0x838383,0xDCDCDC,0x6DEDE4,0x433F41};
	public ModularTool(){
		super(new Tier(){
			@Override
			public int getUses(){
				return 100;
			}
			@Override
			public float getSpeed(){
				return 1F;
			}
			@Override
			public float getAttackDamageBonus(){
				return 1F;
			}
			@Override
			public @NotNull TagKey<Block> getIncorrectBlocksForDrops(){
				return BlockTags.MINEABLE_WITH_PICKAXE;
			}
			@Override
			public int getEnchantmentValue(){
				return 0;
			}
			@Override
			public @NotNull Ingredient getRepairIngredient(){
				return Ingredient.EMPTY;
			}
		},BlockTags.MINEABLE_WITH_PICKAXE,new Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
	}
	private ModularToolProperties getProps(ItemStack itemStack){
		ModularToolProperties props=itemStack.get(DifModComponents.MODULAR_PROPERTIES.get());
		return props!=null?props:ModularToolProperties.DEFAULT;
	}
	@Override
	public int getMaxDamage(@NotNull ItemStack itemStack){
		return getProps(itemStack).maxDamage();
	}
	@Override
	public float getDestroySpeed(@NotNull ItemStack itemStack,@NotNull BlockState blockState){
		ModularToolProperties props=getProps(itemStack);
		String type=props.toolType().toLowerCase();
		int miningLevel=props.miningLevel();
		boolean matches=false;
		if(type.equals("pickaxe")&&blockState.is(BlockTags.MINEABLE_WITH_PICKAXE)) matches=true;
		else if(type.equals("axe")&&blockState.is(BlockTags.MINEABLE_WITH_AXE)) matches=true;
		else if(type.equals("shovel")&&blockState.is(BlockTags.MINEABLE_WITH_SHOVEL)) matches=true;
		else if(type.equals("hoe")&&blockState.is(BlockTags.MINEABLE_WITH_HOE)) matches=true;
		else if(type.equals("sword")&&blockState.is(BlockTags.SWORD_EFFICIENT)) return props.efficiency();
		if(matches){
			if(blockState.is(BlockTags.NEEDS_DIAMOND_TOOL)&&miningLevel<3) return 1F;
			if(blockState.is(BlockTags.NEEDS_IRON_TOOL)&&miningLevel<2) return 1F;
			if(blockState.is(BlockTags.NEEDS_STONE_TOOL)&&miningLevel<1) return 1F;
			return props.efficiency();
		}
		return 1F;
	}
	@Override
	public boolean isCorrectToolForDrops(@NotNull ItemStack itemStack,@NotNull BlockState blockState){
		ModularToolProperties props=getProps(itemStack);
		String type=props.toolType().toLowerCase();
		int miningLevel=props.miningLevel();
		boolean isCorrectType=false;
		if(type.equals("pickaxe")&&blockState.is(BlockTags.MINEABLE_WITH_PICKAXE)) isCorrectType=true;
		else if(type.equals("axe")&&blockState.is(BlockTags.MINEABLE_WITH_AXE)) isCorrectType=true;
		else if(type.equals("shovel")&&blockState.is(BlockTags.MINEABLE_WITH_SHOVEL)) isCorrectType=true;
		else if(type.equals("hoe")&&blockState.is(BlockTags.MINEABLE_WITH_HOE)) isCorrectType=true;
		if(isCorrectType){
			if(blockState.is(BlockTags.NEEDS_DIAMOND_TOOL)&&miningLevel<3) return false;
			if(blockState.is(BlockTags.NEEDS_IRON_TOOL)&&miningLevel<2) return false;
			if(blockState.is(BlockTags.NEEDS_STONE_TOOL)&&miningLevel<1) return false;//DO NOT TOUCH
			return true;
		}
		return super.isCorrectToolForDrops(itemStack,blockState);
	}
	@Override
	public boolean canPerformAction(@NotNull ItemStack itemStack,@NotNull ItemAbility itemAbility){
		String type=getProps(itemStack).toolType().toLowerCase();
		if(type.equals("pickaxe")&&itemAbility.equals(ItemAbilities.PICKAXE_DIG)) return true;
		if(type.equals("axe")&&(itemAbility.equals(ItemAbilities.AXE_DIG)||itemAbility.equals(ItemAbilities.AXE_STRIP)||itemAbility.equals(ItemAbilities.AXE_SCRAPE)||itemAbility.equals(ItemAbilities.AXE_WAX_OFF)))
			return true;
		if(type.equals("shovel")&&(itemAbility.equals(ItemAbilities.SHOVEL_DIG)||itemAbility.equals(ItemAbilities.SHOVEL_FLATTEN)||itemAbility.equals(ItemAbilities.SHOVEL_DOUSE)))
			return true;
		if(type.equals("hoe")&&(itemAbility.equals(ItemAbilities.HOE_DIG)||itemAbility.equals(ItemAbilities.HOE_TILL))) return true;
		if(type.equals("sword")&&itemAbility.equals(ItemAbilities.SWORD_DIG)) return true;//DO NOT TOUCH
		return false;
	}
	@Override
	public @NotNull InteractionResult useOn(@NotNull UseOnContext context){
		Level level=context.getLevel();
		BlockPos pos=context.getClickedPos();
		BlockState state=level.getBlockState(pos);
		ItemStack stack=context.getItemInHand();
		String type=getProps(stack).toolType().toLowerCase();
		BlockState modified=null;
		SoundEvent sound=null;
		// 1. CHOVÁNÍ PRO SEKERU (Odloupnutí kůry, seškrabávání mědi, sundání vosku)
		switch(type){
			case "axe" -> {
				modified=state.getToolModifiedState(context,ItemAbilities.AXE_STRIP,false);
				if(modified!=null){
					sound=SoundEvents.AXE_STRIP;
				}else{
					modified=state.getToolModifiedState(context,ItemAbilities.AXE_SCRAPE,false);
					if(modified!=null){
						sound=SoundEvents.AXE_SCRAPE;
					}else{
						// OPRAVA: Přejmenováno z AXE_UNWAX na AXE_WAX_OFF
						modified=state.getToolModifiedState(context,ItemAbilities.AXE_WAX_OFF,false);
						if(modified!=null){
							sound=SoundEvents.AXE_WAX_OFF;
						}
					}
				}
				// 2. CHOVÁNÍ PRO MOTYKU (Orání hlíny / Farmland)
			}
			case "hoe" -> {
				modified=state.getToolModifiedState(context,ItemAbilities.HOE_TILL,false);
				if(modified!=null){
					sound=SoundEvents.HOE_TILL;
				}
				// 3. CHOVÁNÍ PRO LOPATU (Tvorba travnatých cestiček, hašení ohnišť)
			}
			case "shovel" -> {
				modified=state.getToolModifiedState(context,ItemAbilities.SHOVEL_FLATTEN,false);
				if(modified!=null){
					sound=SoundEvents.SHOVEL_FLATTEN;
				}else{
					modified=state.getToolModifiedState(context,ItemAbilities.SHOVEL_DOUSE,false);
					if(modified!=null){
						sound=SoundEvents.FIRE_EXTINGUISH;
					}
				}
			}
		}
		// Pokud NeoForge našel platnou transformaci bloku, provedeme ji
		if(modified!=null){
			Player player=context.getPlayer();
			level.playSound(player,pos,sound,SoundSource.BLOCKS,1F,1F);
			if(!level.isClientSide){
				level.setBlock(pos,modified,11);
				if(player!=null){
					stack.hurtAndBreak(1,player,EquipmentSlot.MAINHAND);
				}
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return super.useOn(context);
	}
	@Override
	public @NotNull String getDescriptionId(@NotNull ItemStack itemStack) {
		String type = getProps(itemStack).toolType().toLowerCase(Locale.ROOT);
		// Pokud typ paliva/nástroje není prázdný nebo "none", připojíme ho na konec klíče
		if (!type.isEmpty() && !type.equals("none")) {
			return super.getDescriptionId(itemStack) + "." + type;
		}
		return super.getDescriptionId(itemStack);
	}
	@Override
	public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack itemStack){
		ModularToolProperties props=getProps(itemStack);
		ItemAttributeModifiers.Builder builder=ItemAttributeModifiers.builder();
		// FIX: Použijeme Item.BASE_ATTACK_DAMAGE_ID namísto vlastního ResourceLocation
		builder.add(Attributes.ATTACK_DAMAGE,new AttributeModifier(
				Item.BASE_ATTACK_DAMAGE_ID,
				props.attackDamage(),
				AttributeModifier.Operation.ADD_VALUE
		),EquipmentSlotGroup.MAINHAND);
		// FIX: Použijeme Item.BASE_ATTACK_SPEED_ID namísto vlastního ResourceLocation
		builder.add(Attributes.ATTACK_SPEED,new AttributeModifier(
				Item.BASE_ATTACK_SPEED_ID,
				props.attackSpeed(),
				AttributeModifier.Operation.ADD_VALUE
		),EquipmentSlotGroup.MAINHAND);
		return builder.build();
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		ModularToolProperties tag=getProps(itemStack);
		if(tag==null) return;
		list.add(Component.literal("Mining Level: ").withStyle(ChatFormatting.WHITE).append(Component.translatable("dif.mining_level."+tag.miningLevel()).withStyle(Style.EMPTY.withColor(miningLevelColor[tag.miningLevel()]))));
		int remaining=Math.max(0,tag.maxDamage()-itemStack.getDamageValue());
		float ratio=tag.maxDamage()>0?(float)remaining/tag.maxDamage():0;
		int red=(int)(255*(1-ratio));
		int green=(int)(255*ratio);
		int durColor=(red<<16)|(green<<8);
		list.add(Component.literal("Durability: ")
				.append(Component.literal(String.valueOf(remaining)).withStyle(Style.EMPTY.withColor(durColor))).append(Component.literal(" / "+tag.maxDamage()).withStyle(ChatFormatting.GRAY)));
		list.add(Component.literal("Efficiency: ").withStyle(ChatFormatting.WHITE).append(Component.literal(String.valueOf(tag.efficiency())).withStyle(ChatFormatting.GREEN)));
		list.add(Component.literal("Attack Damage: ").withStyle(ChatFormatting.WHITE).append(Component.literal(String.format(Locale.ROOT,"%.1f",1F+tag.attackDamage())).withStyle(ChatFormatting.RED)));
		list.add(Component.literal("Attack Speed: ").withStyle(ChatFormatting.WHITE).append(Component.literal(String.format(Locale.ROOT,"%.1f",4F+tag.attackSpeed())).withStyle(ChatFormatting.YELLOW)));
		//Broken ---if (broken) list.add(Component.literal("Broken").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED).withBold(true)));
		list.add(CommonComponents.EMPTY);
		list.add(Component.literal("Press ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal("Shift").withStyle(ChatFormatting.AQUA))
				.append(Component.literal(" for modifiers info.").withStyle(ChatFormatting.GRAY)));
		list.add(Component.literal("Press ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal("Ctrl").withStyle(ChatFormatting.YELLOW))
				.append(Component.literal(" for parts info.").withStyle(ChatFormatting.GRAY)));
	}
	@Override
	public boolean isEnchantable(@NotNull ItemStack itemStack){
		return false;
	}
	@Override
	public int getEnchantmentValue(@NotNull ItemStack itemStack){
		return 0;
	}
	@Override
	public boolean isRepairable(@NotNull ItemStack itemStack){
		return false;
	}
	@Override
	public boolean isValidRepairItem(@NotNull ItemStack itemStack,@NotNull ItemStack repair){
		return false;
	}
}