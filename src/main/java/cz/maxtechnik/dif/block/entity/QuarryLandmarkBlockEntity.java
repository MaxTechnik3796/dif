package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class QuarryLandmarkBlockEntity extends BlockEntity {

    public static final int MAX_SEARCH = QuarryBlockEntity.MAX_AREA_SIDE;

    private final List<BlockPos> partners = new ArrayList<>(2);
    private boolean  formed      = false;
    private int      formedSizeX = 0;
    private int      formedSizeZ = 0;
    private int      formedHalfX = 0;
    private int      formedHalfZ = 0;
    @Nullable private BlockPos formedCenter = null;

    public QuarryLandmarkBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.QUARRY_LANDMARK.get(), pos, state);
    }

    // ── Pravý klik ───────────────────────────────────────────────────────────

    public void onRightClick(Player player) {
        if (level == null || level.isClientSide) return;

        // Ověř zda partneři stále existují
        if (formed && !partners.isEmpty()) {
            for (BlockPos p : partners) {
                if (!level.getBlockState(p).is(DifModBlocks.QUARRY_LANDMARK.get())) {
                    resetFormation();
                    break;
                }
            }
        }

        List<BlockPos> found = scanLandmarks();
        int total = found.size() + 1;

        if (total == 1) {
            player.sendSystemMessage(Component.literal("§eNalezen §f1§e/§f3§e landmark. Chybí §f2§e."));
            return;
        }
        if (total == 2) {
            player.sendSystemMessage(Component.literal("§eNalezeny §f2§e/§f3§e landmarky. Chybí §f1§e."));
            return;
        }
        if (total > 3) {
            player.sendSystemMessage(Component.literal("§cV okolí je více než 3 landmarky – nelze určit oblast."));
            return;
        }

        List<BlockPos> all = new ArrayList<>(found);
        all.add(worldPosition);
        FormResult result = tryForm(all);

        if (result == null) {
            player.sendSystemMessage(Component.literal(
                    "§cLandmarky netvoří správný L-tvar nebo je vzdálenost mimo rozsah " +
                            "(min 3, max §f" + MAX_SEARCH + "§c bloků)."));
            return;
        }

        applyFormation(all, result);

        player.sendSystemMessage(Component.literal(
                "§aFormace: §f" + result.sizeX + "§ax§f" + result.sizeZ +
                        " §abloků. Polož Quarry ke kraji oblasti."));
    }

    // ── Scan ─────────────────────────────────────────────────────────────────

    private List<BlockPos> scanLandmarks() {
        List<BlockPos> found = new ArrayList<>(3);
        if (level == null) return found;
        int y = worldPosition.getY();
        for (int dx = -MAX_SEARCH; dx <= MAX_SEARCH && found.size() < 3; dx++) {
            for (int dz = -MAX_SEARCH; dz <= MAX_SEARCH && found.size() < 3; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos p = new BlockPos(worldPosition.getX() + dx, y, worldPosition.getZ() + dz);
                if (level.getBlockState(p).is(DifModBlocks.QUARRY_LANDMARK.get())) found.add(p);
            }
        }
        return found;
    }

    // ── Výpočet formace ──────────────────────────────────────────────────────

    @Nullable
    public static FormResult tryForm(List<BlockPos> all) {
        if (all.size() != 3) return null;
        BlockPos a = all.get(0), b = all.get(1), c = all.get(2);

        int minX = Math.min(a.getX(), Math.min(b.getX(), c.getX()));
        int maxX = Math.max(a.getX(), Math.max(b.getX(), c.getX()));
        int minZ = Math.min(a.getZ(), Math.min(b.getZ(), c.getZ()));
        int maxZ = Math.max(a.getZ(), Math.max(b.getZ(), c.getZ()));

        int totalX = maxX - minX;
        int totalZ = maxZ - minZ;

        if (totalX < 2 || totalZ < 2) return null;
        if (totalX > MAX_SEARCH || totalZ > MAX_SEARCH) return null;

        // L-tvar: jeden bod je roh
        if (!isLShape(a, b, c) && !isLShape(b, a, c) && !isLShape(c, a, b)) return null;

        int centerX = (minX + maxX) / 2;
        int centerZ = (minZ + maxZ) / 2;
        int halfX   = totalX / 2;
        int halfZ   = totalZ / 2;
        int mineX   = totalX; 
        int mineZ   = totalZ;

        return new FormResult(mineX, mineZ, halfX, halfZ,
                new BlockPos(centerX, a.getY(), centerZ));
    }

    private static boolean isLShape(BlockPos corner, BlockPos p1, BlockPos p2) {
        return (corner.getX() == p1.getX() && corner.getZ() == p2.getZ())
                || (corner.getX() == p2.getX() && corner.getZ() == p1.getZ());
    }

    // ── Aplikace formace ──────────────────────────────────────────────────────

    private void applyFormation(List<BlockPos> all, FormResult result) {
        if (level == null) return;
        for (BlockPos pos : all) {
            if (level.getBlockEntity(pos) instanceof QuarryLandmarkBlockEntity lbe) {
                lbe.partners.clear();
                for (BlockPos other : all) if (!other.equals(pos)) lbe.partners.add(other);
                lbe.formed       = true;
                lbe.formedSizeX  = result.sizeX;
                lbe.formedSizeZ  = result.sizeZ;
                lbe.formedCenter = result.center;
                lbe.formedHalfX  = result.halfX;
                lbe.formedHalfZ  = result.halfZ;
                lbe.setChanged();
                // Pošle update paket klientovi
                level.sendBlockUpdated(pos, lbe.getBlockState(), lbe.getBlockState(), 3);
            }
        }
    }

    // ── Reset formace ─────────────────────────────────────────────────────────

    private void resetFormation() {
        formed       = false;
        formedCenter = null;
        formedSizeX  = formedSizeZ = formedHalfX = formedHalfZ = 0;
        partners.clear();
        setChanged();
        if (level != null)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // ── Připojení k quarry ────────────────────────────────────────────────────

    // ── Připojení k quarry ────────────────────────────────────────────────────

    /**
     * Voláno z Quarry.onPlace – aplikuje tuto formaci na quarry na dané pozici
     * a zruší všechny landmarky (dropnou item).
     */
    public void applyToQuarry(Level level, BlockPos quarryPos) {
        if (!formed || formedCenter == null) return;
        if (!(level.getBlockEntity(quarryPos) instanceof QuarryBlockEntity qbe)) return;

        qbe.setLandmarkArea(formedHalfX, formedHalfZ, formedCenter);

        // Zruš všechny landmarky skupiny
        List<BlockPos> all = new ArrayList<>(partners);
        all.add(worldPosition);
        for (BlockPos lm : all) {
            if (level.getBlockState(lm).is(DifModBlocks.QUARRY_LANDMARK.get())) {
                net.minecraft.world.level.block.Block.popResource(level, lm,
                        new net.minecraft.world.item.ItemStack(
                                DifModBlocks.QUARRY_LANDMARK.get().asItem()));
                level.removeBlock(lm, false);
            }
        }
    }

    // ── Zničení → reset partnerů ──────────────────────────────────────────────

    public void onRemoved() {
        if (level == null || level.isClientSide || !formed) return;
        for (BlockPos p : new ArrayList<>(partners)) {
            if (level.getBlockEntity(p) instanceof QuarryLandmarkBlockEntity lbe) {
                lbe.formed       = false;
                lbe.formedCenter = null;
                lbe.formedSizeX  = lbe.formedSizeZ = lbe.formedHalfX = lbe.formedHalfZ = 0;
                lbe.partners.clear();
                lbe.setChanged();
                level.sendBlockUpdated(p, lbe.getBlockState(), lbe.getBlockState(), 3);
            }
        }
    }

    // ── Klient-side tracking pro renderer ────────────────────────────────────

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide) {
            updateClientRenderer();
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide) {
            cz.maxtechnik.dif.renderer.LandmarkOverlayRenderer.unregister(worldPosition);
        }
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        load(tag);
        updateClientRenderer();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) load(tag);
        updateClientRenderer();
    }

    private void updateClientRenderer() {
        if (level != null && level.isClientSide) {
            if (formed) {
                cz.maxtechnik.dif.renderer.LandmarkOverlayRenderer.register(this);
            } else {
                cz.maxtechnik.dif.renderer.LandmarkOverlayRenderer.unregister(worldPosition);
            }
        }
    }

    // ── Gettery ───────────────────────────────────────────────────────────────

    public boolean   isFormed()       { return formed; }
    public int       getFormedSizeX() { return formedSizeX; }
    public int       getFormedSizeZ() { return formedSizeZ; }
    public int       getFormedHalfX() { return formedHalfX; }
    public int       getFormedHalfZ() { return formedHalfZ; }
    @Nullable public BlockPos getFormedCenter() { return formedCenter; }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Formed",  formed);
        tag.putInt("FSX", formedSizeX);
        tag.putInt("FSZ", formedSizeZ);
        tag.putInt("FHX", formedHalfX);
        tag.putInt("FHZ", formedHalfZ);
        if (formedCenter != null) tag.put("FC", NbtUtils.writeBlockPos(formedCenter));
        ListTag list = new ListTag();
        for (BlockPos p : partners) list.add(NbtUtils.writeBlockPos(p));
        tag.put("Partners", list);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        formed      = tag.getBoolean("Formed");
        formedSizeX = tag.getInt("FSX");
        formedSizeZ = tag.getInt("FSZ");
        formedHalfX = tag.getInt("FHX");
        formedHalfZ = tag.getInt("FHZ");
        formedCenter = tag.contains("FC") ? NbtUtils.readBlockPos(tag.getCompound("FC")) : null;
        partners.clear();
        ListTag list = tag.getList("Partners", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) partners.add(NbtUtils.readBlockPos(list.getCompound(i)));
    }

    // getUpdateTag musí vracet plná data (stejná jako saveAdditional, BEZ super volání
    // které přidává x/y/z/id – ty do update tagu nepatří a způsobují problémy)
    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Formed",  formed);
        tag.putInt("FSX", formedSizeX);
        tag.putInt("FSZ", formedSizeZ);
        tag.putInt("FHX", formedHalfX);
        tag.putInt("FHZ", formedHalfZ);
        if (formedCenter != null) tag.put("FC", NbtUtils.writeBlockPos(formedCenter));
        ListTag list = new ListTag();
        for (BlockPos p : partners) list.add(NbtUtils.writeBlockPos(p));
        tag.put("Partners", list);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ── Record ────────────────────────────────────────────────────────────────

    public record FormResult(int sizeX, int sizeZ, int halfX, int halfZ, BlockPos center) {}
}