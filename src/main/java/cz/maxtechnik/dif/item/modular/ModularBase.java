package cz.maxtechnik.dif.item.modular;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static cz.maxtechnik.dif.item.modular.ModularRecipes.REPAIR_AMOUNT;
public abstract class ModularBase extends DiggerItem{
	protected String defaultMaterial;
	protected int defaultMiningLevel;
	protected int defaultDurability;
	protected float defaultEfficiency;
	protected float defaultAttackDamage;
	protected float defaultAttackSpeed;
	private static final Map<Block,Block> AXE_STRIPPABLES=(new ImmutableMap.Builder<Block,Block>())
			.put(Blocks.OAK_LOG,Blocks.STRIPPED_OAK_LOG)
			.put(Blocks.SPRUCE_LOG,Blocks.STRIPPED_SPRUCE_LOG)
			.put(Blocks.BIRCH_LOG,Blocks.STRIPPED_BIRCH_LOG)
			.put(Blocks.JUNGLE_LOG,Blocks.STRIPPED_JUNGLE_LOG)
			.put(Blocks.ACACIA_LOG,Blocks.STRIPPED_ACACIA_LOG)
			.put(Blocks.DARK_OAK_LOG,Blocks.STRIPPED_DARK_OAK_LOG)
			.put(Blocks.CRIMSON_STEM,Blocks.STRIPPED_CRIMSON_STEM)
			.put(Blocks.WARPED_STEM,Blocks.STRIPPED_WARPED_STEM)
			.build();
	public ModularBase(int durability,float efficiency,int miningLevel, float attackDamage,float attackSpeed,String material,Properties properties){
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
		},BlockTags.MINEABLE_WITH_PICKAXE,properties);
		this.defaultMaterial=material;
		this.defaultDurability=durability;
		this.defaultEfficiency=efficiency;
		this.defaultMiningLevel=miningLevel;
		this.defaultAttackDamage=attackDamage;
		this.defaultAttackSpeed=attackSpeed;
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
	public static String getPartType(ItemStack itemStack){
		if(isHead(itemStack))return "Head";
		if(isBinding(itemStack))return "Binding";
		if(isHandle(itemStack))return "Handle";
		return "";
	}
	public static boolean isReplacedPartValid(ItemStack template,ItemStack base){
		assert template.getTag()!=null;
		assert base.getTag()!=null;
		if(isTagged(template,DifMod.MODID,"modular_tools_parts/head")){
			return !template.getTag().getString("HeadMaterial").equals(base.getTag().getString("HeadMaterial"));
		}
		if(isTagged(template,DifMod.MODID,"modular_tools_parts/binding")){
			return !template.getTag().getString("BindingMaterial").equals(base.getTag().getString("BindingMaterial"));
		}
		if(isTagged(template,DifMod.MODID,"modular_tools_parts/handle")){
			return !template.getTag().getString("HandleMaterial").equals(base.getTag().getString("HandleMaterial"));
		}
		return false;
	}



	public static void toolRepair(ItemStack template,ItemStack base,ItemStack addition,CompoundTag templateTag,CompoundTag baseTag,CompoundTag additionTag){
		int currentDamage=base.getDamageValue();
		int newDamage=Math.max(0,currentDamage-REPAIR_AMOUNT);
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
		if(base.getItem().equals(DifModItems.MODULAR_PART_PICKAXE_HEAD.get())){
			tool=new ItemStack(DifModItems.MODULAR_PICKAXE.get(),1);
		}else if(base.getItem().equals(DifModItems.MODULAR_PART_AXE_HEAD.get())){
			tool=new ItemStack(DifModItems.MODULAR_AXE.get(),1);
		}else if(base.getItem().equals(DifModItems.MODULAR_PART_SHOVEL_HEAD.get())){
			tool=new ItemStack(DifModItems.MODULAR_SHOVEL.get(),1);
		}else if(base.getItem().equals(DifModItems.MODULAR_PART_SWORD_HEAD.get())){
			tool=new ItemStack(DifModItems.MODULAR_SWORD.get(),1);
		}
		CompoundTag toolTag=new CompoundTag();
		toolTag.putString("Material",baseTag.getString("HeadMaterial"));
		toolTag.putInt("SpecialDurability",1);
		toolTag.putInt("Durability",baseTag.getInt("HeadDurability")+additionTag.getInt("BindingDurability")+templateTag.getInt("HandleDurability")+1);
		toolTag.putInt("HideFlags",4);
		toolTag.putInt("MiningLevel",0);
		toolTag.putInt("Efficiency",4);
		toolTag.putFloat("AttackDamage",2F);
		toolTag.putFloat("AttackSpeed",-2F);
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
	public static boolean toolRepairCheck(ItemStack template,ItemStack base,ItemStack addition,CompoundTag templateTag,CompoundTag baseTag,CompoundTag additionTag){
		return template.getItem().equals(Items.AIR)&&base.getDamageValue()>0&&isTagged(addition,DifMod.MODID,"modular_tools_materials/"+baseTag.getString("Material").toLowerCase());
	}
	public static boolean toolPartReplaceCheck(ItemStack template,ItemStack base,ItemStack addition,CompoundTag templateTag,CompoundTag baseTag,CompoundTag additionTag){
		if(base.getItem().equals(DifModItems.MODULAR_PICKAXE.get())&&isTagged(template,DifMod.MODID,"modular_tools_parts/pickaxe_parts")){
			return isReplacedPartValid(template,base);
		}
		if(base.getItem().equals(DifModItems.MODULAR_AXE.get())&&isTagged(template,DifMod.MODID,"modular_tools_parts/axe_parts")){
			return isReplacedPartValid(template,base);
		}
		if(base.getItem().equals(DifModItems.MODULAR_SHOVEL.get())&&isTagged(template,DifMod.MODID,"modular_tools_parts/shovel_parts")){
			return isReplacedPartValid(template,base);
		}
		if(base.getItem().equals(DifModItems.MODULAR_SWORD.get())&&isTagged(template,DifMod.MODID,"modular_tools_parts/sword_parts")){
			return isReplacedPartValid(template,base);
		}
		return false;
	}
	public static boolean newToolCraftCheck(ItemStack template,ItemStack base,ItemStack addition,CompoundTag templateTag,CompoundTag baseTag,CompoundTag additionTag){
		if(base.getItem().equals(DifModItems.MODULAR_PART_SWORD_HEAD.get())){
			return addition.getItem().equals(DifModItems.MODULAR_PART_SWORD_BINDING.get());
		}else{
			return addition.getItem().equals(DifModItems.MODULAR_PART_BINDING.get());
		}
	}
	public static String materialColor(String partType,CompoundTag tag){
		String mColor="#FFFFFF";
		switch(tag.getString(partType+"Material")){
			case "Wood"->mColor="#745631";
			case "Stone"->mColor="#838383";
			case "Copper"->mColor="#D86D5F";
			case "Iron"->mColor="#DCDCDC";
			case "Gold"->mColor="#F6D142";
			case "Diamond"->mColor="#6DEDE4";
			case "Obsidian"->mColor="#150E22";
			case "Netherite"->mColor="#433F41";
		}
		return mColor;
	}



	public static int colorFromMaterial(String material){
		int color=0xFFFFFF;
		switch(material){
			case "Wood"->color=0x745631;
			case "Stone"->color=0x838383;
			case "Copper"->color=0xD86D5F;
			case "Iron"->color=0xDCDCDC;
			case "Gold"->color=0xF6D142;
			case "Diamond"->color=0x6DEDE4;
			case "Obsidian"->color=0x150E22;
			case "Netherite"->color=0x433F41;
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


	public static void calculateDurability(CompoundTag tag){
		tag.putInt("Durability",tag.getInt("HeadDurability")+tag.getInt("HandleDurability")+tag.getInt("BindingDurability")+tag.getInt("SpecialDurability"));
	}
	protected abstract TagKey<Block> getMineableTag();
	@Override
	public boolean isCorrectToolForDrops(ItemStack itemStack,@NotNull BlockState blockState){
		if(!itemStack.hasTag()) return super.isCorrectToolForDrops(itemStack,blockState);
		CompoundTag tag=itemStack.getOrCreateTag();
		if(tag.getBoolean("Broken")) return false;
		int level=tag.getInt("MiningLevel")+tag.getInt("SpecialMiningLevel");
		if(blockState.is(BlockTags.NEEDS_DIAMOND_TOOL)&&level<3) return false;
		if(blockState.is(BlockTags.NEEDS_IRON_TOOL)&&level<2) return false;
		if(blockState.is(BlockTags.NEEDS_STONE_TOOL)&&level<1) return false;
		return blockState.is(getMineableTag());
	}
	@Override
	public float getDestroySpeed(ItemStack itemStack,@NotNull BlockState state){
		if(!itemStack.hasTag()) return super.getDestroySpeed(itemStack,state);
		CompoundTag tag=itemStack.getOrCreateTag();
		if(tag.getBoolean("Broken")) return 1.0F;
		return state.is(getMineableTag())?tag.getFloat("Efficiency"):1.0F;
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
	public @NotNull InteractionResult useOn(UseOnContext context){
		if(!context.getItemInHand().getItem().equals(DifModItems.MODULAR_AXE.get()))return InteractionResult.PASS;
		Level level=context.getLevel();
		BlockPos blockpos=context.getClickedPos();
		Player player=context.getPlayer();
		BlockState blockstate=level.getBlockState(blockpos);
		Optional<BlockState> optional=Optional.ofNullable(AXE_STRIPPABLES.get(blockstate.getBlock())).map((block)->block.defaultBlockState().setValue(RotatedPillarBlock.AXIS,blockstate.getValue(RotatedPillarBlock.AXIS)));
		if(optional.isPresent()){
			level.playSound(player,blockpos,SoundEvents.AXE_STRIP,SoundSource.BLOCKS,1.0F,1.0F);
			if(!level.isClientSide){
				level.setBlock(blockpos,optional.get(),11);
				if(player!=null){
					context.getItemInHand().hurtAndBreak(1,player,(p)->p.broadcastBreakEvent(context.getHand()));
				}
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}else{
			return InteractionResult.PASS;
		}
	}
	@Override
	public void inventoryTick(@NotNull ItemStack itemStack,@NotNull Level world,@NotNull Entity entity,int slot,boolean selected){
		if(!world.isClientSide()){
			CompoundTag tag=itemStack.getOrCreateTag();
			if(!tag.contains("MiningLevel")) tag.putInt("MiningLevel",defaultMiningLevel);
			if(!tag.contains("SpecialMiningLevel"))tag.putInt("SpecialMiningLevel",0);

			if(!tag.contains("Efficiency")) tag.putFloat("Efficiency",defaultEfficiency);
			if(!tag.contains("AttackDamage")) tag.putFloat("AttackDamage",defaultAttackDamage);
			if(!tag.contains("AttackSpeed")) tag.putFloat("AttackSpeed",defaultAttackSpeed);

			if(!tag.contains("HeadMaterial"))tag.putString("HeadMaterial",defaultMaterial);
			if(!tag.contains("HeadDurability"))tag.putInt("HeadDurability",defaultDurability);
			tag.putInt("HeadColor",colorFromMaterial(tag.getString("HeadMaterial")));

			if(!tag.contains("HandleMaterial"))tag.putString("HandleMaterial",defaultMaterial);
			if(!tag.contains("HandleDurability"))tag.putInt("HandleDurability",defaultDurability);
			tag.putInt("HandleColor",colorFromMaterial(tag.getString("HandleMaterial")));

			if(!tag.contains("BindingMaterial"))tag.putString("BindingMaterial",defaultMaterial);
			if(!tag.contains("BindingDurability"))tag.putInt("BindingDurability",defaultDurability);
			tag.putInt("BindingColor",colorFromMaterial(tag.getString("BindingMaterial")));

			if(!tag.contains("SpecialDurability"))tag.putInt("SpecialDurability",1);
			calculateDurability(tag);

			if(!tag.contains("Material")) tag.putString("Material",tag.getString("HeadMaterial"));

			if(!tag.contains("Broken")) tag.putBoolean("Broken",false);
			if(!tag.contains("Unbreakable")) tag.putBoolean("Unbreakable",false);
			if(!tag.contains("HideFlags")) tag.putInt("HideFlags",4);
			if(itemStack.getMaxDamage()-itemStack.getDamageValue()==1){
				tag.putBoolean("Broken",true);
				tag.putBoolean("Unbreakable",true);
				tag.putInt("CustomModelData",1);
			}else{
				tag.putBoolean("Broken",false);
				tag.putBoolean("Unbreakable",false);
				tag.putInt("CustomModelData",0);
			}
		}
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemStack,Level world,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		if(!itemStack.hasTag()) return;
		assert itemStack.getTag()!=null;
		CompoundTag tag=itemStack.getTag();
		if(!tag.contains("MiningLevel")||!tag.contains("Durability")||!tag.contains("Efficiency")||!tag.contains("AttackDamage")||!tag.contains("AttackSpeed"))return;
		if(Screen.hasControlDown()){
			list.add(Component.literal("Tool Material: ").withStyle(ChatFormatting.WHITE).append(Component.translatable(tag.getString("Material")).withStyle(Style.EMPTY.withColor(TextColor.parseColor(materialColor("",tag))))));

			list.add(Component.literal("Head:").withStyle(ChatFormatting.WHITE));
			list.add(CommonComponents.space().append(Component.literal("Material: ").withStyle(ChatFormatting.WHITE).append(Component.translatable(tag.getString("HeadMaterial")).withStyle(Style.EMPTY.withColor(TextColor.parseColor(materialColor("Head",tag)))))));
			list.add(CommonComponents.space().append(Component.literal("Durability: ").withStyle(ChatFormatting.WHITE).append(Component.translatable(String.valueOf(tag.getInt("HeadDurability"))).withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(durabilityColor("Head",tag)))))));

			list.add(Component.literal("Binding:").withStyle(ChatFormatting.WHITE));
			list.add(CommonComponents.space().append(Component.literal("Material: ").withStyle(ChatFormatting.WHITE).append(Component.translatable(tag.getString("BindingMaterial")).withStyle(Style.EMPTY.withColor(TextColor.parseColor(materialColor("Binding",tag)))))));
			list.add(CommonComponents.space().append(Component.literal("Durability: ").withStyle(ChatFormatting.WHITE).append(Component.translatable(String.valueOf(tag.getInt("BindingDurability"))).withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(durabilityColor("Binding",tag)))))));

			list.add(Component.literal("Handle:").withStyle(ChatFormatting.WHITE));
			list.add(CommonComponents.space().append(Component.literal("Material: ").withStyle(ChatFormatting.WHITE).append(Component.translatable(tag.getString("HandleMaterial")).withStyle(Style.EMPTY.withColor(TextColor.parseColor(materialColor("Handle",tag)))))));
			list.add(CommonComponents.space().append(Component.literal("Durability: ").withStyle(ChatFormatting.WHITE).append(Component.translatable(String.valueOf(tag.getInt("HandleDurability"))).withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(durabilityColor("Handle",tag)))))));

		}else{
			int mLevel=tag.getInt("Mining Level");
			String[] levelColors={"#915A2D","#555555","#C6C6C6","#55FFFF","#301100"};
			String color=mLevel<levelColors.length?levelColors[mLevel]:"#FFFFFF";
			list.add(Component.literal("Mining Level: ").append(Component.translatable("dif.mining_level."+mLevel).withStyle(Style.EMPTY.withColor(TextColor.parseColor(color)))));
			int currentDamage=itemStack.getDamageValue()+1;
			int maxDurability=tag.getInt("Durability");
			int remainingDurability=Math.max(0,maxDurability-currentDamage);
			// Výpočet barvy od zelené (#00FF00) po červenou (#FF0000)
			float ratio=(float)remainingDurability/maxDurability;
			int red=(int)(255*(1-ratio));
			int green=(int)(255*ratio);
			String hexColor=String.format("#%02X%02X00",red,green);
			list.add(Component.literal("Durability: ").append(Component.literal(String.valueOf(remainingDurability)).withStyle(Style.EMPTY.withColor(TextColor.parseColor(hexColor)))).append(Component.literal(" / "+(maxDurability-1)).withStyle(Style.EMPTY.withColor(TextColor.parseColor("#AAAAAA")))));
			list.add(Component.literal("Efficiency: ").append(Component.literal(String.valueOf(tag.getInt("Efficiency"))).withStyle(Style.EMPTY.withColor(TextColor.parseColor("#55FF55")))));
			float displayDamage=1.0F+tag.getFloat("AttackDamage");
			list.add(Component.literal("Attack Damage: ").append(Component.literal(String.format(Locale.ROOT,"%.1f",displayDamage)).withStyle(Style.EMPTY.withColor(TextColor.parseColor("#FF5555")))));
			float displaySpeed=4.0F+tag.getFloat("AttackSpeed");
			list.add(Component.literal("Attack Speed: ").append(Component.literal(String.format(Locale.ROOT,"%.1f",displaySpeed)).withStyle(Style.EMPTY.withColor(TextColor.parseColor("#FFFF55")))));
			if(tag.getBoolean("Broken"))
				list.add(Component.literal("Broken").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED).withBold(true)));
		}

	}
}