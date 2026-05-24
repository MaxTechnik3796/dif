package cz.maxtechnik.dif.block.generator;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

/**
 * Abstraktní základní třída pro všechny generátory na kapalinu.
 *
 * Konkrétní generátory rozšiřují tuto třídu a předávají svůj {@link GeneratorDefinition}.
 * Definice řídí vše: osu hřídele, nastavení kapaliny, rychlosti…
 *
 * Blok záměrně neobsahuje žádné pevně zakódované hodnoty.
 */
public abstract class AbstractFluidGeneratorBlock
        extends DirectionalKineticBlock
        implements IBE<AbstractFluidGeneratorBlockEntity> {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private final GeneratorDefinition definition;

    protected AbstractFluidGeneratorBlock(Properties properties, GeneratorDefinition definition) {
        super(properties);
        this.definition = definition;
        registerDefaultState(super.defaultBlockState().setValue(POWERED, false));
    }

    public GeneratorDefinition getDefinition() {
        return definition;
    }

    // ── Block State ───────────────────────────────────────────────────────────

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    // ── Hřídel / Kinetika ────────────────────────────────────────────────────

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == definition.shaftAxis();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return definition.shaftAxis();
    }

    // ── Redstone ─────────────────────────────────────────────────────────────

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos,
                                      @Nullable Direction direction) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
                                Block block, BlockPos otherPos, boolean moving) {
        boolean powered = level.hasNeighborSignal(pos);
        if (state.getValue(POWERED) != powered) {
            level.setBlock(pos, state.setValue(POWERED, powered), 2);
        }
        super.neighborChanged(state, level, pos, block, otherPos, moving);
    }

    // ── Plnění kýblem ─────────────────────────────────────────────────────────

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        if (tank == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (stack.getItem() instanceof BucketItem bucket) {
            var fluid = bucket.content;
            if (!tank.getFluidInTank(0).isEmpty()) return ItemInteractionResult.FAIL;
            tank.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.EXECUTE);
            if (!player.isCreative())
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            return ItemInteractionResult.SUCCESS;
        }

        IFluidHandlerItem itemTank = Capabilities.FluidHandler.ITEM.getCapability(stack, null);
        if (itemTank == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        itemTank.drain(tank.fill(itemTank.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE),
                IFluidHandler.FluidAction.EXECUTE);
        return ItemInteractionResult.SUCCESS;
    }

    // ── IBE ──────────────────────────────────────────────────────────────────

    @Override
    public Class<AbstractFluidGeneratorBlockEntity> getBlockEntityClass() {
        return AbstractFluidGeneratorBlockEntity.class;
    }
}
