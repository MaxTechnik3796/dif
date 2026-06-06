package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;
public enum ModularMaterial{
	WOOD("wood",60,2F,0,0F,10,1,0.2F,0x745631),
	STONE("stone",130,4F,1,1F,20,1,0.0F,0x838383),
	IRON("iron",250,6F,2,2F,50,1,0.1F,0xDCDCDC),
	COPPER("copper",180,5.5F,1,1.5F,30,1,0.1F,0xD4845A),
	GOLD("gold",32,12F,0,0F,5,1,0.6F,0xFFD700),
	STEEL("steel",1,12F,3,2F,1,1,0.1F,0xFF0000);
	private final String id;
	// Head stats
	private final int headDurability;
	private final float headEfficiency;
	private final int miningLevel;
	private final float attackDamage;
	// Binding stats
	private final int bindingDurability;
	// Handle stats
	private final int handleDurability;
	private final float attackSpeedBonus;
	private final int color;
	public static final int[] miningLevelColor={WOOD.getColor(),STONE.getColor(),IRON.getColor(),0x6DEDE4,0x524B52};
	ModularMaterial(String id,int headDurability,float headEfficiency,int miningLevel,float attackDamage,int bindingDurability,int handleDurability,float attackSpeedBonus,int color){
		this.id=id;
		this.headDurability=headDurability;
		this.headEfficiency=headEfficiency;
		this.miningLevel=miningLevel;
		this.attackDamage=attackDamage;
		this.bindingDurability=bindingDurability;
		this.handleDurability=handleDurability;
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
	public int getHandleDurability(){
		return handleDurability;
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