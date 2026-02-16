package cz.maxtechnik.dif.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SpaceshipGhostBlock extends Block {
	public SpaceshipGhostBlock() {
		super(Properties.of().strength(5F).noOcclusion().noLootTable());
	}

	@Override
	public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

	// Pomocná metoda pro nalezení vlastníka (Mastera)
	private BlockPos findMyMaster(Level level, BlockPos myPos) {
		for (int x = -1; x <= 1; x++) {
			for (int y = 0; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					BlockPos checkPos = myPos.offset(x, y, z);
					if (level.getBlockState(checkPos).getBlock() instanceof Spaceship master) {
						// Ověříme, zda tento ghost patří právě k této lodi
						if (master.getGhostPositions(checkPos).contains(myPos)) {
							return checkPos;
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && !level.isClientSide) {
			BlockPos masterPos = findMyMaster(level, pos);
			if (masterPos != null) {
				// Ghost nespouští řetězové mazání, jen zničí Mastera.
				// Veškerou práci s mazáním ostatních ghostů provede Master v onRemove.
				level.destroyBlock(masterPos, true);
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		BlockPos masterPos = findMyMaster(level, pos);
		if (masterPos != null) {
			return level.getBlockState(masterPos).use(level, player, hand, hit.withPosition(masterPos));
		}
		return InteractionResult.PASS;
	}
}