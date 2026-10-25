package cz.maxtechnik.dif.config;

import net.neoforged.neoforge.common.ModConfigSpec;
public class DifModServerConfig{
	private static final ModConfigSpec.Builder BUILDER=new ModConfigSpec.Builder();
	public static final ModConfigSpec SPEC;
	public static final int MAX=Integer.MAX_VALUE;
	public static final double DOUBLE_MAX=1_000_000D;
	//General:
	public static final ModConfigSpec.IntValue MEGA_TORCH_RADIUS;
	public static final ModConfigSpec.IntValue NANO_GLASS_MAX_SPREAD;
	public static final ModConfigSpec.IntValue JETPACK_CAPACITY;
	public static final ModConfigSpec.IntValue ELECTRUM_DEFORESTER_MAX_LOGS;
	public static final ModConfigSpec.IntValue MITHRIL_FLUID_TANK_CAPACITY;
	public static final ModConfigSpec.DoubleValue QUARRY_STRESS_IMPACT;
	//Solar Panels:
	public static final ModConfigSpec.IntValue SOLAR_PANEL_00;
	public static final ModConfigSpec.IntValue SOLAR_PANEL_01;
	//Burning Generator:
	public static final ModConfigSpec.IntValue BURNING_GENERATOR_ENERGY_PER_TICK;
	public static final ModConfigSpec.IntValue BURNING_GENERATOR_MAX_ENERGY;
	public static final ModConfigSpec.IntValue BURNING_GENERATOR_MAX_EXTRACT;
	//Fast Rails:
	public static final ModConfigSpec.DoubleValue FAST_RAIL_TOP_SPEED;
	public static final ModConfigSpec.DoubleValue FAST_POWERED_RAIL_ACCELERATION;
	//Portal Gun:
	public static final ModConfigSpec.IntValue PORTAL_GUN_CAPACITY;
	public static final ModConfigSpec.IntValue PORTAL_MAX_DISTANCE;
	public static final ModConfigSpec.BooleanValue PORTAL_ALLOW_ENTITIES;
	//Engines:
	public static final ModConfigSpec.DoubleValue PORTABLE_ENGINE_GASOLINE_RPM;
	public static final ModConfigSpec.DoubleValue PORTABLE_ENGINE_GASOLINE_SU;
	public static final ModConfigSpec.DoubleValue PORTABLE_ENGINE_GASOLINE_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue PORTABLE_ENGINE_LPG_RPM;
	public static final ModConfigSpec.DoubleValue PORTABLE_ENGINE_LPG_SU;
	public static final ModConfigSpec.DoubleValue PORTABLE_ENGINE_LPG_CONSUMPTION;

	public static final ModConfigSpec.DoubleValue LARGE_ENGINE_GASOLINE_RPM;
	public static final ModConfigSpec.DoubleValue LARGE_ENGINE_GASOLINE_SU;
	public static final ModConfigSpec.DoubleValue LARGE_ENGINE_GASOLINE_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue LARGE_ENGINE_DIESEL_RPM;
	public static final ModConfigSpec.DoubleValue LARGE_ENGINE_DIESEL_SU;
	public static final ModConfigSpec.DoubleValue LARGE_ENGINE_DIESEL_CONSUMPTION;
	public static final ModConfigSpec.DoubleValue LARGE_ENGINE_HEAVY_FUEL_OIL_RPM;
	public static final ModConfigSpec.DoubleValue LARGE_ENGINE_HEAVY_FUEL_OIL_SU;
	public static final ModConfigSpec.DoubleValue LARGE_ENGINE_HEAVY_FUEL_OIL_CONSUMPTION;
	//Nuke:
	public static final ModConfigSpec.IntValue NUKE_RADIUS;
	public static final ModConfigSpec.IntValue NUKE_DAMAGE_RADIUS;
	public static final ModConfigSpec.IntValue NUKE_BLOCKS_PER_TICK;
	public static final ModConfigSpec.DoubleValue NUKE_MAX_DESTROYABLE_RESISTANCE;
	static{
		BUILDER.push("GeneralSettings");
		MEGA_TORCH_RADIUS=BUILDER.defineInRange("mega_torch_radius",128,32,8192);
		NANO_GLASS_MAX_SPREAD=BUILDER.defineInRange("nano_glass_max_spread",128,1,MAX);
		JETPACK_CAPACITY=BUILDER.defineInRange("jetpack_capacity",16000,1000,MAX);
		ELECTRUM_DEFORESTER_MAX_LOGS=BUILDER.defineInRange("electrum_deforester_max_logs",128,1,MAX);
		MITHRIL_FLUID_TANK_CAPACITY=BUILDER.comment("The amount of liquid a Mithril Fluid Tank can hold per block in buckets (Default: 32 = 4x Create copper tank).").defineInRange("mithril_fluid_tank_capacity",32,1,MAX);
		QUARRY_STRESS_IMPACT=BUILDER.comment("Create Kinetic Stress Impact of Quarry (SU consumed per 1 RPM). Default: 128.0").defineInRange("quarry_stress_impact",128.0D,0.0D,DOUBLE_MAX);
		BUILDER.pop();
		BUILDER.push("SolarPanelSettings");
		SOLAR_PANEL_00=BUILDER.defineInRange("solar_panel_00",2,0,MAX);
		SOLAR_PANEL_01=BUILDER.defineInRange("solar_panel_01",4,0,MAX);
		BUILDER.pop();
		BUILDER.push("BurningGenerator");
		BURNING_GENERATOR_ENERGY_PER_TICK=BUILDER.defineInRange("burning_generator_energy_per_tick",20,0,MAX);
		BURNING_GENERATOR_MAX_ENERGY=BUILDER.defineInRange("burning_generator_max_energy",32000,0,MAX);
		BURNING_GENERATOR_MAX_EXTRACT=BUILDER.defineInRange("burning_generator_max_extract",200,0,MAX);
		BUILDER.pop();
		BUILDER.push("FastRails");
		FAST_RAIL_TOP_SPEED=BUILDER.defineInRange("fast_rail_top_speed",1.2D,0.1D,MAX);
		FAST_POWERED_RAIL_ACCELERATION=BUILDER.defineInRange("fast_powered_rail_acceleration",0.3D,0.01D,MAX);
		BUILDER.pop();
		BUILDER.push("PortalGun");
		PORTAL_GUN_CAPACITY=BUILDER.defineInRange("portal_gun_capacity",24,1,MAX);
		PORTAL_MAX_DISTANCE=BUILDER.defineInRange("portal_max_distance",512,16,MAX);
		PORTAL_ALLOW_ENTITIES=BUILDER.define("portal_allow_entities",true);
		BUILDER.pop();
		BUILDER.push("PortableEngine");
		PORTABLE_ENGINE_GASOLINE_RPM=BUILDER.defineInRange("gasoline_rpm",192D,0D,DOUBLE_MAX);
		PORTABLE_ENGINE_GASOLINE_SU=BUILDER.defineInRange("gasoline_su",74D,0D,DOUBLE_MAX);
		PORTABLE_ENGINE_GASOLINE_CONSUMPTION=BUILDER.defineInRange("gasoline_consumption",1.7D,0D,DOUBLE_MAX);
		PORTABLE_ENGINE_LPG_RPM=BUILDER.defineInRange("lpg_rpm",160D,0D,DOUBLE_MAX);
		PORTABLE_ENGINE_LPG_SU=BUILDER.defineInRange("lpg_su",68D,0D,DOUBLE_MAX);
		PORTABLE_ENGINE_LPG_CONSUMPTION=BUILDER.defineInRange("lpg_consumption",1.2D,0D,DOUBLE_MAX);
		BUILDER.pop();
		BUILDER.push("LargeEngine");
		LARGE_ENGINE_GASOLINE_RPM=BUILDER.defineInRange("gasoline_rpm",192D,0D,DOUBLE_MAX);
		LARGE_ENGINE_GASOLINE_SU=BUILDER.defineInRange("gasoline_su",86D,0D,DOUBLE_MAX);
		LARGE_ENGINE_GASOLINE_CONSUMPTION=BUILDER.defineInRange("gasoline_consumption",2.0D,0D,DOUBLE_MAX);
		LARGE_ENGINE_DIESEL_RPM=BUILDER.defineInRange("diesel_rpm",128D,0D,DOUBLE_MAX);
		LARGE_ENGINE_DIESEL_SU=BUILDER.defineInRange("diesel_su",160D,0D,DOUBLE_MAX);
		LARGE_ENGINE_DIESEL_CONSUMPTION=BUILDER.defineInRange("diesel_consumption",2.5D,0D,DOUBLE_MAX);
		LARGE_ENGINE_HEAVY_FUEL_OIL_RPM=BUILDER.defineInRange("heavy_fuel_oil_rpm",96D,0D,DOUBLE_MAX);
		LARGE_ENGINE_HEAVY_FUEL_OIL_SU=BUILDER.defineInRange("heavy_fuel_oil_su",292D,0D,DOUBLE_MAX);
		LARGE_ENGINE_HEAVY_FUEL_OIL_CONSUMPTION=BUILDER.defineInRange("heavy_fuel_oil_consumption",3.5D,0D,DOUBLE_MAX);
		BUILDER.pop();
		BUILDER.push("Nuke");
		NUKE_RADIUS=BUILDER.defineInRange("nuke_radius",96,16,512);
		NUKE_DAMAGE_RADIUS=BUILDER.defineInRange("nuke_damage_radius",128,16,1024);
		NUKE_BLOCKS_PER_TICK=BUILDER.defineInRange("nuke_blocks_per_tick",36000,1000,MAX);
		NUKE_MAX_DESTROYABLE_RESISTANCE=BUILDER.defineInRange("nuke_max_destroyable_resistance",1500D,0D,MAX);
		BUILDER.pop();
		SPEC=BUILDER.build();
	}
}
