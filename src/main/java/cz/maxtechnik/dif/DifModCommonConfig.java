package cz.maxtechnik.dif;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
@Mod.EventBusSubscriber(modid=DifMod.MODID,bus=Mod.EventBusSubscriber.Bus.MOD)
public class DifModCommonConfig{
	@SubscribeEvent
	static void onLoad(final ModConfigEvent event){
		load();
	}
	@SubscribeEvent
	public static void onReload(ModConfigEvent.Reloading event){
		load();
	}
	private static final ForgeConfigSpec.Builder BUILDER=new ForgeConfigSpec.Builder();
	public static int MAX=Integer.MAX_VALUE;
	public static String reload(String defaultValue){
		return "\nDif-reload required!\nDefault value: "+defaultValue;
	}
	public static String reload(int defaultValue){
		return reload(String.valueOf(defaultValue));
	}

	public static String restart(String defaultValue){
		return "\nRestart required!\nDefault value: "+defaultValue;
	}
	public static String restart(int defaultValue){
		return restart(String.valueOf(defaultValue));
	}

	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_00;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_01;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_02;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_03;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_04;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_ORBIT_MULTIPLIER;

	private static final ForgeConfigSpec.IntValue BURNING_GENERATOR_ENERGY_PER_TICK;
	private static final ForgeConfigSpec.IntValue BURNING_GENERATOR_MAX_ENERGY;
	private static final ForgeConfigSpec.IntValue BURNING_GENERATOR_MAX_EXTRACT;

	private static final ForgeConfigSpec.IntValue MATA_PLANT_MAX_HEIGHT;
	private static final ForgeConfigSpec.IntValue SPACE_SCAFFOLDING_LIFE_TIME;
	private static final ForgeConfigSpec.IntValue MEGA_TORCH_RADIUS;

	private static final ForgeConfigSpec.IntValue JETPACK_MAX_BASIC;
	private static final ForgeConfigSpec.IntValue JETPACK_MAX_TURBO;
	private static final ForgeConfigSpec.IntValue JETPACK_MAX_THRUST;

	static{
		BUILDER.comment("Dif common config.");
		BUILDER.comment("This configuration is generated, do not overwrite anything except the values!");
		BUILDER.comment("+----------------------------------------+");
		BUILDER.comment("Restart required - restart the game/server.");
		BUILDER.comment("Dif-reload required - use '/dif_config_reload' command.");
		BUILDER.comment("+----------------------------------------+");
		BUILDER.push("SolarPanelSettings");
		SOLAR_PANEL_00=BUILDER.comment("Production of Solar Panel 00, (Fe/t.)"+reload(1)).defineInRange("solar_panel_00",1,0,MAX);
		SOLAR_PANEL_01=BUILDER.comment("Production of Solar Panel 01, (Fe/t.)"+reload(5)).defineInRange("solar_panel_01",5,0,MAX);
		SOLAR_PANEL_02=BUILDER.comment("Production of Solar Panel 02, (Fe/t.)"+reload(20)).defineInRange("solar_panel_02",20,0,MAX);
		SOLAR_PANEL_03=BUILDER.comment("Production of Solar Panel 03, (Fe/t.)"+reload(50)).defineInRange("solar_panel_03",50,0,MAX);
		SOLAR_PANEL_04=BUILDER.comment("Production of Solar Panel 04, (Fe/t.)"+reload(100)).defineInRange("solar_panel_04",100,0,MAX);
		SOLAR_PANEL_ORBIT_MULTIPLIER=BUILDER.comment("Production multiplier of Solar Panels on Orbit, (Number.)"+reload(2)).defineInRange("solar_panel_orbit_multiplier",2,1,MAX);
		BUILDER.pop();
		BUILDER.push("BurningGenerator");
		BURNING_GENERATOR_ENERGY_PER_TICK=BUILDER.comment("Production of Burning Generator, (Fe/t.)"+restart(20)).defineInRange("burning_generator_energy_per_tick",20,0,MAX);
		BURNING_GENERATOR_MAX_ENERGY=BUILDER.comment("Maximum capacity of the Burning Generator, (Fe.)"+restart(32000)).defineInRange("burning_generator_max_energy",32000,0,MAX);
		BURNING_GENERATOR_MAX_EXTRACT=BUILDER.comment("Maximum energy output from the Burning Generator, (Fe/t.)"+restart(200)).defineInRange("burning_generator_max_extract",200,0,MAX);
		BUILDER.pop();
		BUILDER.push("GeneralSettings");
		MATA_PLANT_MAX_HEIGHT=BUILDER.comment("Maximal height of Mata Plant, (Blocks.)"+reload(2)).defineInRange("mata_plant_max_height",2,1,MAX);
		SPACE_SCAFFOLDING_LIFE_TIME=BUILDER.comment("Life Time of Space Scaffolding, (t.)"+reload(300)).defineInRange("space_scaffolding_life_time",300,1,MAX);
		MEGA_TORCH_RADIUS=BUILDER.comment("Radius of MEGA Torch, (Blocks.)"+reload(128)).defineInRange("mega_torch_radius",128,32,8192);
		BUILDER.pop();
		BUILDER.push("Jetpack");
		JETPACK_MAX_BASIC=BUILDER.comment("Max Basic of Jetpack, (Number.)"+reload(200)).defineInRange("jetpack_max_basic",200,1,MAX);
		JETPACK_MAX_TURBO=BUILDER.comment("Max Turbo of Jetpack, (Number.)"+reload(100)).defineInRange("jetpack_max_turbo",100,1,MAX);
		JETPACK_MAX_THRUST=BUILDER.comment("Max Thrust of Jetpack, (Number.)"+reload(50)).defineInRange("jetpack_max_thrust",50,1,MAX);
		BUILDER.pop();
		SPEC=BUILDER.build();
	}
	static final ForgeConfigSpec SPEC;
	public static int solarPanel_00;
	public static int solarPanel_01;
	public static int solarPanel_02;
	public static int solarPanel_03;
	public static int solarPanel_04;
	public static int solarPanel_orbit_multiplier;

	public static int burningGeneratorEnergyPerTick;
	public static int burningGeneratorMaxEnergy;
	public static int burningGeneratorMaxExtract;

	public static int mataPlantMaxHeight;
	public static int spaceScaffoldingLifeTime;
	public static int megaTorchRadius;

	public static int jetpackMaxBasic;
	public static int jetpackMaxTurbo;
	public static int jetpackMaxThrust;

	public static void load(){
		DifMod.LOGGER.info("Configuration loaded!");
		solarPanel_00=SOLAR_PANEL_00.get();
		solarPanel_01=SOLAR_PANEL_01.get();
		solarPanel_02=SOLAR_PANEL_02.get();
		solarPanel_03=SOLAR_PANEL_03.get();
		solarPanel_04=SOLAR_PANEL_04.get();
		solarPanel_orbit_multiplier=SOLAR_PANEL_ORBIT_MULTIPLIER.get();

		burningGeneratorEnergyPerTick=BURNING_GENERATOR_ENERGY_PER_TICK.get();
		burningGeneratorMaxEnergy=BURNING_GENERATOR_MAX_ENERGY.get();
		burningGeneratorMaxExtract=BURNING_GENERATOR_MAX_EXTRACT.get();

		mataPlantMaxHeight=MATA_PLANT_MAX_HEIGHT.get();
		spaceScaffoldingLifeTime=SPACE_SCAFFOLDING_LIFE_TIME.get();
		megaTorchRadius=MEGA_TORCH_RADIUS.get();

		jetpackMaxBasic=JETPACK_MAX_BASIC.get();
		jetpackMaxTurbo=JETPACK_MAX_TURBO.get();
		jetpackMaxThrust=JETPACK_MAX_THRUST.get();
	}
}