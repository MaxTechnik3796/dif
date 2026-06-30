package cz.maxtechnik.dif.item.modular.v2;

import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularParts.*;
public enum ModularPartType{
	NONE("none",new ModularParts[]{}),HANDLE("handle",new ModularParts[]{ModularParts.HANDLE}),BINDING("binding",new ModularParts[]{ModularParts.BINDING,SWORD_BINDING}),HEAD("head",new ModularParts[]{AXE_HEAD,PICKAXE_HEAD,SWORD_HEAD,SHOVEL_HEAD,HOE_HEAD,BATTLE_AXE_HEAD,KATANA_HEAD,TIMBER_AXE_HEAD,HAMMER_HEAD,EXCAVATOR_HEAD});
	private final String name;
	private final ModularParts[] parts;
	ModularPartType(String name,ModularParts[] parts){
		this.name=name;
		this.parts=parts;
	}
	public String getName(){
		return this.name;
	}
	public ModularParts[] getParts(){
		return this.parts;
	}
	public static boolean isHandle(ModularParts part){
		for(ModularParts localPart: HANDLE.getParts()){
			if(localPart.equals(part)) return true;
		}
		return false;
	}
	public static boolean isBinding(ModularParts part){
		for(ModularParts localPart: BINDING.getParts()){
			if(localPart.equals(part)) return true;
		}
		return false;
	}
	public static boolean isHead(ModularParts part){
		for(ModularParts localPart: HEAD.getParts()){
			if(localPart.equals(part)) return true;
		}
		return false;
	}
	public static ModularPartType byName(String name){
		try{
			return ModularPartType.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return NONE;
		}
	}
}
