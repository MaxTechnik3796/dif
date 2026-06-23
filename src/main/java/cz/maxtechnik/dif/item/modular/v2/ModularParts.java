package cz.maxtechnik.dif.item.modular.v2;

import net.minecraft.world.item.Item;

import java.util.Locale;

import static cz.maxtechnik.dif.init.basic.DifModItems.*;
public enum ModularParts{
	HANDLE("handle",CASTING_MOLD_HANDLE.get()),
	BINDING("binding",CASTING_MOLD_BINDING.get()),
	AXE_HEAD("axe_head",CASTING_MOLD_AXE_HEAD.get()),
	PICKAXE_HEAD("pickaxe_head",CASTING_MOLD_PICKAXE_HEAD.get()),
	SWORD_HEAD("sword_head",CASTING_MOLD_SWORD_HEAD.get()),
	SHOVEL_HEAD("shovel_head",CASTING_MOLD_SHOVEL_HEAD.get()),
	SWORD_BINDING("sword_binding",CASTING_MOLD_SWORD_BINDING.get()),
	HOE_HEAD("hoe_head",CASTING_MOLD_HOE_HEAD.get()),
	;
	private final String name;
	private final Item castingMold;
	ModularParts(String name,Item castingMold){
		this.name=name;
		this.castingMold=castingMold;
	}
	public String getName(){
		return this.name;
	}
	public Item getCastingMold(){
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
