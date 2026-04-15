package cz.maxtechnik.dif.init.events;

import net.minecraft.core.BlockPos;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.simibubi.create.content.kinetics.KineticNetwork;

/**
 * Manages wireless shaft pairing and speed transfer.
 *
 * Lifecycle:
 *  - Shaft placed / loaded without NBT partner → seekPartner() every 20 ticks
 *  - Shaft loaded with NBT partner             → restorePair() on onLoad()
 *  - Shaft DESTROYED                           → onBlockDestroyed() → partner gets pendingUnpair flag
 *  - Shaft UNLOADED (not destroyed)            → onChunkUnloaded() → speed clears so receiver stops
 *
 * Speed transfer:
 *  - Source writes its speed via setSpeed(sourcePos, speed)
 *  - Receiver reads via getSpeed(sourcePos)  (sourcePos = partner's position)
 */
@Mod.EventBusSubscriber // registers the @SubscribeEvent below on the FORGE bus
public class ReinforcedNetworkManager {

    public static Field capField;
    public static Field stressField;

    static {
        try {
            capField = KineticNetwork.class.getDeclaredField("currentCapacity");
            capField.setAccessible(true);
            stressField = KineticNetwork.class.getDeclaredField("currentStress");
            stressField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Pairing tables ---

    // frequency → pos.asLong() of the single shaft that is still looking for a partner
    private static final Map<String, Long> seeking = new ConcurrentHashMap<>();

    // pos.asLong() ↔ partnerPos.asLong()  (both directions stored)
    private static final Map<Long, Long> pairs = new ConcurrentHashMap<>();

    // --- Data tables ---
    private static final Map<Long, Float> speeds     = new ConcurrentHashMap<>();
    private static final Map<Long, Float> capacities = new ConcurrentHashMap<>();
    private static final Map<Long, Float> stresses   = new ConcurrentHashMap<>();

    // --- Destruction notification ---
    private static final Set<Long> pendingUnpair = ConcurrentHashMap.newKeySet();

    // ==========================================================================
    //  Pairing API
    // ==========================================================================

    /**
     * Called when a shaft is placed / loaded and has no stored partnerPos in NBT.
     * Runs at most every 20 ticks per shaft.
     *
     * @return the partner's BlockPos if a match was just found, null if still waiting.
     */
    public static BlockPos seekPartner(String frequency, BlockPos myPos) {
        if (frequency == null || myPos == null) return null;
        long myLong = myPos.asLong();

        Long existing = seeking.get(frequency);
        if (existing != null && !existing.equals(myLong)) {
            // Found a waiting shaft on the same frequency → form a pair
            pairs.put(myLong, existing);
            pairs.put(existing, myLong);
            seeking.remove(frequency);
            return BlockPos.of(existing);
        }
        // No partner yet – register ourselves as waiting
        seeking.put(frequency, myLong);
        return null;
    }

    /**
     * Call this in BlockEntity.onLoad() when the shaft already has a partnerPos from NBT.
     * Restores the in-memory pairs map after a world restart so destruction notifications work.
     */
    public static void restorePair(BlockPos a, BlockPos b) {
        if (a == null || b == null) return;
        pairs.putIfAbsent(a.asLong(), b.asLong());
        pairs.putIfAbsent(b.asLong(), a.asLong());
    }

    /**
     * Returns the paired partner position from the in-memory map, or null.
     * Useful to detect "someone paired with me" between seek ticks.
     */
    public static BlockPos getPartner(BlockPos pos) {
        if (pos == null) return null;
        Long l = pairs.get(pos.asLong());
        return l != null ? BlockPos.of(l) : null;
    }

    // ==========================================================================
    //  Destruction / unload cleanup
    // ==========================================================================

    /**
     * Called from Block.onRemove() – the block was DESTROYED (not just unloaded).
     * Puts the partner into pendingUnpair so it detects the break on its next tick.
     */
    public static void onBlockDestroyed(String frequency, BlockPos pos) {
        if (pos == null) return;
        long myLong = pos.asLong();

        // Remove from seeking queue if we were still looking
        seeking.remove(frequency, myLong);

        // Notify partner
        Long partnerLong = pairs.remove(myLong);
        if (partnerLong != null) {
            pairs.remove(partnerLong);
            pendingUnpair.add(partnerLong);
        }

        // Clean up data
        speeds.remove(myLong);
        capacities.remove(myLong);
        stresses.remove(myLong);
        pendingUnpair.remove(myLong);
    }

    /**
     * Poll whether this shaft's partner was destroyed.
     * Returns true (once!) if the partner was destroyed since last call.
     */
    public static boolean pollPartnerDestroyed(BlockPos pos) {
        if (pos == null) return false;
        return pendingUnpair.remove(pos.asLong());
    }

    /**
     * Called from BlockEntity.onChunkUnloaded() when the chunk is UNLOADED (not destroyed).
     * Clears speed so the receiver stops – same as if you physically removed a generator.
     * The pair bond in NBT is kept intact.
     */
    public static void onChunkUnloaded(String frequency, BlockPos pos) {
        if (pos == null) return;
        long myLong = pos.asLong();
        seeking.remove(frequency, myLong);
        speeds.remove(myLong);
        capacities.remove(myLong);
        stresses.remove(myLong);
        // do NOT touch pairs – the bond survives unload
    }

    public static void breakPair(BlockPos a, BlockPos b) {
        if (a != null) {
            pairs.remove(a.asLong());
            speeds.remove(a.asLong());
            capacities.remove(a.asLong());
            stresses.remove(a.asLong());
        }
        if (b != null) {
            pairs.remove(b.asLong());
            speeds.remove(b.asLong());
            capacities.remove(b.asLong());
            stresses.remove(b.asLong());
        }
    }

    // ==========================================================================
    //  Data API
    // ==========================================================================

    /**
     * Sets the speed broadcasted by a transmitter shaft.
     * Called every tick when the transmitter's speed changes.
     */
    public static void setSpeed(BlockPos pos, float speed) {
        if (pos == null) return;
        speeds.put(pos.asLong(), speed);
    }

    public static void updateStats(BlockPos pos, float speed, float capacity, float stress) {
        if (pos == null) return;
        long l = pos.asLong();
        speeds.put(l, speed);
        capacities.put(l, capacity);
        stresses.put(l, stress);
    }

    public static float getSpeed(BlockPos sourcePos) {
        return sourcePos == null ? 0f : speeds.getOrDefault(sourcePos.asLong(), 0f);
    }

    public static float getCapacity(BlockPos sourcePos) {
        return sourcePos == null ? 0f : capacities.getOrDefault(sourcePos.asLong(), 0f);
    }

    public static float getStress(BlockPos sourcePos) {
        return sourcePos == null ? 0f : stresses.getOrDefault(sourcePos.asLong(), 0f);
    }

    // ==========================================================================
    //  World lifecycle – clear static state on server stop
    // ==========================================================================

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        seeking.clear();
        pairs.clear();
        speeds.clear();
        capacities.clear();
        stresses.clear();
        pendingUnpair.clear();
    }
}