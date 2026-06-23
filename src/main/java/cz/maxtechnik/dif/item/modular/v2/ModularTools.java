package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularTools{
	NONE("none"),
	AXE("axe"),
	PICKAXE("pickaxe"),
	SWORD("sword"),
	SHOVEL("shovel"),
	HOE("hoe");
	private final String name;
	ModularTools(String name){
		this.name=name;
	}
	public String getName(){
		return this.name;
	}
	public static ModularTools byName(String name){
		try{
			return ModularTools.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return AXE;
		}
	}
}
