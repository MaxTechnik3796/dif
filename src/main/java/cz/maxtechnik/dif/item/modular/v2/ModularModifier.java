package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularModifier{
	//regular(max lvl):
	EFFICIENCY("efficiency",5),
	FORTUNE("fortune",3),
	SILK_TOUCH("silk_touch",1),
	SHARPNESS("sharpness",5),


	//material(-1):
	ECOLOGICAL("ecological",-1),
	MAGNETIC("magnetic",-1),
	CHEAP("cheap",-1),
	SHINY("shiny",-1),

	//special(-2):
	EXCAVATOR("excavator",-2);
	private final String name;
	private final int maxLvl;
	ModularModifier(String name,int maxLvl){
		this.name=name;
		this.maxLvl=maxLvl;
	}
	public String getName(){
		return name;
	}
	public int getMaxLvl(){
		return maxLvl;
	}
	public static ModularModifier byName(String name){
		try{
			return ModularModifier.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return EFFICIENCY;
		}
	}
}
