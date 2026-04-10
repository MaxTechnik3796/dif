package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet pro přeřazení.
 * direction: +1 = řadit nahoru, -1 = řadit dolů.
 *
 * ═══ PRAVIDLA PŘEVODOVKY ═══════════════════════════════════════════════════
 *
 * Sekvence: R(-1) ↔ N(0) ↔ 1 ↔ 2 ↔ … ↔ 7  (nelze přeskakovat)
 *
 * Zpátečka (R = -1):
 *   → VYŽADUJE nulovou rychlost (< 0.5 km/h).
 *   → Pokud podmínka nesplněna → fallback na N.
 *
 * Upshift za jízdy (1↑2, 2↑3 atd.):
 *   → Seamless – krátký torque cut přes applyShiftCooldown().
 *
 * Downshift za jízdy (3↓2, 4↓3 atd.):
 *   → Rev protection: odmítne downshift pokud by RPM překročily omezovač.
 */
public class ShiftGearPacket {

    private final int direction; // +1 nebo -1

    public ShiftGearPacket(int direction) {
        this.direction = direction;
    }

    // ── Kodek ─────────────────────────────────────────────────────────────────
    public static ShiftGearPacket decode(FriendlyByteBuf buf) {
        return new ShiftGearPacket(buf.readByte());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(direction);
    }

    // ── Server handler ────────────────────────────────────────────────────────
    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            if (!(player.getVehicle() instanceof BaseCarEntity car)) return;
            if (!car.isEngineOn()) return;

            int current  = car.getCurrentGear();   // -1=R, 0=N, 1..n
            int maxGear  = car.getGearRatios().length;
            float speedKmh = car.getSpeedKmh();

            int newGear = current + direction;

            // ── Ohraničení sekvence ───────────────────────────────────────────
            if (newGear < -1)     newGear = -1;
            if (newGear > maxGear) newGear = maxGear;

            // ── Zpátečka: pouze při nulové rychlosti A s palivem ─────────────
            if (newGear == -1) {
                if (speedKmh > 0.5f || car.getFuelMb() <= 0.0f) {
                    newGear = 0; // Fallback na N – příliš rychle nebo bez paliva
                }
                // Do zpátečky se neaplikuje cooldown
            }

            // ── Downshift za jízdy (3↓2, 4↓3 atd.): rev protection ───────────
            else if (direction < 0 && current > 1 && newGear > 0) {
                float[] ratios = car.getGearRatios();
                float newRatio = ratios[newGear - 1];
                float rpmConv  = car.getMaxRPM() /
                        ((car.getMaxSpeedKmh() / 72.0f) * ratios[ratios.length - 1]);
                float estRPM = Math.abs(speedKmh / 72.0f) * newRatio * rpmConv;

                if (estRPM > car.getRedlineRPM() * 1.02f) {
                    // Řazení by přetočilo motor – odmítáme
                    return;
                }
            }

            // ── Aplikace nového stupně ────────────────────────────────────────
            if (newGear != current) {
                car.setCurrentGear(newGear);

                // Seamless torque cut jen při upshiftu za jízdy (1→2, 2→3 atd.)
                if (direction > 0 && newGear > 1) {
                    car.applyShiftCooldown();
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}