package cz.maxtechnik.dif.item.modular.v2;

import cz.maxtechnik.dif.config.DifModClientConfig;
import cz.maxtechnik.dif.init.other.DifModComponents;
import cz.maxtechnik.dif.util.ClientTooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static cz.maxtechnik.dif.item.modular.v2.ModularReforge.*;
import static java.lang.Math.round;
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
	/**
	 * Get tool props. (MODULAR_TOOL_PROPERTIES)
	 * @param itemStack tool
	 * @return props (components)
	 */
	public static ModularToolProperties getProps(ItemStack itemStack){
		ModularToolProperties props=itemStack.get(DifModComponents.MODULAR_TOOL_PROPERTIES.get());
		return props!=null?props:ModularToolProperties.DEFAULT;
	}
	public static boolean isModularTool(ItemStack itemStack){
		return itemStack.getItem() instanceof ModularTool;
	}
	public static ModularTools getToolType(ItemStack itemStack){
		return ModularTools.byName(getProps(itemStack).toolType());
	}
	public static ModularMaterial getMaterial(ModularPartType partType,ItemStack itemStack){
		return switch(partType){
			case HEAD -> ModularMaterial.byName(getProps(itemStack).headMaterial());
			case BINDING -> ModularMaterial.byName(getProps(itemStack).bindingMaterial());
			case HANDLE -> ModularMaterial.byName(getProps(itemStack).handleMaterial());
			default -> ModularMaterial.NONE;
		};
	}
	/**
	 * Is tool broken?
	 * @param itemStack tool
	 * @return Is tool broken? (T/F)
	 */
	public boolean isBroken(ItemStack itemStack){
		int max=getMaxDamage(itemStack);
		return max>0&&itemStack.getDamageValue()>=max-1;
	}
	/**
	 * Get all active material modifiers for a tool (reads props + 3 materials once).
	 * @param itemStack tool
	 * @return EnumSet of active material modifiers
	 */
	public static EnumSet<ModularModifier> getMaterialModifiers(ItemStack itemStack){
		ModularToolProperties props=getProps(itemStack);
		ModularModifier head=ModularMaterial.byName(props.headMaterial()).getModifier();
		ModularModifier binding=ModularMaterial.byName(props.bindingMaterial()).getModifier();
		ModularModifier handle=ModularMaterial.byName(props.handleMaterial()).getModifier();
		return EnumSet.of(head,binding,handle);
	}
	/**
	 * Check if tool has a specific material modifier.
	 * @param itemStack tool
	 * @param modifier modifier
	 * @return T/F
	 */
	public static boolean hasMaterialModifier(ItemStack itemStack,ModularModifier modifier){
		return getMaterialModifiers(itemStack).contains(modifier);
	}
	/**
	 *
	 * Apply damage to tool.
	 * @param itemStack tool
	 * @param amount damage amount
	 * @param entity player
	 */
	public void damageTool(ItemStack itemStack,int amount,LivingEntity entity){
		EnumSet<ModularModifier> mods=getMaterialModifiers(itemStack);
		if(mods.contains(ModularModifier.UNBREAKABLE_MAT)&&entity.level().getRandom().nextBoolean()) return;
		int maxDmg=getMaxDamage(itemStack);
		int currentDmg=itemStack.getDamageValue();
		if(mods.contains(ModularModifier.STONEBOUND)&&currentDmg>=maxDmg/2&&entity.level().getRandom().nextBoolean()) return;
		int newDmg=currentDmg+amount;
		if(newDmg>=maxDmg) itemStack.setDamageValue(maxDmg-1);
		else itemStack.setDamageValue(newDmg);
		if(isBroken(itemStack)&&entity instanceof Player){
			entity.level().playSound(null,entity.getX(),entity.getY(),entity.getZ(),SoundEvents.ITEM_BREAK,SoundSource.PLAYERS,1F,1F);
		}
	}
	/**
	 * Get Durability from components.
	 * @param itemStack tool
	 * @return Durability value.
	 */
	@Override
	public int getMaxDamage(@NotNull ItemStack itemStack){
		ModularToolProperties props=getProps(itemStack);
		int head=ModularMaterial.byName(props.headMaterial()).getHeadDurability();
		int binding=ModularMaterial.byName(props.bindingMaterial()).getBindingDurability();
		int handle=ModularMaterial.byName(props.handleMaterial()).getHandleDurability();
		int modifier=reinforcedLevel(itemStack);
		float reforge=getReforge(itemStack).getDurability();
		float baseMax=(head+binding+handle+modifier)*reforge;
		if(hasMaterialModifier(itemStack,ModularModifier.PRECISE)){
			baseMax*=1.1F;
		}
		return round(baseMax);
	}
	/**
	 * Get Efficiency from components.
	 * @param itemStack tool
	 * @return Efficiency value.
	 */
	private float getLiveEfficiency(ItemStack itemStack){
		if(isBroken(itemStack)) return 1F;
		float head=ModularMaterial.byName(getProps(itemStack).headMaterial()).getHeadEfficiency();
		float modifier=efficiencyLevel(itemStack);
		float reforge=getReforge(itemStack).getEfficiency();
		float eff=(head+modifier)*reforge;
		if(hasMaterialModifier(itemStack,ModularModifier.LIGHTWEIGHT)){
			eff*=1.05F;
		}
		return eff;
	}
	/**
	 * Get All ModularToolModifiers in List from components.
	 * @param itemStack tool
	 * @return All ModularToolModifiers in List.
	 */
	private static List<ModularToolModifiers.entry> getAllModifiers(ItemStack itemStack){
		ModularToolModifiers component=itemStack.get(DifModComponents.MODULAR_TOOL_MODIFIERS);
		if(component==null) return List.of();
		return component.modifiers();
	}
	/**
	 * Get MiningLevel from components or 0 if broken.
	 * @param itemStack tool
	 * @return MiningLevel from components or 0 if broken.
	 */
	private int getLiveMiningLevel(ItemStack itemStack){
		if(isBroken(itemStack)) return 0;
		return ModularMaterial.byName(getProps(itemStack).headMaterial()).getMiningLevel();
	}
	/**
	 * Get bonus Damage for tool type.
	 * @param type tool type
	 * @return Damage value.
	 */
	private float getBaseDamageForType(String type){
		return switch(type.toLowerCase(Locale.ROOT)){
			case "sword","katana" -> 3F;
			case "axe","timber_axe" -> 5F;
			case "battle_axe" -> 6F;
			case "shovel","excavator" -> 1.5F;
			case "hoe" -> 0F;
			default -> 1F;
		};
	}
	/**
	 * Get bonus Speed for tool type.
	 * @param type tool type
	 * @return Speed value.
	 */
	private float getBaseSpeedForType(String type){
		return switch(type.toLowerCase(Locale.ROOT)){
			case "pickaxe","hammer" -> -2.8F;
			case "axe","shovel","timber_axe","excavator" -> -3F;
			case "battle_axe" -> -3.2F;
			case "katana" -> -1F;
			case "hoe" -> -0F;
			default -> -2.4F;
		};
	}
	/**
	 * Get final Efficiency value or 1F if tool is not correct.
	 * @param itemStack tool
	 * @param blockState target block
	 * @return Final Efficiency value.
	 */
	@Override
	public float getDestroySpeed(@NotNull ItemStack itemStack,@NotNull BlockState blockState){
		if(isBroken(itemStack)) return 1.0F;
		String type=getProps(itemStack).toolType().toLowerCase();
		float efficiency=getLiveEfficiency(itemStack);
		if((type.equals("sword")||type.equals("katana"))&&blockState.is(BlockTags.SWORD_EFFICIENT)) return efficiency;
		if(matchesToolType(type,blockState)){
			if(notMeetsMiningLevel(getLiveMiningLevel(itemStack),blockState)) return 1F;
			return efficiency;
		}
		return 1F;
	}
	/**
	 * Is correct tool for drops for target block?
	 * @param itemStack tool
	 * @param blockState target block
	 * @return Is correct tool? (T/F)
	 */
	@Override
	public boolean isCorrectToolForDrops(@NotNull ItemStack itemStack,@NotNull BlockState blockState){
		if(isBroken(itemStack)) return false;
		String type=getProps(itemStack).toolType().toLowerCase();
		if(matchesToolType(type,blockState)){
			return !notMeetsMiningLevel(getLiveMiningLevel(itemStack),blockState);
		}
		return super.isCorrectToolForDrops(itemStack,blockState);
	}
	/**
	 * Check if the tool type matches the block's mineable tag.
	 * Swords are NOT included here – handled separately in {@link #getDestroySpeed}.
	 * @param type tool type (lowercase)
	 * @param blockState target block
	 * @return true if this tool type can mine this block
	 */
	private static boolean matchesToolType(String type,BlockState blockState){
		if((type.equals("pickaxe")||type.equals("hammer"))&&blockState.is(BlockTags.MINEABLE_WITH_PICKAXE)) return true;
		if((type.equals("axe")||type.equals("timber_axe")||type.equals("battle_axe"))&&blockState.is(BlockTags.MINEABLE_WITH_AXE)) return true;
		if((type.equals("shovel")||type.equals("excavator"))&&blockState.is(BlockTags.MINEABLE_WITH_SHOVEL)) return true;
		return type.equals("hoe")&&blockState.is(BlockTags.MINEABLE_WITH_HOE);
	}
	private static boolean notMeetsMiningLevel(int miningLevel,BlockState blockState){
		if(blockState.is(NEEDS_NETHERITE_TOOL)&&miningLevel<4) return true;
		if(blockState.is(BlockTags.NEEDS_DIAMOND_TOOL)&&miningLevel<3) return true;
		if(blockState.is(BlockTags.NEEDS_IRON_TOOL)&&miningLevel<2) return true;
		return blockState.is(BlockTags.NEEDS_STONE_TOOL)&&miningLevel<1;
	}
	public static final TagKey<Block> NEEDS_NETHERITE_TOOL=TagKey.create(Registries.BLOCK,ResourceLocation.withDefaultNamespace("needs_netherite_tool"));
	/**
	 * On block is mined with tool.
	 * @param itemStack tool
	 * @param level level
	 * @param blockState target block
	 * @param blockPos pos
	 * @param miningEntity player
	 * @return Is correct?
	 */
	@Override
	public boolean mineBlock(@NotNull ItemStack itemStack,@NotNull Level level,@NotNull BlockState blockState,@NotNull BlockPos blockPos,@NotNull LivingEntity miningEntity){
		if(!level.isClientSide&&blockState.getDestroySpeed(level,blockPos)!=0F&&!isBroken(itemStack)) this.damageTool(itemStack,1,miningEntity);
		return true;
	}
	/**
	 * On entity is hit with tool.
	 * @param itemStack tool
	 * @param target target entity
	 * @param attacker player
	 * @return Is correct?
	 */
	@Override
	public boolean hurtEnemy(@NotNull ItemStack itemStack,@NotNull LivingEntity target,@NotNull LivingEntity attacker){
		if(!isBroken(itemStack)){
			ModularToolProperties props=getProps(itemStack);
			String type=props.toolType().toLowerCase(Locale.ROOT);
			int amt=(type.equals("sword")||type.equals("katana"))?1:2;
			this.damageTool(itemStack,amt,attacker);
		}
		return true;
	}
	/**
	 * On tool action. (RMB) (simulated)
	 * @param itemStack tool
	 * @param itemAbility action
	 * @return Is correct?
	 */
	@Override
	public boolean canPerformAction(@NotNull ItemStack itemStack,@NotNull ItemAbility itemAbility){
		if(isBroken(itemStack)) return false;
		String type=getProps(itemStack).toolType().toLowerCase();
		if((type.equals(ModularTools.PICKAXE.getName())||type.equals(ModularTools.HAMMER.getName()))&&itemAbility.equals(ItemAbilities.PICKAXE_DIG)) return true;
		else if((type.equals(ModularTools.AXE.getName())||type.equals(ModularTools.TIMBER_AXE.getName())||type.equals(ModularTools.BATTLE_AXE.getName()))&&(itemAbility.equals(ItemAbilities.AXE_DIG)||itemAbility.equals(ItemAbilities.AXE_STRIP)||itemAbility.equals(ItemAbilities.AXE_SCRAPE)||itemAbility.equals(ItemAbilities.AXE_WAX_OFF))) return true;
		else if((type.equals(ModularTools.SHOVEL.getName())||type.equals(ModularTools.EXCAVATOR.getName()))&&(itemAbility.equals(ItemAbilities.SHOVEL_DIG)||itemAbility.equals(ItemAbilities.SHOVEL_FLATTEN)||itemAbility.equals(ItemAbilities.SHOVEL_DOUSE))) return true;
		else if(type.equals(ModularTools.HOE.getName())&&(itemAbility.equals(ItemAbilities.HOE_DIG)||itemAbility.equals(ItemAbilities.HOE_TILL))) return true;
		else return (type.equals(ModularTools.SWORD.getName())||type.equals(ModularTools.KATANA.getName()))&&itemAbility.equals(ItemAbilities.SWORD_DIG);
	}
	/**
	 * On tool action. (RMB)
	 * @param context action context
	 * @return Is correct?
	 */
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
			case "axe","timber_axe","battle_axe" -> {
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
				// CULTIVATOR reforge: AOE tilling at EPIC power (3×3)
				if(getReforge(itemStack)==CULTIVATOR){
					if(ModularMiningHandler.hoeCultivatorTill(context,this,1)){
						Player player=context.getPlayer();
						level.playSound(player,pos,SoundEvents.HOE_TILL,SoundSource.BLOCKS,1F,1F);
						return InteractionResult.sidedSuccess(level.isClientSide);
					}
				}
				// Default: single-block till
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
			// Excavator 3×3 flatten on RMB – delegates to ModularMiningHandler
			case "excavator" -> {
				if(ModularMiningHandler.excavatorFlatten(context,this)){
					Player player=context.getPlayer();
					level.playSound(player,pos,SoundEvents.SHOVEL_FLATTEN,SoundSource.BLOCKS,1F,1F);
					return InteractionResult.sidedSuccess(level.isClientSide);
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
	/**
	 * Get efficiency modifier value.
	 * @param itemStack tool
	 * @return efficiency modifier value.
	 */
	public float efficiencyLevel(ItemStack itemStack){
		int lvl=getModifierLevel(itemStack,ModularModifier.EFFICIENCY);
		return lvl>0?lvl*lvl+1F:0F;
	}
	/**
	 * Get knockback modifier value.
	 * @param itemStack tool
	 * @return knockback modifier value.
	 */
	public float knockbackLevel(ItemStack itemStack){
		return switch(getModifierLevel(itemStack,ModularModifier.KNOCKBACK)){
			case 1 -> 0.2F;
			case 2 -> 0.4F;
			case 3 -> 0.6F;
			default -> 0F;
		};
	}
	/**
	 * Get sharpness modifier value.
	 * @param itemStack tool
	 * @return sharpness modifier value.
	 */
	public float sharpnessDamage(ItemStack itemStack){
		return switch(getModifierLevel(itemStack,ModularModifier.SHARPNESS)){
			case 1 -> 0.5F;
			case 2 -> 1F;
			case 3 -> 1.5F;
			case 4 -> 2F;
			case 5 -> 2.5F;
			case 6 -> 3F;
			default -> 0F;
		};
	}
	/**
	 * Get reinforced modifier value.
	 * @param itemStack tool
	 * @return reinforced modifier value.
	 */
	public int reinforcedLevel(ItemStack itemStack){
		return switch(getModifierLevel(itemStack,ModularModifier.REINFORCED)){
			case 1 -> 256;
			case 2 -> 512;
			case 3 -> 768;
			case 4 -> 1024;
			default -> 0;
		};
	}
	/**
	 * Apply ItemAttributeModifiers.
	 * @param itemStack tool
	 * @return new ItemAttributeModifiers.
	 */
	@Override
	public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack itemStack){
		ModularToolProperties props=getProps(itemStack);
		ItemAttributeModifiers.Builder builder=ItemAttributeModifiers.builder();
		float finalDamage=0F;
		float finalSpeed=getBaseSpeedForType(props.toolType());
		float finalKnockback=0F;
		if(!isBroken(itemStack)){
			ModularMaterial head=ModularMaterial.byName(props.headMaterial());
			ModularMaterial.byName(props.handleMaterial());
			finalDamage=(getBaseDamageForType(props.toolType())+head.getAttackDamage()+sharpnessDamage(itemStack))*getReforge(itemStack).getAttackDamage();
			float basePenalty=Math.abs(getBaseSpeedForType(props.toolType()));
			if(hasMaterialModifier(itemStack,ModularModifier.LIGHTWEIGHT)){
				basePenalty*=0.95F;
			}
			float speedBeforeReforge=4F-basePenalty;
			float reforgeMultiplier=getReforge(itemStack).getAttackSpeed();
			float totalSpeed=speedBeforeReforge*reforgeMultiplier;
			finalSpeed=totalSpeed-4F;
			finalKnockback=knockbackLevel(itemStack);
		}
		builder.add(Attributes.ATTACK_DAMAGE,new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,finalDamage,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
		builder.add(Attributes.ATTACK_SPEED,new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,finalSpeed,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
		builder.add(Attributes.ATTACK_KNOCKBACK,new AttributeModifier(ResourceLocation.withDefaultNamespace("base_attack_knockback"),finalKnockback,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
		return builder.build();
	}
	@Override
	public void inventoryTick(@NotNull ItemStack itemStack,@NotNull Level level,@NotNull Entity entity,int slot,boolean isSelected){
		super.inventoryTick(itemStack,level,entity,slot,isSelected);
		if(level.isClientSide) return;
		long gameTime=level.getGameTime();
		// Self-repair: only evaluate when tick aligns and tool is damaged (cheap guard first)
		if(itemStack.isDamaged()&&(gameTime%40==0||gameTime%80==0)){
			EnumSet<ModularModifier> mods=getMaterialModifiers(itemStack);
			if(gameTime%40==0&&mods.contains(ModularModifier.RENEWABLE)){
				itemStack.setDamageValue(Math.max(0,itemStack.getDamageValue()-1));
			}else if(gameTime%80==0&&mods.contains(ModularModifier.SELF_REPAIR)){
				itemStack.setDamageValue(Math.max(0,itemStack.getDamageValue()-1));
			}
		}
		// Magnetic: only when held in hand and every 10 ticks
		if(gameTime%10==0&&entity instanceof Player player&&(player.getMainHandItem()==itemStack||player.getOffhandItem()==itemStack)){
			if(getMaterialModifiers(itemStack).contains(ModularModifier.MAGNETIC)){
				AABB box=player.getBoundingBox().inflate(5D);
				for(ItemEntity item: level.getEntitiesOfClass(ItemEntity.class,box)){
					if(item.isAlive()&&!item.hasPickUpDelay()) item.teleportTo(player.getX(),player.getY()+0.5,player.getZ());
				}
				for(ExperienceOrb orb: level.getEntitiesOfClass(ExperienceOrb.class,box)){
					if(orb.isAlive()) orb.teleportTo(player.getX(),player.getY()+0.5,player.getZ());
				}
			}
		}
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		ModularToolProperties props=getProps(itemStack);
		if(props.toolType().equals("none")) return;
		String type=props.toolType().toLowerCase(Locale.ROOT);
		boolean isWeapon=type.equals("sword")||type.equals("katana")||type.equals("battle_axe");
		ModularMaterial head=ModularMaterial.byName(props.headMaterial());
		ModularMaterial binding=ModularMaterial.byName(props.bindingMaterial());
		ModularMaterial handle=ModularMaterial.byName(props.handleMaterial());
		ModularReforge reforge=getReforge(itemStack);
		int reforgeColor=0xAA00AA;
		// ─── pre-compute raw (no-reforge) values ───
		float rawEff=head.getHeadEfficiency()+efficiencyLevel(itemStack);
		if(hasMaterialModifier(itemStack,ModularModifier.LIGHTWEIGHT)) rawEff*=1.05F;
		float effMultiplier=reforge.getEfficiency();
		float finalEff=rawEff*effMultiplier;
		float effBonus=rawEff*(effMultiplier-1F);
		float rawDmg=getBaseDamageForType(type)+head.getAttackDamage()+sharpnessDamage(itemStack);
		float dmgMultiplier=reforge.getAttackDamage();
		float finalDmg=1F+rawDmg*dmgMultiplier;
		float dmgBonus=rawDmg*(dmgMultiplier-1F);
		float rawSpdPenalty=Math.abs(getBaseSpeedForType(type));
		if(hasMaterialModifier(itemStack,ModularModifier.LIGHTWEIGHT)){
			rawSpdPenalty*=0.95F;
		}
		float spdBeforeReforge=4F-rawSpdPenalty;
		float spdMultiplier=reforge.getAttackSpeed();
		float finalSpd=spdBeforeReforge*spdMultiplier;
		float spdBonus=finalSpd-spdBeforeReforge;
		int rawDur=head.getHeadDurability()+binding.getBindingDurability()+handle.getHandleDurability()+reinforcedLevel(itemStack);
		if(hasMaterialModifier(itemStack,ModularModifier.PRECISE)) rawDur=(int)(rawDur*1.1F);
		float durMultiplier=reforge.getDurability();
		int maxDmg=Math.round(rawDur*durMultiplier);
		int durBonus=maxDmg-rawDur;
		int remaining=Math.max(0,maxDmg-itemStack.getDamageValue());
		float ratio=maxDmg>0?(float)remaining/maxDmg:0;
		int durColor=((int)(255*(1-ratio))<<16)|((int)(255*ratio)<<8);
		int miningLvl=getLiveMiningLevel(itemStack);
		// Fortune/Looting from LUCK modifier
		int luckLevel=getModifierLevel(itemStack,ModularModifier.LUCK);
		// Broken warning
		if(isBroken(itemStack)){
			list.add(Component.literal("!! BROKEN !!").withStyle(Style.EMPTY.withColor(0xFF3333).withBold(true)));
			list.add(CommonComponents.EMPTY);
		}
		// Level, Tier, Damage, Efficiency, Fortune/Looting, Attack Speed, Durability
		list.add(Component.literal("Level: ").withStyle(ChatFormatting.GRAY).append(Component.translatable("dif.mining_level."+miningLvl).withStyle(Style.EMPTY.withColor(ModularMaterial.miningLevelColor[miningLvl]))));
		appendStatLine(list,"Damage: ",String.format(Locale.ROOT,"+%.1f",finalDmg),ChatFormatting.RED,dmgBonus);
		appendStatLine(list,"Efficiency: ",String.format(Locale.ROOT,"%.1f",finalEff),ChatFormatting.GREEN,effBonus);
		if(luckLevel>0){
			String luckLabel=isWeapon?"Looting: ":"Fortune: ";
			appendStatLine(list,luckLabel,String.valueOf(luckLevel),ChatFormatting.GREEN,0F);
		}
		appendStatLine(list,"Speed: ",String.format(Locale.ROOT,"+%.1f",finalSpd),ChatFormatting.YELLOW,spdBonus);
		appendStatLine(list,"Durability: ",remaining+"/"+maxDmg,ChatFormatting.WHITE,durBonus,durColor);
		list.add(CommonComponents.EMPTY);
		// Check config & shift key
		boolean compactMode=DifModClientConfig.COMPACT_TOOLTIPS.get();
		boolean showAll=!compactMode;
		if(compactMode&&FMLEnvironment.dist.isClient())
			showAll=ClientTooltipHelper.isShiftDown();
		if(showAll){
			// Parts
			list.add(Component.literal("Head: ").withStyle(ChatFormatting.GRAY).append(Component.translatable("dif.material."+head.getName()).withStyle(Style.EMPTY.withColor(head.getColor()))));
			list.add(Component.literal("Binding: ").withStyle(ChatFormatting.GRAY).append(Component.translatable("dif.material."+binding.getName()).withStyle(Style.EMPTY.withColor(binding.getColor()))));
			list.add(Component.literal("Handle: ").withStyle(ChatFormatting.GRAY).append(Component.translatable("dif.material."+handle.getName()).withStyle(Style.EMPTY.withColor(handle.getColor()))));
			list.add(CommonComponents.EMPTY);
			// Material modifiers
			List<String> materialModifiers=getMaterialModifierNames(head,binding,handle);
			for(String materialModifier: materialModifiers){
				list.add(Component.translatable("dif.modifier."+materialModifier).withStyle(Style.EMPTY.withColor(ModularModifier.byName(materialModifier).getColor())));
			}
			// Applied modifiers / enchants
			List<ModularToolModifiers.entry> appliedMods=getAllModifiers(itemStack);
			if(!appliedMods.isEmpty()){
				list.add(CommonComponents.EMPTY);
				for(ModularToolModifiers.entry entry: appliedMods){
					ModularModifier modifier=ModularModifier.byName(entry.id());
					boolean isLegendary=modifier==ModularModifier.MENDING||entry.lvl()>modifier.getMaxLvl();
					int modColor=isLegendary?0xFFAA00:0x5555FF;
					MutableComponent nameComp=Component.translatable("dif.modifier."+entry.id());
					if(modifier.getMaxLvl()>1) nameComp=nameComp.copy().append(Component.literal(" ")).append(Component.translatable("enchantment.level."+entry.lvl()).withStyle(Style.EMPTY.withColor(modColor)));
					list.add(nameComp.withStyle(Style.EMPTY.withColor(modColor)));
				}
			}
			// Reforge
			if(!reforge.getName().isEmpty()&&!reforge.getName().equals("none")){
				list.add(CommonComponents.EMPTY);
				list.add(Component.translatable("dif.reforge."+reforge.getName()).withStyle(Style.EMPTY.withColor(reforgeColor).withBold(true)));
				if(reforge.hasDescription()){
					list.add(Component.translatable("dif.reforge."+reforge.getName()+".desc").withStyle(Style.EMPTY.withColor(0x9999AA).withItalic(true)));
				}
			}
		}else{
			list.add(Component.literal("Hold ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal("SHIFT").withStyle(ChatFormatting.YELLOW))
					.append(Component.literal(" for details").withStyle(ChatFormatting.GRAY)));
		}
	}
	/**
	 * Helper: add a stat line with optional reforge bonus in parentheses.
	 * Uses value's own color for the value part, bonus in green.
	 */
	private static void appendStatLine(List<Component> list,String label,String value,ChatFormatting valueColor,float bonus){
		appendStatLine(list,label,value,valueColor,bonus,-1);
	}
	private static void appendStatLine(List<Component> list,String label,String value,ChatFormatting valueColor,float bonus,int valueColorOverride){
		Component valComp=valueColorOverride>=0
				?Component.literal(value).withStyle(Style.EMPTY.withColor(valueColorOverride))
				:Component.literal(value).withStyle(valueColor);
		MutableComponent line=Component.literal(label).withStyle(ChatFormatting.GRAY).append(valComp);
		if(Math.abs(bonus)>=0.05F){
			line=line.append(Component.literal(String.format(Locale.ROOT," (%.1f)",bonus)).withStyle(Style.EMPTY.withColor(0x5555FF)));
		}
		list.add(line);
	}
	/**
	 * Get unique material modifier names for tooltip (deduplicates, preserves insertion order).
	 * @param head head material
	 * @param binding binding material
	 * @param handle handle material
	 * @return Unique modifier names in order: head, binding (if different), handle (if different).
	 */
	private static @NotNull List<String> getMaterialModifierNames(ModularMaterial head,ModularMaterial binding,ModularMaterial handle){
		LinkedHashSet<String> seen=new LinkedHashSet<>();
		seen.add(head.getModifier().getName().toLowerCase(Locale.ROOT));
		seen.add(binding.getModifier().getName().toLowerCase(Locale.ROOT));
		seen.add(handle.getModifier().getName().toLowerCase(Locale.ROOT));
		return new ArrayList<>(seen);
	}
	@Override
	public <T extends LivingEntity> int damageItem(@NotNull ItemStack itemStack,int amount,@Nullable T entity,@NotNull Consumer<Item> onBroken){
		return 0;
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
	public boolean isFoil(@NotNull ItemStack itemStack){
		return false;
	}
	/**
	 * Create DescriptionId from tool type
	 * @param itemStack tool
	 * @return new DescriptionId
	 */
	@Override
	public @NotNull String getDescriptionId(@NotNull ItemStack itemStack){
		String type=getProps(itemStack).toolType();
		if(type.isEmpty()||type.equals("none")) return super.getDescriptionId(itemStack);
		String reforge=getReforge(itemStack).getName();
		if(!reforge.isEmpty()&&!reforge.equals("none")) return super.getDescriptionId(itemStack)+"."+type+"."+reforge;
		return super.getDescriptionId(itemStack)+"."+type;
	}
	/**
	 * Apply color to component from tool tier.
	 * @param itemStack tool
	 * @return new Component with color.
	 */
	@Override
	public @NotNull Component getName(@NotNull ItemStack itemStack){
		return Component.translatable(getDescriptionId(itemStack)).withStyle(Style.EMPTY.withItalic(false));
	}
	/**
	 * Add modifier to tool.
	 * @param provider provider
	 * @param itemStack tool
	 * @param modifier modifier
	 */
	@SuppressWarnings("unused")
	public static void setModifier(HolderLookup.Provider provider,ItemStack itemStack,ModularModifier modifier){
		setModifier(provider,itemStack,modifier,1);
	}
	/**
	 * Add modifier to tool with lvl.
	 * @param provider provider
	 * @param itemStack tool
	 * @param modifier modifier
	 * @param lvl modifier lvl
	 */
	public static void setModifier(HolderLookup.Provider provider,ItemStack itemStack,ModularModifier modifier,int lvl){
		ModularToolModifiers component=itemStack.get(DifModComponents.MODULAR_TOOL_MODIFIERS);
		if(component==null) component=ModularToolModifiers.DEFAULT;
		List<ModularToolModifiers.entry> newModifiers=new ArrayList<>(component.modifiers());
		boolean found=false;
		for(int i=0;i<newModifiers.size();i++){
			if(newModifiers.get(i).id().equals(modifier.getName())){
				ModularToolModifiers.entry existing=newModifiers.get(i);
				newModifiers.set(i,new ModularToolModifiers.entry(existing.id(),lvl));
				found=true;
				break;
			}
		}
		if(!found) newModifiers.add(new ModularToolModifiers.entry(modifier.getName(),lvl));
		itemStack.set(DifModComponents.MODULAR_TOOL_MODIFIERS,new ModularToolModifiers(newModifiers));
		switch(modifier){
			case SILK_TOUCH -> addEnchantment(provider,itemStack,Enchantments.SILK_TOUCH,lvl-getEnchantmentLevel(provider,itemStack,Enchantments.SILK_TOUCH));
			case LUCK -> {
				addEnchantment(provider,itemStack,Enchantments.FORTUNE,lvl-getEnchantmentLevel(provider,itemStack,Enchantments.FORTUNE));
				addEnchantment(provider,itemStack,Enchantments.LOOTING,lvl-getEnchantmentLevel(provider,itemStack,Enchantments.LOOTING));
			}
			case SWEEPING_EDGE -> addEnchantment(provider,itemStack,Enchantments.SWEEPING_EDGE,lvl-getEnchantmentLevel(provider,itemStack,Enchantments.SWEEPING_EDGE));
			case MENDING -> addEnchantment(provider,itemStack,Enchantments.MENDING,lvl-getEnchantmentLevel(provider,itemStack,Enchantments.MENDING));
			case VOLCANIC -> addEnchantment(provider,itemStack,Enchantments.FIRE_ASPECT,lvl-getEnchantmentLevel(provider,itemStack,Enchantments.FIRE_ASPECT));
			default -> {
			}
		}
	}
	/**
	 * Check if is modifier on tool.
	 * @param itemStack tool
	 * @param modifier modifier
	 * @return Is modifier on tool?
	 */
	public static boolean isModifier(ItemStack itemStack,ModularModifier modifier){
		ModularToolModifiers component=itemStack.get(DifModComponents.MODULAR_TOOL_MODIFIERS);
		if(component==null) return false;
		for(ModularToolModifiers.entry entry: component.modifiers())
			if(entry.id().equals(modifier.getName())) return true;
		return false;
	}
	/**
	 * Get modifier lvl on tool.
	 * @param itemStack tool
	 * @param modifier modifier
	 * @return Modifier lvl.
	 */
	public static int getModifierLevel(ItemStack itemStack,ModularModifier modifier){
		ModularToolModifiers component=itemStack.get(DifModComponents.MODULAR_TOOL_MODIFIERS);
		if(component==null) return 0;
		for(ModularToolModifiers.entry entry: component.modifiers())
			if(entry.id().equals(modifier.getName())) return entry.lvl();
		return 0;
	}
	/**
	 * Remove modifier from tool.
	 * @param provider provider
	 * @param itemStack tool
	 * @param modifier modifier
	 */
	@SuppressWarnings("unused")
	public static void removeModifier(HolderLookup.Provider provider,ItemStack itemStack,ModularModifier modifier){
		int oldLvl=getModifierLevel(itemStack,modifier);
		ModularToolModifiers component=itemStack.get(DifModComponents.MODULAR_TOOL_MODIFIERS);
		if(component==null) return;
		List<ModularToolModifiers.entry> newModifiers=component.modifiers().stream().filter(entry->!entry.id().equals(modifier.getName())).collect(Collectors.toList());
		if(newModifiers.size()!=component.modifiers().size()){
			itemStack.set(DifModComponents.MODULAR_TOOL_MODIFIERS,new ModularToolModifiers(newModifiers));
			switch(modifier){
				case SILK_TOUCH -> subtractEnchantment(provider,itemStack,Enchantments.SILK_TOUCH,oldLvl);
				case LUCK -> {
					subtractEnchantment(provider,itemStack,Enchantments.FORTUNE,oldLvl);
					subtractEnchantment(provider,itemStack,Enchantments.LOOTING,oldLvl);
				}
				case SWEEPING_EDGE -> subtractEnchantment(provider,itemStack,Enchantments.SWEEPING_EDGE,oldLvl);
				case MENDING -> subtractEnchantment(provider,itemStack,Enchantments.MENDING,oldLvl);
				case VOLCANIC -> subtractEnchantment(provider,itemStack,Enchantments.FIRE_ASPECT,oldLvl);
				default -> {
				}
			}
		}
	}
	/**
	 * Upgrade modifier on tool by 1 lvl.
	 * @param provider provider
	 * @param itemStack tool
	 * @param modifier modifier
	 */
	public static void upgradeModifier(HolderLookup.Provider provider,ItemStack itemStack,ModularModifier modifier){
		upgradeModifier(provider,itemStack,modifier,1);
	}
	/**
	 * Upgrade modifier on tool by lvl amount.
	 * @param provider provider
	 * @param itemStack tool
	 * @param modifier modifier
	 * @param lvl lvl
	 */
	public static void upgradeModifier(HolderLookup.Provider provider,ItemStack itemStack,ModularModifier modifier,int lvl){
		if(isModifier(itemStack,modifier)){
			int newLvl=getModifierLevel(itemStack,modifier)+lvl;
			setModifier(provider,itemStack,modifier,newLvl);
		}else setModifier(provider,itemStack,modifier,lvl);
	}
	/**
	 * Get enchantment lvl from tool.
	 * @param provider provider
	 * @param itemStack tool
	 * @param enchantment enchantment
	 * @return Enchantment lvl.
	 */
	public static int getEnchantmentLevel(HolderLookup.Provider provider,ItemStack itemStack,ResourceKey<Enchantment> enchantment){
		Holder<Enchantment> holder=getEnchantmentHolder(provider,enchantment);
		return itemStack.getEnchantmentLevel(holder);
	}
	/**
	 * Add/Create enchantment on tool.
	 * @param provider provider
	 * @param itemStack tool
	 * @param enchantment enchantment
	 * @param lvl lvl
	 */
	public static void addEnchantment(HolderLookup.Provider provider,ItemStack itemStack,ResourceKey<Enchantment> enchantment,int lvl){
		manipulateEnchantment(provider,itemStack,enchantment,lvl,0);
	}
	/**
	 * Subtract/Remove enchantment on tool.
	 * @param provider provider
	 * @param itemStack tool
	 * @param enchantment enchantment
	 * @param lvl lvl
	 */
	public static void subtractEnchantment(HolderLookup.Provider provider,ItemStack itemStack,ResourceKey<Enchantment> enchantment,int lvl){
		manipulateEnchantment(provider,itemStack,enchantment,lvl,1);
	}
	/**
	 * Set/Create/Remove enchantment lvl on tool.
	 * @param provider provider
	 * @param itemStack tool
	 * @param enchantment enchantment
	 * @param lvl lvl
	 */
	@SuppressWarnings("unused")
	public static void setEnchantment(HolderLookup.Provider provider,ItemStack itemStack,ResourceKey<Enchantment> enchantment,int lvl){
		manipulateEnchantment(provider,itemStack,enchantment,lvl,2);
	}
	/**
	 * Manipulate with tool enchantments.
	 * @param provider provider
	 * @param itemStack tool
	 * @param enchantment enchantment
	 * @param lvl lvl
	 * @param mode mode
	 */
	private static void manipulateEnchantment(HolderLookup.Provider provider,ItemStack itemStack,ResourceKey<Enchantment> enchantment,int lvl,int mode){
		Holder<Enchantment> enchantmentHolder=getEnchantmentHolder(provider,enchantment);
		EnchantmentHelper.updateEnchantments(itemStack,mutable->{
			if(lvl<=0) mutable.removeIf(holder->holder.is(enchantment));
			else{
				switch(mode){
					case 0 -> mutable.set(enchantmentHolder,itemStack.getEnchantmentLevel(enchantmentHolder)+lvl);
					case 1 -> {
						if(itemStack.getEnchantmentLevel(enchantmentHolder)-lvl<=0) manipulateEnchantment(provider,itemStack,enchantment,-1,0);
						else mutable.set(enchantmentHolder,itemStack.getEnchantmentLevel(enchantmentHolder)-lvl);
					}
					case 2 -> mutable.set(enchantmentHolder,lvl);
					default -> {
					}
				}
			}
		});
		hideEnchantments(itemStack);
	}
	private static void hideEnchantments(ItemStack itemStack){
		var enchants=itemStack.get(DataComponents.ENCHANTMENTS);
		if(enchants!=null) itemStack.set(DataComponents.ENCHANTMENTS,enchants.withTooltip(false));
	}
	/**
	 * Get Holder<Enchantment> of enchantment.
	 * @param provider provider
	 * @param enchantment enchantment
	 * @return Holder<Enchantment>
	 */
	private static Holder<Enchantment> getEnchantmentHolder(HolderLookup.Provider provider,ResourceKey<Enchantment> enchantment){
		return provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantment);
	}
	/**
	 * Set new ModularReforge to tool.
	 * @param provider provider
	 * @param itemStack tool
	 * @param reforge reforge
	 */
	@SuppressWarnings("unused")
	public static void setReforge(HolderLookup.Provider provider,ItemStack itemStack,ModularReforge reforge){
		ModularToolProperties props=getProps(itemStack);
		ModularReforge oldReforge=getReforge(itemStack);
		if(oldReforge==null) return;
		if(oldReforge.equals(reforge)) return;
		itemStack.set(DifModComponents.MODULAR_TOOL_PROPERTIES.get(),new ModularToolProperties(props.toolType(),props.headMaterial(),props.bindingMaterial(),props.handleMaterial(),reforge.name()));
		if(oldReforge.equals(GLEAMING)) subtractEnchantment(provider,itemStack,Enchantments.FORTUNE,1);
		if(reforge.equals(GLEAMING)) addEnchantment(provider,itemStack,Enchantments.FORTUNE,1);
		if(oldReforge.equals(HARVESTER)) subtractEnchantment(provider,itemStack,Enchantments.FORTUNE,1);
		if(reforge.equals(HARVESTER)) addEnchantment(provider,itemStack,Enchantments.FORTUNE,1);
		if(oldReforge.equals(REAPER)) subtractEnchantment(provider,itemStack,Enchantments.LOOTING,1);
		if(reforge.equals(REAPER)) addEnchantment(provider,itemStack,Enchantments.LOOTING,1);
	}
	/**
	 * Get ModularReforge from tool.
	 * @param itemStack tool
	 * @return ModularReforge.
	 */
	public static ModularReforge getReforge(ItemStack itemStack){
		ModularToolProperties props=getProps(itemStack);
		return byName(props.reforge());
	}
}