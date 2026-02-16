package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.SpaceShipBE;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class Spaceship extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public Spaceship() {
		super(Properties.of().strength(5F, 6F).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	// Definice pozic, kde se mají nacházet ghost blocky vůči hlavnímu bloku
	public Set<BlockPos> getGhostPositions(BlockPos masterPos) {
		Set<BlockPos> positions = new HashSet<>();
		// Spodní kříž (pod lodí)
		positions.add(masterPos.below());
		positions.add(masterPos.below().north());
		positions.add(masterPos.below().south());
		positions.add(masterPos.below().east());
		positions.add(masterPos.below().west());
		// Horní kříž (vedle lodi)
		positions.add(masterPos.north());
		positions.add(masterPos.south());
		positions.add(masterPos.east());
		positions.add(masterPos.west());
		return positions;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		if (!level.isClientSide) {
			BlockPos finalMasterPos = pos.above();

			// Kontrola kolizí (aby se loď nepostavila do jiného bloku)
			boolean blocked = !level.getBlockState(finalMasterPos).canBeReplaced();
			for (BlockPos ghostPos : getGhostPositions(finalMasterPos)) {
				if (ghostPos.equals(pos)) continue; // Ignoruj místo, kde stojíš teď
				if (!level.getBlockState(ghostPos).canBeReplaced()) {
					blocked = true;
					break;
				}
			}

			if (blocked) {
				if (placer instanceof Player p) p.displayClientMessage(Component.literal("§cInvalid placement!"), true);
				level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
				return;
			}

			// Samotné položení: Smaže dočasný blok, položí Mastera o 1 výše a vytvoří Ghosty
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(finalMasterPos, state, 3);
			for (BlockPos ghostPos : getGhostPositions(finalMasterPos)) {
				level.setBlock(ghostPos, DifModBlocks.SPACESHIP_GHOSTBLOCK.get().defaultBlockState(), 3);
			}
		}
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()) && !level.isClientSide) {
			// HLAVNÍ FUNKCE: Při zničení hlavního bloku nastavíme všechna místa ghostů na AIR
			for (BlockPos ghostPos : getGhostPositions(pos)) {
				// Podmínka: Změníme na AIR jen pokud je tam opravdu GhostBlock (ochrana sousedních staveb)
				if (level.getBlockState(ghostPos).is(DifModBlocks.SPACESHIP_GHOSTBLOCK.get())) {
					level.setBlock(ghostPos, Blocks.AIR.defaultBlockState(), 3);
				}
			}

			if (level.getBlockEntity(pos) instanceof SpaceShipBE be) be.drops();
			super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!level.isClientSide && level.getBlockEntity(pos) instanceof SpaceShipBE be) {
			NetworkHooks.openScreen(((ServerPlayer) player), be, pos);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Nullable @Override public BlockEntity newBlockEntity(BlockPos p, BlockState s) { return new SpaceShipBE(p, s); }
	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
}