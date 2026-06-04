package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularMaterial{
	WOOD("wood",60,2F,0,0F,10,0.5F,0.2F,0x745631),
	STONE("stone",130,4F,1,1F,20,0.8F,0.0F,0x838383),
	IRON("iron",250,6F,2,2F,50,1.0F,-0.1F,0xDCDCDC),
	COPPER("copper",180,5.5F,1,1.5F,30,0.9F,0.1F,0xD4845A),
	GOLD("gold",32,12F,0,0F,5,0.3F,0.6F,0xFFD700),
	DIAMOND("diamond",1561,8F,3,3F,150,1.2F,0.1F,0x6DEDE4),
	NETHERITE("netherite",2031,9F,4,4F,250,1.3F,0.2F,0x524B52);
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
		}catch(IllegalArgumentException exception){
			return WOOD;
		}
	}
}