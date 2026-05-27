package cz.maxtechnik.dif.block;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import cz.maxtechnik.dif.block.entity.CokeOvenBlockEntity;
import cz.maxtechnik.dif.block.entity.CokeOvenPartBE;
import cz.maxtechnik.dif.gui.menu.CokeOvenMenu;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Coke Oven — jeden blok, dva stavy:
 *
 *   FORMED=false, MASTER=false → čistý blok bez BE, nulová režie
 *   FORMED=true,  MASTER=false → Part blok — CokeOvenPartBE (jen BlockPos mastera)
 *   FORMED=true,  MASTER=true  → Master blok — CokeOvenBlockEntity (ticker, inventory, fluid)
 *
 * AKTIVACE (Create Wrench na prostřední blok stěny 3×3×3):
 *   - Validace: kliknutý blok musí být přesně na středu jedné ze 6 stěn
 *   - form(): všech 27 dostane FORMED=true, kliknutý dostane MASTER=true
 *
 * DEAKTIVACE (rozbití libovolného bloku):
 *   - Itemy vypadnou z mastera
 *   - Fluid zmizí
 *   - Všech 27 bloků → FORMED=false, MASTER=false, BE odstraněny
 */
public class CokeOvenBlock extends BaseEntityBlock implements IWrenchable {

    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final BooleanProperty MASTER = BooleanProperty.create("master");

    public static final MapCodec<CokeOvenBlock> CODEC = simpleCodec(p -> new CokeOvenBlock());
    @Override protected @NotNull MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    public CokeOvenBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(3.5f, 8.0f)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any()
                .setValue(FORMED, false)
                .setValue(MASTER, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FORMED, MASTER);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BlockEntity factory — závisí na MASTER property
    // ══════════════════════════════════════════════════════════════════════════

    @Nullable @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        if (!state.getValue(FORMED)) return null;
        if (state.getValue(MASTER))  return new CokeOvenBlockEntity(pos, state);
        return new CokeOvenPartBE(pos, state);
    }

    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,
                                                                  @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide || !state.getValue(MASTER)) return null;
        return createTickerHelper(type, DifModBlockEntities.COKE_OVEN.get(),
                CokeOvenBlockEntity::serverTick);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Wrench → form
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        if (level.isClientSide || state.getValue(FORMED)) return InteractionResult.PASS;

        if (!isValidController(level, pos)) {
            if (player != null) player.sendSystemMessage(
                    Component.translatable("dif.coke_oven.invalid_structure"));
            return InteractionResult.CONSUME;
        }

        form(level, pos);
        if (player != null) player.sendSystemMessage(
                Component.translatable("dif.coke_oven.formed"));
        return InteractionResult.CONSUME;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Pravý klik → GUI (FORMED)
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state,
                                                        @NotNull Level level, @NotNull BlockPos pos,
                                                        @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!state.getValue(FORMED)) return InteractionResult.PASS;
        if (level.isClientSide)      return InteractionResult.SUCCESS;
        CokeOvenBlockEntity master = getMaster(level, pos);
        if (master != null) player.openMenu(master, master.getBlockPos());
        return InteractionResult.CONSUME;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Rozbití → deform
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level,
                         @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
        if (state.getValue(FORMED) && !state.is(newState.getBlock())) {
            // FIX: Zavři menu všem hráčům před deformem aby nedošlo ke kicku
            if (level instanceof ServerLevel serverLevel) {
                CokeOvenBlockEntity master = getMaster(level, pos);
                if (master != null) {
                    BlockPos masterPos = master.getBlockPos();
                    serverLevel.players().forEach(player -> {
                        if (player.containerMenu instanceof CokeOvenMenu menu) {
                            if (menu.getBlockEntity().getBlockPos().equals(masterPos)) {
                                player.closeContainer();
                            }
                        }
                    });
                }
            }
            deform(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Form / Deform
    // ══════════════════════════════════════════════════════════════════════════

    private static void form(Level level, BlockPos masterPos) {
        BlockPos origin = getOriginFromMaster(level, masterPos);
        if (origin == null) return;

        for (BlockPos p : all27(origin)) {
            BlockState cur = level.getBlockState(p);
            boolean isMaster = p.equals(masterPos);
            level.setBlock(p, cur.setValue(FORMED, true).setValue(MASTER, isMaster), 3);
        }

        // FIX: Ulož origin do master BE aby deform fungoval i bez okolních bloků
        if (level.getBlockEntity(masterPos) instanceof CokeOvenBlockEntity masterBE) {
            masterBE.setStructureOrigin(origin);
        }

        // Nastav markery v Part BE
        for (BlockPos p : all27(origin)) {
            if (p.equals(masterPos)) continue;
            if (level.getBlockEntity(p) instanceof CokeOvenPartBE part) {
                part.setMaster(masterPos);
            }
        }
    }

    static void deform(Level level, BlockPos anyPos) {
        CokeOvenBlockEntity master = getMaster(level, anyPos);
        if (master == null) return;

        BlockPos masterPos = master.getBlockPos();

        // FIX: Použij uložený origin místo výpočtu z okolí (ten může selhat při zničení bloku)
        BlockPos origin = master.getStructureOrigin();
        if (origin == null) origin = getOriginFromMaster(level, masterPos);
        if (origin == null) return;

        // Vysyp itemy
        if (level instanceof ServerLevel) {
            Containers.dropContents(level, masterPos, (Container) master.inventory);
        }

        // Deformuj všechny bloky
        for (BlockPos p : all27(origin)) {
            BlockState cur = level.getBlockState(p);
            if (cur.hasProperty(FORMED) && cur.getValue(FORMED)) {
                level.setBlock(p, cur.setValue(FORMED, false).setValue(MASTER, false), 3);
                level.removeBlockEntity(p);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Validace controlleru
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * FIX: Podmínka okraje nyní kontroluje "blok ven nesmí být volný (unformed) CokeOven".
     * To umožňuje dvěma pecím stát vedle sebe bez mezery.
     * Zároveň všech 27 bloků nesmí být součástí jiné aktivní (formed) pece.
     */
    static boolean isValidController(Level level, BlockPos pos) {
        for (Direction face : Direction.values()) {
            // Okraj: blok ven nesmí být volný (unformed) CokeOven blok
            // (může být vzduch, jiný blok, nebo formed CokeOven jiné pece)
            BlockPos outside = pos.relative(face);
            BlockState outsideState = level.getBlockState(outside);
            boolean outsideIsFreeCokeOven = (outsideState.getBlock() instanceof CokeOvenBlock)
                    && !outsideState.getValue(FORMED);
            if (outsideIsFreeCokeOven) continue;

            // Střed kostky = 1 krok dovnitř
            BlockPos center = pos.relative(face.getOpposite());

            // Zkontroluj 4 přímé sousedy na rovině stěny
            Direction a = perpA(face), b = perpB(face);
            if (!isUnformedCokeOven(level, center.relative(a))
                    || !isUnformedCokeOven(level, center.relative(a.getOpposite()))
                    || !isUnformedCokeOven(level, center.relative(b))
                    || !isUnformedCokeOven(level, center.relative(b.getOpposite()))) continue;

            // Zkontroluj celou 3×3×3 — všechny musí být unformed CokeOven
            BlockPos origin = center.offset(-1, -1, -1);
            boolean valid = true;
            for (BlockPos p : all27(origin)) {
                if (!isUnformedCokeOven(level, p)) { valid = false; break; }
            }
            if (valid) return true;
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Najde origin (levý dolní zadní roh) 3×3×3 kostky z pozice mastera.
     * Master je na okraji kostky — posuneme se dovnitř a zjistíme origin.
     */
    @Nullable
    static BlockPos getOriginFromMaster(Level level, BlockPos masterPos) {
        for (Direction face : Direction.values()) {
            if (isCokeOven(level, masterPos.relative(face))) continue; // tato strana je ven
            BlockPos center = masterPos.relative(face.getOpposite());
            BlockPos origin = center.offset(-1, -1, -1);
            for (BlockPos p : all27(origin)) {
                if (p.equals(masterPos)) return origin;
            }
        }
        return null;
    }

    @Nullable
    public static CokeOvenBlockEntity getMaster(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CokeOvenBlockEntity m) return m;
        if (be instanceof CokeOvenPartBE part) {
            BlockPos mp = part.getMasterPos();
            if (mp != null && level.getBlockEntity(mp) instanceof CokeOvenBlockEntity m) return m;
        }
        return null;
    }

    static Iterable<BlockPos> all27(BlockPos origin) {
        List<BlockPos> list = new ArrayList<>(27);
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                for (int z = 0; z < 3; z++)
                    list.add(origin.offset(x, y, z));
        return list;
    }

    private static boolean isCokeOven(Level level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof CokeOvenBlock;
    }

    /** FIX: Nová helper metoda — vrátí true pouze pro CokeOven který NENÍ součástí aktivní pece */
    private static boolean isUnformedCokeOven(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CokeOvenBlock)) return false;
        return !state.getValue(FORMED);
    }

    private static Direction perpA(Direction face) {
        return switch (face.getAxis()) {
            case X -> Direction.UP;
            case Y -> Direction.NORTH;
            case Z -> Direction.UP;
        };
    }

    private static Direction perpB(Direction face) {
        return switch (face.getAxis()) {
            case X -> Direction.NORTH;
            case Y -> Direction.EAST;
            case Z -> Direction.EAST;
        };
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }
}