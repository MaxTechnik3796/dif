package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularModifier.*;
import static cz.maxtechnik.dif.item.modular.v2.ModularTier.*;
public enum ModularMaterial{
	NONE("none","",0,0F,0,0F,0,0,0F,COMMON,ModularModifier.NONE,0xFFFFFF,0),
	WOOD("wood","",60,2F,0,0F,10,1,0.2F,COMMON,RENEWABLE,0x745631,0),
	STONE("stone","",130,4F,1,1F,20,1,0.0F,COMMON,STONEBOUND,0x838383,0),
	IRON("iron","c:molten_iron",250,6F,2,2F,50,1,0.1F,RARE,MAGNETIC,0xDCDCDC,1),
	COPPER("copper","c:molten_copper",180,5.5F,1,1.5F,30,1,0.1F,RARE,VERDANT,0xD4845A,1),
	GOLD("gold","c:molten_gold",32,12F,0,0F,5,1,0.6F,RARE,LUCKY_MAT,0xFFD700,1),
	STEEL("steel","c:molten_steel",1,12F,3,2F,1,1,0.1F,EPIC,MOMENTUM,0x434343,1),
	OBSIDIAN("obsidian","c:molten_obsidian",1,12F,3,2F,1,1,0.1F,EPIC,UNBREAKABLE_MAT,0x2A1F3D,0),
	ZINC("zinc","c:molten_zinc",1,12F,3,2F,1,1,0.1F,EPIC,SELF_REPAIR,0xB5D1BA,1),
	BRASS("brass","c:molten_brass",1,12F,3,2F,1,1,0.1F,EPIC,PRECISE,0xE4B763,1),
	NICKEL("nickel","c:molten_nickel",1,12F,3,2F,1,1,0.1F,EPIC,TOXIC,0xBFC6CB,1),
	MITHRIL("mithril","c:molten_mithril",1,12F,3,2F,1,1,0.1F,EPIC,LIGHTWEIGHT,0x80CDB4,1);
	private final String name;
	private final String liquid;
	private final int headDurability;
	private final float headEfficiency;
	private final int miningLevel;
	private final float attackDamage;
	private final int bindingDurability;
	private final int handleDurability;
	private final float attackSpeedBonus;
	private final ModularTier tier;
	private final ModularModifier modifier;
	private final int color;
	private final int minHeatTier;
	public static final int[] miningLevelColor={WOOD.getColor(),STONE.getColor(),IRON.getColor(),0x6DEDE4,0x524B52};
	ModularMaterial(String name,String liquid,int headDurability,float headEfficiency,int miningLevel,float attackDamage,int bindingDurability,int handleDurability,float attackSpeedBonus,ModularTier tier,ModularModifier modifier,int color,int minHeatTier){
		this.name=name;
		this.liquid=liquid;
		this.headDurability=headDurability;
		this.headEfficiency=headEfficiency;
		this.miningLevel=miningLevel;
		this.attackDamage=attackDamage;
		this.bindingDurability=bindingDurability;
		this.handleDurability=handleDurability;
		this.attackSpeedBonus=attackSpeedBonus;
		this.tier=tier;
		this.modifier=modifier;
		this.color=color;
		this.minHeatTier=minHeatTier;
	}
	public String getName(){
		return name;
	}
	public String getLiquid(){
		return liquid;
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
	public ModularTier getTier(){
		return tier;
	}
	public ModularModifier getModifier(){
		return modifier;
	}
	public int getColor(){
		return color;
	}
	public int getMinHeatTier(){
		return minHeatTier;
	}
	public static ModularMaterial byName(String name){
		try{
			return ModularMaterial.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return NONE;
		}
	}
}