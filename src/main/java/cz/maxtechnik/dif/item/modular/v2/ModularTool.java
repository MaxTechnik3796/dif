package cz.maxtechnik.dif.item.modular.v2;

import cz.maxtechnik.dif.init.other.DifModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
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
import net.minecraft.world.entity.LivingEntity;
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
	// Pomocná metoda pro zjištění, zda je nástroj zlomený
	public boolean isBroken(ItemStack stack){
		return stack.getDamageValue()>=getMaxDamage(stack);
	}
	// Vlastní bezpečná metoda pro aplikaci poškození nástroje
	public void damageTool(ItemStack stack,int amount,LivingEntity entity){
		int maxDmg=getMaxDamage(stack);
		int currentDmg=stack.getDamageValue();
		int newDmg=currentDmg+amount;
		if(newDmg>=maxDmg){
			stack.setDamageValue(maxDmg); // Zasekne se na max damage (vstup do stavu BROKEN)
			if(entity instanceof Player){
				entity.level().playSound(null,entity.getX(),entity.getY(),entity.getZ(),
						SoundEvents.ITEM_BREAK,SoundSource.PLAYERS,1.0F,1.0F);
			}
		}else{
			stack.setDamageValue(newDmg);
		}
	}
	// ====================================================================
	// VÝPOČET STATISTIK Z MATERIÁLŮ
	// ====================================================================
	@Override
	public int getMaxDamage(@NotNull ItemStack itemStack){
		ModularToolProperties props=getProps(itemStack);
		ModularMaterial head=ModularMaterial.byName(props.headMaterial());
		ModularMaterial binding=ModularMaterial.byName(props.bindingMaterial());
		ModularMaterial handle=ModularMaterial.byName(props.handleMaterial());
		return (int)((head.getHeadDurability()+binding.getBindingDurability())*handle.getHandleDurabilityMultiplier());
	}
	private float getLiveEfficiency(ItemStack stack){
		if(isBroken(stack)) return 1F; // Zlomený nástroj těží rychlostí ruky
		return ModularMaterial.byName(getProps(stack).headMaterial()).getHeadEfficiency();
	}
	private int getLiveMiningLevel(ItemStack stack){
		if(isBroken(stack)) return 0; // Zlomený nástroj ztrácí schopnost dropovat vzácné rudy
		return ModularMaterial.byName(getProps(stack).headMaterial()).getMiningLevel();
	}
	private float getBaseDamageForType(String type){
		return switch(type.toLowerCase(Locale.ROOT)){
			case "sword" -> 3F;
			case "axe" -> 5F;
			case "shovel" -> 1.5F;
			case "hoe" -> 0F;
			default -> 1F;
		};
	}
	private float getBaseSpeedForType(String type){
		return switch(type.toLowerCase(Locale.ROOT)){
			case "pickaxe" -> -2.8F;
			case "axe","shovel" -> -3F;
			case "hoe" -> -1F;
			default -> -2.4F;
		};
	}
	// ====================================================================
	// INTERKACE S MECHANIKAMI MINECRAFTU
	// ====================================================================
	@Override
	public float getDestroySpeed(@NotNull ItemStack itemStack,@NotNull BlockState blockState){
		if(isBroken(itemStack)) return 1.0F; // Záchranná brzda pro rychlost těžení
		ModularToolProperties props=getProps(itemStack);
		String type=props.toolType().toLowerCase();
		int miningLevel=getLiveMiningLevel(itemStack);
		float efficiency=getLiveEfficiency(itemStack);
		boolean matches=false;
		if(type.equals("pickaxe")&&blockState.is(BlockTags.MINEABLE_WITH_PICKAXE)) matches=true;
		else if(type.equals("axe")&&blockState.is(BlockTags.MINEABLE_WITH_AXE)) matches=true;
		else if(type.equals("shovel")&&blockState.is(BlockTags.MINEABLE_WITH_SHOVEL)) matches=true;
		else if(type.equals("hoe")&&blockState.is(BlockTags.MINEABLE_WITH_HOE)) matches=true;
		else if(type.equals("sword")&&blockState.is(BlockTags.SWORD_EFFICIENT)) return efficiency;
		if(matches){
			if(blockState.is(BlockTags.NEEDS_DIAMOND_TOOL)&&miningLevel<3) return 1F;
			if(blockState.is(BlockTags.NEEDS_IRON_TOOL)&&miningLevel<2) return 1F;
			if(blockState.is(BlockTags.NEEDS_STONE_TOOL)&&miningLevel<1) return 1F;
			return efficiency;
		}
		return 1F;
	}
	@Override
	public boolean isCorrectToolForDrops(@NotNull ItemStack itemStack,@NotNull BlockState blockState){
		if(isBroken(itemStack)) return false;
		String type=getProps(itemStack).toolType().toLowerCase();
		int miningLevel=getLiveMiningLevel(itemStack);
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
	public boolean mineBlock(@NotNull ItemStack itemStack,@NotNull Level level,@NotNull BlockState blockState,@NotNull BlockPos blockPos,@NotNull LivingEntity miningEntity){
		if(!level.isClientSide&&blockState.getDestroySpeed(level,blockPos)!=0F&&!isBroken(itemStack))
			this.damageTool(itemStack,1,miningEntity);
		return true;
	}
	@Override
	public boolean hurtEnemy(@NotNull ItemStack itemStack,@NotNull LivingEntity target,@NotNull LivingEntity attacker){
		if(!isBroken(itemStack)){
			int amt=getProps(itemStack).toolType().toLowerCase(Locale.ROOT).equals("sword")?1:2;
			this.damageTool(itemStack,amt,attacker);
		}
		return true;
	}
	@Override
	public boolean canPerformAction(@NotNull ItemStack itemStack,@NotNull ItemAbility itemAbility){
		if(isBroken(itemStack)) return false;
		String type=getProps(itemStack).toolType().toLowerCase();
		if(type.equals("pickaxe")&&itemAbility.equals(ItemAbilities.PICKAXE_DIG)) return true;
		if(type.equals("axe")&&(itemAbility.equals(ItemAbilities.AXE_DIG)||itemAbility.equals(ItemAbilities.AXE_STRIP)||itemAbility.equals(ItemAbilities.AXE_SCRAPE)||itemAbility.equals(ItemAbilities.AXE_WAX_OFF)))
			return true;
		if(type.equals("shovel")&&(itemAbility.equals(ItemAbilities.SHOVEL_DIG)||itemAbility.equals(ItemAbilities.SHOVEL_FLATTEN)||itemAbility.equals(ItemAbilities.SHOVEL_DOUSE)))
			return true;
		if(type.equals("hoe")&&(itemAbility.equals(ItemAbilities.HOE_DIG)||itemAbility.equals(ItemAbilities.HOE_TILL)))
			return true;
		if(type.equals("sword")&&itemAbility.equals(ItemAbilities.SWORD_DIG)) return true;//DO NOT TOUCH
		return false;
	}
	@Override
	public @NotNull InteractionResult useOn(@NotNull UseOnContext context){
		ItemStack itemStack=context.getItemInHand();
		if(isBroken(itemStack)) return InteractionResult.PASS;
		Level level=context.getLevel();
		BlockPos pos=context.getClickedPos();
		BlockState state=level.getBlockState(pos);
		String type=getProps(itemStack).toolType().toLowerCase();
		BlockState modified=null;
		SoundEvent sound=null;
		switch(type){
			case "axe" -> {
				modified=state.getToolModifiedState(context,ItemAbilities.AXE_STRIP,false);
				if(modified!=null) sound=SoundEvents.AXE_STRIP;
				else{
					modified=state.getToolModifiedState(context,ItemAbilities.AXE_SCRAPE,false);
					if(modified!=null) sound=SoundEvents.AXE_SCRAPE;
					else{
						modified=state.getToolModifiedState(context,ItemAbilities.AXE_WAX_OFF,false);
						if(modified!=null) sound=SoundEvents.AXE_WAX_OFF;
					}
				}
			}
			case "hoe" -> {
				modified=state.getToolModifiedState(context,ItemAbilities.HOE_TILL,false);
				if(modified!=null) sound=SoundEvents.HOE_TILL;
			}
			case "shovel" -> {
				modified=state.getToolModifiedState(context,ItemAbilities.SHOVEL_FLATTEN,false);
				if(modified!=null) sound=SoundEvents.SHOVEL_FLATTEN;
				else{
					modified=state.getToolModifiedState(context,ItemAbilities.SHOVEL_DOUSE,false);
					if(modified!=null) sound=SoundEvents.FIRE_EXTINGUISH;
				}
			}
		}
		if(modified!=null){
			Player player=context.getPlayer();
			level.playSound(player,pos,sound,SoundSource.BLOCKS,1F,1F);
			if(!level.isClientSide){
				level.setBlock(pos,modified,11);
				if(player!=null) this.damageTool(itemStack,1,player);
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return super.useOn(context);
	}
	@Override
	public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack itemStack){
		ModularToolProperties props=getProps(itemStack);
		ItemAttributeModifiers.Builder builder=ItemAttributeModifiers.builder();
		float finalDamage=0F;
		float finalSpeed=getBaseSpeedForType(props.toolType());
		if(!isBroken(itemStack)){
			ModularMaterial head=ModularMaterial.byName(props.headMaterial());
			ModularMaterial handle=ModularMaterial.byName(props.handleMaterial());
			finalDamage=getBaseDamageForType(props.toolType())+head.getAttackDamage();
			finalSpeed=getBaseSpeedForType(props.toolType())+handle.getAttackSpeedBonus();
		}
		builder.add(Attributes.ATTACK_DAMAGE,new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,finalDamage,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
		builder.add(Attributes.ATTACK_SPEED,new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,finalSpeed,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
		return builder.build();
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		ModularToolProperties props=getProps(itemStack);
		if(props.toolType().equals("none")) return;
		ModularMaterial head=ModularMaterial.byName(props.headMaterial());
		ModularMaterial binding=ModularMaterial.byName(props.bindingMaterial());
		ModularMaterial handle=ModularMaterial.byName(props.handleMaterial());
		int maxDmg=getMaxDamage(itemStack);
		int miningLvl=getLiveMiningLevel(itemStack);
		float eff=getLiveEfficiency(itemStack);
		float dmg=1F+getBaseDamageForType(props.toolType())+(isBroken(itemStack)?0:head.getAttackDamage());
		float spd=4F+getBaseSpeedForType(props.toolType())+(isBroken(itemStack)?0:handle.getAttackSpeedBonus());
		if(isBroken(itemStack)){
			list.add(Component.literal("!! BROKEN !!").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withBold(true)));
		}
		if(Screen.hasControlDown()){
			list.add(Component.literal("Head: ").withStyle(ChatFormatting.WHITE)
					.append(Component.translatable("dif.material."+head.getId())
							.withStyle(Style.EMPTY.withColor(head.getColor()))));
			list.add(Component.literal("  Durability: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(String.valueOf(head.getHeadDurability())).withStyle(ChatFormatting.YELLOW)));
			list.add(Component.literal("Binding: ").withStyle(ChatFormatting.WHITE)
					.append(Component.translatable("dif.material."+binding.getId())
							.withStyle(Style.EMPTY.withColor(binding.getColor()))));
			list.add(Component.literal("  Durability: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(String.valueOf(binding.getBindingDurability())).withStyle(ChatFormatting.YELLOW)));
			list.add(Component.literal("Handle: ").withStyle(ChatFormatting.WHITE)
					.append(Component.translatable("dif.material."+handle.getId())
							.withStyle(Style.EMPTY.withColor(handle.getColor()))));
			list.add(Component.literal("  Durability Multiplier: ").withStyle(ChatFormatting.WHITE)
					.append(Component.literal(String.valueOf(handle.getHandleDurabilityMultiplier())).withStyle(ChatFormatting.YELLOW)));
		}else{
			list.add(Component.literal("Mining Level: ").withStyle(ChatFormatting.WHITE).append(Component.translatable("dif.mining_level."+miningLvl).withStyle(Style.EMPTY.withColor(miningLevelColor[miningLvl]))));
			int remaining=Math.max(0,maxDmg-itemStack.getDamageValue());
			float ratio=maxDmg>0?(float)remaining/maxDmg:0;
			int durColor=((int)(255*(1-ratio))<<16)|((int)(255*ratio)<<8);
			list.add(Component.literal("Durability: ").append(Component.literal(String.valueOf(remaining)).withStyle(Style.EMPTY.withColor(durColor))).append(Component.literal(" / "+maxDmg).withStyle(ChatFormatting.GRAY)));
			list.add(Component.literal("Efficiency: ").withStyle(ChatFormatting.WHITE).append(Component.literal(String.valueOf(eff)).withStyle(ChatFormatting.GREEN)));
			list.add(Component.literal("Attack Damage: ").withStyle(ChatFormatting.WHITE).append(Component.literal(String.format(Locale.ROOT,"%.1f",dmg)).withStyle(ChatFormatting.RED)));
			list.add(Component.literal("Attack Speed: ").withStyle(ChatFormatting.WHITE).append(Component.literal(String.format(Locale.ROOT,"%.1f",spd)).withStyle(ChatFormatting.YELLOW)));
			list.add(CommonComponents.EMPTY);
			list.add(Component.literal("Press ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal("Shift").withStyle(ChatFormatting.AQUA))
					.append(Component.literal(" for modifiers info.").withStyle(ChatFormatting.GRAY)));
			list.add(Component.literal("Press ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal("Ctrl").withStyle(ChatFormatting.YELLOW))
					.append(Component.literal(" for parts info.").withStyle(ChatFormatting.GRAY)));
		}
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
	@Override
	public @NotNull String getDescriptionId(@NotNull ItemStack itemStack){
		String type=getProps(itemStack).toolType().toLowerCase(Locale.ROOT);
		if(!type.isEmpty()&&!type.equals("none"))
			return super.getDescriptionId(itemStack)+"."+type;
		return super.getDescriptionId(itemStack);
	}
}