package cz.maxtechnik.dif.util;

public enum FuelType{
	GASOLINE("gasoline"),
	DIESEL("diesel"),
	LPG("lpg"),
	HEAVY_FUEL_OIL("heavy_fuel_oil"),
	INVALID("");
	final String id;
	FuelType(String id){
		this.id=id;
	}
}
