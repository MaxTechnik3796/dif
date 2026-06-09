package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularParts{
	HANDLE("handle"),
	BINDING("binding"),
	AXE_HEAD("axe_head"),
	PICKAXE_HEAD("pickaxe_head"),
	SWORD_HEAD("sword_head"),
	SHOVEL_HEAD("shovel_head"),
	SWORD_BINDING("sword_binding"),
	HOE_HEAD("hoe_head");
	private final String name;
	ModularParts(String name){
		this.name=name;
	}
	public String getName(){
		return this.name;
	}
	public static ModularParts byName(String name){
		try{
			return ModularParts.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return HANDLE;
		}
	}
}
