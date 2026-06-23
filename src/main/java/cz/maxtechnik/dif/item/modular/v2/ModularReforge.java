package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularTools.*;
public enum ModularReforge{
	NONE("none",new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE},new Float[]{0F,0F,0F,0F,0F},new Float[]{0F,0F,0F,0F,0F},new Float[]{0F,0F,0F,0F,0F},new Float[]{1F,1F,1F,1F,1F}),

	PROSPECTOR("prospector",new ModularTools[]{PICKAXE},new Float[]{0F,0F,0F,0F,0F},new Float[]{0F,0F,0F,0F,0F},new Float[]{0F,0F,0F,0F,0F},new Float[]{1F,1F,1F,1F,1F});

	private final String name;
	private final ModularTools[] tools;
	private final Float[] attackDamage;
	private final Float[] attackSpeed;
	private final Float[] efficiency;
	private final Float[] durability;
	ModularReforge(String name,ModularTools[] tool,Float[] attackDamage,Float[] attackSpeed,Float[] efficiency,Float[] durability){
		this.name=name;
		this.tools=tool;
		this.attackDamage=attackDamage;
		this.attackSpeed=attackSpeed;
		this.efficiency=efficiency;
		this.durability=durability;
	}
	public String getName(){
		return name;
	}
	public ModularTools[] getTools(){
		return tools;
	}
	public Float[] getAttackDamage(){
		return attackDamage;
	}
	public Float[] getAttackSpeed(){
		return attackSpeed;
	}
	public Float[] getEfficiency(){
		return efficiency;
	}
	public Float[] getDurability(){
		return durability;
	}
	public static ModularReforge byName(String name){
		try{
			return ModularReforge.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return NONE;
		}
	}
}
