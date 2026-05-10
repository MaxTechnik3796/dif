package cz.maxtechnik.dif.util;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
public enum HeatLevel implements StringRepresentable{
	NONE(0),         // žádný ohřev
	HEATED(40),      // kindled blaze burner -> 40 ticků/operace (2s)
	SUPERHEATED(20); // seething blaze burner -> 20 ticků/operace (1s)
	public final int ticksPerOp;
	HeatLevel(int ticksPerOp){
		this.ticksPerOp=ticksPerOp;
	}
	public boolean isActive(){
		return this!=NONE;
	}
	@Override
	public @NotNull String getSerializedName(){
		return name().toLowerCase();
	}
	/**
	 * Zjisti heat level z bloku pod controllerem.
	 * Funguje s Create blaze burnerem (property "blaze" nebo "heat_level"),
	 * ohněm, lávou a magma blokem jako fallback.
	 */
	public static HeatLevel below(Level level,BlockPos pos){
		BlockState state=level.getBlockState(pos.below());
		// Create kompatibilita - hledej property "blaze" nebo "heat_level"
		for(Property<?> prop: state.getProperties()){
			String name=prop.getName();
			if(!name.equals("blaze")&&!name.equals("heat_level")) continue;
			String value=state.getValue(prop).toString().toLowerCase();
			if(value.equals("seething")) return SUPERHEATED;
			if(value.equals("kindled")||value.equals("faded")) return HEATED;
		}
		return NONE;
	}
}