package cz.maxtechnik.dif.item.modular.v2;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Locale;

import static cz.maxtechnik.dif.init.basic.DifModItems.*;
public enum ModularParts{
	NONE("none",CASTING_MOLD),
	HANDLE("handle",CASTING_MOLD_HANDLE),
	BINDING("binding",CASTING_MOLD_BINDING),
	AXE_HEAD("axe_head",CASTING_MOLD_AXE_HEAD),
	PICKAXE_HEAD("pickaxe_head",CASTING_MOLD_PICKAXE_HEAD),
	SWORD_HEAD("sword_head",CASTING_MOLD_SWORD_HEAD),
	SHOVEL_HEAD("shovel_head",CASTING_MOLD_SHOVEL_HEAD),
	SWORD_BINDING("sword_binding",CASTING_MOLD_SWORD_BINDING),
	HOE_HEAD("hoe_head",CASTING_MOLD_HOE_HEAD),
	BATTLE_AXE_HEAD("battle_axe_head",CASTING_MOLD_BATTLE_AXE_HEAD),
	KATANA_HEAD("katana_head",CASTING_MOLD_KATANA_HEAD),
	TIMBER_AXE_HEAD("timber_axe_head",CASTING_MOLD_TIMBER_AXE_HEAD),
	HAMMER_HEAD("hammer_head",CASTING_MOLD_HAMMER_HEAD),
	EXCAVATOR_HEAD("excavator_head",CASTING_MOLD_EXCAVATOR_HEAD);
	private final String name;
	private final DeferredItem<Item> castingMold;
	ModularParts(String name,DeferredItem<Item> castingMold){
		this.name=name;
		this.castingMold=castingMold;
	}
	public String getName(){
		return this.name;
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
