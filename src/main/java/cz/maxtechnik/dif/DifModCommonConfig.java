package cz.maxtechnik.dif;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
@Mod.EventBusSubscriber(modid=DifMod.MODID,bus=Mod.EventBusSubscriber.Bus.MOD)
public class DifModCommonConfig{
	private static final ForgeConfigSpec.Builder BUILDER=new ForgeConfigSpec.Builder();

	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_00;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_01;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_02;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_03;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_04;

	private static final ForgeConfigSpec.IntValue BURNING_GENERATOR_ENERGY_PER_TICK;
	private static final ForgeConfigSpec.IntValue BURNING_GENERATOR_MAX_ENERGY;
	private static final ForgeConfigSpec.IntValue BURNING_GENERATOR_MAX_EXTRACT;

	private static final ForgeConfigSpec.IntValue MATA_PLANT_MAX_HEIGHT;
	private static final ForgeConfigSpec.BooleanValue DISABLE_END;

	private static final ForgeConfigSpec.IntValue PORTAL_GUN_MAX_AMMO;
	private static final ForgeConfigSpec.IntValue PORTAL_GUN_MAX_RANGE;
	private static final ForgeConfigSpec.IntValue PORTAL_GUN_COOLDOWN;

	private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK;
	private static final ForgeConfigSpec.IntValue MAGIC_NUMBER;
	private static final ForgeConfigSpec.ConfigValue<String>MAGIC_NUMBER_INTRODUCTION;
	static{
		BUILDER.comment("Dif common config.");
		BUILDER.comment("This configuration is generated, do not overwrite anything except the values!");
		BUILDER.comment("+----------------------------------------+");
		BUILDER.comment("Restart required - restart the game/server.");
		BUILDER.comment("Dif-reload required - use '/dif_config_reload' command.");
		BUILDER.comment("+----------------------------------------+");
		BUILDER.push("SolarPanelSettings");
		SOLAR_PANEL_00=BUILDER.comment("Production of Solar Panel 00, (Fe/t.)\nDif-reload required!\nDefault value: 1").defineInRange("solar_panel_00",1,0,Integer.MAX_VALUE);
		SOLAR_PANEL_01=BUILDER.comment("Production of Solar Panel 01, (Fe/t.)\nDif-reload required!\nDefault value: 5").defineInRange("solar_panel_01",5,0,Integer.MAX_VALUE);
		SOLAR_PANEL_02=BUILDER.comment("Production of Solar Panel 02, (Fe/t.)\nDif-reload required!\nDefault value: 20").defineInRange("solar_panel_02",20,0,Integer.MAX_VALUE);
		SOLAR_PANEL_03=BUILDER.comment("Production of Solar Panel 03, (Fe/t.)\nDif-reload required!\nDefault value: 50").defineInRange("solar_panel_03",50,0,Integer.MAX_VALUE);
		SOLAR_PANEL_04=BUILDER.comment("Production of Solar Panel 04, (Fe/t.)\nDif-reload required!\nDefault value: 100").defineInRange("solar_panel_04",100,0,Integer.MAX_VALUE);
		BUILDER.pop();
		BUILDER.push("BurningGenerator");
		BURNING_GENERATOR_ENERGY_PER_TICK=BUILDER.comment("Production of Burning Generator, (Fe/t.)\nRestart required!\nDefault value: 20").defineInRange("burning_generator_energy_per_tick",20,0,Integer.MAX_VALUE);
		BURNING_GENERATOR_MAX_ENERGY=BUILDER.comment("Maximum capacity of the Burning Generator, (Fe.)\nRestart required!\nDefault value: 32000").defineInRange("burning_generator_max_energy",32000,0,Integer.MAX_VALUE);
		BURNING_GENERATOR_MAX_EXTRACT=BUILDER.comment("Maximum energy output from the Burning Generator, (Fe/t.)\nRestart required!\nDefault value: 200").defineInRange("burning_generator_max_extract",200,0,Integer.MAX_VALUE);
		BUILDER.pop();
		BUILDER.push("GeneralSettings");
		MATA_PLANT_MAX_HEIGHT=BUILDER.comment("Maximal height of Mata Plant, (Blocks.)\nDif-reload required!\nDefault value: 2").defineInRange("mata_plant_max_height",2,1,Integer.MAX_VALUE);
		DISABLE_END=BUILDER.comment("Disable End dimension, (t/f.)\nDif-reload required!\nDefault value: false").define("disable_end",false);
		BUILDER.pop();
		BUILDER.push("PortalGun");
		PORTAL_GUN_MAX_AMMO=BUILDER.comment("Maximal capacity of Portal Gun, (Shots.)\nDRestart required!\nDefault value: 16").defineInRange("portal_gun_max_ammo",16,1,Integer.MAX_VALUE);
		PORTAL_GUN_MAX_RANGE=BUILDER.comment("Maximal shot power of Portal Gun, (Number.)\nDif-reload required!\nDefault value: 3").defineInRange("portal_gun_max_range",3,1,255);
		PORTAL_GUN_COOLDOWN=BUILDER.comment("Cooldown of Portal Gun, (t.)\nDif-reload required!\nDefault value: 80").defineInRange("portal_gun_cooldown",80,0,Integer.MAX_VALUE);
		BUILDER.pop();
		BUILDER.push("TrashSettings");
		LOG_DIRT_BLOCK=BUILDER.comment("Whether to log the dirt item").define("logDirtBlock",false);
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

	public static int burningGeneratorEnergyPerTick;
	public static int burningGeneratorMaxEnergy;
	public static int burningGeneratorMaxExtract;

	public static int mataPlantMaxHeight;
	public static boolean disableEnd;

	public static int portalGunMaxAmmo;
	public static int portalGunMaxRange;
	public static int portalGunCooldown;

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
	public static void load(){
		DifMod.LOGGER.debug("Configuration loaded!");
		solarPanel_00=SOLAR_PANEL_00.get();
		solarPanel_01=SOLAR_PANEL_01.get();
		solarPanel_02=SOLAR_PANEL_02.get();
		solarPanel_03=SOLAR_PANEL_03.get();
		solarPanel_04=SOLAR_PANEL_04.get();

		burningGeneratorEnergyPerTick=BURNING_GENERATOR_ENERGY_PER_TICK.get();
		burningGeneratorMaxEnergy=BURNING_GENERATOR_MAX_ENERGY.get();
		burningGeneratorMaxExtract=BURNING_GENERATOR_MAX_EXTRACT.get();

		mataPlantMaxHeight=MATA_PLANT_MAX_HEIGHT.get();
		disableEnd=DISABLE_END.get();

		portalGunMaxAmmo=PORTAL_GUN_MAX_AMMO.get();
		portalGunMaxRange=PORTAL_GUN_MAX_RANGE.get();
		portalGunCooldown=PORTAL_GUN_COOLDOWN.get();

		logDirtBlock=LOG_DIRT_BLOCK.get();
		magicNumber=MAGIC_NUMBER.get();
		magicNumberIntroduction=MAGIC_NUMBER_INTRODUCTION.get();
	}
}