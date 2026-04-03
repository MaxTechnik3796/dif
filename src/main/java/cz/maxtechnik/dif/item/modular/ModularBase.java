package cz.maxtechnik.dif.item.modular;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;
public abstract class ModularBase extends DiggerItem{
	protected abstract TagKey<Block> getMineableTag();
	public String[] materials={
			"Wood",
			"Stone",
			"Copper",
			"Iron",
			"Gold",
			"Diamond",
			"Obsidian",
			"Netherite"
	};
	public static int repairAmount=5, cheepRepairAmount=15;
	protected String material;
	protected int defaultMiningLevel, defaultDurability, defaultEfficiency;
	protected float defaultAttackDamage, defaultAttackSpeed;
	protected static int defaultMaxModifiers=3;
	protected static int[] efficiencyModifierStages={3,5,7}, efficiencyModifierLevels={2,4,6}, fortuneModifierStages={3,5,7}, fortuneModifierLevels={1,2,3};
	public ModularBase(int durability,int efficiency,int miningLevel,float attackDamage,float attackSpeed,String material){
		super(attackDamage,attackSpeed,new Tier(){
			@Override
			public int getUses(){
				return 1;
			}
			@Override
			public float getSpeed(){
				return 0.1F;
			}
			@Override
			public float getAttackDamageBonus(){
				return 0.1F;
			}
			@Override
			public int getLevel(){
				return 0;
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
		this.material=material;
		this.defaultDurability=durability;
		this.defaultEfficiency=efficiency;
		this.defaultMiningLevel=miningLevel;
		this.defaultAttackDamage=attackDamage;
		this.defaultAttackSpeed=attackSpeed;
	}
	@Override
	public boolean isCorrectToolForDrops(ItemStack itemStack,@NotNull BlockState blockState){
		if(!itemStack.hasTag()) return super.isCorrectToolForDrops(itemStack,blockState);
		CompoundTag tag=itemStack.getOrCreateTag();
		if(tag.getBoolean("Broken")) return false;
		int level=tag.getInt("MiningLevel")+tag.getInt("BonusMiningLevel");
		if(tag.getInt("SpecialMiningLevel")>level) level=tag.getInt("SpecialMiningLevel");
		if(blockState.is(BlockTags.NEEDS_DIAMOND_TOOL)&&level<3) return false;
		if(blockState.is(BlockTags.NEEDS_IRON_TOOL)&&level<2) return false;
		if(blockState.is(BlockTags.NEEDS_STONE_TOOL)&&level<1) return false;
		return blockState.is(getMineableTag());
	}
	@Override
	public float getDestroySpeed(ItemStack itemStack,@NotNull BlockState blockState){
		if(!itemStack.hasTag()) return super.getDestroySpeed(itemStack,blockState);
		CompoundTag tag=itemStack.getOrCreateTag();
		if(tag.getBoolean("Broken")) return 1F;
		int speeeed=containsMaterial(itemStack,"Gold")?8:0;
		return blockState.is(getMineableTag())?tag.getInt("Efficiency")+tag.getInt("SpecialEfficiency")+speeeed:1F;
	}
	@Override
	public int getMaxDamage(ItemStack itemStack){
		if(itemStack.hasTag()){
			assert itemStack.getTag()!=null;
			if(itemStack.getTag().contains("Durability")){
				return itemStack.getTag().getInt("Durability");
			}
		}
		return this.defaultDurability;
	}
	@Override
	public Multimap<Attribute,AttributeModifier> getAttributeModifiers(EquipmentSlot slot,ItemStack itemStack){
		if(slot==EquipmentSlot.MAINHAND&&itemStack.hasTag()){
			CompoundTag tag=itemStack.getOrCreateTag();
			ImmutableMultimap.Builder<Attribute,AttributeModifier> builders=ImmutableMultimap.builder();
			float damage=tag.contains("AttackDamage")?tag.getFloat("AttackDamage"):defaultAttackDamage;
			float speed=tag.contains("AttackSpeed")?tag.getFloat("AttackSpeed"):defaultAttackSpeed;
			if(!tag.getBoolean("Broken")){
				builders.put(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,"Tool modifier",damage,AttributeModifier.Operation.ADDITION));
				builders.put(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_UUID,"Tool modifier",speed,AttributeModifier.Operation.ADDITION));
			}
			return builders.build();
		}
		return super.getAttributeModifiers(slot,itemStack);
	}
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack itemStack,Enchantment enchantment){
		return false;
	}
	@Override
	public boolean isEnchantable(@NotNull ItemStack itemStack){
		return false;
	}
	@Override
	public int getEnchantmentValue(){
		return 0;
	}
	@Override
	public boolean isBookEnchantable(ItemStack itemStack,ItemStack book){
		return false;
	}
	@Override
	public boolean isFoil(@NotNull ItemStack itemStack){
		return false;
	}
	@Override
	public boolean isRepairable(@NotNull ItemStack itemStack){
		return false;
	}
	@Override
	public boolean isValidRepairItem(@NotNull ItemStack pToRepair,@NotNull ItemStack pRepair){
		return false;
	}
	public static boolean isTagged(ItemStack itemStack,String namespace,String path){
		return itemStack.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath(namespace,path)));
	}
	public static boolean isHead(ItemStack itemStack){
		return isTagged(itemStack,DifMod.MODID,"modular_tools_parts/head");
	}
	public static boolean isBinding(ItemStack itemStack){
		return isTagged(itemStack,DifMod.MODID,"modular_tools_parts/binding");
	}
	public static boolean isHandle(ItemStack itemStack){
		return isTagged(itemStack,DifMod.MODID,"modular_tools_parts/handle");
	}
	public static boolean isReplacedPartValid(ItemStack template,ItemStack base){
		assert template.getTag()!=null;
		assert base.getTag()!=null;
		if(isTagged(template,DifMod.MODID,"modular_tools_parts/head"))
			return !template.getTag().getString("HeadMaterial").equals(base.getTag().getString("HeadMaterial"));
		if(isTagged(template,DifMod.MODID,"modular_tools_parts/binding"))
			return !template.getTag().getString("BindingMaterial").equals(base.getTag().getString("BindingMaterial"));
		if(isTagged(template,DifMod.MODID,"modular_tools_parts/handle"))
			return !template.getTag().getString("HandleMaterial").equals(base.getTag().getString("HandleMaterial"));
		return false;
	}
	public static String getPartType(ItemStack itemStack){
		if(isHead(itemStack)) return "Head";
		if(isBinding(itemStack)) return "Binding";
		if(isHandle(itemStack)) return "Handle";
		return "";
	}
	public static void toolRepair(ItemStack template,ItemStack base,ItemStack addition,CompoundTag templateTag,CompoundTag baseTag,CompoundTag additionTag){
		int currentDamage=base.getDamageValue();
		int repair=containsMaterial(base,"Stone")?cheepRepairAmount:repairAmount;
		int newDamage=Math.max(0,currentDamage-repair);
		base.setDamageValue(newDamage);
		if(base.hasTag()&&base.getTag()!=null){
			if(base.getTag().getBoolean("Broken")&&newDamage<(base.getMaxDamage()-1)){
				baseTag.putBoolean("Broken",false);
				baseTag.putBoolean("Unbreakable",false);
				baseTag.putInt("CustomModelData",0);
			}
		}
	}
	public static void toolPartReplace(ItemStack template,ItemStack base,ItemStack addition,CompoundTag templateTag,CompoundTag baseTag,CompoundTag additionTag){
		if(isTagged(template,DifMod.MODID,"modular_tools_parts/head")){
			baseTag.putString("Material",templateTag.getString("HeadMaterial"));
			baseTag.putString("HeadMaterial",templateTag.getString("HeadMaterial"));
			baseTag.putInt("HeadColor",templateTag.getInt("HeadColor"));
			baseTag.putInt("HeadDurability",templateTag.getInt("HeadDurability"));
			baseTag.putInt("MiningLevel",miningLevelFromMaterial(baseTag.getString("Material")));
			baseTag.putInt("Efficiency",efficiencyFromMaterial(baseTag.getString("Material")));
			baseTag.putFloat("AttackDamage",attackDamageFromMaterial(baseTag.getString("Material"),base));
			baseTag.putFloat("AttackSpeed",attackSpeedFromMaterial(baseTag.getString("Material"),base));
		}
		if(isTagged(template,DifMod.MODID,"modular_tools_parts/binding")){
			baseTag.putString("BindingMaterial",templateTag.getString("BindingMaterial"));
			baseTag.putInt("BindingColor",templateTag.getInt("BindingColor"));
			baseTag.putInt("BindingDurability",templateTag.getInt("BindingDurability"));
		}
		if(isTagged(template,DifMod.MODID,"modular_tools_parts/handle")){
			baseTag.putString("HandleMaterial",templateTag.getString("HandleMaterial"));
			baseTag.putInt("HandleColor",templateTag.getInt("HandleColor"));
			baseTag.putInt("HandleDurability",templateTag.getInt("HandleDurability"));
		}
		calculateDurability(baseTag);
		if(baseTag.getBoolean("Broken")&&baseTag.getInt("Durability")-base.getDamageValue()>0){
			baseTag.putBoolean("Broken",false);
			baseTag.putBoolean("Unbreakable",false);
			baseTag.putInt("CustomModelData",0);
		}
		if(!(base.getDamageValue()<baseTag.getInt("Durability"))){
			base.setDamageValue(baseTag.getInt("Durability")-1);
			baseTag.putBoolean("Broken",true);
			baseTag.putBoolean("Unbreakable",true);
			baseTag.putInt("CustomModelData",1);
		}
	}
	public static ItemStack newToolCraft(ItemStack template,ItemStack base,ItemStack addition,CompoundTag templateTag,CompoundTag baseTag,CompoundTag additionTag){
		ItemStack tool=new ItemStack(Items.AIR);
		if(base.getItem().equals(DifModItems.MODULAR_PART_PICKAXE_HEAD.get()))
			tool=new ItemStack(DifModItems.MODULAR_PICKAXE.get());
		if(base.getItem().equals(DifModItems.MODULAR_PART_AXE_HEAD.get()))
			tool=new ItemStack(DifModItems.MODULAR_AXE.get());
		if(base.getItem().equals(DifModItems.MODULAR_PART_SHOVEL_HEAD.get()))
			tool=new ItemStack(DifModItems.MODULAR_SHOVEL.get());
		if(base.getItem().equals(DifModItems.MODULAR_PART_SWORD_HEAD.get()))
			tool=new ItemStack(DifModItems.MODULAR_SWORD.get());
		CompoundTag toolTag=new CompoundTag();
		toolTag.putString("Material",baseTag.getString("HeadMaterial"));
		toolTag.putInt("SpecialDurability",1);
		toolTag.putInt("SpecialMiningLevel",0);
		toolTag.putInt("BonusMiningLevel",0);
		toolTag.putInt("SpecialEfficiency",0);
		toolTag.putInt("HideFlags",5);
		toolTag.putInt("MaxModifiers",defaultMaxModifiers);
		toolTag.putInt("Durability",baseTag.getInt("HeadDurability")+additionTag.getInt("BindingDurability")+templateTag.getInt("HandleDurability")+1);
		toolTag.putInt("MiningLevel",miningLevelFromMaterial(toolTag.getString("Material")));
		toolTag.putInt("Efficiency",efficiencyFromMaterial(toolTag.getString("Material")));
		toolTag.putFloat("AttackDamage",attackDamageFromMaterial(toolTag.getString("Material"),tool));
		toolTag.putFloat("AttackSpeed",attackSpeedFromMaterial(toolTag.getString("Material"),tool));
		toolTag.putString("HeadMaterial",baseTag.getString("HeadMaterial"));
		toolTag.putString("BindingMaterial",additionTag.getString("BindingMaterial"));
		toolTag.putString("HandleMaterial",templateTag.getString("HandleMaterial"));
		toolTag.putInt("HeadDurability",baseTag.getInt("HeadDurability"));
		toolTag.putInt("BindingDurability",additionTag.getInt("BindingDurability"));
		toolTag.putInt("HandleDurability",templateTag.getInt("HandleDurability"));
		toolTag.putInt("HeadColor",baseTag.getInt("HeadColor"));
		toolTag.putInt("BindingColor",additionTag.getInt("BindingColor"));
		toolTag.putInt("HandleColor",templateTag.getInt("HandleColor"));
		tool.setTag(toolTag);
		return tool;
	}
	public static void applyModifiers(ItemStack template,ItemStack base,ItemStack addition,CompoundTag templateTag,CompoundTag baseTag,CompoundTag additionTag){
		if(isTagged(template,DifMod.MODID,"modular_tools_modifiers/efficiency")){
			if(baseTag.getInt("EfficiencyModifierProgress")+1>=efficiencyModifierStages[baseTag.getInt("EfficiencyModifier")]){
				baseTag.putInt("EfficiencyModifierProgress",0);
				baseTag.putInt("EfficiencyModifier",baseTag.getInt("EfficiencyModifier")+1);
				baseTag.putInt("SpecialEfficiency",baseTag.getInt("EfficiencyModifier")+efficiencyModifierLevels[baseTag.getInt("EfficiencyModifier")-1]);
			}else{
				if(baseTag.getInt("EfficiencyModifierProgress")==0)
					baseTag.putInt("MaxModifiers",baseTag.getInt("MaxModifiers")-1);
				baseTag.putInt("EfficiencyModifierProgress",baseTag.getInt("EfficiencyModifierProgress")+1);
			}
		}
		if(isTagged(template,DifMod.MODID,"modular_tools_modifiers/fortune")){
			if(baseTag.getInt("FortuneModifierProgress")+1>=fortuneModifierStages[baseTag.getInt("FortuneModifier")]){
				baseTag.putInt("FortuneModifierProgress",0);
				baseTag.putInt("FortuneModifier",baseTag.getInt("FortuneModifier")+1);
				setEnchantmentLevel(base,Enchantments.BLOCK_FORTUNE,fortuneModifierLevels[baseTag.getInt("FortuneModifier")-1]);
			}else{
				if(baseTag.getInt("FortuneModifierProgress")==0)
					baseTag.putInt("MaxModifiers",baseTag.getInt("MaxModifiers")-1);
				baseTag.putInt("FortuneModifierProgress",baseTag.getInt("FortuneModifierProgress")+1);
			}
		}
		if(isTagged(template,DifMod.MODID,"modular_tools_modifiers/silk_touch")){
			baseTag.putBoolean("SilkTouchModifier",true);
			baseTag.putInt("MaxModifiers",baseTag.getInt("MaxModifiers")-1);
			setEnchantmentLevel(base,Enchantments.SILK_TOUCH,1);
		}
		if(isTagged(template,DifMod.MODID,"modular_tools_modifiers/diamond")){
			baseTag.putBoolean("DiamondModifier",true);
			baseTag.putInt("MaxModifiers",baseTag.getInt("MaxModifiers")-1);
			baseTag.putInt("SpecialMiningLevel",3);
		}
		if(isTagged(template,DifMod.MODID,"modular_tools_modifiers/blazing")){
			baseTag.putBoolean("BlazingModifier",true);
			baseTag.putInt("MaxModifiers",baseTag.getInt("MaxModifiers")-1);
		}
	}
	public static boolean toolRepairCheck(ItemStack template,ItemStack base,ItemStack addition,CompoundTag templateTag,CompoundTag baseTag,CompoundTag additionTag){
		return template.getItem().equals(Items.AIR)&&base.getDamageValue()>0&&isTagged(addition,DifMod.MODID,"modular_tools_materials/"+baseTag.getString("Material").toLowerCase());
	}
	public static boolean toolPartReplaceCheck(ItemStack template,ItemStack base,ItemStack addition,CompoundTag templateTag,CompoundTag baseTag,CompoundTag additionTag){
		if(base.getItem().equals(DifModItems.MODULAR_PICKAXE.get())&&isTagged(template,DifMod.MODID,"modular_tools_parts/pickaxe_parts"))
			return isReplacedPartValid(template,base);
		if(base.getItem().equals(DifModItems.MODULAR_AXE.get())&&isTagged(template,DifMod.MODID,"modular_tools_parts/axe_parts"))
			return isReplacedPartValid(template,base);
		if(base.getItem().equals(DifModItems.MODULAR_SHOVEL.get())&&isTagged(template,DifMod.MODID,"modular_tools_parts/shovel_parts"))
			return isReplacedPartValid(template,base);
		if(base.getItem().equals(DifModItems.MODULAR_SWORD.get())&&isTagged(template,DifMod.MODID,"modular_tools_parts/sword_parts"))
			return isReplacedPartValid(template,base);
		return false;
	}
	public static boolean newToolCraftCheck(ItemStack template,ItemStack base,ItemStack addition,CompoundTag templateTag,CompoundTag baseTag,CompoundTag additionTag){
		if(base.getItem().equals(DifModItems.MODULAR_PART_SWORD_HEAD.get()))
			return addition.getItem().equals(DifModItems.MODULAR_PART_SWORD_BINDING.get());
		else return addition.getItem().equals(DifModItems.MODULAR_PART_BINDING.get());
	}
	public static boolean applyModifiersCheck(ItemStack template,ItemStack base,ItemStack addition,CompoundTag templateTag,CompoundTag baseTag,CompoundTag additionTag){
		if(!addition.isEmpty()) return false;
		if(isTagged(template,DifMod.MODID,"modular_tools_modifiers/efficiency")){
			if(baseTag.getInt("EfficiencyModifierProgress")==0&&baseTag.getInt("MaxModifiers")==0) return false;
			return !(baseTag.getInt("EfficiencyModifier")>=3);
		}
		if(isTagged(template,DifMod.MODID,"modular_tools_modifiers/fortune")&&!baseTag.getBoolean("SilkTouchModifier")){
			if(baseTag.getInt("FortuneModifierProgress")==0&&baseTag.getInt("MaxModifiers")==0) return false;
			return !(baseTag.getInt("FortuneModifier")>=3);
		}
		if(isTagged(template,DifMod.MODID,"modular_tools_modifiers/silk_touch")&&baseTag.getInt("FortuneModifier")==0&&baseTag.getInt("FortuneModifierProgress")==0&&baseTag.getInt("MaxModifiers")>0&&!baseTag.getBoolean("SilkTouchModifier")) return true;
		if(isTagged(template,DifMod.MODID,"modular_tools_modifiers/diamond")&&baseTag.getInt("MaxModifiers")>0&&!baseTag.getBoolean("DiamondModifier")) return true;
		if(isTagged(template,DifMod.MODID,"modular_tools_modifiers/blazing")&&baseTag.getInt("MaxModifiers")>0&&!baseTag.getBoolean("BlazingModifier")) return true;
		return false;
	}
	public static String miningLevelColor(CompoundTag tag){
		int mLevel=tag.getInt("MiningLevel")+tag.getInt("BonusMiningLevel");
		if(tag.getInt("SpecialMiningLevel")>mLevel) mLevel=tag.getInt("SpecialMiningLevel");
		String[] levelColors={"#745631","#838383","#DCDCDC","#6DEDE4","#433F41"};
		return mLevel<levelColors.length?levelColors[mLevel]:"#FFFFFF";
	}
	public static String miningLevelColor(String material){
		CompoundTag tag=new CompoundTag();
		int level=0;
		switch(material){
			case "Stone" -> level=1;
			case "Iron" -> level=2;
			case "Diamond" -> level=3;
			case "Netherite" -> level=4;
		}
		tag.putInt("MiningLevel",level);
		return miningLevelColor(tag);
	}
	public static String colorHexFromMaterial(String material){
		String color="#FFFFFF";
		switch(material){
			case "Wood" -> color="#745631";
			case "Stone" -> color="#838383";
			case "Copper" -> color="#D86D5F";
			case "Iron" -> color="#DCDCDC";
			case "Gold" -> color="#F6D142";
			case "Diamond" -> color="#6DEDE4";
			case "Obsidian" -> color="#391872";
			case "Netherite" -> color="#433F41";
		}
		return color;
	}
	public static int colorIntFromMaterial(String material){
		int color=0xFFFFFF;
		switch(material){
			case "Wood" -> color=0x745631;
			case "Stone" -> color=0x838383;
			case "Copper" -> color=0xD86D5F;
			case "Iron" -> color=0xDCDCDC;
			case "Gold" -> color=0xF6D142;
			case "Diamond" -> color=0x6DEDE4;
			case "Obsidian" -> color=0x391872;
			case "Netherite" -> color=0x433F41;
		}
		return color;
	}
	public static ChatFormatting durabilityColor(String partType,CompoundTag tag){
		ChatFormatting dColor=ChatFormatting.DARK_AQUA;
		if(tag.getInt(partType+"Durability")>0){
			dColor=ChatFormatting.GREEN;
		}else if(tag.getInt(partType+"Durability")<0){
			dColor=ChatFormatting.RED;
		}
		return dColor;
	}
	public static int durabilityFromMaterial(String partType,String material){
		int durability=0;
		switch(material){
			case "Wood" -> durability=30;
			case "Stone" -> durability=70;
			case "Copper" -> durability=80;
			case "Iron" -> durability=190;
			case "Gold" -> durability=10;
			case "Diamond" -> durability=1450;
			case "Obsidian" -> durability=1800;
			case "Netherite" -> durability=2850;
		}
		switch(partType){
			case "Binding" -> durability=(int)(durability*0.5);
			case "Handle" -> durability=(int)(durability*0.15);
		}
		return durability;
	}
	public static int miningLevelFromMaterial(String material){
		int miningLevel=0;
		switch(material){
			case "Stone","Copper" -> miningLevel=1;
			case "Iron" -> miningLevel=2;
			case "Diamond","Obsidian" -> miningLevel=3;
			case "Netherite" -> miningLevel=4;
		}
		return miningLevel;
	}
	public static int efficiencyFromMaterial(String material){
		int efficiency=1;
		switch(material){
			case "Wood" -> efficiency=2;
			case "Stone" -> efficiency=4;
			case "Copper" -> efficiency=5;
			case "Iron" -> efficiency=6;
			case "Gold" -> efficiency=12;
			case "Diamond" -> efficiency=8;
			case "Obsidian","Netherite" -> efficiency=9;
		}
		return efficiency;
	}
	public static float attackDamageFromMaterial(String material,ItemStack itemStack){
		float attacksDamage=1F;
		if(itemStack.getItem().equals(DifModItems.MODULAR_PICKAXE.get())){
			switch(material){
				case "Stone","Copper" -> attacksDamage=2F;
				case "Iron" -> attacksDamage=3F;
				case "Diamond" -> attacksDamage=4F;
				case "Obsidian","Netherite" -> attacksDamage=5F;
			}
		}
		if(itemStack.getItem().equals(DifModItems.MODULAR_SHOVEL.get())){
			attacksDamage=1.5F;
			switch(material){
				case "Stone","Copper" -> attacksDamage=2.5F;
				case "Iron" -> attacksDamage=3.5F;
				case "Diamond" -> attacksDamage=4.5F;
				case "Obsidian","Netherite" -> attacksDamage=5.5F;
			}
		}
		if(itemStack.getItem().equals(DifModItems.MODULAR_AXE.get())){
			attacksDamage=6F;
			switch(material){
				case "Stone","Copper","Iron","Diamond" -> attacksDamage=8F;
				case "Obsidian","Netherite" -> attacksDamage=9F;
			}
		}
		if(itemStack.getItem().equals(DifModItems.MODULAR_SWORD.get())){
			attacksDamage=3F;
			switch(material){
				case "Stone","Copper" -> attacksDamage=4F;
				case "Iron" -> attacksDamage=5F;
				case "Diamond" -> attacksDamage=6F;
				case "Obsidian","Netherite" -> attacksDamage=7F;
			}
		}
		return attacksDamage;
	}
	public static float attackSpeedFromMaterial(String material,ItemStack itemStack){
		float attacksSpeed=-2.8F;
		if(itemStack.getItem().equals(DifModItems.MODULAR_SHOVEL.get())) attacksSpeed=-3F;
		if(itemStack.getItem().equals(DifModItems.MODULAR_AXE.get())){
			switch(material){
				case "Wood","Stone","Copper" -> attacksSpeed=-3.2F;
				case "Iron" -> attacksSpeed=-3.1F;
				case "Gold","Diamond","Obsidian","Netherite" -> attacksSpeed=-3F;
			}
		}
		if(itemStack.getItem().equals(DifModItems.MODULAR_SWORD.get())) attacksSpeed=-2.4F;
		return attacksSpeed;
	}
	public static void calculateDurability(CompoundTag tag){
		tag.putInt("Durability",tag.getInt("HeadDurability")+tag.getInt("HandleDurability")+tag.getInt("BindingDurability")+tag.getInt("SpecialDurability"));
	}
	public static void showDurability(ItemStack itemStack,CompoundTag tag,List<Component> list){
		int currentDamage=itemStack.getDamageValue()+1;
		int maxDurability=tag.getInt("Durability");
		int remainingDurability=Math.max(0,maxDurability-currentDamage);
		float ratio=(float)remainingDurability/maxDurability;
		int red=(int)(255*(1-ratio));
		int green=(int)(255*ratio);
		String hexColor=String.format("#%02X%02X00",red,green);
		list.add(Component.literal("Durability: ").append(Component.literal(String.valueOf(remainingDurability)).withStyle(Style.EMPTY.withColor(TextColor.parseColor(hexColor)))).append(Component.literal(" / "+(maxDurability-1)).withStyle(Style.EMPTY.withColor(TextColor.parseColor("#AAAAAA")))));
	}
	@Override
	public @NotNull InteractionResult useOn(UseOnContext context){
		if(!context.getItemInHand().getItem().equals(DifModItems.MODULAR_AXE.get())) return InteractionResult.PASS;
		Level level=context.getLevel();
		BlockPos blockpos=context.getClickedPos();
		Player player=context.getPlayer();
		BlockState blockstate=level.getBlockState(blockpos);
		Optional<BlockState> optional=Optional.ofNullable(blockstate.getToolModifiedState(context,ToolActions.AXE_STRIP,false));
		Optional<BlockState> optional1=optional.isPresent()?Optional.empty():Optional.ofNullable(blockstate.getToolModifiedState(context,ToolActions.AXE_SCRAPE,false));
		Optional<BlockState> optional2=optional.isPresent()||optional1.isPresent()?Optional.empty():Optional.ofNullable(blockstate.getToolModifiedState(context,ToolActions.AXE_WAX_OFF,false));
		ItemStack itemstack=context.getItemInHand();
		Optional<BlockState> optional3=Optional.empty();
		if(optional.isPresent()){
			level.playSound(player,blockpos,SoundEvents.AXE_STRIP,SoundSource.BLOCKS,1.0F,1.0F);
			optional3=optional;
		}else if(optional1.isPresent()){
			level.playSound(player,blockpos,SoundEvents.AXE_SCRAPE,SoundSource.BLOCKS,1.0F,1.0F);
			level.levelEvent(player,3005,blockpos,0);
			optional3=optional1;
		}else if(optional2.isPresent()){
			level.playSound(player,blockpos,SoundEvents.AXE_WAX_OFF,SoundSource.BLOCKS,1.0F,1.0F);
			level.levelEvent(player,3004,blockpos,0);
			optional3=optional2;
		}
		if(optional3.isPresent()){
			if(player instanceof ServerPlayer){
				CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player,blockpos,itemstack);
			}
			level.setBlock(blockpos,optional3.get(),11);
			level.gameEvent(GameEvent.BLOCK_CHANGE,blockpos,GameEvent.Context.of(player,optional3.get()));
			if(player!=null){
				itemstack.hurtAndBreak(1,player,(p_150686_)->{
					p_150686_.broadcastBreakEvent(context.getHand());
				});
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}else{
			return InteractionResult.PASS;
		}
	}
	public static void setEnchantmentLevel(ItemStack itemStack,Enchantment enchantment,int level){
		Map<Enchantment,Integer> enchantments=EnchantmentHelper.getEnchantments(itemStack);
		if(level<=0) enchantments.remove(enchantment);
		else enchantments.put(enchantment,level);
		EnchantmentHelper.setEnchantments(enchantments,itemStack);
	}
	public static boolean containsMaterial(ItemStack itemStack,String material){
		CompoundTag tag=itemStack.getOrCreateTag();
		if(tag.contains("HeadMaterial")&&tag.getString("HeadMaterial").equals(material)) return true;
		if(tag.contains("BindingMaterial")&&tag.getString("BindingMaterial").equals(material)) return true;
		return tag.contains("HandleMaterial")&&tag.getString("HandleMaterial").equals(material);
	}
	public static boolean isInMainHand(ItemStack itemStack,Player player){
		return player.getItemBySlot(EquipmentSlot.MAINHAND).equals(itemStack);
	}
	public static boolean isInOffHand(ItemStack itemStack,Player player){
		return player.getItemBySlot(EquipmentSlot.OFFHAND).equals(itemStack);
	}
	public static ItemStack newToolFromMaterials(Item toolItem,String headMaterial,String bindingMaterial,String handleMaterial){
		ItemStack modular_tools_icon=new ItemStack(toolItem);
		CompoundTag tag=new CompoundTag();
		tag.putString("Material",headMaterial);
		tag.putInt("MaxModifiers",defaultMaxModifiers);
		tag.putInt("SpecialDurability",1);
		tag.putInt("SpecialMiningLevel",0);
		tag.putInt("BonusMiningLevel",0);
		tag.putInt("SpecialEfficiency",0);
		tag.putInt("HideFlags",5);
		tag.putInt("MiningLevel",miningLevelFromMaterial(tag.getString(headMaterial)));
		tag.putInt("Efficiency",efficiencyFromMaterial(tag.getString(headMaterial)));
		tag.putFloat("AttackDamage",attackDamageFromMaterial(tag.getString(headMaterial),modular_tools_icon));
		tag.putFloat("AttackSpeed",attackSpeedFromMaterial(tag.getString(headMaterial),modular_tools_icon));
		tag.putString("HeadMaterial",headMaterial);
		tag.putString("BindingMaterial",bindingMaterial);
		tag.putString("HandleMaterial",handleMaterial);
		tag.putInt("HeadDurability",durabilityFromMaterial("Head",headMaterial));
		tag.putInt("BindingDurability",durabilityFromMaterial("Binding",bindingMaterial));
		tag.putInt("HandleDurability",durabilityFromMaterial("handle",handleMaterial));
		tag.putInt("Durability",tag.getInt("HeadDurability")+tag.getInt("BindingDurability")+tag.getInt("HandleDurability")+1);
		tag.putInt("HeadColor",colorIntFromMaterial(headMaterial));
		tag.putInt("BindingColor",colorIntFromMaterial(bindingMaterial));
		tag.putInt("HandleColor",colorIntFromMaterial(handleMaterial));
		modular_tools_icon.setTag(tag);
		return modular_tools_icon;
	}
	public static Component modifierTipFormMaterial(String material){
		Component component=Component.literal("");
		switch(material){
			case "Wood" ->
					component=Component.literal("Natural").withStyle(Style.EMPTY.withColor(TextColor.parseColor(colorHexFromMaterial("Wood"))));
			case "Stone" ->
					component=Component.literal("Cheep").withStyle(Style.EMPTY.withColor(TextColor.parseColor(colorHexFromMaterial("Stone"))));
			//case "Copper"->;
			case "Iron" ->
					component=Component.literal("Magnetic").withStyle(Style.EMPTY.withColor(TextColor.parseColor(colorHexFromMaterial("Iron"))));
			case "Gold"->component=Component.literal("Speeeed").withStyle(Style.EMPTY.withColor(TextColor.parseColor(colorHexFromMaterial("Gold"))));
			//case "Diamond"->;
			//case "Obsidian"->;
			//case "Netherite"->;
		}
		return component;
	}
	@Override
	public void inventoryTick(@NotNull ItemStack itemStack,@NotNull Level world,@NotNull Entity entity,int slot,boolean selected){
		if(!world.isClientSide()){
			CompoundTag tag=itemStack.getOrCreateTag();
			if(!tag.contains("MaxModifiers")) tag.putInt("MaxModifiers",defaultMaxModifiers);
			if(!tag.contains("SilkTouchModifier")) tag.putBoolean("SilkTouchModifier",false);
			if(!tag.contains("DiamondModifier")) tag.putBoolean("DiamondModifier",false);
			if(!tag.contains("EfficiencyModifierProgress")) tag.putInt("EfficiencyModifierProgress",0);
			if(!tag.contains("EfficiencyModifier")) tag.putInt("EfficiencyModifier",0);
			if(!tag.contains("FortuneModifierProgress")) tag.putInt("FortuneModifierProgress",0);
			if(!tag.contains("FortuneModifier")) tag.putInt("FortuneModifier",0);
			if(!tag.contains("MiningLevel")) tag.putInt("MiningLevel",defaultMiningLevel);
			if(!tag.contains("BonusMiningLevel")) tag.putInt("BonusMiningLevel",0);
			if(!tag.contains("SpecialMiningLevel")) tag.putInt("SpecialMiningLevel",0);
			if(!tag.contains("Efficiency")) tag.putInt("Efficiency",defaultEfficiency);
			if(!tag.contains("SpecialEfficiency")) tag.putInt("SpecialEfficiency",0);
			if(!tag.contains("AttackDamage")) tag.putFloat("AttackDamage",defaultAttackDamage);
			if(!tag.contains("AttackSpeed")) tag.putFloat("AttackSpeed",defaultAttackSpeed);
			if(!tag.contains("HeadMaterial")) tag.putString("HeadMaterial",material);
			if(!tag.contains("HeadDurability")) tag.putInt("HeadDurability",defaultDurability);
			if(!tag.contains("HeadColor")) tag.putInt("HeadColor",colorIntFromMaterial(tag.getString("HeadMaterial")));
			if(!tag.contains("BindingMaterial")) tag.putString("BindingMaterial",material);
			if(!tag.contains("BindingDurability")) tag.putInt("BindingDurability",defaultDurability);
			if(!tag.contains("BindingColor"))
				tag.putInt("BindingColor",colorIntFromMaterial(tag.getString("BindingMaterial")));
			if(!tag.contains("HandleMaterial")) tag.putString("HandleMaterial",material);
			if(!tag.contains("HandleDurability")) tag.putInt("HandleDurability",defaultDurability);
			if(!tag.contains("HandleColor"))
				tag.putInt("HandleColor",colorIntFromMaterial(tag.getString("HandleMaterial")));
			if(!tag.contains("SpecialDurability")) tag.putInt("SpecialDurability",1);
			calculateDurability(tag);
			if(!tag.contains("Material")) tag.putString("Material",tag.getString("HeadMaterial"));
			if(!tag.contains("Broken")) tag.putBoolean("Broken",false);
			if(!tag.contains("Unbreakable")) tag.putBoolean("Unbreakable",false);
			if(!tag.contains("HideFlags")) tag.putInt("HideFlags",5);
			if(itemStack.getMaxDamage()-itemStack.getDamageValue()==1){
				tag.putBoolean("Broken",true);
				tag.putBoolean("Unbreakable",true);
				tag.putInt("CustomModelData",1);
			}else{
				tag.putBoolean("Broken",false);
				tag.putBoolean("Unbreakable",false);
				tag.putInt("CustomModelData",0);
			}
			if(entity instanceof Player player){
				if(containsMaterial(itemStack,"Wood")&&DifMod.rouletteBoolean(500)&&!(isInMainHand(itemStack,player)))
					itemStack.setDamageValue(itemStack.getDamageValue()-1);
			}
		}
		if(entity instanceof Player player){
			if(containsMaterial(itemStack,"Iron")&&isInMainHand(itemStack,player)||isInOffHand(itemStack,player)){
				// Definice dosahu magnetu (např. 8 bloků)
				double range=4D;
				// Najdeme všechny ItemEntity v okolí hráče
				List<ItemEntity> items=world.getEntitiesOfClass(ItemEntity.class,player.getBoundingBox().inflate(range));
				for(ItemEntity itemEntity: items){
					if(itemEntity.isRemoved()||!itemEntity.isAlive()) continue;
					// Vektor směru od itemu k hráči
					Vec3 moveVec=player.position().subtract(itemEntity.position());
					// Normalizace a nastavení rychlosti přitahování (0.2 je tak akorát	)
					if(moveVec.lengthSqr()<0.4D){
						// Volitelně: Můžeme itemu nastavit nulový horizontální pohyb, aby hned zastavil
						itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().multiply(0.8D,1.0D,0.8D));
						continue;
					}
					itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(moveVec.normalize().scale(0.1D)));
				}
			}
		}
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,Level world,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		if(!itemStack.hasTag()) return;
		assert itemStack.getTag()!=null;
		CompoundTag tag=itemStack.getTag();
		if(!tag.contains("MiningLevel")||!tag.contains("Durability")||!tag.contains("Efficiency")||!tag.contains("AttackDamage")||!tag.contains("AttackSpeed"))
			return;
		if(Screen.hasShiftDown()){//Shift
			list.add(Component.literal("Remaining Modifiers:").withStyle(ChatFormatting.WHITE).append(CommonComponents.space().append(Component.literal(String.valueOf(tag.getInt("MaxModifiers"))).withStyle(ChatFormatting.YELLOW))));
			if(tag.getInt("EfficiencyModifierProgress")>0||tag.getInt("EfficiencyModifier")>0){
				if(tag.getInt("EfficiencyModifierProgress")>0)
					list.add(Component.literal("Efficiency:").withStyle(ChatFormatting.RED).append(CommonComponents.space().append(Component.literal(String.valueOf(tag.getInt("EfficiencyModifier"))).append(CommonComponents.space().append(CommonComponents.space().append(Component.literal("( "+tag.getInt("EfficiencyModifierProgress")+" / "+efficiencyModifierStages[tag.getInt("EfficiencyModifier")]+" )")))))));
				else
					list.add(Component.literal("Efficiency:").withStyle(ChatFormatting.RED).append(CommonComponents.space().append(Component.literal(String.valueOf(tag.getInt("EfficiencyModifier"))))));
			}
			if(tag.getInt("FortuneModifierProgress")>0||tag.getInt("FortuneModifier")>0){
				if(tag.getInt("FortuneModifierProgress")>0)
					list.add(Component.literal("Fortune:").withStyle(ChatFormatting.BLUE).append(CommonComponents.space().append(Component.literal(String.valueOf(tag.getInt("FortuneModifier"))).append(CommonComponents.space().append(CommonComponents.space().append(Component.literal("( "+tag.getInt("FortuneModifierProgress")+" / "+fortuneModifierStages[tag.getInt("FortuneModifier")]+" )")))))));
				else
					list.add(Component.literal("Fortune:").withStyle(ChatFormatting.BLUE).append(CommonComponents.space().append(Component.literal(String.valueOf(tag.getInt("FortuneModifier"))))));
			}
			if(tag.getBoolean("SilkTouchModifier"))
				list.add(Component.literal("SilkTouch").withStyle(Style.EMPTY.withColor(TextColor.parseColor("#FDFD96"))));
			if(tag.getBoolean("DiamondModifier"))
				list.add(Component.literal("Diamond").withStyle(Style.EMPTY.withColor(TextColor.parseColor(miningLevelColor("Diamond")))));
			if(tag.getBoolean("BlazingModifier"))
				list.add(Component.literal("Blazing").withStyle(Style.EMPTY.withColor(TextColor.parseColor("#FF5A00"))));
			if(tag.contains("HeadMaterial")||tag.contains("HandleMaterial")||tag.contains("BindingMaterial")){
				list.add(CommonComponents.EMPTY);
				for(String material: materials)
					if(containsMaterial(itemStack,material)) list.add(modifierTipFormMaterial(material));
			}
		}else if(Screen.hasControlDown()){//Control
			list.add(Component.literal("Tool Material:").withStyle(ChatFormatting.WHITE).append(CommonComponents.space()).append(Component.translatable(tag.getString("Material")).withStyle(Style.EMPTY.withColor(TextColor.parseColor(colorHexFromMaterial(tag.getString("Material")))))));
			list.add(Component.literal("Head:").withStyle(ChatFormatting.WHITE));
			list.add(CommonComponents.space().append(Component.literal("Material:").withStyle(ChatFormatting.WHITE).append(CommonComponents.space()).append(Component.translatable(tag.getString("HeadMaterial")).withStyle(Style.EMPTY.withColor(TextColor.parseColor(colorHexFromMaterial(tag.getString("HeadMaterial"))))))));
			list.add(CommonComponents.space().append(Component.literal("Durability:").withStyle(ChatFormatting.WHITE).append(CommonComponents.space()).append(Component.translatable(String.valueOf(tag.getInt("HeadDurability"))).withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(durabilityColor("Head",tag)))))));
			list.add(Component.literal("Binding:").withStyle(ChatFormatting.WHITE));
			list.add(CommonComponents.space().append(Component.literal("Material:").withStyle(ChatFormatting.WHITE).append(CommonComponents.space()).append(Component.translatable(tag.getString("BindingMaterial")).withStyle(Style.EMPTY.withColor(TextColor.parseColor(colorHexFromMaterial(tag.getString("BindingMaterial"))))))));
			list.add(CommonComponents.space().append(Component.literal("Durability:").withStyle(ChatFormatting.WHITE).append(CommonComponents.space()).append(Component.translatable(String.valueOf(tag.getInt("BindingDurability"))).withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(durabilityColor("Binding",tag)))))));
			list.add(Component.literal("Handle:").withStyle(ChatFormatting.WHITE));
			list.add(CommonComponents.space().append(Component.literal("Material:").withStyle(ChatFormatting.WHITE).append(CommonComponents.space()).append(Component.translatable(tag.getString("HandleMaterial")).withStyle(Style.EMPTY.withColor(TextColor.parseColor(colorHexFromMaterial(tag.getString("HandleMaterial"))))))));
			list.add(CommonComponents.space().append(Component.literal("Durability:").withStyle(ChatFormatting.WHITE).append(CommonComponents.space()).append(Component.translatable(String.valueOf(tag.getInt("HandleDurability"))).withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(durabilityColor("Handle",tag)))))));
		}else{//None
			int miningLevel=tag.getInt("MiningLevel")+tag.getInt("BonusMiningLevel");
			if(tag.getInt("SpecialMiningLevel")>miningLevel) miningLevel=tag.getInt("SpecialMiningLevel");
			if(miningLevel>4) miningLevel=4;
			list.add(Component.literal("Mining Level:").withStyle(ChatFormatting.WHITE).append(CommonComponents.space()).append(Component.translatable("dif.mining_level."+miningLevel).withStyle(Style.EMPTY.withColor(TextColor.parseColor(miningLevelColor(tag))))));
			showDurability(itemStack,tag,list);
			int speeeed=containsMaterial(itemStack,"Gold")?8:0;
			list.add(Component.literal("Efficiency:").withStyle(ChatFormatting.WHITE).append(CommonComponents.space()).append(Component.literal(String.valueOf(tag.getInt("Efficiency")+tag.getInt("SpecialEfficiency")+speeeed)).withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))));
			list.add(Component.literal("Attack Damage:").withStyle(ChatFormatting.WHITE).append(CommonComponents.space()).append(Component.literal(String.format(Locale.ROOT,"%.1f",1.0F+tag.getFloat("AttackDamage"))).withStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
			list.add(Component.literal("Attack Speed:").withStyle(ChatFormatting.WHITE).append(CommonComponents.space()).append(Component.literal(String.format(Locale.ROOT,"%.1f",4.0F+tag.getFloat("AttackSpeed"))).withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW))));
			if(tag.getBoolean("Broken"))
				list.add(Component.literal("Broken").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED).withBold(true)));
			list.add(CommonComponents.EMPTY);
			list.add(Component.literal("Press").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)).append(CommonComponents.space().append(Component.literal("Shift").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withItalic(true)).append(CommonComponents.space().append(Component.literal("for modifiers info.").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)))))));
			list.add(Component.literal("Press").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)).append(CommonComponents.space().append(Component.literal("Ctrl").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(true)).append(CommonComponents.space().append(Component.literal("for parts info.").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)))))));
		}
	}
	public static ItemStack newSingleMaterialPreFab(Item toolItem,String material){
		return newToolFromMaterials(toolItem,material,material,material);
	}
	public static ItemStack newPartFromMaterial(Item partItem,String material){
		ItemStack itemStack=new ItemStack(partItem);
		CompoundTag tag=new CompoundTag();
		if(isHead(partItem.getDefaultInstance())){
			if(!tag.contains("HeadMaterial")) tag.putString("HeadMaterial",material);
			if(!tag.contains("HeadDurability")) tag.putInt("HeadDurability",durabilityFromMaterial("Head",material));
			if(!tag.contains("HeadColor")) tag.putInt("HeadColor",colorIntFromMaterial(tag.getString("HeadMaterial")));
		}
		if(isBinding(partItem.getDefaultInstance())){
			if(!tag.contains("BindingMaterial")) tag.putString("BindingMaterial",material);
			if(!tag.contains("BindingDurability")) tag.putInt("BindingDurability",durabilityFromMaterial("Binding",material));
			if(!tag.contains("BindingColor")) tag.putInt("BindingColor",colorIntFromMaterial(tag.getString("BindingMaterial")));
		}
		if(isHandle(partItem.getDefaultInstance())){
			if(!tag.contains("HandleMaterial")) tag.putString("HandleMaterial",material);
			if(!tag.contains("HandleDurability")) tag.putInt("HandleDurability",durabilityFromMaterial("Handle",material));
			if(!tag.contains("HandleColor")) tag.putInt("HandleColor",colorIntFromMaterial(tag.getString("HandleMaterial")));
		}
		itemStack.setTag(tag);
		return itemStack;
	}
}