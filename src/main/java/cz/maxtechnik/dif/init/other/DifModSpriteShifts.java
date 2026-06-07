package cz.maxtechnik.dif.init.other;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.resources.ResourceLocation;
public class DifModSpriteShifts{
	public static final CTSpriteShiftEntry ZINC_CASING=shifter("zinc_casing","zinc_casing_connected");
	public static final CTSpriteShiftEntry STEEL_CASING=shifter("steel_casing","steel_casing_connected");
	public static final CTSpriteShiftEntry AURORA_CASING=shifter("aurora_casing","aurora_casing_connected");
	public static final CTSpriteShiftEntry FORGE_GLASS=shifter("forge_glass","forge_glass_connected");

	public static final CTSpriteShiftEntry DISTILLATION_TANK=shifterRect("tank/distillation_tank","tank/distillation_tank_connected");
	public static final CTSpriteShiftEntry DISTILLATION_TANK_TOP=shifterRect("tank/distillation_tank_top","tank/distillation_tank_top_connected");
	public static final CTSpriteShiftEntry DISTILLATION_TANK_INNER=shifterRect("tank/distillation_tank_inner","tank/distillation_tank_inner_connected");

	private static CTSpriteShiftEntry shifter(String basic,String connected){
		return CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/"+basic),ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/"+connected));
	}

	private static CTSpriteShiftEntry shifterRect(String basic,String connected){
		return CTSpriteShifter.getCT(AllCTTypes.RECTANGLE,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/"+basic),ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/"+connected));
	}
}