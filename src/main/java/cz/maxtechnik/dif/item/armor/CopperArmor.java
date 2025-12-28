package cz.maxtechnik.dif.item.armor;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
public abstract class CopperArmor extends ArmorItem{
	public CopperArmor(ArmorItem.Type type,Item.Properties properties){
		super(new ArmorMaterial(){
			@Override
			public int getDurabilityForType(ArmorItem.@NotNull Type type){
				return new int[]{13,15,16,11}[type.getSlot().getIndex()]*15;
			}
			@Override
			public int getDefenseForType(ArmorItem.@NotNull Type type){
				return new int[]{2,5,6,2}[type.getSlot().getIndex()];
			}
			@Override
			public int getEnchantmentValue(){
				return 9;
			}
			@Override
			public @NotNull SoundEvent getEquipSound(){
				return SoundEvents.EMPTY;
			}
			@Override
			public @NotNull Ingredient getRepairIngredient(){
				return Ingredient.of(new ItemStack(Items.COPPER_INGOT));
			}
			@Override
			public @NotNull String getName(){
				return "Copper";
			}
			@Override
			public float getToughness(){
				return 0f;
			}
			@Override
			public float getKnockbackResistance(){
				return 0f;
			}
		},type,properties);
	}
	public static class Helmet extends CopperArmor{
		public Helmet(){
			super(ArmorItem.Type.HELMET,new Item.Properties());
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){
			return "dif:textures/models/armor/copper_layer_1.png";
		}
	}
	public static class Chestplate extends CopperArmor{
		public Chestplate(){
			super(ArmorItem.Type.CHESTPLATE,new Item.Properties());
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){
			return "dif:textures/models/armor/copper_layer_1.png";
		}
	}
	public static class Leggings extends CopperArmor{
		public Leggings(){
			super(ArmorItem.Type.LEGGINGS,new Item.Properties());
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){
			return "dif:textures/models/armor/copper_layer_2.png";
		}
	}
	public static class Boots extends CopperArmor{
		public Boots(){
			super(ArmorItem.Type.BOOTS,new Item.Properties());
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){
			return "dif:textures/models/armor/copper_layer_1.png";
		}
	}
}