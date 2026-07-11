package cz.maxtechnik.dif;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DifModClientConfig {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;

	public static final ModConfigSpec.BooleanValue COMPACT_TOOLTIPS;

	static {
		BUILDER.comment("Dif client config.");
		BUILDER.push("Tooltips");
		COMPACT_TOOLTIPS = BUILDER.comment("Enable compact modular tooltips (require holding SHIFT to show full details).")
				.define("compactTooltips", false);
		BUILDER.pop();
		SPEC = BUILDER.build();
	}
}
