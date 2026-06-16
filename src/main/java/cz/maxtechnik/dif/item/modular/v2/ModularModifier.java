package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularMaterial.*;
public enum ModularModifier{
	//regular(max lvl):
	EFFICIENCY("efficiency",5,ModularMaterial.NONE),
	FORTUNE("fortune",3,ModularMaterial.NONE),
	SILK_TOUCH("silk_touch",1,ModularMaterial.NONE),
	SHARPNESS("sharpness",5,ModularMaterial.NONE),


	//material(-1):
	ECOLOGICAL("ecological",-1,WOOD),
	MAGNETIC("magnetic",-1,IRON),
	CHEAP("cheap",-1,STONE),
	SHINY("shiny",-1,GOLD),

	//special(-2):
	EXCAVATOR("excavator",-2,ModularMaterial.NONE),

	NONE("none",-3,ModularMaterial.NONE);
	private final String name;
	private final int maxLvl;
	private final ModularMaterial material;
	ModularModifier(String name,int maxLvl,ModularMaterial material){
		this.name=name;
		this.maxLvl=maxLvl;
		this.material=material;
	}
	public String getName(){
		return name;
	}
	public int getMaxLvl(){
		return maxLvl;
	}
	public ModularMaterial getMaterial(){
		return material;
	}
	public static ModularModifier byName(String name){
		try{
			return ModularModifier.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return EFFICIENCY;
		}
	}
}
