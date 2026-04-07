package cz.maxtechnik.dif.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final String MOD_ID = "dif"; // ZMĚŇ NA SVŮJ SKUTEČNÝ MOD ID

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ShiftGearPacket.class, ShiftGearPacket::encode, ShiftGearPacket::decode, ShiftGearPacket::handle);
    }
}