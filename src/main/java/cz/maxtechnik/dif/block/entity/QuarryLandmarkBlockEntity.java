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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class QuarryLandmarkBlockEntity extends BlockEntity {

    public static final int MAX_SEARCH = QuarryBlockEntity.MAX_AREA_SIDE;

    private final List<BlockPos> partners = new ArrayList<>(2);
    private boolean  formed    = false;
    private int formedHalfX    = 0;
    private int formedHalfZ    = 0;
    // Střed oblasti uložen absolutně
    @Nullable private BlockPos formedCenter = null;

    public QuarryLandmarkBlockEntity(BlockPos pos, BlockState state) {
        super(DifModBlockEntities.QUARRY_LANDMARK.get(), pos, state);
    }

    // ── Pravý klik ───────────────────────────────────────────────────────────

    public void onRightClick(Player player) {
        if (level == null || level.isClientSide) return;

        // Ověř že partneři stále existují
        if (formed && !partners.isEmpty()) {
            for (BlockPos partnerPos : partners) {
                if (!level.getBlockState(partnerPos).is(DifModBlocks.QUARRY_LANDMARK.get())) {
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

        // Zobrazení je inclusize – sizeX = maxX-minX = plná šířka těžební oblasti
        player.sendSystemMessage(Component.literal(
                "§aFormace: §f" + result.sizeX() + "§ax§f" + result.sizeZ() +
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
                BlockPos scanPos = new BlockPos(worldPosition.getX() + dx, y, worldPosition.getZ() + dz);
                if (level.getBlockState(scanPos).is(DifModBlocks.QUARRY_LANDMARK.get()))
                    found.add(scanPos);
            }
        }
        return found;
    }

    // ── Výpočet formace ──────────────────────────────────────────────────────

    /**
     * Pokusí se zformovat oblast ze 3 landmarků v L-tvaru.
     *
     * Oprava lichého/sudého problému:
     *   minX/maxX jsou absolutní pozice rohových bloků.
     *   halfX = ceil((maxX-minX) / 2) zajistí pokrytí i při lichém rozměru.
     *   Střed = (minX+maxX)/2 zaokrouhleno dolů – quarry vždy přidá +1 na správnou stranu.
     */
    @Nullable
    public static FormResult tryForm(List<BlockPos> all) {
        if (all.size() != 3) return null;
        BlockPos a = all.get(0), b = all.get(1), c = all.get(2);

        int minX = Math.min(a.getX(), Math.min(b.getX(), c.getX()));
        int maxX = Math.max(a.getX(), Math.max(b.getX(), c.getX()));
        int minZ = Math.min(a.getZ(), Math.min(b.getZ(), c.getZ()));
        int maxZ = Math.max(a.getZ(), Math.max(b.getZ(), c.getZ()));

        int spanX = maxX - minX; // počet bloků - 1
        int spanZ = maxZ - minZ;

        if (spanX < 2 || spanZ < 2) return null;
        if (spanX > MAX_SEARCH || spanZ > MAX_SEARCH) return null;

        if (!isLShape(a, b, c) && !isLShape(b, a, c) && !isLShape(c, a, b)) return null;

        // Střed: přesný střed (pro sudé strany přesně na středu,
        // pro liché strany zaokrouhleno dolů – FrameBuilder pak pokryje oba směry správně)
        int centerX = (minX + maxX) / 2;
        int centerZ = (minZ + maxZ) / 2;

        // halfX/Z = vzdálenost středu k okraji (inkluzivní).
        // Použijeme ceiling dělení aby liché strany neztratily jeden blok.
        // Příklad: span=7 → half=4 → střed±4 = 9 bloků (centerX-4 až centerX+4)
        // Ale centerX = (minX+maxX)/2 může být posunuto → použijeme max(centerX-minX, maxX-centerX)
        int halfX = Math.max(centerX - minX, maxX - centerX);
        int halfZ = Math.max(centerZ - minZ, maxZ - centerZ);

        // sizeX/Z = skutečný počet těžených bloků uvnitř framu
        int sizeX = spanX - 1; // vnitřek = span - 2 stěny; ale quarry těží span-1? přizpůsob GUI
        int sizeZ = spanZ - 1;

        return new FormResult(sizeX, sizeZ, halfX, halfZ,
                new BlockPos(centerX, a.getY(), centerZ));
    }

    private static boolean isLShape(BlockPos corner, BlockPos p1, BlockPos p2) {
        return (corner.getX() == p1.getX() && corner.getZ() == p2.getZ())
                || (corner.getX() == p2.getX() && corner.getZ() == p1.getZ());
    }

    // ── Aplikace formace ──────────────────────────────────────────────────────

    private void applyFormation(List<BlockPos> all, FormResult result) {
        if (level == null) return;
        for (BlockPos lmPos : all) {
            if (!(level.getBlockEntity(lmPos) instanceof QuarryLandmarkBlockEntity lbe)) continue;
            lbe.partners.clear();
            for (BlockPos other : all) if (!other.equals(lmPos)) lbe.partners.add(other);
            lbe.formed      = true;
            lbe.formedCenter = result.center();
            lbe.formedHalfX  = result.halfX();
            lbe.formedHalfZ  = result.halfZ();
            lbe.setChanged();
            level.sendBlockUpdated(lmPos, lbe.getBlockState(), lbe.getBlockState(), 3);
        }
    }

    // ── Reset formace ─────────────────────────────────────────────────────────

    private void resetFormation() {
        formed       = false;
        formedCenter = null;
        formedHalfX  = formedHalfZ = 0;
        partners.clear();
        setChanged();
        if (level != null)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // ── Připojení k quarry ────────────────────────────────────────────────────

    /**
     * Voláno z Quarry.onPlace – aplikuje tuto formaci na quarry a zničí všechny landmarky.
     */
    public void applyToQuarry(Level level, BlockPos quarryPos) {
        if (!formed || formedCenter == null) return;
        if (!(level.getBlockEntity(quarryPos) instanceof QuarryBlockEntity quarry)) return;

        quarry.setLandmarkArea(formedHalfX, formedHalfZ, formedCenter);

        List<BlockPos> all = new ArrayList<>(partners);
        all.add(worldPosition);
        for (BlockPos lmPos : all) {
            if (level.getBlockState(lmPos).is(DifModBlocks.QUARRY_LANDMARK.get())) {
                Block.popResource(level, lmPos,
                        new ItemStack(DifModBlocks.QUARRY_LANDMARK.get().asItem()));
                level.removeBlock(lmPos, false);
            }
        }
    }

    // ── Zničení → reset partnerů ──────────────────────────────────────────────

    public void onRemoved() {
        if (level == null || level.isClientSide || !formed) return;
        for (BlockPos partnerPos : new ArrayList<>(partners)) {
            if (!(level.getBlockEntity(partnerPos) instanceof QuarryLandmarkBlockEntity lbe)) continue;
            lbe.formed       = false;
            lbe.formedCenter = null;
            lbe.formedHalfX  = lbe.formedHalfZ = 0;
            lbe.partners.clear();
            lbe.setChanged();
            level.sendBlockUpdated(partnerPos, lbe.getBlockState(), lbe.getBlockState(), 3);
        }
    }

    // ── Klient-side tracking pro renderer ────────────────────────────────────

    @Override public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide) updateClientRenderer();
    }

    @Override public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide)
            cz.maxtechnik.dif.renderer.LandmarkOverlayRenderer.unregister(worldPosition);
    }

    @Override public void handleUpdateTag(@NotNull CompoundTag tag) {
        load(tag);
        updateClientRenderer();
    }

    @Override public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) load(tag);
        updateClientRenderer();
    }

    private void updateClientRenderer() {
        if (level == null || !level.isClientSide) return;
        if (formed) cz.maxtechnik.dif.renderer.LandmarkOverlayRenderer.register(this);
        else        cz.maxtechnik.dif.renderer.LandmarkOverlayRenderer.unregister(worldPosition);
    }

    // ── Gettery ───────────────────────────────────────────────────────────────

    public boolean   isFormed()       { return formed; }
    public int       getFormedHalfX() { return formedHalfX; }
    public int       getFormedHalfZ() { return formedHalfZ; }
    @Nullable public BlockPos getFormedCenter() { return formedCenter; }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Formed", formed);
        tag.putInt("FHX", formedHalfX);
        tag.putInt("FHZ", formedHalfZ);
        if (formedCenter != null) tag.put("FC", NbtUtils.writeBlockPos(formedCenter));
        ListTag list = new ListTag();
        for (BlockPos partnerPos : partners) list.add(NbtUtils.writeBlockPos(partnerPos));
        tag.put("Partners", list);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        formed      = tag.getBoolean("Formed");
        formedHalfX = tag.getInt("FHX");
        formedHalfZ = tag.getInt("FHZ");
        formedCenter = tag.contains("FC") ? NbtUtils.readBlockPos(tag.getCompound("FC")) : null;
        partners.clear();
        ListTag list = tag.getList("Partners", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) partners.add(NbtUtils.readBlockPos(list.getCompound(i)));
    }

    @Override public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Formed", formed);
        tag.putInt("FHX", formedHalfX);
        tag.putInt("FHZ", formedHalfZ);
        if (formedCenter != null) tag.put("FC", NbtUtils.writeBlockPos(formedCenter));
        ListTag list = new ListTag();
        for (BlockPos partnerPos : partners) list.add(NbtUtils.writeBlockPos(partnerPos));
        tag.put("Partners", list);
        return tag;
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ── Record ────────────────────────────────────────────────────────────────

    public record FormResult(int sizeX, int sizeZ, int halfX, int halfZ, BlockPos center) {}
}