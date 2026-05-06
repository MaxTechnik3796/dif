package cz.maxtechnik.dif.item.armor;

import cz.maxtechnik.dif.init.other.DifModTiers;
import net.minecraft.world.item.ArmorItem;

public abstract class CarbonSuit extends ArmorItem {
	public CarbonSuit(Type type, Properties properties) {
		super(DifModTiers.ARMOR_MATERIAL_CARBON, type, properties);
	}

	public static class Helmet extends CarbonSuit {
		public Helmet() {
			super(Type.HELMET, new Properties());
		}
	}

	public static class Chestplate extends CarbonSuit {
		public Chestplate() {
			super(Type.CHESTPLATE, new Properties());
		}
	}

	public static class Leggings extends CarbonSuit {
		public Leggings() {
			super(Type.LEGGINGS, new Properties());
		}
	}

	public static class Boots extends CarbonSuit {
		public Boots() {
			super(Type.BOOTS, new Properties());
		}
	}
}
