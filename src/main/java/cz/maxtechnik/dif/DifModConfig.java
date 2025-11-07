package cz.maxtechnik.dif;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid=DifMod.MODID,bus=Mod.EventBusSubscriber.Bus.MOD)
public class DifModConfig{
	private static final ForgeConfigSpec.Builder BUILDER=new ForgeConfigSpec.Builder();

	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_00;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_01;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_02;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_03;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_04;

	private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK;
	private static final ForgeConfigSpec.IntValue MAGIC_NUMBER;
	private static final ForgeConfigSpec.ConfigValue<String>MAGIC_NUMBER_INTRODUCTION;
	static{
		BUILDER.push("SolarPanelSettings");
		SOLAR_PANEL_00=BUILDER.comment("Production of Solar Panel 00, Fe/t.\nDefault value: 1").defineInRange("solar_panel_00",1,0,Integer.MAX_VALUE);
		SOLAR_PANEL_01=BUILDER.comment("Production of Solar Panel 01, Fe/t.\nDefault value: 5").defineInRange("solar_panel_01",5,0,Integer.MAX_VALUE);
		SOLAR_PANEL_02=BUILDER.comment("Production of Solar Panel 02, Fe/t.\nDefault value: 20").defineInRange("solar_panel_02",20,0,Integer.MAX_VALUE);
		SOLAR_PANEL_03=BUILDER.comment("Production of Solar Panel 03, Fe/t.\nDefault value: 50").defineInRange("solar_panel_03",50,0,Integer.MAX_VALUE);
		SOLAR_PANEL_04=BUILDER.comment("Production of Solar Panel 04, Fe/t.\nDefault value: 100").defineInRange("solar_panel_04",100,0,Integer.MAX_VALUE);
		BUILDER.pop();
		BUILDER.push("GeneralSettings");
		LOG_DIRT_BLOCK=BUILDER.comment("Whether to log the dirt block").define("logDirtBlock",false);
		MAGIC_NUMBER=BUILDER.comment("The magic number").defineInRange("magicNumber",42,0,1000);
		MAGIC_NUMBER_INTRODUCTION=BUILDER.comment("A string to introduce the magic number").define("magicNumberIntroduction","The magic number is...");
		BUILDER.pop();
		SPEC=BUILDER.build();
	}
	static final ForgeConfigSpec SPEC;
	public static int solarPanel_00;
	public static int solarPanel_01;
	public static int solarPanel_02;
	public static int solarPanel_03;
	public static int solarPanel_04;

	public static boolean logDirtBlock;
	public static int magicNumber;
	public static String magicNumberIntroduction;

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		load();
	}
	@SubscribeEvent
	public static void onReload(ModConfigEvent.Reloading event){
		load();
	}
	private static void load(){
		solarPanel_00=SOLAR_PANEL_00.get();
		solarPanel_01=SOLAR_PANEL_01.get();
		solarPanel_02=SOLAR_PANEL_02.get();
		solarPanel_03=SOLAR_PANEL_03.get();
		solarPanel_04=SOLAR_PANEL_04.get();

		logDirtBlock=LOG_DIRT_BLOCK.get();
		magicNumber=MAGIC_NUMBER.get();
		magicNumberIntroduction=MAGIC_NUMBER_INTRODUCTION.get();
	}
}