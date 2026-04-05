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
	static final ForgeConfigSpec SPEC;
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
	public static String restart(double defaultValue){
		return restart(String.valueOf(defaultValue));
	}

	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_00;public static int solarPanel_00;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_01;public static int solarPanel_01;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_02;public static int solarPanel_02;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_03;public static int solarPanel_03;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_04;public static int solarPanel_04;
	private static final ForgeConfigSpec.IntValue SOLAR_PANEL_ORBIT_MULTIPLIER;public static int solarPanel_orbit_multiplier;

	private static final ForgeConfigSpec.IntValue BURNING_GENERATOR_ENERGY_PER_TICK;public static int burningGeneratorEnergyPerTick;
	private static final ForgeConfigSpec.IntValue BURNING_GENERATOR_MAX_ENERGY;public static int burningGeneratorMaxEnergy;
	private static final ForgeConfigSpec.IntValue BURNING_GENERATOR_MAX_EXTRACT;public static int burningGeneratorMaxExtract;

	private static final ForgeConfigSpec.IntValue MATA_PLANT_MAX_HEIGHT;public static int mataPlantMaxHeight;
	private static final ForgeConfigSpec.IntValue SPACE_SCAFFOLDING_LIFE_TIME;public static int spaceScaffoldingLifeTime;
	private static final ForgeConfigSpec.IntValue MEGA_TORCH_RADIUS;public static int megaTorchRadius;

	private static final ForgeConfigSpec.IntValue JETPACK_MAX_BASIC;public static int jetpackMaxBasic;
	private static final ForgeConfigSpec.IntValue JETPACK_MAX_TURBO;public static int jetpackMaxTurbo;
	private static final ForgeConfigSpec.IntValue JETPACK_MAX_THRUST;public static int jetpackMaxThrust;

	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_DEFAULT_MAX_MODIFIERS;public static int modularToolsDefaultMaxModifiers;
	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_REPAIR_AMOUNT;public static int modularToolsRepairAmount;
	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_CHEEP_REPAIR_AMOUNT;public static int modularToolsCheepRepairAmount;

	private static final ForgeConfigSpec.DoubleValue FAST_RAIL_TOP_SPEED;public static double fastRailTopSpeed;
	private static final ForgeConfigSpec.DoubleValue FAST_POWERED_RAIL_ACCELERATION;public static double fastPoweredRailAcceleration;

	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_0;public static int modularToolsEfficiencyModifierStage0;
	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_1;public static int modularToolsEfficiencyModifierStage1;
	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_2;public static int modularToolsEfficiencyModifierStage2;
	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_0;public static int modularToolsEfficiencyModifierLevel0;
	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_1;public static int modularToolsEfficiencyModifierLevel1;
	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_2;public static int modularToolsEfficiencyModifierLevel2;

	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_0;public static int modularToolsFortuneModifierStage0;
	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_1;public static int modularToolsFortuneModifierStage1;
	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_2;public static int modularToolsFortuneModifierStage2;
	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_0;public static int modularToolsFortuneModifierLevel0;
	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_1;public static int modularToolsFortuneModifierLevel1;
	private static final ForgeConfigSpec.IntValue MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_2;public static int modularToolsFortuneModifierLevel2;

	static{
		BUILDER.comment("Dif common config.");
		BUILDER.comment("This configuration is generated, do not overwrite anything except the values!");
		BUILDER.comment("+-------------------------------------------------------+");
		BUILDER.comment("Restart required - restart the game/server.");
		BUILDER.comment("Dif-reload required - use '/dif_config_reload' command.");
		BUILDER.comment("+-------------------------------------------------------+");
		BUILDER.push("GeneralSettings");
			MATA_PLANT_MAX_HEIGHT=BUILDER.comment("Maximal height of Mata Plant, (Blocks.)"+reload(2)).defineInRange("mata_plant_max_height",2,1,MAX);
			SPACE_SCAFFOLDING_LIFE_TIME=BUILDER.comment("Life Time of Space Scaffolding, (t.)"+reload(300)).defineInRange("space_scaffolding_life_time",300,1,MAX);
			MEGA_TORCH_RADIUS=BUILDER.comment("Radius of MEGA Torch, (Blocks.)"+reload(128)).defineInRange("mega_torch_radius",128,32,8192);
		BUILDER.pop();
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
		BUILDER.push("Jetpack");
			JETPACK_MAX_BASIC=BUILDER.comment("Max Basic of Jetpack, (Number.)"+reload(200)).defineInRange("jetpack_max_basic",200,1,MAX);
			JETPACK_MAX_TURBO=BUILDER.comment("Max Turbo of Jetpack, (Number.)"+reload(100)).defineInRange("jetpack_max_turbo",100,1,MAX);
			JETPACK_MAX_THRUST=BUILDER.comment("Max Thrust of Jetpack, (Number.)"+reload(50)).defineInRange("jetpack_max_thrust",50,1,MAX);
		BUILDER.pop();
		BUILDER.push("FastRails");
			FAST_RAIL_TOP_SPEED=BUILDER.comment("Top speed of Fast Rail/Fast Powered Rail, (Number)"+restart(1.7D)).defineInRange("fast_rail_top_speed",1.7D,0.1D,(float)MAX);
			FAST_POWERED_RAIL_ACCELERATION=BUILDER.comment("Acceleration of Speed Powered Rail, (Number)"+restart(0.7D)).defineInRange("fast_powered_rail_acceleration",0.7D,0.1D,(float)MAX);
		BUILDER.pop();
		BUILDER.push("ModularTools");
			MODULAR_TOOLS_DEFAULT_MAX_MODIFIERS=BUILDER.comment("Default maximum of modifiers, (Number.)"+reload(3)).defineInRange("modular_tools_default_max_modifiers",3,0,MAX);
			MODULAR_TOOLS_REPAIR_AMOUNT=BUILDER.comment("Amount of durability restored when repair, (Number)"+reload(5)).defineInRange("modular_tools_repair_amount",5,1,MAX);
			MODULAR_TOOLS_CHEEP_REPAIR_AMOUNT=BUILDER.comment("Amount of durability restored when repair with Cheep modifier, (Number)"+reload(15)).defineInRange("modular_tools_cheep_repair_amount",15,1,MAX);
			BUILDER.push("EfficiencyModifier");
				BUILDER.push("Stages");
					MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_0=BUILDER.comment("Required items to reach Efficiency modifier 1, (Number)"+reload(3)).defineInRange("modular_tools_efficiency_modifier_stage_0",3,1,MAX);
					MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_1=BUILDER.comment("Required items to reach Efficiency modifier 2, (Number)"+reload(5)).defineInRange("modular_tools_efficiency_modifier_stage_1",5,1,MAX);
					MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_2=BUILDER.comment("Required items to reach Efficiency modifier 3, (Number)"+reload(7)).defineInRange("modular_tools_efficiency_modifier_stage_2",7,1,MAX);
				BUILDER.pop();
				BUILDER.push("Levels");
					MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_0=BUILDER.comment("Level of Efficiency modifier 1, (Number)"+reload(2)).defineInRange("modular_tools_efficiency_modifier_level_0",2,1,MAX);
					MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_1=BUILDER.comment("Level of Efficiency modifier 2, (Number)"+reload(4)).defineInRange("modular_tools_efficiency_modifier_level_1",4,1,MAX);
					MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_2=BUILDER.comment("Level of Efficiency modifier 3, (Number)"+reload(6)).defineInRange("modular_tools_efficiency_modifier_level:2",6,1,MAX);
				BUILDER.pop();
			BUILDER.pop();
			BUILDER.push("FortuneModifier");
				BUILDER.push("Stages");
					MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_0=BUILDER.comment("Required items to reach Fortune modifier 1, (Number)"+reload(3)).defineInRange("modular_tools_fortune_modifier_stage_0",3,1,MAX);
					MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_1=BUILDER.comment("Required items to reach Fortune modifier 2, (Number)"+reload(5)).defineInRange("modular_tools_fortune_modifier_stage_1",5,1,MAX);
					MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_2=BUILDER.comment("Required items to reach Fortune modifier 3, (Number)"+reload(7)).defineInRange("modular_tools_fortune_modifier_stage_2",7,1,MAX);
				BUILDER.pop();
				BUILDER.push("Levels");
					MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_0=BUILDER.comment("Level of Fortune modifier 1, (Number)"+reload(2)).defineInRange("modular_tools_fortune_modifier_level_0",2,1,MAX);
					MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_1=BUILDER.comment("Level of Fortune modifier 2, (Number)"+reload(4)).defineInRange("modular_tools_fortune_modifier_level_1",4,1,MAX);
					MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_2=BUILDER.comment("Level of Fortune modifier 3, (Number)"+reload(6)).defineInRange("modular_tools_fortune_modifier_level_2",6,1,MAX);
				BUILDER.pop();
			BUILDER.pop();
		BUILDER.pop();

		SPEC=BUILDER.build();
	}
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

		modularToolsDefaultMaxModifiers=MODULAR_TOOLS_DEFAULT_MAX_MODIFIERS.get();
		modularToolsRepairAmount=MODULAR_TOOLS_REPAIR_AMOUNT.get();
		modularToolsCheepRepairAmount=MODULAR_TOOLS_CHEEP_REPAIR_AMOUNT.get();

		fastRailTopSpeed=FAST_RAIL_TOP_SPEED.get();
		fastPoweredRailAcceleration=FAST_POWERED_RAIL_ACCELERATION.get();

		modularToolsEfficiencyModifierStage0=MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_0.get();
		modularToolsEfficiencyModifierStage1=MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_1.get();
		modularToolsEfficiencyModifierStage2=MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_2.get();

		modularToolsEfficiencyModifierLevel0=MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_0.get();
		modularToolsEfficiencyModifierLevel1=MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_1.get();
		modularToolsEfficiencyModifierLevel2=MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_2.get();

		modularToolsFortuneModifierStage0=MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_0.get();
		modularToolsFortuneModifierStage1=MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_1.get();
		modularToolsFortuneModifierStage2=MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_2.get();

		modularToolsFortuneModifierLevel0=MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_0.get();
		modularToolsFortuneModifierLevel1=MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_1.get();
		modularToolsFortuneModifierLevel2=MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_2.get();
	}
}