package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularTier{
	COMMON("common",0,0xFFFFFF),
	RARE("rare",1,0x5555FF),
	EPIC("epic",2,0xAA00AA),
	LEGENDARY("legendary",3,0xFFAA00),
	MYTHIC("mythic",4,0xFF55FF);
	private final String name;
	private final int reforgeIndex;
	private final int color;
	ModularTier(String name,int reforgeIndex,int color){
		this.name=name;
		this.reforgeIndex=reforgeIndex;
		this.color=color;
	}
	public String getName(){
		return name;
	}
	public int getReforgeIndex(){
		return reforgeIndex;
	}
	public int getColor(){
		return color;
	}
	public static ModularTier byName(String name){
		try{
			return ModularTier.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return COMMON;
		}
	}
}
