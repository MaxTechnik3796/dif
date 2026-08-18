package cz.maxtechnik.dif.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DifModCommonConfig{
	private static final ModConfigSpec.Builder BUILDER=new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;

	static{
		SPEC=BUILDER.build();
	}
}