package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.function.Supplier;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final String MOD_ID = "dif"; // !!! MUSÍ BÝT MALÝMI PÍSMENY !!!

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "main"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ShiftGearPacket.class, ShiftGearPacket::encode, ShiftGearPacket::decode, ShiftGearPacket::handle);
        CHANNEL.registerMessage(id++, SyncCarPositionPacket.class, SyncCarPositionPacket::encode, SyncCarPositionPacket::decode, SyncCarPositionPacket::handle);
    }

    public record ShiftGearPacket(int direction) {
        public static ShiftGearPacket decode(FriendlyByteBuf buf) { return new ShiftGearPacket(buf.readByte()); }
        public void encode(FriendlyByteBuf buf) { buf.writeByte(direction); }
        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer p = ctx.get().getSender();
                if (p == null || !(p.getVehicle() instanceof BaseCarEntity c) || !c.isEngineOn()) return;
                int cur = c.getCurrentGear();
                int max = c.getGearRatios().length;
                int newGear = Math.max(-1, Math.min(max, cur + direction));
                if (newGear == -1 && (c.getSpeedKmh() > 0.5f || c.getFuelMb() <= 0f)) newGear = 0;
                else if (direction < 0 && cur > 1 && newGear > 0) {
                    float rpmC = c.getMaxRPM() / ((c.getMaxSpeedKmh() / 72f) * c.getGearRatios()[max - 1]);
                    if ((c.getSpeedKmh() / 72f) * c.getGearRatios()[newGear - 1] * rpmC > c.getRedlineRPM() * 1.02f) return;
                }
                if (newGear != cur) {
                    c.setCurrentGear(newGear);
                    if (direction > 0 && newGear > 1) c.applyShiftCooldown();
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public record SyncCarPositionPacket(int entityId, double x, double y, double z, float yRot, float velocity) {
        public static SyncCarPositionPacket decode(FriendlyByteBuf buf) {
            return new SyncCarPositionPacket(buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat());
        }
        public void encode(FriendlyByteBuf buf) {
            buf.writeInt(entityId); buf.writeDouble(x); buf.writeDouble(y); buf.writeDouble(z); buf.writeFloat(yRot); buf.writeFloat(velocity);
        }
        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> applyMove(this));
            ctx.get().setPacketHandled(true);
        }
        @OnlyIn(Dist.CLIENT)
        private static void applyMove(SyncCarPositionPacket p) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.level.getEntity(p.entityId) instanceof BaseCarEntity c) {
                if (mc.player == null || mc.player.getVehicle() != c) c.lerpTo(p.x, p.y, p.z, p.yRot, c.getXRot(), 3, true);
                c.setVelocityFromPacket(p.velocity);
            }
        }
    }
}