package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularTier{
	COMMON("common",1F,1F,1F,0xFFFFFF),
	RARE("rare",1.2F,1.2F,1.2F,0x5555FF),
	EPIC("epic",1.3F,1.3F,1.3F,0xAA00AA),
	LEGENDARY("legendary",1.4F,1.4F,1.4F,0xFFAA00),
	MYTHIC("mythic",1.5F,1.5F,1.5F,0xFF55FF),;
	final String name;
	final float efficiency;
	final float durability;
	final float attackDamage;
	final int color;
	ModularTier(String name,float efficiency,float durability,float attackDamage,int color){
		this.name=name;
		this.efficiency=efficiency;
		this.durability=durability;
		this.attackDamage=attackDamage;
		this.color=color;
	}
	public float getEfficiencyModifier(){
		return efficiency;
	}
	public float getDurabilityModifier(){
		return durability;
	}
	public float getAttackDamageModifier(){
		return attackDamage;
	}
	public int getColor(){
		return color;
	}
	public static ModularTier byName(String name){
		try{
			return ModularTier.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return COMMON;
		}
	}
}
