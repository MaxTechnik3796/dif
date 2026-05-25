package cz.maxtechnik.dif.block;

import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import cz.maxtechnik.dif.block.entity.DistillationTankBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Destilační tank — multiblok věž.
 * Stavění: stejné jako Create Fluid Tank — postav základnu (1×1, 2×2 nebo 3×3),
 * pak klikni s dalším blokem na vrchní stěnu → automaticky se přidá patro
 */
public class DistillationTank extends FluidTankBlock {

	public DistillationTank() {
		super(BlockBehaviour.Properties.of()
				.strength(5F, 6F)
				.sound(SoundType.METAL)
				.requiresCorrectToolForDrops(), false);
	}

	@Override
	public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType() {
		return DifModBlockEntities.DISTILLATION_TANK.get();
	}

	//Ticker — Create logiku + naši recipe logiku
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		BlockEntityTicker<T> createTicker = super.getTicker(level, state, type);
		if (level.isClientSide) return createTicker;

		if (type != DifModBlockEntities.DISTILLATION_TANK.get()) return createTicker;

		return (lvl, pos, st, be) -> {
			if (createTicker != null) createTicker.tick(lvl, pos, st, be);
			if (be instanceof DistillationTankBlockEntity dbe) {
				DistillationTankBlockEntity.serverTick(lvl, dbe);
			}
		};
	}

	//Sousedící blok se změnil → invaliduj cache věže
	@Override
	public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                @NotNull Block neighborBlock, @NotNull BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, level, pos, neighborBlock, fromPos, isMoving);

		// Zajímají nás jen změny bezprostředně pod nebo nad námi
		if (!fromPos.equals(pos.above()) && !fromPos.equals(pos.below())) return;

		if (level.getBlockEntity(pos) instanceof DistillationTankBlockEntity dbe) {
			DistillationTankBlockEntity master = dbe.getTowerMaster();
			if (master != null) master.notifyMultiUpdated();
		}
	}
}