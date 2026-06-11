package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularModifiers{
	//regular:
	EFFICIENCY("efficiency",5),
	FORTUNE("fortune",3),
	SILK_TOUCH("silk_touch",1),
	SHARPNESS("sharpness",5),


	//material:
	ECOLOGICAL("ecological",-1),
	MAGNETIC("magnetic",-1),
	CHEAP("cheap",-1),
	SHINY("shiny",-1),

	//special:
	EXCAVATOR("excavator",-2);
	private final String name;
	private final int maxLvl;
	ModularModifiers(String name, int maxLvl){
		this.name=name;
		this.maxLvl=maxLvl;
	}
	public String getName(){
		return name;
	}
	public int getMaxLvl(){
		return maxLvl;
	}
	public static ModularModifiers byName(String name){
		try{
			return ModularModifiers.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return EFFICIENCY;
		}
	}
}
