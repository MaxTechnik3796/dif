package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularTools.*;
public enum ModularModifier{
	//enchant holder:
	SILK_TOUCH("silk_touch",1,false,0xFFFFFF,new ModularTools[]{AXE,PICKAXE,SHOVEL,HOE,HAMMER,TIMBER_AXE,EXCAVATOR,BATTLE_AXE}),
	LUCK("luck",3,false,0xFFFFFF,new ModularTools[]{SWORD,KATANA}),
	SWEEPING_EDGE("sweeping_edge",3,false,0xFFFFFF,new ModularTools[]{SWORD,KATANA}),
	MENDING("mending",1,false,0xFFFFFF,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	//custom stats:
	KNOCKBACK("knockback",2,false,0xFFFFFF,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	SHARPNESS("sharpness",5,false,0xFFFFFF,new ModularTools[]{AXE,BATTLE_AXE,SWORD,KATANA}),
	EFFICIENCY("efficiency",5,false,0xFFFFFF,new ModularTools[]{AXE,PICKAXE,SHOVEL,HOE,HAMMER,TIMBER_AXE,EXCAVATOR,BATTLE_AXE}),
	REINFORCED("reinforced",3,false,0xFFFFFF,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	//material:
	ECOLOGICAL("ecological",-1,false,0x745631,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	MAGNETIC("magnetic",-1,false,0xDCDCDC,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	CHEAP("cheap",-1,false,0x838383,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	SHINY("shiny",-1,false,0xFFD700,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR});
	private final String name;
	private final int maxLvl;
	private final boolean hasDescription;
	private final int color;
	private final ModularTools[] tools;
	ModularModifier(String name,int maxLvl,boolean hasDescription,int color,ModularTools[] allowedTools){
		this.name=name;
		this.maxLvl=maxLvl;
		this.hasDescription=hasDescription;
		this.color=color;
		this.tools=allowedTools;
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
	public ModularTools[] getTools(){
		return tools;
	}
	public boolean isAllowedOn(ModularTools tool){
		for(ModularTools modularTools: tools){
			if(modularTools==tool) return true;
		}
		return false;
	}
	public static ModularModifier byName(String name){
		try{
			return ModularModifier.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return ECOLOGICAL;
		}
	}
}
