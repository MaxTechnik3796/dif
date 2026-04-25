package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.QuarryFrameBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class QuarryFrame extends BaseEntityBlock {

	public QuarryFrame() {
		super(Properties.of().strength(1F, 1F).noLootTable().sound(SoundType.METAL));
	}

	@Override public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }

	@Nullable @Override public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new QuarryFrameBlockEntity(pos, state);
	}

	@Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
			@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
		return level.isClientSide ? null
				: createTickerHelper(type, DifModBlockEntities.QUARRY_FRAME.get(), (level1, pos, state1, be) -> QuarryFrameBlockEntity.tick(level1, pos, be));
	}
}