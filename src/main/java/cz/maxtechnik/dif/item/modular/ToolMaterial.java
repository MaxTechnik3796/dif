package cz.maxtechnik.dif.item.modular;

public enum ToolMaterial {
	WOOD    ("Wood",     30,   2,  0, 0x745631),
	STONE   ("Stone",    70,   4,  1, 0x838383),
	COPPER  ("Copper",   80,   5,  1, 0xD86D5F),
	IRON    ("Iron",    190,   6,  2, 0xDCDCDC),
	GOLD    ("Gold",     10,  12,  0, 0xF6D142),
	DIAMOND ("Diamond", 1450,  8,  3, 0x6DEDE4),
	OBSIDIAN("Obsidian",1800,  9,  3, 0x391872),
	NETHERITE("Netherite",2850,9,  4, 0x433F41);

	// Základní durabilita hlavy
	// Binding dostane 50%, Handle 15%
	public final String name;
	public final int baseDurability;
	public final int efficiency;
	public final int miningLevel;
	public final int color;

	ToolMaterial(String name, int baseDurability, int efficiency, int miningLevel, int color) {
		this.name = name;
		this.baseDurability = baseDurability;
		this.efficiency = efficiency;
		this.miningLevel = miningLevel;
		this.color = color;
	}

	public int headDurability() {
		return baseDurability;
	}

	public int bindingDurability() {
		return (int)(baseDurability * 0.5);
	}

	public int handleDurability() {
		return (int)(baseDurability * 0.15);
	}

	public String colorHex() {
		return String.format("#%06X", color);
	}

	public static ToolMaterial fromName(String name) {
		for (ToolMaterial m : values())
			if (m.name.equals(name)) return m;
		return WOOD;
	}

	// Každý materiál má svůj pasivní efekt popsaný textem
	public String passiveKey() {
		return switch (this) {
			case WOOD -> "natural";
			case STONE -> "cheap";
			case IRON -> "magnetic";
			case GOLD -> "speedy";
			default -> "";
		};
	}
}