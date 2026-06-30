package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularTools.*;
public enum ModularModifier{
	NONE("none",0,0xFFFFFF,new ModularTools[]{}),
	//enchant holder:
	SILK_TOUCH("silk_touch",1,0xFFFFFF,new ModularTools[]{AXE,PICKAXE,SHOVEL,HOE,HAMMER,TIMBER_AXE,EXCAVATOR,BATTLE_AXE}),
	LUCK("luck",3,0xFFFFFF,new ModularTools[]{SWORD,KATANA,AXE,PICKAXE,SHOVEL,HOE,HAMMER,TIMBER_AXE,EXCAVATOR,BATTLE_AXE}),
	SWEEPING_EDGE("sweeping_edge",3,0xFFFFFF,new ModularTools[]{SWORD,KATANA}),
	MENDING("mending",1,0xFFFFFF,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	VOLCANIC("volcanic",1,0xFF4500,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),//custom stats:
	KNOCKBACK("knockback",2,0xFFFFFF,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	SHARPNESS("sharpness",5,0xFFFFFF,new ModularTools[]{AXE,BATTLE_AXE,SWORD,KATANA}),
	EFFICIENCY("efficiency",5,0xFFFFFF,new ModularTools[]{AXE,PICKAXE,SHOVEL,HOE,HAMMER,TIMBER_AXE,EXCAVATOR,BATTLE_AXE}),
	REINFORCED("reinforced",3,0xFFFFFF,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),//material:
	//material:
	RENEWABLE("renewable",-1,0x745631,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	STONEBOUND("stonebound",-1,0x838383,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	MAGNETIC("magnetic",-1,0xDCDCDC,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	VERDANT("verdant",-1,0xD4845A,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	LUCKY_MAT("lucky_mat",-1,0xFFD700,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	MOMENTUM("momentum",-1,0x434343,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	UNBREAKABLE_MAT("unbreakable_mat",-1,0x2A1F3D,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	SELF_REPAIR("self_repair",-1,0xB5D1BA,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	TOXIC("toxic",-1,0xBFC6CB,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	PRECISE("precise",-1,0xE4B763,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR}),
	LIGHTWEIGHT("lightweight",-1,0x80CDB4,new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR});
	private final String name;
	private final int maxLvl;
	private final int color;
	private final ModularTools[] tools;
	ModularModifier(String name,int maxLvl,int color,ModularTools[] tools){
		this.name=name;
		this.maxLvl=maxLvl;
		this.color=color;
		this.tools=tools;
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
			return NONE;
		}
	}
}
