package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularModifier{
	//enchant holder:
	SILK_TOUCH("silk_touch",1,false,0xFFFFFF),
	LUCK("luck",3,false,0xFFFFFF),
	SWEEPING_EDGE("sweeping_edge",3,false,0xFFFFFF),
	MENDING("mending",1,false,0xFFFFFF),
	//custom stats:
	KNOCKBACK("knockback",2,false,0xFFFFFF),
	SHARPNESS("sharpness",5,false,0xFFFFFF),
	EFFICIENCY("efficiency",5,false,0xFFFFFF),
	REINFORCED("reinforced",3,false,0xFFFFFF),
	//material:
	ECOLOGICAL("ecological",-1,false,0x745631),
	MAGNETIC("magnetic",-1,false,0xDCDCDC),
	CHEAP("cheap",-1,false,0x838383),
	SHINY("shiny",-1,false,0xFFD700),
	//special:
	EXCAVATOR("excavator",-2,true,0xFFFFFF);
	private final String name;
	private final int maxLvl;
	private final boolean hasDescription;
	private final int color;
	ModularModifier(String name,int maxLvl,boolean hasDescription,int color){
		this.name=name;
		this.maxLvl=maxLvl;
		this.hasDescription=hasDescription;
		this.color=color;
	}
	public String getName(){
		return name;
	}
	public int getMaxLvl(){
		return maxLvl;
	}
	public boolean hasDescription(){
		return hasDescription;
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
