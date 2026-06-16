package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;

public enum ModularModifier{
	//material:
	ECOLOGICAL("ecological",0x745631),
	MAGNETIC("magnetic",0xDCDCDC),
	CHEAP("cheap",-0x838383),
	SHINY("shiny",0xFFD700),

	//special:
	EXCAVATOR("excavator",0xFFFFFF),

	NONE("none",0xFFFFFF);
	private final String name;
	private final int color;
	ModularModifier(String name,int color){
		this.name=name;
		this.color=color;
	}
	public String getName(){
		return name;
	}
	public int getColor(){
		return color;
	}
	public static ModularModifier byName(String name){
		try{
			return ModularModifier.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return ECOLOGICAL;
		}
	}
}
