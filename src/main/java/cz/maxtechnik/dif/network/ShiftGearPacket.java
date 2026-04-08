package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ShiftGearPacket {

    private final int direction; // +1 nebo -1

    public ShiftGearPacket(int direction) {
        this.direction = direction;
    }

    //Kodek
    public static ShiftGearPacket decode(FriendlyByteBuf buf) {
        return new ShiftGearPacket(buf.readByte());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(direction);
    }

    //Zpracování na serveru
    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            if (!(player.getVehicle() instanceof BaseCarEntity car)) return;
            if (!car.isEngineOn()) return;

            int current = car.getCurrentGear();
            int newGear = Math.max(1, Math.min(car.getGearRatios().length, current + direction));

            if (newGear != current) {
                car.setCurrentGear(newGear);
                car.applyShiftCooldown(); // torque cut při přeřazení
            }
        });
        ctx.setPacketHandled(true);
    }
}