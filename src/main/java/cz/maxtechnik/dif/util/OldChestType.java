package cz.maxtechnik.dif.util;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
public enum OldChestType implements StringRepresentable{
	SINGLE("single"),
	LEFT("left"),
	RIGHT("right");
	private final String name;
	OldChestType(String name){
		this.name=name;
	}
	public @NotNull String getSerializedName(){
		return this.name;
	}
}