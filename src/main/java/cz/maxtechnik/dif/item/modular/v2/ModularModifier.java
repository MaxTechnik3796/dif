package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;

public enum ModularModifier{
	//material:
	ECOLOGICAL("ecological",false,.1F,1F,1F,1F,0x745631),
	MAGNETIC("magnetic",false,1F,1F,1F,1F,0xDCDCDC),
	CHEAP("cheap",false,1F,1F,1F,1F,0x838383),
	SHINY("shiny",false,1F,1F,1F,1F,0xFFD700),

	//special:
	EXCAVATOR("excavator",true,1F,1F,1F,1F,0xFFFFFF);




	private final String name;
	private final boolean hasDescription;
	private final float attackDamage;
	private final float attackSpeed;
	private final float efficiency;
	private final float durability;
	private final int color;
	ModularModifier(String name,boolean hasDescription,Float attackDamage,Float attackSpeed,Float efficiency,float durability, int color){
		this.name=name;
		this.hasDescription=hasDescription;
		this.attackSpeed=attackSpeed;
		this.attackDamage=attackDamage;
		this.efficiency=efficiency;
		this.durability=durability;
		this.color=color;
	}
	public String getName(){
		return name;
	}
	public boolean hasDescription(){
		return hasDescription;
	}
	public float getAttackDamage(){
		return attackDamage;
	}
	public float getAttackSpeed(){
		return attackSpeed;
	}
	public float getEfficiency(){
		return efficiency;
	}
	public float getDurability(){
		return durability;
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
