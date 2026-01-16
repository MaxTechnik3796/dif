package cz.maxtechnik.dif.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
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

import static java.lang.Math.round;
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
		if(!itemStack.hasTag())return super.isCorrectToolForDrops(itemStack,blockState);
		assert itemStack.getTag()!=null;
		int level=itemStack.getTag().getInt("MiningLevel");
		// Kontrola požadavků bloku proti úrovni v NBT
		if (blockState.is(BlockTags.NEEDS_DIAMOND_TOOL)&&level<3)return false;
		if (blockState.is(BlockTags.NEEDS_IRON_TOOL)&&level<2) return false;
		if (blockState.is(BlockTags.NEEDS_STONE_TOOL)&&level<1) return false;
		return blockState.is(BlockTags.MINEABLE_WITH_PICKAXE);
	}
	@Override
	public int getMaxDamage(ItemStack stack){
		if(stack.hasTag()){
			assert stack.getTag()!=null;
			if(!stack.getTag().contains("Durability"))stack.getOrCreateTag().putInt("Durability",DURABILITY);
			return stack.getTag().getInt("Durability");
		}
		return super.getMaxDamage(stack);
	}
	@Override
	public float getDestroySpeed(ItemStack itemStack,@NotNull BlockState state){
		if(itemStack.hasTag()){
			assert itemStack.getTag()!=null;
			if(!itemStack.getTag().contains("Efficiency"))itemStack.getOrCreateTag().putFloat("Efficiency",EFFICIENCY);
			return itemStack.isCorrectToolForDrops(state)?itemStack.getTag().getFloat("Efficiency"):1F;
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
			if(!itemStack.getTag().contains("AttackDamage"))itemStack.getOrCreateTag().putFloat("AttackDamage",ATTACK_DAMAGE);
			damage=itemStack.getTag().getFloat("AttackDamage");
			builders.put(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,"Weapon modifier",damage,AttributeModifier.Operation.ADDITION));

			// Attack Speed
			// Pozor: Výchozí speed je 4.0, modifikátor je záporné číslo (např. -2.8 znamená výslednou rychlost 1.2)
			float speed;
			if(!itemStack.getTag().contains("AttackSpeed"))itemStack.getOrCreateTag().putFloat("AttackSpeed",ATTACK_SPEED);
			speed=itemStack.getTag().getFloat("AttackSpeed");
			builders.put(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_UUID,"Weapon modifier",speed,AttributeModifier.Operation.ADDITION));
			return builders.build();
		}
		return super.getAttributeModifiers(slot,itemStack);
	}
	@Override
	public void appendHoverText(@NotNull ItemStack itemstack,Level world,@NotNull List<Component>list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemstack,world,list,flag);
		assert itemstack.getTag()!=null;
		String MiningLevelColor;
		switch(itemstack.getTag().getInt("MiningLevel")){
			case 0->MiningLevelColor="#915A2D";
			case 1->MiningLevelColor="#555555";
			case 2->MiningLevelColor="#C6C6C6";
			case 3->MiningLevelColor="#55FFFF";
			case 4->MiningLevelColor="#301100";
			default->MiningLevelColor="#FFFFFF";
		}

		list.add(Component.literal("MiningLevel: ").append(Component.translatable("dif.mining_level."+itemstack.getTag().getInt("MiningLevel")).withStyle(Style.EMPTY.withColor(TextColor.parseColor(MiningLevelColor)))));
		list.add(Component.literal("Durability: ").append(Component.literal(String.valueOf(itemstack.getTag().getInt("Durability")))));
		list.add(Component.literal("Efficiency: ").append(Component.literal(String.valueOf(itemstack.getTag().getInt("Efficiency")))));
		list.add(Component.literal("AttackDamage: ").append(Component.literal(String.valueOf(itemstack.getTag().getInt("AttackDamage")))));
		list.add(Component.literal("AttackSpeed: ").append(Component.literal(String.valueOf(itemstack.getTag().getInt("AttackSpeed")))));
	}
	@Override
	public void inventoryTick(@NotNull ItemStack itemStack,@NotNull Level world,@NotNull Entity entity,int slot,boolean selected){
		super.inventoryTick(itemStack,world,entity,slot,selected);
		assert itemStack.getTag()!=null;
		if(!itemStack.getTag().contains("MiningLevel"))itemStack.getOrCreateTag().putInt("MiningLevel",MINING_LEVEL);
		if(!itemStack.getTag().contains("Durability"))itemStack.getOrCreateTag().putInt("Durability",DURABILITY);
		if(!itemStack.getTag().contains("Efficiency"))itemStack.getOrCreateTag().putFloat("Efficiency",EFFICIENCY);
		if(!itemStack.getTag().contains("AttackDamage"))itemStack.getOrCreateTag().putFloat("AttackDamage",ATTACK_DAMAGE);
		if(!itemStack.getTag().contains("AttackSpeed"))itemStack.getOrCreateTag().putFloat("AttackSpeed",ATTACK_SPEED);
	}
}