package cz.maxtechnik.dif.block.industrial.entity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import cz.maxtechnik.dif.init.events.ReinforcedNetworkManager;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Wireless shaft block entity. Two ReinforcedShaftBlockEntities on the same
 * frequency pair up and transfer kinetic rotation wirelessly.
 *
 * Role detection is AUTOMATIC:
 *   - If this shaft has a kinetic source (connected to a real generator/shaft),
 *     it is the TRANSMITTER and broadcasts its network speed.
 *   - If this shaft has NO kinetic source, it is the RECEIVER and generates
 *     rotation from the partner's broadcasted speed.
 */
public class ReinforcedShaftBlockEntity extends GeneratingKineticBlockEntity {

    // --- Persistent state (saved to NBT) ---
    public String frequency = "default_link";

    /** Position of our paired partner. Null = not yet paired. Saved in NBT. */
    @Nullable
    public BlockPos partnerPos = null;

    // --- Runtime state (not saved) ---
    private float lastTransmittedSpeed = 0f;
    private float lastReceivedSpeed = 0f;

    /** Counts down; when ≤ 0 we attempt seekPartner(). Reset to SEARCH_INTERVAL. */
    private int searchCooldown = 5; // search soon after first load

    /** Counts down; when ≤ 0 we do an expensive chunk-loaded verification of partner. */
    private int verifyCooldown = 0;

    // --- Tuning constants ---
    private static final int SEARCH_INTERVAL = 20;   // ticks between seek attempts when unpaired
    private static final int VERIFY_INTERVAL = 80;   // ticks between partner-existence checks
    private static final float CAPACITY_SU   = 2048f; // SU capacity the receiver provides

    // ==========================================================================
    //  Construction
    // ==========================================================================

    public ReinforcedShaftBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.REINFORCED_SHAFT.get(), pos, state);
    }

    // ==========================================================================
    //  Auto role detection
    // ==========================================================================

    /**
     * A shaft is a transmitter if it receives rotation from an external kinetic
     * source (a generator, another shaft, etc.) — i.e. {@code hasSource()} is true.
     * When the shaft is the receiver it has no external source because IT IS the
     * generator for its local network.
     */
    public boolean isTransmitter() {
        return hasSource();
    }

    // ==========================================================================
    //  Load / unload hooks
    // ==========================================================================

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null || level.isClientSide) return;

        if (partnerPos != null) {
            // We survived a world restart with a known partner – restore the in-memory pair map
            // so that destruction notifications work again.
            ReinforcedNetworkManager.restorePair(worldPosition, partnerPos);
            // If we're a transmitter, broadcast our speed immediately
            if (isTransmitter()) {
                ReinforcedNetworkManager.setSpeed(worldPosition, getSpeed());
            }
        }
        // If partnerPos == null we will start seeking on first tick via searchCooldown
    }

    /**
     * Forge hook – called ONLY when the chunk is unloaded, NOT on block destruction.
     * Clears speed so the paired receiver stops, but keeps the pair bond in NBT.
     */
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            ReinforcedNetworkManager.onChunkUnloaded(frequency, worldPosition);
        }
    }

    /**
     * Called exclusively from ReinforcedShaftBlock.onRemove() when the block is DESTROYED.
     * Must run BEFORE super.onRemove() removes the block entity from the world.
     */
    public void onDestroyed() {
        ReinforcedNetworkManager.onBlockDestroyed(frequency, worldPosition);
    }

    // ==========================================================================
    //  Main tick
    // ==========================================================================

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        // --- 1. Check if our partner was destroyed ---
        if (ReinforcedNetworkManager.pollPartnerDestroyed(worldPosition)) {
            unpairSelf();
            return;
        }

        // --- 2. Not paired yet → seek ---
        if (partnerPos == null) {
            handleUnpairedTick();
            return;
        }

        // --- 3. Paired – periodic partner existence verification ---
        if (--verifyCooldown <= 0) {
            verifyCooldown = VERIFY_INTERVAL;
            if (!verifyPartner()) return; // partner vanished, we already unpaired
        }

        // --- 4. Speed sync ---
        handleSpeedSync();
    }

    // ==========================================================================
    //  Tick helpers
    // ==========================================================================

    private void handleUnpairedTick() {
        // Check if someone else already paired with us (they called seekPartner first)
        BlockPos networkPartner = ReinforcedNetworkManager.getPartner(worldPosition);
        if (networkPartner != null) {
            applyPair(networkPartner);
            return;
        }

        // Throttled seek
        if (--searchCooldown <= 0) {
            searchCooldown = SEARCH_INTERVAL;
            BlockPos found = ReinforcedNetworkManager.seekPartner(frequency, worldPosition);
            if (found != null) {
                applyPair(found);
            }
        }
    }

    private void handleSpeedSync() {
        if (isTransmitter()) {
            // I have a real kinetic source → broadcast my network speed
            float currentSpeed = getSpeed();
            if (currentSpeed != lastTransmittedSpeed) {
                lastTransmittedSpeed = currentSpeed;
                ReinforcedNetworkManager.setSpeed(worldPosition, currentSpeed);
            }
        } else {
            // I have no local source → receive wirelessly and generate rotation
            float received = ReinforcedNetworkManager.getSpeed(partnerPos);
            if (received != lastReceivedSpeed) {
                lastReceivedSpeed = received;
                updateGeneratedRotation();
            }
        }
    }

    /**
     * Checks that partnerPos still holds a ReinforcedShaftBlockEntity with the same frequency,
     * but only when that chunk is actually loaded (avoids false-positives for unloaded chunks).
     *
     * @return false if the partner was found to be gone and we unpaired.
     */
    private boolean verifyPartner() {
        if (!level.isLoaded(partnerPos)) {
            // Chunk is not loaded – we can't tell if block exists; assume it still does.
            return true;
        }
        if (level.getBlockEntity(partnerPos) instanceof ReinforcedShaftBlockEntity partnerBE
                && partnerBE.frequency.equals(frequency)) {
            return true; // all good
        }
        // Partner is loaded but doesn't exist or changed frequency → clean break
        ReinforcedNetworkManager.breakPair(worldPosition, partnerPos);
        unpairSelf();
        return false;
    }

    // ==========================================================================
    //  Pair management helpers
    // ==========================================================================

    private void applyPair(BlockPos partner) {
        partnerPos = partner;
        verifyCooldown = VERIFY_INTERVAL;
        lastTransmittedSpeed = 0f;
        lastReceivedSpeed = 0f;
        setChanged();
        // Immediately sync speed
        if (isTransmitter()) {
            ReinforcedNetworkManager.setSpeed(worldPosition, getSpeed());
        } else {
            updateGeneratedRotation();
        }
    }

    private void unpairSelf() {
        partnerPos = null;
        lastTransmittedSpeed = 0f;
        lastReceivedSpeed = 0f;
        searchCooldown = SEARCH_INTERVAL; // start searching again
        // Stop generating if we were a receiver
        updateGeneratedRotation();
        setChanged();
    }

    /**
     * Called from the Block when the player changes the frequency via sneak+right-click.
     * Resets the pairing.
     */
    public void changeFrequency(String newFrequency) {
        if (newFrequency.equals(frequency)) return;
        // Leave old pair / seeking slot
        if (partnerPos != null) {
            ReinforcedNetworkManager.onBlockDestroyed(frequency, worldPosition); // notifies old partner
        } else {
            ReinforcedNetworkManager.onChunkUnloaded(frequency, worldPosition);
        }
        frequency = newFrequency;
        partnerPos = null;
        lastTransmittedSpeed = 0f;
        lastReceivedSpeed = 0f;
        searchCooldown = 5;
        updateGeneratedRotation();
        setChanged();
    }

    // ==========================================================================
    //  Create kinetics overrides
    // ==========================================================================

    /**
     * Only generate speed when we are the RECEIVER (no external kinetic source)
     * and we have a partner transmitting to us.
     * When we are the transmitter (hasSource() == true), return 0 so we don't
     * conflict with the real generator in our local network.
     */
    @Override
    public float getGeneratedSpeed() {
        if (!isTransmitter() && partnerPos != null) {
            return ReinforcedNetworkManager.getSpeed(partnerPos);
        }
        return 0f;
    }

    /**
     * CRITICAL FIX: Transmitter must NOT call super.updateGeneratedRotation() because
     * that would register it as a 0-RPM generator in the same Create network that already
     * has a real generator → speed conflict → enormous overstress on both sides.
     *
     * Only the receiver (no external source) should register itself as a generator.
     */
    @Override
    public void updateGeneratedRotation() {
        if (!isTransmitter()) {
            super.updateGeneratedRotation();
        }
        // Transmitters are powered externally – do nothing, let Create handle it normally.
    }

    /**
     * The receiver acts as a kinetic source for its local network.
     * We provide a large fixed capacity so the connected machines don't overstress.
     * Expressed in SU/RPM so that actual SU = capacity × speed.
     *
     * Guard against speed=0 at startup (before partner sends speed) to avoid
     * returning an astronomically large value that causes stress bugs.
     */
    @Override
    public float calculateAddedStressCapacity() {
        if (isTransmitter()) return 0f;
        if (partnerPos == null) return 0f;
        float speed = Math.abs(ReinforcedNetworkManager.getSpeed(partnerPos));
        return speed > 0.1f ? CAPACITY_SU / speed : 0f;
    }

    // ==========================================================================
    //  NBT
    // ==========================================================================

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putString("Frequency", frequency);
        if (partnerPos != null) {
            compound.put("PartnerPos", NbtUtils.writeBlockPos(partnerPos));
        }
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        if (compound.contains("Frequency")) frequency = compound.getString("Frequency");
        partnerPos = compound.contains("PartnerPos")
                ? NbtUtils.readBlockPos(compound.getCompound("PartnerPos"))
                : null;
    }
}