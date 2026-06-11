package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularModifiers{
	EFFICIENCY("efficiency",5),
	FORTUNE("fortune",3),
	SILK_TOUCH("silk_touch",1),
	SHARPNESS("sharpness",5);
	final String name;
	final int maxLvl;
	ModularModifiers(String name, int maxLvl){
		this.name=name;
		this.maxLvl=maxLvl;
	}
	public String getName(){
		return this.name;
	}
	public int getMaxLvl(){
		return this.maxLvl;
	}
	public static ModularModifiers byName(String name){
		try{
			return ModularModifiers.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return EFFICIENCY;
		}
	}
}
