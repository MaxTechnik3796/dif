package cz.maxtechnik.dif.config;


import net.neoforged.neoforge.common.ModConfigSpec;

public class DifModCommonConfig{
	private static final ModConfigSpec.Builder BUILDER=new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;
	public static final int MAX=Integer.MAX_VALUE;
	public static final ModConfigSpec.IntValue SOLAR_PANEL_00;
	public static final ModConfigSpec.IntValue SOLAR_PANEL_01;
	public static final ModConfigSpec.IntValue SOLAR_PANEL_02;
	public static final ModConfigSpec.IntValue SOLAR_PANEL_03;
	public static final ModConfigSpec.IntValue SOLAR_PANEL_04;
	public static final ModConfigSpec.IntValue SOLAR_PANEL_ORBIT_MULTIPLIER;

	public static final ModConfigSpec.IntValue BURNING_GENERATOR_ENERGY_PER_TICK;
	public static final ModConfigSpec.IntValue BURNING_GENERATOR_MAX_ENERGY;
	public static final ModConfigSpec.IntValue BURNING_GENERATOR_MAX_EXTRACT;


	public static final ModConfigSpec.IntValue SPACE_SCAFFOLDING_LIFE_TIME;
	public static final ModConfigSpec.IntValue MEGA_TORCH_RADIUS;
	public static final ModConfigSpec.IntValue NANO_GLASS_MAX_SPREAD;

	public static final ModConfigSpec.IntValue JETPACK_MAX_BASIC;
	public static final ModConfigSpec.IntValue JETPACK_MAX_TURBO;

	public static final ModConfigSpec.IntValue MODULAR_TOOLS_DEFAULT_MAX_MODIFIERS;
	public static final ModConfigSpec.IntValue MODULAR_TOOLS_REPAIR_AMOUNT;
	public static final ModConfigSpec.IntValue MODULAR_TOOLS_CHEEP_REPAIR_AMOUNT;

	public static final ModConfigSpec.DoubleValue FAST_RAIL_TOP_SPEED;
	public static final ModConfigSpec.DoubleValue FAST_POWERED_RAIL_ACCELERATION;

	public static final ModConfigSpec.IntValue MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_0;
	public static final ModConfigSpec.IntValue MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_1;
	public static final ModConfigSpec.IntValue MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_2;
	public static final ModConfigSpec.IntValue MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_0;
	public static final ModConfigSpec.IntValue MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_1;
	public static final ModConfigSpec.IntValue MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_2;

	public static final ModConfigSpec.IntValue MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_0;
	public static final ModConfigSpec.IntValue MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_1;
	public static final ModConfigSpec.IntValue MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_2;
	public static final ModConfigSpec.IntValue MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_0;
	public static final ModConfigSpec.IntValue MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_1;
	public static final ModConfigSpec.IntValue MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_2;

	public static final ModConfigSpec.IntValue PORTAL_GUN_MAX_DURABILITY;
	public static final ModConfigSpec.IntValue PORTAL_GUN_ENERGY_PER_SHOT;
	public static final ModConfigSpec.IntValue PORTAL_GUN_ENERGY_PER_PEARL;
	public static final ModConfigSpec.IntValue PORTAL_GUN_SHOT_COOLDOWN;
	public static final ModConfigSpec.IntValue PORTAL_TELEPORT_COOLDOWN;
	public static final ModConfigSpec.IntValue PORTAL_MAX_DISTANCE;
	public static final ModConfigSpec.IntValue PORTAL_CHUNK_LOAD_TIMEOUT;
	public static final ModConfigSpec.IntValue PORTAL_MAX_ENTITIES_PER_TICK;
	public static final ModConfigSpec.BooleanValue PORTAL_ALLOW_ENTITIES;
	public static final ModConfigSpec.BooleanValue PORTAL_ALLOW_ITEMS;

	public static final double DOUBLE_MAX=1_000_000.0D;
	public static final ModConfigSpec.DoubleValue ENGINE_DIESEL_RPM;
	public static final ModConfigSpec.DoubleValue ENGINE_DIESEL_SU;
	public static final ModConfigSpec.DoubleValue ENGINE_DIESEL_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue ENGINE_DIESEL_SMALL_ENGINE_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue ENGINE_HEAVY_FUEL_OIL_RPM;
	public static final ModConfigSpec.DoubleValue ENGINE_HEAVY_FUEL_OIL_SU;
	public static final ModConfigSpec.DoubleValue ENGINE_HEAVY_FUEL_OIL_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue ENGINE_HEAVY_FUEL_OIL_SMALL_ENGINE_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue ENGINE_GASOLINE_RPM;
	public static final ModConfigSpec.DoubleValue ENGINE_GASOLINE_SU;
	public static final ModConfigSpec.DoubleValue ENGINE_GASOLINE_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue ENGINE_GASOLINE_SMALL_ENGINE_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue ENGINE_LPG_RPM;
	public static final ModConfigSpec.DoubleValue ENGINE_LPG_SU;
	public static final ModConfigSpec.DoubleValue ENGINE_LPG_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue ENGINE_LPG_SMALL_ENGINE_CONSUMPTION;

	static{
		BUILDER.comment("Dif common config.");
		BUILDER.comment("This configuration is generated, do not overwrite anything except the values!");
		BUILDER.comment("+-------------------------------------------------------+");
		BUILDER.comment("Restart required - restart the game/server.");
		BUILDER.comment("Dif-reload required - use '/dif_config_reload' command.");
		BUILDER.comment("+-------------------------------------------------------+");
		BUILDER.push("GeneralSettings");
		SPACE_SCAFFOLDING_LIFE_TIME=BUILDER.defineInRange("space_scaffolding_life_time",300,1,MAX);
		MEGA_TORCH_RADIUS=BUILDER.defineInRange("mega_torch_radius",128,32,8192);
		// Maximum number of blocks NanoGlass will spread to when triggered (BFS limit)
		NANO_GLASS_MAX_SPREAD=BUILDER.defineInRange("nano_glass_max_spread",128,1,MAX);
		BUILDER.pop();
		BUILDER.push("SolarPanelSettings");
		SOLAR_PANEL_00=BUILDER.defineInRange("solar_panel_00",1,0,MAX);
		SOLAR_PANEL_01=BUILDER.defineInRange("solar_panel_01",5,0,MAX);
		SOLAR_PANEL_02=BUILDER.defineInRange("solar_panel_02",20,0,MAX);
		SOLAR_PANEL_03=BUILDER.defineInRange("solar_panel_03",50,0,MAX);
		SOLAR_PANEL_04=BUILDER.defineInRange("solar_panel_04",100,0,MAX);
		SOLAR_PANEL_ORBIT_MULTIPLIER=BUILDER.defineInRange("solar_panel_orbit_multiplier",2,1,MAX);
		BUILDER.pop();
		BUILDER.push("BurningGenerator");
		BURNING_GENERATOR_ENERGY_PER_TICK=BUILDER.defineInRange("burning_generator_energy_per_tick",20,0,MAX);
		BURNING_GENERATOR_MAX_ENERGY=BUILDER.defineInRange("burning_generator_max_energy",32000,0,MAX);
		BURNING_GENERATOR_MAX_EXTRACT=BUILDER.defineInRange("burning_generator_max_extract",200,0,MAX);
		BUILDER.pop();
		BUILDER.push("Jetpack");
		JETPACK_MAX_BASIC=BUILDER.defineInRange("jetpack_max_basic",200,1,MAX);
		JETPACK_MAX_TURBO=BUILDER.defineInRange("jetpack_max_turbo",300,1,MAX);
		BUILDER.pop();
		BUILDER.push("FastRails");
		FAST_RAIL_TOP_SPEED=BUILDER.defineInRange("fast_rail_top_speed",1.2D,0.1D,MAX);
		FAST_POWERED_RAIL_ACCELERATION=BUILDER.defineInRange("fast_powered_rail_acceleration",0.5D,0.1D,MAX);
		BUILDER.pop();
		BUILDER.push("ModularTools");
		MODULAR_TOOLS_DEFAULT_MAX_MODIFIERS=BUILDER.defineInRange("modular_tools_default_max_modifiers",3,0,MAX);
		MODULAR_TOOLS_REPAIR_AMOUNT=BUILDER.defineInRange("modular_tools_repair_amount",5,1,MAX);
		MODULAR_TOOLS_CHEEP_REPAIR_AMOUNT=BUILDER.defineInRange("modular_tools_cheep_repair_amount",15,1,MAX);
		BUILDER.push("EfficiencyModifier");
		BUILDER.push("Stages");
		MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_0=BUILDER.defineInRange("modular_tools_efficiency_modifier_stage_0",3,1,MAX);
		MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_1=BUILDER.defineInRange("modular_tools_efficiency_modifier_stage_1",5,1,MAX);
		MODULAR_TOOLS_EFFICIENCY_MODIFIER_STAGE_2=BUILDER.defineInRange("modular_tools_efficiency_modifier_stage_2",7,1,MAX);
		BUILDER.pop();
		BUILDER.push("Levels");
		MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_0=BUILDER.defineInRange("modular_tools_efficiency_modifier_level_0",2,1,MAX);
		MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_1=BUILDER.defineInRange("modular_tools_efficiency_modifier_level_1",4,1,MAX);
		MODULAR_TOOLS_EFFICIENCY_MODIFIER_LEVEL_2=BUILDER.defineInRange("modular_tools_efficiency_modifier_level:2",6,1,MAX);
		BUILDER.pop();
		BUILDER.pop();
		BUILDER.push("FortuneModifier");
		BUILDER.push("Stages");
		MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_0=BUILDER.defineInRange("modular_tools_fortune_modifier_stage_0",3,1,MAX);
		MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_1=BUILDER.defineInRange("modular_tools_fortune_modifier_stage_1",5,1,MAX);
		MODULAR_TOOLS_FORTUNE_MODIFIER_STAGE_2=BUILDER.defineInRange("modular_tools_fortune_modifier_stage_2",7,1,MAX);
		BUILDER.pop();
		BUILDER.push("Levels");
		MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_0=BUILDER.defineInRange("modular_tools_fortune_modifier_level_0",2,1,MAX);
		MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_1=BUILDER.defineInRange("modular_tools_fortune_modifier_level_1",4,1,MAX);
		MODULAR_TOOLS_FORTUNE_MODIFIER_LEVEL_2=BUILDER.defineInRange("modular_tools_fortune_modifier_level_2",6,1,MAX);
		BUILDER.pop();
		BUILDER.pop();
		BUILDER.pop();
		BUILDER.push("PortalGun");
		PORTAL_GUN_MAX_DURABILITY=BUILDER.defineInRange("portal_gun_max_durability",24,1,MAX);
		PORTAL_GUN_ENERGY_PER_SHOT=BUILDER.defineInRange("portal_gun_energy_per_shot",1,1,MAX);
		PORTAL_GUN_ENERGY_PER_PEARL=BUILDER.defineInRange("portal_gun_energy_per_pearl",4,1,MAX);
		PORTAL_GUN_SHOT_COOLDOWN=BUILDER.defineInRange("portal_gun_shot_cooldown",10,1,MAX);
		PORTAL_TELEPORT_COOLDOWN=BUILDER.defineInRange("portal_teleport_cooldown",20,1,MAX);
		PORTAL_MAX_DISTANCE=BUILDER.defineInRange("portal_max_distance",256,16,MAX);
		PORTAL_CHUNK_LOAD_TIMEOUT=BUILDER.defineInRange("portal_chunk_load_timeout",100,20,MAX);
		PORTAL_MAX_ENTITIES_PER_TICK=BUILDER.defineInRange("portal_max_entities_per_tick",5,1,MAX);
		PORTAL_ALLOW_ENTITIES=BUILDER.define("portal_allow_entities",true);
		PORTAL_ALLOW_ITEMS=BUILDER.define("portal_allow_items",true);
		BUILDER.pop();
		BUILDER.push("Engines");
		BUILDER.push("Diesel");
		ENGINE_DIESEL_RPM=BUILDER.defineInRange("engine_diesel_rpm",12.0D,0.0D,DOUBLE_MAX);
		ENGINE_DIESEL_SU=BUILDER.defineInRange("engine_diesel_su",2.0D,0.0D,DOUBLE_MAX);
		ENGINE_DIESEL_CONSUMPTION=BUILDER.defineInRange("engine_diesel_consumption",1.0D,0.0D,DOUBLE_MAX);
		ENGINE_DIESEL_SMALL_ENGINE_CONSUMPTION=BUILDER.defineInRange("engine_diesel_small_engine_consumption",1.0D,0.0D,DOUBLE_MAX);
		BUILDER.pop();
		BUILDER.push("HeavyFuelOil");
		ENGINE_HEAVY_FUEL_OIL_RPM=BUILDER.defineInRange("engine_heavy_fuel_oil_rpm",12.0D,0.0D,DOUBLE_MAX);
		ENGINE_HEAVY_FUEL_OIL_SU=BUILDER.defineInRange("engine_heavy_fuel_oil_su",2.0D,0.0D,DOUBLE_MAX);
		ENGINE_HEAVY_FUEL_OIL_CONSUMPTION=BUILDER.defineInRange("engine_heavy_fuel_oil_consumption",1.0D,0.0D,DOUBLE_MAX);
		ENGINE_HEAVY_FUEL_OIL_SMALL_ENGINE_CONSUMPTION=BUILDER.defineInRange("engine_heavy_fuel_oil_small_engine_consumption",1.0D,0.0D,DOUBLE_MAX);
		BUILDER.pop();
		BUILDER.push("Gasoline");
		ENGINE_GASOLINE_RPM=BUILDER.defineInRange("engine_gasoline_rpm",12.0D,0.0D,DOUBLE_MAX);
		ENGINE_GASOLINE_SU=BUILDER.defineInRange("engine_gasoline_su",2.0D,0.0D,DOUBLE_MAX);
		ENGINE_GASOLINE_CONSUMPTION=BUILDER.defineInRange("engine_gasoline_consumption",1.0D,0.0D,DOUBLE_MAX);
		ENGINE_GASOLINE_SMALL_ENGINE_CONSUMPTION=BUILDER.defineInRange("engine_gasoline_small_engine_consumption",1.0D,0.0D,DOUBLE_MAX);
		BUILDER.pop();
		BUILDER.push("LPG");
		ENGINE_LPG_RPM=BUILDER.defineInRange("engine_lpg_rpm",12.0D,0.0D,DOUBLE_MAX);
		ENGINE_LPG_SU=BUILDER.defineInRange("engine_lpg_su",2.0D,0.0D,DOUBLE_MAX);
		ENGINE_LPG_CONSUMPTION=BUILDER.defineInRange("engine_lpg_consumption",1.0D,0.0D,DOUBLE_MAX);
		ENGINE_LPG_SMALL_ENGINE_CONSUMPTION=BUILDER.defineInRange("engine_lpg_small_engine_consumption",1.0D,0.0D,DOUBLE_MAX);
		BUILDER.pop();
		BUILDER.pop();
		SPEC=BUILDER.build();
	}
}