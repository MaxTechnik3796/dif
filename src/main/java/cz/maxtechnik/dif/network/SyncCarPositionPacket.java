package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Paket odesílaný ze SERVERU → KLIENTOVI každý tick pro vozidla.
 * Opravuje desynchronizaci polohy (auto "skáče" při zastavení nebo po vystoupení).
 *
 * Registrace: DifMod.commonSetup → addNetworkMessage(SyncCarPositionPacket.class, ...)
 * Odesílání:  BaseCarEntity.tick() na serveru →
 *             PacketDistributor.TRACKING_ENTITY_AND_SELF
 */
public class SyncCarPositionPacket {

    private final int   entityId;
    private final double x, y, z;
    private final float yRot;
    private final float velocity; // interní fyzikální rychlost (b/t)

    public SyncCarPositionPacket(int entityId, double x, double y, double z,
                                 float yRot, float velocity) {
        this.entityId = entityId;
        this.x        = x;
        this.y        = y;
        this.z        = z;
        this.yRot     = yRot;
        this.velocity = velocity;
    }

    // ── Kodek ────────────────────────────────────────────────────────────────

    public static SyncCarPositionPacket decode(FriendlyByteBuf buf) {
        return new SyncCarPositionPacket(
                buf.readInt(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(yRot);
        buf.writeFloat(velocity);
    }

    // ── Zpracování na klientovi ───────────────────────────────────────────────

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(this::applyOnClient);
        ctx.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void applyOnClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof BaseCarEntity car)) return;

        // Hráč, který auto řídí, opravuje pozici sám (predikce klienta).
        // Všichni ostatní (spoluhráči, diváci) dostanou autoritativní polohu ze serveru.
        boolean isLocalDriver = mc.player != null && mc.player.getVehicle() == car;
        if (!isLocalDriver) {
            car.lerpTo(x, y, z, yRot, car.getXRot(), 3, true);
        }

        // Rychlost vždy synchronizujeme – HUD tachometr a RPM počítají z ní.
        car.setVelocityFromPacket(velocity);
    }
}