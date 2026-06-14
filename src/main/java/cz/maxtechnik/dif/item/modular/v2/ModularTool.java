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
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class ModularTool extends DiggerItem{
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
		},BlockTags.MINEABLE_WITH_PICKAXE,new Properties().stacksTo(1).fireResistant());
	}
	private ModularToolProperties getProps(ItemStack itemStack){
		ModularToolProperties props=itemStack.get(DifModComponents.MODULAR_TOOL_PROPERTIES.get());
		return props!=null?props:ModularToolProperties.DEFAULT;
	}
	// Pomocná metoda pro zjištění, zda je nástroj zlomený
	public boolean isBroken(ItemStack itemStack){
		return itemStack.getDamageValue()>=getMaxDamage(itemStack);
	}
	// Vlastní bezpečná metoda pro aplikaci poškození nástroje
	public void damageTool(ItemStack itemStack,int amount,LivingEntity entity){
		int maxDmg=getMaxDamage(itemStack);
		int currentDmg=itemStack.getDamageValue();
		int newDmg=currentDmg+amount;
		if(newDmg>=maxDmg){
			itemStack.setDamageValue(maxDmg); // Zasekne se na max damage (vstup do stavu BROKEN)
			if(entity instanceof Player){
				entity.level().playSound(null,entity.getX(),entity.getY(),entity.getZ(),
						SoundEvents.ITEM_BREAK,SoundSource.PLAYERS,1.0F,1.0F);
			}
		}else{
			itemStack.setDamageValue(newDmg);
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
		ModularTier tier=ModularTier.byName(props.tier());
		return (int)(((head.getHeadDurability()+binding.getBindingDurability())+handle.getHandleDurability())*tier.getDurabilityModifier());
	}
	private float getLiveEfficiency(ItemStack itemStack){
		if(isBroken(itemStack)) return 1F;
		return ModularMaterial.byName(getProps(itemStack).headMaterial()).getHeadEfficiency()*ModularTier.byName(getProps(itemStack).tier()).getEfficiencyModifier();
	}
	private int getLiveMiningLevel(ItemStack itemStack){
		if(isBroken(itemStack)) return 0;
		return ModularMaterial.byName(getProps(itemStack).headMaterial()).getMiningLevel();
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
			finalDamage=(getBaseDamageForType(props.toolType())+head.getAttackDamage())*ModularTier.byName(props.tier()).getAttackDamageModifier();
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
		float dmg=1F+(getBaseDamageForType(props.toolType())+head.getAttackDamage())*ModularTier.byName(props.tier()).getAttackDamageModifier();
		float spd=4F+getBaseSpeedForType(props.toolType())+handle.getAttackSpeedBonus();
		int remaining=Math.max(0,maxDmg-itemStack.getDamageValue());
		float ratio=maxDmg>0?(float)remaining/maxDmg:0;
		int durColor=((int)(255*(1-ratio))<<16)|((int)(255*ratio)<<8);
		if(isBroken(itemStack)){
			list.add(Component.literal(" !! BROKEN !! ")
					.withStyle(Style.EMPTY.withColor(0xFF3333).withBold(true)));
			list.add(CommonComponents.EMPTY);
		}
		list.add(
				Component.literal("───── Stats ─────")
						.withStyle(Style.EMPTY.withColor(0x6644BB))
		);
		list.add(
				Component.literal("Tier: ").withStyle(ChatFormatting.GRAY)
						.append(Component.translatable("dif.tier."+props.tier()).withColor(ModularTier.byName(props.tier()).getColor()))
		);
		list.add(
				Component.literal("Mining: ").withStyle(ChatFormatting.GRAY)
						.append(Component.translatable("dif.mining_level."+miningLvl)
								.withStyle(Style.EMPTY.withColor(ModularMaterial.miningLevelColor[miningLvl])))
		);
		list.add(
				Component.literal("Durability: ").withStyle(ChatFormatting.GRAY)
						.append(Component.literal(String.valueOf(remaining))
								.withStyle(Style.EMPTY.withColor(durColor)))
						.append(Component.literal(" / "+maxDmg)
								.withStyle(ChatFormatting.DARK_GRAY))
		);
		list.add(
				Component.literal("Efficiency: ").withStyle(ChatFormatting.GRAY)
						.append(Component.literal(String.format(Locale.ROOT,"%.1f",eff))
								.withStyle(ChatFormatting.GREEN))
		);
		list.add(
				Component.literal("Damage: ").withStyle(ChatFormatting.GRAY)
						.append(Component.literal(String.format(Locale.ROOT,"%.1f",dmg))
								.withStyle(ChatFormatting.RED))
		);
		list.add(
				Component.literal("Speed: ").withStyle(ChatFormatting.GRAY)
						.append(Component.literal(String.format(Locale.ROOT,"%.1f",spd))
								.withStyle(ChatFormatting.YELLOW))
		);
		list.add(
				Component.literal("───── Parts ─────")
						.withStyle(Style.EMPTY.withColor(0x6644BB))
		);
		list.add(
				Component.literal(" Head: ").withStyle(Style.EMPTY.withColor(0x888888))
						.append(Component.translatable("dif.material."+head.getName())
								.withStyle(Style.EMPTY.withColor(head.getColor())))
						.append(Component.literal("  "+head.getHeadDurability())
								.withStyle(Style.EMPTY.withColor(0xFFAA00)))
		);
		list.add(
				Component.literal(" Binding: ").withStyle(Style.EMPTY.withColor(0x888888))
						.append(Component.translatable("dif.material."+binding.getName())
								.withStyle(Style.EMPTY.withColor(binding.getColor())))
						.append(Component.literal("  "+binding.getBindingDurability())
								.withStyle(Style.EMPTY.withColor(0xFFAA00)))
		);
		list.add(
				Component.literal(" Handle: ").withStyle(Style.EMPTY.withColor(0x888888))
						.append(Component.translatable("dif.material."+handle.getName())
								.withStyle(Style.EMPTY.withColor(handle.getColor())))
						.append(Component.literal("  "+
										handle.getHandleDurability())
								.withStyle(Style.EMPTY.withColor(0xFFAA00)))
		);
		list.add(
				Component.literal("───── Modifiers ─────")
						.withStyle(Style.EMPTY.withColor(0x6644BB))
		);
		ArrayList<String> materialModifiers=getStrings(head,binding,handle);
		for(String materialModifier: materialModifiers){
			list.add(
					Component.translatable("dif.modifier."+materialModifier).withStyle(Style.EMPTY.withColor(head.getColor()))
			);
		}
	}
	private static @NotNull ArrayList<String> getStrings(ModularMaterial head,ModularMaterial binding,ModularMaterial handle){
		ArrayList<String> materialModifiers=new ArrayList<>();
		String headModifier=head.getModifier().getName().toLowerCase(Locale.ROOT);
		String bindingModifier=binding.getModifier().getName().toLowerCase(Locale.ROOT);
		String handleModifier=handle.getModifier().getName().toLowerCase(Locale.ROOT);
		materialModifiers.add(headModifier);
		if(!materialModifiers.get(0).equals(bindingModifier))
			materialModifiers.add(bindingModifier);
		if(!materialModifiers.get(0).equals(handleModifier))
			if(!(materialModifiers.size()==1)){
				if(!materialModifiers.get(1).equals(handleModifier))
					materialModifiers.add(handleModifier);
			}else materialModifiers.add(handleModifier);
		return materialModifiers;
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
	@Override
	public @NotNull Component getName(@NotNull ItemStack itemStack){
		int rarityColor=ModularTier.byName(getProps(itemStack).tier()).getColor();
		return Component.translatable(getDescriptionId(itemStack))
				.withStyle(Style.EMPTY
						.withColor(rarityColor)
						.withItalic(false));
	}
	public void addModifier(ItemStack itemStack,ModularModifier modifier,int lvl){
		ModularToolModifiers component=itemStack.get(DifModComponents.MODULAR_TOOL_MODIFIERS);
		if(component==null) return;
		component.modifiers().add(new ModularToolModifiers.entry(modifier.getName(),lvl));
		itemStack.set(DifModComponents.MODULAR_TOOL_MODIFIERS,component);
	}
	public boolean isModifier(ItemStack itemStack,ModularModifier modifier){
		ModularToolModifiers component=itemStack.get(DifModComponents.MODULAR_TOOL_MODIFIERS);
		if(component==null) return false;
		for(ModularToolModifiers.entry entry: component.modifiers()){
			if(entry.id().equals(modifier.getName())) return true;
		}
		return false;
	}
	public int getModifierLvl(ItemStack itemStack,ModularModifier modifier){
		ModularToolModifiers component=itemStack.get(DifModComponents.MODULAR_TOOL_MODIFIERS);
		if(component==null) return -3;
		for(ModularToolModifiers.entry entry: component.modifiers()){
			if(entry.id().equals(modifier.getName())) return entry.lvl();
		}
		return -3;
	}
	public void setModifierLvl(ItemStack itemStack,ModularModifier modifier,int lvl){
		ModularToolModifiers component=itemStack.get(DifModComponents.MODULAR_TOOL_MODIFIERS);
		if(component==null) return;
		List<ModularToolModifiers.entry> newModifiers=new ArrayList<>();
		boolean found=false;
		for(ModularToolModifiers.entry entry: component.modifiers()){
			if(entry.id().equals(modifier.getName())){
				newModifiers.add(new ModularToolModifiers.entry(entry.id(),lvl));
				found=true;
			}else{
				newModifiers.add(entry);
			}
		}
		if(found)
			itemStack.set(DifModComponents.MODULAR_TOOL_MODIFIERS,new ModularToolModifiers(component.toolType(),newModifiers));
	}
	public void removeModifier(ItemStack itemStack,ModularModifier modifier){
		ModularToolModifiers component=itemStack.get(DifModComponents.MODULAR_TOOL_MODIFIERS);
		if(component==null) return;
		List<ModularToolModifiers.entry> newModifiers=new ArrayList<>();
		boolean removed=false;
		for(ModularToolModifiers.entry entry: component.modifiers()){
			if(entry.id().equals(modifier.getName())) removed=true;
			else newModifiers.add(entry);

		}
		if(removed)
			itemStack.set(DifModComponents.MODULAR_TOOL_MODIFIERS,new ModularToolModifiers(component.toolType(),newModifiers));
	}
}











