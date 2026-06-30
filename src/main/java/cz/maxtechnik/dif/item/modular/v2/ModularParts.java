package cz.maxtechnik.dif.item.modular.v2;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.Locale;

import static cz.maxtechnik.dif.init.basic.DifModItems.*;
public enum ModularParts{
	NONE("none",0F,CASTING_MOLD),
	HANDLE("handle",1F,CASTING_MOLD_HANDLE),
	BINDING("binding",2F,CASTING_MOLD_BINDING),
	AXE_HEAD("axe_head",3F,CASTING_MOLD_AXE_HEAD),
	PICKAXE_HEAD("pickaxe_head",4F,CASTING_MOLD_PICKAXE_HEAD),
	SWORD_HEAD("sword_head",5F,CASTING_MOLD_SWORD_HEAD),
	SHOVEL_HEAD("shovel_head",6F,CASTING_MOLD_SHOVEL_HEAD),
	SWORD_BINDING("sword_binding",7F,CASTING_MOLD_SWORD_BINDING),
	HOE_HEAD("hoe_head",8F,CASTING_MOLD_HOE_HEAD),
	BATTLE_AXE_HEAD("battle_axe_head",9F,CASTING_MOLD_BATTLE_AXE_HEAD),
	KATANA_HEAD("katana_head",10F,CASTING_MOLD_KATANA_HEAD),
	TIMBER_AXE_HEAD("timber_axe_head",11F,CASTING_MOLD_TIMBER_AXE_HEAD),
	HAMMER_HEAD("hammer_head",12F,CASTING_MOLD_HAMMER_HEAD),
	EXCAVATOR_HEAD("excavator_head",13F,CASTING_MOLD_EXCAVATOR_HEAD);
	private final String name;
	private final float renderIndex;
	private final DeferredItem<Item> castingMold;
	ModularParts(String name,float renderIndex,DeferredItem<Item> castingMold){
		this.name=name;
		this.renderIndex=renderIndex;
		this.castingMold=castingMold;
	}
	public String getName(){
		return this.name;
	}
	public float getRenderIndex(){
		return this.renderIndex;
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
