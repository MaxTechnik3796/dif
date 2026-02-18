
package cz.maxtechnik.dif.item.armor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
public abstract class CarbonSuit extends ArmorItem{
	public CarbonSuit(Type type,Properties properties){
		super(new ArmorMaterial(){
			@Override
			public int getDurabilityForType(@NotNull Type type){
				return new int[]{13,15,16,11}[type.getSlot().getIndex()]*128;
			}
			@Override
			public int getDefenseForType(@NotNull Type type){
				return new int[]{4,7,9,4}[type.getSlot().getIndex()];
			}
			@Override
			public int getEnchantmentValue(){
				return 15;
			}
			@Override
			public @NotNull SoundEvent getEquipSound(){
				return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath("minecraft","item.armor.equip_diamond")));
			}
			@Override
			public @NotNull Ingredient getRepairIngredient(){
				return Ingredient.of();
			}
			@Override
			public @NotNull String getName(){
				return "carbon_suit";
			}
			@Override
			public float getToughness(){
				return 3F;
			}
			@Override
			public float getKnockbackResistance(){
				return 0.2F;
			}
		},type,properties);
	}
	public static class Helmet extends CarbonSuit{
		public Helmet(){
			super(Type.HELMET,new Properties());
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){
			return "dif:textures/models/armor/carbon_suit_layer_1.png";
		}
	}
	public static class Chestplate extends CarbonSuit{
		public Chestplate(){
			super(Type.CHESTPLATE,new Properties());
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){
			return "dif:textures/models/armor/carbon_suit_layer_1.png";
		}
	}
	public static class Leggings extends CarbonSuit{
		public Leggings(){
			super(Type.LEGGINGS,new Properties());
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){
			return "dif:textures/models/armor/carbon_suit_layer_2.png";
		}
	}
	public static class Boots extends CarbonSuit{
		public Boots(){
			super(Type.BOOTS,new Properties());
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){
			return "dif:textures/models/armor/carbon_suit_layer_1.png";
		}
	}
}
