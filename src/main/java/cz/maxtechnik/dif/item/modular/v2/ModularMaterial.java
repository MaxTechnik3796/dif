package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularModifier.*;
import static cz.maxtechnik.dif.item.modular.v2.ModularTier.*;
public enum ModularMaterial{
	NONE("none","",0,0F,0,0F,0,0,COMMON,ModularModifier.NONE,0xFFFFFF,0),
	WOOD("wood","",40,2F,0,0F,10,20,COMMON,RENEWABLE,0x745631,0),
	STONE("stone","",96,4F,1,1F,24,48,COMMON,STONEBOUND,0x838383,0),
	IRON("iron","c:molten_iron",168,6F,2,2F,42,84,RARE,MAGNETIC,0xDCDCDC,1),
	COPPER("copper","c:molten_copper",96,5F,1,1.5F,32,64,RARE,VERDANT,0xD4845A,1),
	GOLD("gold","c:molten_gold",32,12F,1,1F,8,16,RARE,LUCKY_MAT,0xFFD700,1),
	STEEL("steel","c:molten_steel",1280,8F,3,3F,320,640,EPIC,MOMENTUM,0x434343,1),
	OBSIDIAN("obsidian","c:molten_obsidian",1536,4F,3,1.5F,384,768,RARE,UNBREAKABLE_MAT,0x2A1F3D,0),
	ZINC("zinc","c:molten_zinc",240,6F,2,2F,60,120,RARE,SELF_REPAIR,0xB5D1BA,1),
	BRASS("brass","c:molten_brass",512,7F,3,3F,128,256,RARE,PRECISE,0xE4B763,1),
	NICKEL("nickel","c:molten_nickel",140,6F,2,2F,35,70,RARE,TOXIC,0xBFC6CB,1),
	MITHRIL("mithril","c:molten_mithril",1040,9F,4,4F,260,640,EPIC,LIGHTWEIGHT,0x80CDB4,1);
	private final String name;
	private final String liquid;
	private final int headDurability;
	private final float headEfficiency;
	private final int miningLevel;
	private final float attackDamage;
	private final int bindingDurability;
	private final int handleDurability;
	private final ModularTier tier;
	private final ModularModifier modifier;
	private final int color;
	private final int minHeatTier;
	public static final int[] miningLevelColor={WOOD.getColor(),STONE.getColor(),IRON.getColor(),0x6DEDE4,0x524B52};
	ModularMaterial(String name,String liquid,int headDurability,float headEfficiency,int miningLevel,float attackDamage,int bindingDurability,int handleDurability,ModularTier tier,ModularModifier modifier,int color,int minHeatTier){
		this.name=name;
		this.liquid=liquid;
		this.headDurability=headDurability;
		this.headEfficiency=headEfficiency;
		this.miningLevel=miningLevel;
		this.attackDamage=attackDamage;
		this.bindingDurability=bindingDurability;
		this.handleDurability=handleDurability;
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