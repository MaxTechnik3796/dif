package cz.maxtechnik.dif.item.modular.v2;

import javax.annotation.Nullable;
import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularParts.*;
public enum ModularTools{
	NONE("none",0,ModularParts.NONE,ModularParts.NONE,ModularParts.NONE),
	PICKAXE("pickaxe",1,PICKAXE_HEAD,BINDING,HANDLE),
	AXE("axe",2,AXE_HEAD,BINDING,HANDLE),
	SWORD("sword",3,SWORD_HEAD,SWORD_BINDING,HANDLE),
	SHOVEL("shovel",4,SHOVEL_HEAD,BINDING,HANDLE),
	HOE("hoe",5,HOE_HEAD,BINDING,HANDLE),
	KATANA("katana",6,KATANA_HEAD,BINDING,HANDLE),
	BATTLE_AXE("battle_axe",7,BATTLE_AXE_HEAD,BINDING,HANDLE),
	HAMMER("hammer",8,HAMMER_HEAD,BINDING,HANDLE),
	TIMBER_AXE("timber_axe",9,TIMBER_AXE_HEAD,BINDING,HANDLE),
	EXCAVATOR("excavator",10,EXCAVATOR_HEAD,BINDING,HANDLE);
	private final String name;
	private final int toolID;
	private final ModularParts head;
	private final ModularParts binding;
	private final ModularParts handle;
	ModularTools(String name,int toolID,ModularParts head,ModularParts binding,ModularParts handle){
		this.name=name;
		this.toolID=toolID;
		this.head=head;
		this.binding=binding;
		this.handle=handle;
	}
	public ModularParts getHead(){
		return this.head;
	}
	public ModularParts getBinding(){
		return this.binding;
	}
	public ModularParts getHandle(){
		return this.handle;
	}
	public static @Nullable ModularTools getToolFromParts(ModularParts head,ModularParts binding,ModularParts handle){
		for(ModularTools tool: ModularTools.values()){
			if(tool.getHead().equals(head)&&tool.getBinding().equals(binding)&&tool.getHandle().equals(handle)) return tool;
		}
		return null;
	}
	public String getName(){
		return this.name;
	}
	public int getID(){
		return this.toolID;
	}
	public static ModularTools byName(String name){
		try{
			return ModularTools.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return NONE;
		}
	}
}
