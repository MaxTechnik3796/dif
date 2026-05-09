package cz.maxtechnik.dif.item.armor;

import cz.maxtechnik.dif.init.other.DifModTiers;
import net.minecraft.world.item.*;
public abstract class CopperArmor extends ArmorItem{
	public CopperArmor(ArmorItem.Type type,Item.Properties properties){
		super(DifModTiers.ARMOR_MATERIAL,type,properties.stacksTo(1));
	}
	public static class Helmet extends CopperArmor{
		public Helmet(){
			super(ArmorItem.Type.HELMET,new Item.Properties().stacksTo(1));
		}
	}
	public static class Chestplate extends CopperArmor{
		public Chestplate(){
			super(ArmorItem.Type.CHESTPLATE,new Item.Properties().stacksTo(1));
		}
	}
	public static class Leggings extends CopperArmor{
		public Leggings(){
			super(ArmorItem.Type.LEGGINGS,new Item.Properties().stacksTo(1));
		}
	}
	public static class Boots extends CopperArmor{
		public Boots(){
			super(ArmorItem.Type.BOOTS,new Item.Properties().stacksTo(1));
		}
	}
}