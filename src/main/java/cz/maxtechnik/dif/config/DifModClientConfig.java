package cz.maxtechnik.dif.config;

import net.neoforged.neoforge.common.ModConfigSpec;
public class DifModClientConfig{
	private static final ModConfigSpec.Builder BUILDER=new ModConfigSpec.Builder();
	public static final ModConfigSpec.ConfigValue<Anchor> JETPACK_OVERLAYER_POS;
	public static final ModConfigSpec SPEC;
	static{
		JETPACK_OVERLAYER_POS=BUILDER.defineEnum("jetpackOverlayerPos",Anchor.TOP_LEFT);
		SPEC=BUILDER.build();
	}
}