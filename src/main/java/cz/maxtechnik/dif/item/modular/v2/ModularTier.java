package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularTier{
	COMMON("common",1F,1F,1F),
	RARE("rare",1.2F,1.2F,1.2F),
	EPIC("epic",1.3F,1.3F,1.3F),
	LEGENDARY("legendary",1.4F,1.4F,1.4F),
	MYTHIC("mythic",1.5F,1.5F,1.5F),;
	final String name;
	final float efficiency;
	final float durability;
	final float attackDamage;
	ModularTier(String name,float efficiency,float durability,float attackDamage){
		this.name=name;
		this.efficiency=efficiency;
		this.durability=durability;
		this.attackDamage=attackDamage;
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
	public static ModularTier byName(String name){
		try{
			return ModularTier.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return COMMON;
		}
	}
}
