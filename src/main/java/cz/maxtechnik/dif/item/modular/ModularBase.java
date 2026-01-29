package cz.maxtechnik.dif.item.modular;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
/**
 * Abstraktní základ pro všechny modulární nástroje (krumpáč, sekera, lopata...).
 */
public abstract class ModularBase extends DiggerItem{
	protected String defaultMaterial;
	protected int defaultMiningLevel;
	protected int defaultDurability;
	protected float defaultEfficiency;
	protected float defaultAttackDamage;
	protected float defaultAttackSpeed;
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
		return isTagged(itemStack,"dif","modular_tools_parts/head");
	}
	public static boolean isBinding(ItemStack itemStack){
		return isTagged(itemStack,"dif","modular_tools_parts/binding");
	}
	public static boolean isHandle(ItemStack itemStack){
		return isTagged(itemStack,"dif","modular_tools_parts/handle");
	}
	public static String getPartType(ItemStack itemStack){
		if(isHead(itemStack))return "Head";
		if(isBinding(itemStack))return "Binding";
		if(isHandle(itemStack))return "Handle";
		return "";
	}
	public static int colorFromMaterial(String material){
		int color=0xFFFFFF;
		switch(material){
			case "Wood"->color=0x915A2D;
			case "Stone"->color=0x6E6E6E;
			case "Iron"->color=0xB5B5B5;
			case "Gold"->color=0xFFCF4D;
			case "Diamond"->color=0x00C7BA;
			case "Netherite"->color=0x3E1504;
		}
		return color;
	}


	/**
	 * Každý nástroj musí definovat, které bloky těží (např. BlockTags.MINEABLE_WITH_PICKAXE).
	 */
	protected abstract TagKey<Block> getMineableTag();
	@Override
	public boolean isCorrectToolForDrops(ItemStack itemStack,@NotNull BlockState blockState){
		if(!itemStack.hasTag()) return super.isCorrectToolForDrops(itemStack,blockState);
		CompoundTag tag=itemStack.getOrCreateTag();
		if(tag.getBoolean("Broken")) return false;
		int level=tag.getInt("MiningLevel");
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
	public void inventoryTick(@NotNull ItemStack itemStack,@NotNull Level world,@NotNull Entity entity,int slot,boolean selected){
		if(!world.isClientSide()){
			CompoundTag tag=itemStack.getOrCreateTag();
			if(!tag.contains("MiningLevel")) tag.putInt("MiningLevel",defaultMiningLevel);

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


			if(!tag.contains("SpecialDurability"))tag.putInt("SpecialDurability",0);
			if(!tag.contains("Durability")) tag.putInt("Durability",tag.getInt("HeadDurability")+tag.getInt("HandleDurability")+tag.getInt("BindingDurability")+tag.getInt("SpecialDurability"));


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
			list.add(Component.literal("Broken").withStyle(Style.EMPTY.withColor(TextColor.parseColor("#4F0803")).withBold(true)));
	}
}