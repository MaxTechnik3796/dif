package cz.maxtechnik.dif.util;

public enum HeatLevel{
	NONE(0),
	HEATED(10),
	SUPERHEATED(5);
	public final int speed;
	HeatLevel(int speed){
		this.speed=speed;
	}
}