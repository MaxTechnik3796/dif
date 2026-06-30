package cz.maxtechnik.dif.item.modular.v2;

import javax.annotation.Nullable;
import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularParts.*;
public enum ModularTools{
	NONE("none",ModularParts.NONE,ModularParts.NONE,ModularParts.NONE),
	PICKAXE("pickaxe",PICKAXE_HEAD,BINDING,HANDLE),
	AXE("axe",AXE_HEAD,BINDING,HANDLE),
	SWORD("sword",SWORD_HEAD,SWORD_BINDING,HANDLE),
	SHOVEL("shovel",SHOVEL_HEAD,BINDING,HANDLE),
	HOE("hoe",HOE_HEAD,BINDING,HANDLE),
	KATANA("katana",KATANA_HEAD,BINDING,HANDLE),
	BATTLE_AXE("battle_axe",BATTLE_AXE_HEAD,BINDING,HANDLE),
	HAMMER("hammer",HAMMER_HEAD,BINDING,HANDLE),
	TIMBER_AXE("timber_axe",TIMBER_AXE_HEAD,BINDING,HANDLE),
	EXCAVATOR("excavator",EXCAVATOR_HEAD,BINDING,HANDLE);
	private final String name;
	private final ModularParts head;
	private final ModularParts binding;
	private final ModularParts handle;
	ModularTools(String name,ModularParts head,ModularParts binding,ModularParts handle){
		this.name=name;
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
	public static boolean isHead(ModularTools tool,ModularParts head){
		return tool.getHead().equals(head);
	}
	public static boolean isBinding(ModularTools tool,ModularParts part){
		return tool.getBinding().equals(part);
	}
	public static boolean isHandle(ModularTools tool,ModularParts part){
		return tool.getHandle().equals(part);
	}
	public String getName(){
		return this.name;
	}
	public static ModularTools byName(String name){
		try{
			return ModularTools.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return NONE;
		}
	}
}
