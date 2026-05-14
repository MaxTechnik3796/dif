package cz.maxtechnik.dif.entity;

import cz.maxtechnik.dif.init.other.DifModParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class NuclearWaveEntity extends Entity {

    // ── Konstanty ─────────────────────────────────────────────────────────
    private static final double WAVE_SPEED        = 3.0;
    private static final int    WAVE_MAX_RADIUS   = 128;
    private static final int    PARTICLES_PER_TICK = 6; // cca 8 * 42 ticků = ~336 částic celkem
    private static final double SEND_RADIUS       = 512.0;

    private static final EntityDataAccessor<Integer> DATA_TICK =
            SynchedEntityData.defineId(NuclearWaveEntity.class, EntityDataSerializers.INT);

    private double waveRadius = 0;

    public NuclearWaveEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_TICK, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel sl)) return;

        waveRadius += WAVE_SPEED;
        if (waveRadius > WAVE_MAX_RADIUS) {
            this.discard();
            return;
        }

        double ox = getX(), oy = getY(), oz = getZ();

        // Rázová vlna: spawnujeme náhodné částice na aktuálním kruhu (waveRadius)
        // Spawnuje se jich cca 8 za tick, takže za celou dobu (42 ticků) jich vznikne okolo 336
        for (int i = 0; i < PARTICLES_PER_TICK; i++) {
            double angle = Math.random() * Math.PI * 2;
            double px = ox + Math.cos(angle) * waveRadius;
            double pz = oz + Math.sin(angle) * waveRadius;

            // Všechny částice jsou přesně na 'oy' (jedné vrstvě) a rychlost je 0.0f
            sendParticle(sl, cz.maxtechnik.dif.init.other.DifModParticles.HUGE_SMOKE.get(), px, oy, pz, 0.0f);
        }
    }

    private void sendParticle(ServerLevel serverLevel, net.minecraft.core.particles.SimpleParticleType particleType, double x, double y, double z, float speed){
        // Posíláme paket na 512 bloků bez rychlosti
        for(net.minecraft.server.level.ServerPlayer player: serverLevel.getPlayers(p->p.distanceToSqr(x,y,z)<SEND_RADIUS*SEND_RADIUS))
            player.connection.send(
                    new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(particleType,true,x,y,z,0F,0F,0F,speed,0)
            );
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        waveRadius = tag.getDouble("WaveRadius");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("WaveRadius", waveRadius);
    }

    @Override public boolean isAttackable() { return false; }
    @Override public boolean isPickable()   { return false; }
}