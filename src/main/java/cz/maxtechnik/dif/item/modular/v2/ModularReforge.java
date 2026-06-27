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
			new Float[]{1F,1F,0.95F,0.95F,0.95F},
			new Float[]{1F,1F,1.1F,1.15F,1.25F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	HEAVY("heavy",new ModularTools[]{SWORD,BATTLE_AXE},
			new Float[]{1F,1F,1.2F,1.25F,1.35F},
			new Float[]{1F,1F,0.9F,0.88F,0.85F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	RAZOR("razor",new ModularTools[]{SWORD,BATTLE_AXE,KATANA},
			new Float[]{1F,1F,1.15F,1.15F,1.25F},
			new Float[]{1F,1F,1.05F,1.06F,1.1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	VAMPIRIC("vampiric",new ModularTools[]{SWORD,KATANA},
			new Float[]{1F,1F,1.05F,1.05F,1.1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	FROZEN("frozen",new ModularTools[]{SWORD,KATANA},
			new Float[]{1F,1F,1.05F,1.1F,1.15F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	// (slow je special efekt)
	PHANTOM("phantom",new ModularTools[]{SWORD,BATTLE_AXE,KATANA},
			new Float[]{1F,1F,1.05F,1.1F,1.15F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	CURSE("curse",new ModularTools[]{KATANA},
			new Float[]{1F,1F,1.15F,1.2F,1.25F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	// (weakness je special efekt)
	DRAIN("drain",new ModularTools[]{BATTLE_AXE},
			new Float[]{1F,1F,1.1F,1.15F,1.2F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	SAVAGE("savage",new ModularTools[]{AXE,BATTLE_AXE},
			new Float[]{1F,1F,1.15F,1.2F,1.25F},
			new Float[]{1F,1F,1F,1.05F,1.1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F}),
	// TĚŽEBNÍ
	REINFORCED("reinforced",new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,BATTLE_AXE,HAMMER,EXCAVATOR,TIMBER_AXE},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.05F,1.05F,1.1F},
			new Float[]{1F,1F,1.15F,1.2F,1.25F}),
	PROSPECTOR("prospector",new ModularTools[]{PICKAXE,HAMMER},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.2F,1.2F,1.3F},
			new Float[]{1F,1F,1.05F,1.1F,1.2F}),
	STURDY("sturdy",new ModularTools[]{AXE,PICKAXE,SWORD,SHOVEL,HOE,HAMMER,EXCAVATOR,TIMBER_AXE},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.25F,1.3F,1.4F}),
	ARCANE("arcane",new ModularTools[]{PICKAXE,HAMMER},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.25F,1.3F,1.4F},
			new Float[]{1F,1F,0.9F,0.9F,0.85F}),
	CRUSHER("crusher",new ModularTools[]{HAMMER},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.1F,1.15F,1.2F},
			new Float[]{1F,1F,1F,1F,1F}),
	REAPER("reaper",new ModularTools[]{AXE,TIMBER_AXE},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.1F,1.15F,1.25F},
			new Float[]{1F,1F,1F,1F,1F}),
	// (looting je special efekt)
	LUMBERING("lumbering",new ModularTools[]{AXE,TIMBER_AXE},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.2F,1.25F,1.35F},
			new Float[]{1F,1F,1F,1F,1F}),
	SWIFTDIG("swiftdig",new ModularTools[]{SHOVEL,EXCAVATOR},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.15F,1.2F,1.3F},
			new Float[]{1F,1F,0.9F,0.9F,0.85F}),
	GROUNDBREAKER("grounbreaker",new ModularTools[]{SHOVEL,EXCAVATOR},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1F,1F,1F},
			new Float[]{1F,1F,1.2F,1.25F,1.4F},
			new Float[]{1F,1F,1.1F,1.15F,1.2F});
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
