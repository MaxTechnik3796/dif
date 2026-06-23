package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularTier{
	COMMON("common",0xFFFFFF),
	RARE("rare",0x5555FF),
	EPIC("epic",0xAA00AA),
	LEGENDARY("legendary",0xFFAA00),
	MYTHIC("mythic",0xFF55FF),;
	final String name;
	final int color;
	ModularTier(String name,int color){
		this.name=name;

		this.color=color;
	}
	public String getName(){
		return name;
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
