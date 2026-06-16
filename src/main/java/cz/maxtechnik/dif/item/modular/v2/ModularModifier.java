package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularMaterial.*;
public enum ModularModifier{
	//regular(max lvl):
	EFFICIENCY("efficiency",5,0xFFFFFF),
	FORTUNE("fortune",3,0xFFFFFF),
	SILK_TOUCH("silk_touch",1,0xFFFFFF),
	SHARPNESS("sharpness",5,0xFFFFFF),


	//material(-1):
	ECOLOGICAL("ecological",-1,WOOD.getColor()),
	MAGNETIC("magnetic",-1,IRON.getColor()),
	CHEAP("cheap",-1,STONE.getColor()),
	SHINY("shiny",-1,GOLD.getColor()),

	//special(-2):
	EXCAVATOR("excavator",-2,0xFFFFFF),

	NONE("none",-3,0xFFFFFF);
	private final String name;
	private final int maxLvl;
	private final int color;
	ModularModifier(String name,int maxLvl,int color){
		this.name=name;
		this.maxLvl=maxLvl;
		this.color=color;
	}
	public String getName(){
		return name;
	}
	public int getMaxLvl(){
		return maxLvl;
	}
	public int getColor(){
		return color;
	}
	public static ModularModifier byName(String name){
		try{
			return ModularModifier.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return EFFICIENCY;
		}
	}
}
