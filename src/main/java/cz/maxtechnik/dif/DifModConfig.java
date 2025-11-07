package cz.maxtechnik.dif;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = DifMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DifModConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

	// --- SOLAR PANEL SETTINGS GROUP START ---
	// Pevné reference na konfigurační hodnoty, které Forge spravuje
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_00;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_01;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_02;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_03;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_04;

	// Původní GeneralSettings
	private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK;
	private static final ForgeConfigSpec.IntValue MAGIC_NUMBER;
	private static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION;


	static{
		// Seskupení pro solární panely
		BUILDER.push("SolarPanelSettings");

		SOLAR_PANEL_00 = BUILDER.comment("Production of Solar Panel 00, Fe/t").defineInRange("solar_panel_00", 1, 0, Integer.MAX_VALUE);
		SOLAR_PANEL_01 = BUILDER.comment("Production of Solar Panel 01, Fe/t").defineInRange("solar_panel_01", 5, 0, Integer.MAX_VALUE);
		SOLAR_PANEL_02 = BUILDER.comment("Production of Solar Panel 02, Fe/t").defineInRange("solar_panel_02", 20, 0, Integer.MAX_VALUE);
		SOLAR_PANEL_03 = BUILDER.comment("Production of Solar Panel 03, Fe/t").defineInRange("solar_panel_03", 50, 0, Integer.MAX_VALUE);
		SOLAR_PANEL_04 = BUILDER.comment("Production of Solar Panel 04, Fe/t").defineInRange("solar_panel_04", 100, 0, Integer.MAX_VALUE);

		BUILDER.pop(); // Ukončení SolarPanelSettings

		// Seskupení pro obecná nastavení
		BUILDER.push("GeneralSettings");

		LOG_DIRT_BLOCK = BUILDER.comment("Whether to log the dirt block").define("logDirtBlock", false);
		MAGIC_NUMBER = BUILDER.comment("The magic number").defineInRange("magicNumber", 42, 0, 1000);
		MAGIC_NUMBER_INTRODUCTION = BUILDER.comment("A string to introduce the magic number").define("magicNumberIntroduction", "The magic number is...");

		BUILDER.pop(); // Ukončení GeneralSettings

		SPEC = BUILDER.build();
	}

	static final ForgeConfigSpec SPEC;

	// Dynamické proměnné, které ukládají aktuálně načtené hodnoty
	public static int solarPanel_00;
	public static int solarPanel_01;
	public static int solarPanel_02;
	public static int solarPanel_03;
	public static int solarPanel_04;

	public static boolean logDirtBlock;
	public static int magicNumber;
	public static String magicNumberIntroduction;

	/**
	 * Tato metoda se zavolá, když je konfigurační soubor poprvé načten (Loading)
	 * NEBO když je za běhu přenačten (Reloading), což je klíčové pro Create mod.
	 */
	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		// Důležité: Tuto metodu nyní volá jak ModConfigEvent.Loading, tak ModConfigEvent.Reloading.
		// Pokaždé se tak aktualizují veřejné statické proměnné ze statických ForgeConfigSpec instancí.

		solarPanel_00 = SOLAR_PANEL_00.get();
		solarPanel_01 = SOLAR_PANEL_01.get();
		solarPanel_02 = SOLAR_PANEL_02.get();
		solarPanel_03 = SOLAR_PANEL_03.get();
		solarPanel_04 = SOLAR_PANEL_04.get();

		logDirtBlock = LOG_DIRT_BLOCK.get();
		magicNumber = MAGIC_NUMBER.get();
		magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

		// TIP: Pokud byste potřeboval provést nějaké akce POUZE při startu
		// (např. registrace, které nelze za běhu měnit),
		// musíte zkontrolovat typ události:
		// if (event instanceof ModConfigEvent.Loading) { ... }
	}
}