package cz.maxtechnik.dif.init.events;

public class QuarryStats {
    public static int STONE_HEAD_DP_REQ = 5;
    public static int IRON_HEAD_DP_REQ = 10;
    public static int DIAMOND_HEAD_DP_REQ = 25;

    public static int IRON_ENGINE_DP_GEN = 15;
    public static int IRON_ENGINE_FE_COST = 20;

    public static int GOLD_ENGINE_DP_GEN = 50;
    public static int GOLD_ENGINE_FE_COST = 100;

    public static int DIAMOND_ENGINE_DP_GEN = 150;
    public static int DIAMOND_ENGINE_FE_COST = 500;

    public static int MAX_ACTIVE_DP = 200; // Mximální použitelná rychlost (Progress za tick)
    public static double DUP_ENGINE_PENALTY = 1.5; // Penalta x1.5 FE spotřeby navíc za dvě stejné položky
    
    public static int QUARRY_ENERGY_CAPACITY = 100000;
    public static int QUARRY_ENERGY_INPUT = 5000;
}
