package cz.maxtechnik.dif.init.other;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.resources.ResourceLocation;
public class DifModSpriteShifts{
	public static final CTSpriteShiftEntry ZINC_CASING_SHIFT=shifter("zinc_casing","zinc_casing_connected");




	private static CTSpriteShiftEntry shifter(String basic,String connected){
		return CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL,ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/"+basic),ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"block/"+connected));
	}
}