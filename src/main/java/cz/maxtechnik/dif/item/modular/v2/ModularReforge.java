package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularTools.*;
public enum ModularReforge{
	NONE("none",new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,KATANA,BATTLE_AXE,HAMMER,TIMBER_AXE,EXCAVATOR},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	// BOJOVÉ
	SWIFT("swift",new ModularTools[]{SWORD,AXE,KATANA},
			new Float[]{1F,1F,0.95F,0.95F,0.95F},      // attackDamage: -5% od epic
			new Float[]{1F,1F,1.1F,1.15F,1.25F},        // attackSpeed: +10/15/25%
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	HEAVY("heavy",new ModularTools[]{SWORD,BATTLE_AXE},
			new Float[]{1F,1F,1.2F,1.25F,1.35F},        // attackDamage: +20/25/35%
			new Float[]{1F,1F,0.9F,0.88F,0.85F},        // attackSpeed: -10/12/15%
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	RAZOR("razor",new ModularTools[]{SWORD,BATTLE_AXE,KATANA},
			new Float[]{1F,1F,1.15F,1.15F,1.25F},       // attackDamage: +15/15/25%
			new Float[]{1F,1F,1.05F,1.06F,1.1F},        // attackSpeed: +5/6/10%
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	VAMPIRIC("vampiric",new ModularTools[]{SWORD,KATANA},
			new Float[]{1F,1F,1.05F,1.05F,1.1F},        // attackDamage: +5/5/10%
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	// (lifesteal je special efekt, ne v těchto polích)
	FROZEN("frozen",new ModularTools[]{SWORD,KATANA},
			new Float[]{1F,1F,1.05F,1.1F,1.15F},        // attackDamage: +5/10/15%
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	// (slow je special efekt)
	PHANTOM("phantom",new ModularTools[]{SWORD,BATTLE_AXE,KATANA},
			new Float[]{1F,1F,1.05F,1.1F,1.15F},        // attackDamage: +5/10/15%
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	// (strike chance je special efekt)
	CURSE("curse",new ModularTools[]{KATANA},
			new Float[]{1F,1F,1.15F,1.2F,1.25F},        // attackDamage: +15/20/25%
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	// (weakness je special efekt)
	DRAIN("drain",new ModularTools[]{BATTLE_AXE},
			new Float[]{1F,1F,1.1F,1.15F,1.2F},         // attackDamage: +10/15/20%
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	// (lifesteal je special efekt)
	SAVAGE("savage",new ModularTools[]{AXE,BATTLE_AXE},
			new Float[]{1F,1F,1.15F,1.2F,1.25F},        // attackDamage: +15/20/25%
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	// (armor pierce je special efekt)
	// TĚŽEBNÍ
	REINFORCED("reinforced",new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,BATTLE_AXE,HAMMER,EXCAVATOR,TIMBER_AXE},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.05F,1.05F,1.1F},        // efficiency: +5/5/10%
			new Float[]{1F,1F,1.15F,1.2F,1.25F}),       // durability: +15/20/25%
	PROSPECTOR("prospector",new ModularTools[]{PICKAXE,HAMMER},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.2F,1.2F,1.3F},          // efficiency: +20/20/30%
			new Float[]{1F,1F,1.05F,1.1F,1.2F}),        // durability: +5/10/20%
	STURDY("sturdy",new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,HAMMER,EXCAVATOR,TIMBER_AXE},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.25F,1.3F,1.4F}),        // durability: +25/30/40%
	ARCANE("arcane",new ModularTools[]{PICKAXE,HAMMER},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.25F,1.3F,1.4F},         // efficiency: +25/30/40%
			new Float[]{1F,1F,0.9F,0.9F,0.85F}),        // durability: -10/10/15%
	CRUSHER("crusher",new ModularTools[]{HAMMER},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.1F,1.15F,1.2F},         // efficiency: +10/15/20%
			new Float[]{1F,1F,1F,1F,1F}),
	REAPER("reaper",new ModularTools[]{AXE,TIMBER_AXE},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.1F,1.15F,1.25F},        // efficiency: +10/15/25%
			new Float[]{1F,1F,1F,1F,1F}),
	// (looting je special efekt)
	LUMBERING("lumbering",new ModularTools[]{AXE,TIMBER_AXE},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.2F,1.25F,1.35F},        // efficiency: +20/25/35%
			new Float[]{1F,1F,1F,1F,1F}),
	SWIFTDIG("swiftdig",new ModularTools[]{SHOVEL,EXCAVATOR},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.15F,1.2F,1.3F},         // efficiency: +15/20/30%
			new Float[]{1F,1F,0.9F,0.9F,0.85F}),        // durability: -10/10/15%
	GROUNDBREAKER("grounbreaker",new ModularTools[]{SHOVEL,EXCAVATOR},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.2F,1.25F,1.4F},         // efficiency: +20/25/40%
			new Float[]{1F,1F,1.1F,1.15F,1.2F});       // durability: +10/15/20%
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
