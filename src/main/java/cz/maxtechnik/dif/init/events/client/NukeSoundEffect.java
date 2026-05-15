package cz.maxtechnik.dif.init.events.client;
 
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
 
public class NukeSoundEffect {
 
    // ── Konfigurace ───────────────────────────────────────────────────────────
    private static final double MAX_DISTANCE = 1024.0;
    private static final double BLOCKS_PER_TICK = 2.0; // 2 blocky = 1 tick delay
    private static final float  VOLUME_SCALE = 8.0f;   // Zvýšení hlasitosti aby to bylo slyšet na 1024b
 
    // { pitch, volume, delayTicks }
    private static final float[][] PHASES = {
            { 0.30f,  8.0f,  0 },   // rumble
            { 0.50f, 16.0f,  5 },   // hlavní boom
            { 0.40f, 12.0f,  8 },   // distant shockwave
            { 0.25f,  6.0f, 20 },   // dozvuk
    };
 
    private static final SoundEvent[] SOUNDS = {
            SoundEvents.GENERIC_EXPLODE.value(),
            SoundEvents.GENERIC_EXPLODE.value(),
            SoundEvents.LIGHTNING_BOLT_THUNDER,
            SoundEvents.LIGHTNING_BOLT_THUNDER,
    };
 
    // ── Hlavní metoda ─────────────────────────────────────────────────────────
    public static void play(ServerLevel level, double x, double y, double z) {
        long currentTick = level.getServer().getTickCount();
 
        for (ServerPlayer player : level.getPlayers(p -> p.distanceToSqr(x, y, z) < MAX_DISTANCE * MAX_DISTANCE)) {
            double distance = Math.sqrt(player.distanceToSqr(x, y, z));
            int travelDelay = (int) (distance / BLOCKS_PER_TICK);
 
            for (int i = 0; i < PHASES.length; i++) {
                float pitch      = PHASES[i][0];
                float volume     = PHASES[i][1] * VOLUME_SCALE;
                int   phaseDelay = (int) PHASES[i][2];
                SoundEvent sound = SOUNDS[i];
                int totalDelay   = travelDelay + phaseDelay;
 
                level.getServer().tell(new TickTask(
                        (int)(currentTick + totalDelay),
                        () -> {
                            if (player.level() == level && !player.isRemoved()) {
                                player.connection.send(new ClientboundSoundPacket(
                                        Holder.direct(sound),
                                        SoundSource.BLOCKS,
                                        x, y, z,
                                        volume, pitch,
                                        level.getRandom().nextLong()
                                ));
                            }
                        }
                ));
            }
        }
    }
}