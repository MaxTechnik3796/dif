package cz.maxtechnik.dif.item.modular.v2;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Locale;

import static cz.maxtechnik.dif.init.basic.DifModItems.*;
public enum ModularParts{
	NONE("none",0,CASTING_MOLD),
	HANDLE("handle",1,CASTING_MOLD_HANDLE),
	BINDING("binding",2,CASTING_MOLD_BINDING),
	AXE_HEAD("axe_head",3,CASTING_MOLD_AXE_HEAD),
	PICKAXE_HEAD("pickaxe_head",4,CASTING_MOLD_PICKAXE_HEAD),
	SWORD_HEAD("sword_head",5,CASTING_MOLD_SWORD_HEAD),
	SHOVEL_HEAD("shovel_head",6,CASTING_MOLD_SHOVEL_HEAD),
	SWORD_BINDING("sword_binding",7,CASTING_MOLD_SWORD_BINDING),
	HOE_HEAD("hoe_head",8,CASTING_MOLD_HOE_HEAD),
	BATTLE_AXE_HEAD("battle_axe_head",9,CASTING_MOLD_BATTLE_AXE_HEAD),
	KATANA_HEAD("katana_head",10,CASTING_MOLD_KATANA_HEAD),
	TIMBER_AXE_HEAD("timber_axe_head",11,CASTING_MOLD_TIMBER_AXE_HEAD),
	HAMMER_HEAD("hammer_head",12,CASTING_MOLD_HAMMER_HEAD),
	EXCAVATOR_HEAD("excavator_head",13,CASTING_MOLD_EXCAVATOR_HEAD);
	private final String name;
	private final int partID;
	private final DeferredItem<Item> castingMold;
	ModularParts(String name,int partID,DeferredItem<Item> castingMold){
		this.name=name;
		this.partID=partID;
		this.castingMold=castingMold;
	}
	public String getName(){
		return this.name;
	}
	public int getID(){
		return this.partID;
	}
	public DeferredItem<Item> getCastingMold(){
		return castingMold;
	}
	public static ModularParts byName(String name){
		try{
			return ModularParts.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return HANDLE;
		}
	}
}
