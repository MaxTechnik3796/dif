package cz.maxtechnik.dif.item.armor;

import cz.maxtechnik.dif.init.DifModTiers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
public abstract class CopperArmor extends ArmorItem{
	public CopperArmor(ArmorItem.Type type,Item.Properties properties){
		super(DifModTiers.ARMOR_MATERIAL,type,properties);
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