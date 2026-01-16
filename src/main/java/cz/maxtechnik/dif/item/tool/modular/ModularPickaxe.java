package cz.maxtechnik.dif.item.tool.modular;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class ModularPickaxe extends PickaxeItem{
	int MINING_LEVEL=0;
	int DURABILITY=100;
	float EFFICIENCY=4F;
	float ATTACK_DAMAGE=2F;
	float ATTACK_SPEED=-2.8F;
	public ModularPickaxe(){
		super(new Tier(){
			@Override
			public int getUses(){
				return 100;
			}
			@Override
			public float getSpeed(){
				return 4F;
			}
			@Override
			public float getAttackDamageBonus(){
				return 2F;
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
		},1,-2.8F,new Properties());
	}
	@Override
	public boolean isCorrectToolForDrops(ItemStack itemStack,@NotNull BlockState blockState){
		if(!itemStack.hasTag()) return super.isCorrectToolForDrops(itemStack,blockState);
		assert itemStack.getTag()!=null;
		int level=itemStack.getTag().getInt("MiningLevel");
		// Kontrola požadavků bloku proti úrovni v NBT
		if(itemStack.getTag().getBoolean("Broken")) return false;
		if(blockState.is(BlockTags.NEEDS_DIAMOND_TOOL)&&level<3) return false;
		if(blockState.is(BlockTags.NEEDS_IRON_TOOL)&&level<2) return false;
		if(blockState.is(BlockTags.NEEDS_STONE_TOOL)&&level<1) return false;
		return blockState.is(BlockTags.MINEABLE_WITH_PICKAXE);
	}
	@Override
	public int getMaxDamage(ItemStack itemStack){
		if(itemStack.hasTag()){
			assert itemStack.getTag()!=null;
			if(!itemStack.getTag().contains("Durability")) itemStack.getOrCreateTag().putInt("Durability",DURABILITY);
			return itemStack.getTag().getInt("Durability");
		}
		return super.getMaxDamage(itemStack);
	}
	@Override
	public float getDestroySpeed(ItemStack itemStack,@NotNull BlockState state){
		if(itemStack.hasTag()){
			assert itemStack.getTag()!=null;
			if(!itemStack.getTag().contains("Efficiency")) itemStack.getOrCreateTag().putFloat("Efficiency",EFFICIENCY);
			if(itemStack.getTag().getBoolean("Broken")){
				return 1F;
			}else{
				return itemStack.isCorrectToolForDrops(state)?itemStack.getTag().getFloat("Efficiency"):1F;
			}
		}
		return super.getDestroySpeed(itemStack,state);
	}
	@Override
	public Multimap<Attribute,AttributeModifier> getAttributeModifiers(EquipmentSlot slot,ItemStack itemStack){
		if(slot==EquipmentSlot.MAINHAND&&itemStack.hasTag()){
			assert itemStack.getTag()!=null;
			ImmutableMultimap.Builder<Attribute,AttributeModifier> builders=ImmutableMultimap.builder();
			// Attack Damage
			float damage;
			if(!itemStack.getTag().contains("AttackDamage"))
				itemStack.getOrCreateTag().putFloat("AttackDamage",ATTACK_DAMAGE);
			damage=itemStack.getTag().getFloat("AttackDamage");
			if(!itemStack.getTag().getBoolean("Broken"))
				builders.put(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,"Weapon modifier",damage,AttributeModifier.Operation.ADDITION));
			// Attack Speed
			// Pozor: Výchozí speed je 4.0, modifikátor je záporné číslo (např. -2.8 znamená výslednou rychlost 1.2)
			float speed;
			if(!itemStack.getTag().contains("AttackSpeed"))
				itemStack.getOrCreateTag().putFloat("AttackSpeed",ATTACK_SPEED);
			speed=itemStack.getTag().getFloat("AttackSpeed");
			if(!itemStack.getTag().getBoolean("Broken"))
				builders.put(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_UUID,"Weapon modifier",speed,AttributeModifier.Operation.ADDITION));
			return builders.build();
		}
		return super.getAttributeModifiers(slot,itemStack);
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemstack,Level world,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemstack,world,list,flag);
		if(!itemstack.hasTag()) return;
		CompoundTag tag=itemstack.getTag();
		if(tag!=null){
			// --- Mining Level Logic ---
			String miningLevelColor;
			int mLevel=tag.getInt("MiningLevel");
			switch(mLevel){
				case 0 -> miningLevelColor="#915A2D"; // Dřevo
				case 1 -> miningLevelColor="#555555"; // Kámen
				case 2 -> miningLevelColor="#C6C6C6"; // Železo
				case 3 -> miningLevelColor="#55FFFF"; // Diamant
				case 4 -> miningLevelColor="#301100"; // Netherite
				default -> miningLevelColor="#FFFFFF";
			}
			list.add(Component.literal("Mining Level: ").append(Component.translatable("dif.mining_level."+mLevel).withStyle(Style.EMPTY.withColor(TextColor.parseColor(miningLevelColor)))));
			int currentDamage=itemstack.getDamageValue()+1;
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
			list.add(Component.literal("Attack Damage: ").append(Component.literal(String.valueOf(displayDamage)).withStyle(Style.EMPTY.withColor(TextColor.parseColor("#FF5555")))));
			float displaySpeed=4.0F+tag.getFloat("AttackSpeed");
			list.add(Component.literal("Attack Speed: ").append(Component.literal(String.valueOf(displaySpeed)).withStyle(Style.EMPTY.withColor(TextColor.parseColor("#FFFF55")))));
		}
	}
	@Override
	public void inventoryTick(@NotNull ItemStack itemStack,@NotNull Level world,@NotNull Entity entity,int slot,boolean selected){
		super.inventoryTick(itemStack,world,entity,slot,selected);
		assert itemStack.getTag()!=null;
		if(!itemStack.getTag().contains("MiningLevel")) itemStack.getOrCreateTag().putInt("MiningLevel",MINING_LEVEL);
		if(!itemStack.getTag().contains("Durability")) itemStack.getOrCreateTag().putInt("Durability",DURABILITY);
		if(!itemStack.getTag().contains("Efficiency")) itemStack.getOrCreateTag().putFloat("Efficiency",EFFICIENCY);
		if(!itemStack.getTag().contains("AttackDamage"))itemStack.getOrCreateTag().putFloat("AttackDamage",ATTACK_DAMAGE);
		if(!itemStack.getTag().contains("AttackSpeed")) itemStack.getOrCreateTag().putFloat("AttackSpeed",ATTACK_SPEED);
		if(!itemStack.getTag().contains("Broken")) itemStack.getOrCreateTag().putBoolean("Broken",false);
		if(!itemStack.getTag().contains("Unbreakable")) itemStack.getOrCreateTag().putBoolean("Unbreakable",false);
		if(!itemStack.getTag().contains("CustomModelData")) itemStack.getOrCreateTag().putFloat("CustomModelData",0);
		if(!itemStack.getTag().contains("HeadColor")) itemStack.getOrCreateTag().putFloat("HeadColor",0xFFFFFF);
		if(!itemStack.getTag().contains("HandleColor")) itemStack.getOrCreateTag().putFloat("HandleColor",0x915A2D);
		if(!itemStack.getTag().contains("BindingColor")) itemStack.getOrCreateTag().putFloat("BindingColor",0xFFFF00);
		if(itemStack.getMaxDamage()-itemStack.getDamageValue()==1){
			itemStack.getOrCreateTag().putBoolean("Broken",true);
			itemStack.getOrCreateTag().putBoolean("Unbreakable",true);
			itemStack.getOrCreateTag().putInt("CustomModelData",1);
		}else{
			itemStack.getOrCreateTag().putBoolean("Broken",false);
			itemStack.getOrCreateTag().putBoolean("Unbreakable",false);
			itemStack.getOrCreateTag().putInt("CustomModelData",0);
		}
	}
}