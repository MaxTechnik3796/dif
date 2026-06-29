package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularTools{
	NONE("none",0),
	PICKAXE("pickaxe",1),
	AXE("axe",2),
	SWORD("sword",3),
	SHOVEL("shovel",4),
	HOE("hoe",5),
	KATANA("katana",6),
	BATTLE_AXE("battle_axe",7),
	HAMMER("hammer",8),
	TIMBER_AXE("timber_axe",9),
	EXCAVATOR("excavator",10);
	private final String name;
	private final int toolID;
	ModularTools(String name, int toolID){
		this.name=name;
		this.toolID=toolID;
	}
	public String getName(){
		return this.name;
	}
	public int getID(){
		return this.toolID;
	}
	public static ModularTools byName(String name){
		try{
			return ModularTools.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return AXE;
		}
	}
}
