package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularMaterial{
	WOOD("wood",60,2.0f,0,0.0f,10,0.5f,0.2f,0x745631),
	STONE("stone",130,4.0f,1,1.0f,20,0.8f,0.0f,0x838383),
	IRON("iron",250,6.0f,2,2.0f,50,1.0f,-0.1f,0xDCDCDC),
	COPPER("copper",180,5.5f,1,1.5f,30,0.9f,0.1f,0xD86D5F),
	GOLD("gold",32,12.0f,0,0.0f,5,0.3f,0.6f,0xF6D142),
	DIAMOND("diamond",1561,8.0f,3,3.0f,150,1.2f,0.1f,0x6DEDE4),
	NETHERITE("netherite",2031,9.0f,4,4.0f,250,1.3f,0.2f,0x433F41);
	private final String id;
	// Head stats
	private final int headDurability;
	private final float headEfficiency;
	private final int miningLevel;
	private final float attackDamage;
	// Binding stats
	private final int bindingDurability;
	// Handle stats
	private final float handleDurabilityMultiplier;
	private final float attackSpeedBonus;
	private final int color;
	ModularMaterial(String id,int headDurability,float headEfficiency,int miningLevel,float attackDamage,int bindingDurability,float handleDurabilityMultiplier,float attackSpeedBonus,int color){
		this.id=id;
		this.headDurability=headDurability;
		this.headEfficiency=headEfficiency;
		this.miningLevel=miningLevel;
		this.attackDamage=attackDamage;
		this.bindingDurability=bindingDurability;
		this.handleDurabilityMultiplier=handleDurabilityMultiplier;
		this.attackSpeedBonus=attackSpeedBonus;
		this.color=color;
	}
	public String getId(){
		return id;
	}
	public int getHeadDurability(){
		return headDurability;
	}
	public float getHeadEfficiency(){
		return headEfficiency;
	}
	public int getMiningLevel(){
		return miningLevel;
	}
	public float getAttackDamage(){
		return attackDamage;
	}
	public int getBindingDurability(){
		return bindingDurability;
	}
	public float getHandleDurabilityMultiplier(){
		return handleDurabilityMultiplier;
	}
	public float getAttackSpeedBonus(){
		return attackSpeedBonus;
	}
	public int getColor(){
		return color;
	}
	public static ModularMaterial byName(String name){
		try{
			return ModularMaterial.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException e){
			return WOOD; // Výchozí záchranný materiál při chybě/typu
		}
	}
}