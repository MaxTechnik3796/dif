package cz.maxtechnik.dif.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DifModServerConfig{
	private static final ModConfigSpec.Builder BUILDER=new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;
	public static final int MAX=Integer.MAX_VALUE;
	public static final double DOUBLE_MAX=1_000_000.0D;

	// General
	public static final ModConfigSpec.IntValue SPACE_SCAFFOLDING_LIFE_TIME;
	public static final ModConfigSpec.IntValue MEGA_TORCH_RADIUS;
	public static final ModConfigSpec.IntValue NANO_GLASS_MAX_SPREAD;
	public static final ModConfigSpec.IntValue JETPACK_CAPACITY;
	public static final ModConfigSpec.IntValue ELECTRUM_DEFORESTER_MAX_LOGS;

	// Solar Panels
	public static final ModConfigSpec.IntValue SOLAR_PANEL_00;
	public static final ModConfigSpec.IntValue SOLAR_PANEL_01;
	public static final ModConfigSpec.IntValue SOLAR_PANEL_02;
	public static final ModConfigSpec.IntValue SOLAR_PANEL_03;
	public static final ModConfigSpec.IntValue SOLAR_PANEL_04;
	public static final ModConfigSpec.IntValue SOLAR_PANEL_ORBIT_MULTIPLIER;

	// Burning Generator
	public static final ModConfigSpec.IntValue BURNING_GENERATOR_ENERGY_PER_TICK;
	public static final ModConfigSpec.IntValue BURNING_GENERATOR_MAX_ENERGY;
	public static final ModConfigSpec.IntValue BURNING_GENERATOR_MAX_EXTRACT;

	// Fast Rails
	public static final ModConfigSpec.DoubleValue FAST_RAIL_TOP_SPEED;
	public static final ModConfigSpec.DoubleValue FAST_POWERED_RAIL_ACCELERATION;

	// Portal Gun
	public static final ModConfigSpec.IntValue PORTAL_GUN_MAX_DURABILITY;
	public static final ModConfigSpec.IntValue PORTAL_GUN_ENERGY_PER_PEARL;
	public static final ModConfigSpec.IntValue PORTAL_MAX_DISTANCE;
	public static final ModConfigSpec.BooleanValue PORTAL_ALLOW_ENTITIES;

	// Quarry
	public static final ModConfigSpec.DoubleValue QUARRY_STRESS_IMPACT;

	// Engines
	public static final ModConfigSpec.DoubleValue ENGINE_DIESEL_RPM;
	public static final ModConfigSpec.DoubleValue ENGINE_DIESEL_SU;
	public static final ModConfigSpec.DoubleValue ENGINE_DIESEL_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue ENGINE_DIESEL_PORTABLE_SU;
	public static final ModConfigSpec.DoubleValue ENGINE_DIESEL_PORTABLE_CONSUMPTION;

	public static final ModConfigSpec.DoubleValue ENGINE_HEAVY_FUEL_OIL_RPM;
	public static final ModConfigSpec.DoubleValue ENGINE_HEAVY_FUEL_OIL_SU;
	public static final ModConfigSpec.DoubleValue ENGINE_HEAVY_FUEL_OIL_CONSUMPTION;

	public static final ModConfigSpec.DoubleValue ENGINE_GASOLINE_RPM;
	public static final ModConfigSpec.DoubleValue ENGINE_GASOLINE_SU;
	public static final ModConfigSpec.DoubleValue ENGINE_GASOLINE_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue ENGINE_GASOLINE_PORTABLE_SU;
	public static final ModConfigSpec.DoubleValue ENGINE_GASOLINE_PORTABLE_CONSUMPTION;

	public static final ModConfigSpec.DoubleValue ENGINE_LPG_RPM;
	public static final ModConfigSpec.DoubleValue ENGINE_LPG_SU;
	public static final ModConfigSpec.DoubleValue ENGINE_LPG_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue ENGINE_LPG_PORTABLE_SU;
	public static final ModConfigSpec.DoubleValue ENGINE_LPG_PORTABLE_CONSUMPTION;

	static{
		BUILDER.comment("Dif server config (automatically synced from server to client).");

		BUILDER.push("GeneralSettings");
		SPACE_SCAFFOLDING_LIFE_TIME=BUILDER.defineInRange("space_scaffolding_life_time",300,1,MAX);
		MEGA_TORCH_RADIUS=BUILDER.defineInRange("mega_torch_radius",128,32,8192);
		NANO_GLASS_MAX_SPREAD=BUILDER.defineInRange("nano_glass_max_spread",128,1,MAX);
		JETPACK_CAPACITY=BUILDER.defineInRange("jetpack_capacity",16000,1000,MAX);
		ELECTRUM_DEFORESTER_MAX_LOGS=BUILDER.defineInRange("electrum_deforester_max_logs",128,1,MAX);
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

		BUILDER.push("FastRails");
		FAST_RAIL_TOP_SPEED=BUILDER.defineInRange("fast_rail_top_speed",1.2D,0.1D,MAX);
		FAST_POWERED_RAIL_ACCELERATION=BUILDER.defineInRange("fast_powered_rail_acceleration",0.5D,0.1D,MAX);
		BUILDER.pop();

		BUILDER.push("PortalGun");
		PORTAL_GUN_MAX_DURABILITY=BUILDER.defineInRange("portal_gun_max_durability",24,1,MAX);
		PORTAL_GUN_ENERGY_PER_PEARL=BUILDER.defineInRange("portal_gun_energy_per_pearl",4,1,MAX);
		PORTAL_MAX_DISTANCE=BUILDER.defineInRange("portal_max_distance",512,16,MAX);
		PORTAL_ALLOW_ENTITIES=BUILDER.define("portal_allow_entities",true);
		BUILDER.pop();

		BUILDER.push("Quarry");
		QUARRY_STRESS_IMPACT=BUILDER.comment("Create Kinetic Stress Impact of Quarry (SU consumed per 1 RPM). Default: 128.0").defineInRange("quarry_stress_impact",128.0D,0.0D,DOUBLE_MAX);
		BUILDER.pop();

		BUILDER.push("Engines");
		BUILDER.push("Diesel");
		ENGINE_DIESEL_RPM=BUILDER.defineInRange("engine_diesel_rpm",120.0D,0.0D,DOUBLE_MAX);
		ENGINE_DIESEL_SU=BUILDER.defineInRange("engine_diesel_su",120.0D,0.0D,DOUBLE_MAX);
		ENGINE_DIESEL_CONSUMPTION=BUILDER.comment("Diesel consumption in mB per second (20 ticks). Default: 2.5 mB/s").defineInRange("engine_diesel_consumption",2.5D,0.0D,DOUBLE_MAX);
		ENGINE_DIESEL_PORTABLE_SU=BUILDER.comment("Diesel stress capacity for portable engine. Default: 90.0 SU").defineInRange("engine_diesel_portable_su",90.0D,0.0D,DOUBLE_MAX);
		ENGINE_DIESEL_PORTABLE_CONSUMPTION=BUILDER.comment("Diesel consumption for portable engine in mB per second (20 ticks). Default: 1.88 mB/s").defineInRange("engine_diesel_portable_consumption",1.88D,0.0D,DOUBLE_MAX);
		BUILDER.pop();

		BUILDER.push("HeavyFuelOil");
		ENGINE_HEAVY_FUEL_OIL_RPM=BUILDER.defineInRange("engine_heavy_fuel_oil_rpm",80.0D,0.0D,DOUBLE_MAX);
		ENGINE_HEAVY_FUEL_OIL_SU=BUILDER.defineInRange("engine_heavy_fuel_oil_su",264.0D,0.0D,DOUBLE_MAX);
		ENGINE_HEAVY_FUEL_OIL_CONSUMPTION=BUILDER.comment("Heavy Fuel Oil consumption in mB per second (20 ticks). Default: 3.5 mB/s").defineInRange("engine_heavy_fuel_oil_consumption",3.5D,0.0D,DOUBLE_MAX);
		BUILDER.pop();

		BUILDER.push("Gasoline");
		ENGINE_GASOLINE_RPM=BUILDER.defineInRange("engine_gasoline_rpm",210.0D,0.0D,DOUBLE_MAX);
		ENGINE_GASOLINE_SU=BUILDER.defineInRange("engine_gasoline_su",52.0D,0.0D,DOUBLE_MAX);
		ENGINE_GASOLINE_CONSUMPTION=BUILDER.comment("Gasoline consumption in mB per second (20 ticks). Default: 2.0 mB/s").defineInRange("engine_gasoline_consumption",2.0D,0.0D,DOUBLE_MAX);
		ENGINE_GASOLINE_PORTABLE_SU=BUILDER.comment("Gasoline stress capacity for portable engine. Default: 39.0 SU").defineInRange("engine_gasoline_portable_su",39.0D,0.0D,DOUBLE_MAX);
		ENGINE_GASOLINE_PORTABLE_CONSUMPTION=BUILDER.comment("Gasoline consumption for portable engine in mB per second (20 ticks). Default: 1.5 mB/s").defineInRange("engine_gasoline_portable_consumption",1.5D,0.0D,DOUBLE_MAX);
		BUILDER.pop();

		BUILDER.push("LPG");
		ENGINE_LPG_RPM=BUILDER.defineInRange("engine_lpg_rpm",160.0D,0.0D,DOUBLE_MAX);
		ENGINE_LPG_SU=BUILDER.defineInRange("engine_lpg_su",52.0D,0.0D,DOUBLE_MAX);
		ENGINE_LPG_CONSUMPTION=BUILDER.comment("LPG consumption in mB per second (20 ticks). Default: 1.5 mB/s").defineInRange("engine_lpg_consumption",1.5D,0.0D,DOUBLE_MAX);
		ENGINE_LPG_PORTABLE_SU=BUILDER.comment("LPG stress capacity for portable engine. Default: 39.0 SU").defineInRange("engine_lpg_portable_su",39.0D,0.0D,DOUBLE_MAX);
		ENGINE_LPG_PORTABLE_CONSUMPTION=BUILDER.comment("LPG consumption for portable engine in mB per second (20 ticks). Default: 1.13 mB/s").defineInRange("engine_lpg_portable_consumption",1.13D,0.0D,DOUBLE_MAX);
		BUILDER.pop();

		BUILDER.pop();
		SPEC=BUILDER.build();
	}
}
