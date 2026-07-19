package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularTools.*;
public enum ModularReforge{
	NONE("none",new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR},
			1F,
			1F,
			1F,
			1F),
	// BOJOVÉ
	SWIFT("swift",new ModularTools[]{SWORD,AXE,KATANA},
			0.95F, 1.15F, 1F, 1F),
	HEAVY("heavy",new ModularTools[]{SWORD,BATTLE_AXE,KATANA},
			1.25F, 0.9F, 1F, 1F),
	RAZOR("razor",new ModularTools[]{SWORD,BATTLE_AXE,KATANA},
			1.1F, 1.05F, 1F, 1F),
	VAMPIRIC("vampiric",new ModularTools[]{SWORD,KATANA},
			1F, 1F, 1F, 1F),
	FROZEN("frozen",new ModularTools[]{SWORD,KATANA},
			1.05F, 1F, 1F, 1F),// (slow je special efekt)
	PHANTOM("phantom",new ModularTools[]{SWORD,BATTLE_AXE,KATANA},
			1.F, 1F, 1F, 1F),
	CURSE("curse",new ModularTools[]{KATANA},
			1.1F, 1F, 1F, 1F),// (weakness je special efekt)
	DRAIN("drain",new ModularTools[]{BATTLE_AXE},
			1F, 1F, 1F, 1F),
	SAVAGE("savage",new ModularTools[]{AXE,BATTLE_AXE},
			1.1F, 1F, 1F, 1F),

	REINFORCED("reinforced",new ModularTools[]{AXE,PICKAXE,SHOVEL,HOE,BATTLE_AXE,HAMMER,EXCAVATOR,TIMBER_AXE},
			1F, 1F, 1F, 1.1F),
	PROSPECTOR("prospector",new ModularTools[]{PICKAXE,HAMMER},
			1F, 1F, 1.05F, 1.05F),
	STURDY("sturdy",new ModularTools[]{AXE,PICKAXE,SHOVEL,HOE,HAMMER,EXCAVATOR,TIMBER_AXE},
			1F, 1F, 0.9F, 1.2F),
	ARCANE("arcane",new ModularTools[]{PICKAXE,HAMMER,SHOVEL,EXCAVATOR},
			1F, 1F, 1.2F, 0.9F),
	GLEAMING("gleaming",new ModularTools[]{PICKAXE,HAMMER,SHOVEL,EXCAVATOR},
			1F, 1F, 1F, 1F),//Gleaming tady je epic a legendary +1 fortune a mythic +2 fortune
	CRUSHER("crusher",new ModularTools[]{HAMMER},
			1F, 1F, 1.1F, 1F),
	REAPER("reaper",new ModularTools[]{AXE,TIMBER_AXE},
			1F, 1F, 1.1F, 1F),//looting nebude potřeba
	LUMBERING("lumbering",new ModularTools[]{AXE,TIMBER_AXE},
			1F, 1F, 1.05F, 1.05F),
	HARVESTER("harvester",new ModularTools[]{HOE},
			1F, 1F, 1F, 1F),//Harvester tady je epic +1 fortune, legendary +2 fortune a mythic +3 fortune
	CULTIVATOR("cultivator",new ModularTools[]{HOE},
			1F, 1F, 1F, 1F);

	private final String name;
	private final ModularTools[] tools;
	private final float attackDamage;
	private final float attackSpeed;
	private final float efficiency;
	private final float durability;
	ModularReforge(String name,ModularTools[] tool,float attackDamage,float attackSpeed,float efficiency,float durability){
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
	public static ModularReforge byName(String name){
		try{
			return ModularReforge.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return NONE;
		}
	}
}
