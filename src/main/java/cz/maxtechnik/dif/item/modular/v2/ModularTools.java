package cz.maxtechnik.dif.item.modular.v2;

import javax.annotation.Nullable;
import java.util.Locale;

import static cz.maxtechnik.dif.item.modular.v2.ModularParts.*;
public enum ModularTools{
	NONE("none",0F,ModularParts.NONE,ModularParts.NONE,ModularParts.NONE),
	PICKAXE("pickaxe",1F,PICKAXE_HEAD,BINDING,HANDLE),
	AXE("axe",2F,AXE_HEAD,BINDING,HANDLE),
	SWORD("sword",3F,SWORD_HEAD,SWORD_BINDING,HANDLE),
	SHOVEL("shovel",4F,SHOVEL_HEAD,BINDING,HANDLE),
	HOE("hoe",5F,HOE_HEAD,BINDING,HANDLE),
	KATANA("katana",6F,KATANA_HEAD,BINDING,HANDLE),
	BATTLE_AXE("battle_axe",7F,BATTLE_AXE_HEAD,BINDING,HANDLE),
	HAMMER("hammer",8F,HAMMER_HEAD,BINDING,HANDLE),
	TIMBER_AXE("timber_axe",9F,TIMBER_AXE_HEAD,BINDING,HANDLE),
	EXCAVATOR("excavator",10F,EXCAVATOR_HEAD,BINDING,HANDLE);
	private final String name;
	private final float renderIndex;
	private final ModularParts head;
	private final ModularParts binding;
	private final ModularParts handle;
	ModularTools(String name,float renderIndex,ModularParts head,ModularParts binding,ModularParts handle){
		this.name=name;
		this.renderIndex=renderIndex;
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
	public static boolean isHead(ModularTools tool,ModularParts part){
		return tool.getHead().equals(part);
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
	public float getRenderIndex(){
		return this.renderIndex;
	}
	public static ModularTools byName(String name){
		try{
			return ModularTools.valueOf(name.toUpperCase(Locale.ROOT));
		}catch(IllegalArgumentException exception){
			return NONE;
		}
	}
}
