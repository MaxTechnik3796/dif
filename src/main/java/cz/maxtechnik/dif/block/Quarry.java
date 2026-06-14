package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.QuarryArea;
import cz.maxtechnik.dif.block.entity.QuarryAreaManager;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import cz.maxtechnik.dif.block.entity.QuarryLandmarkBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Blok těžebního zařízení (Quarry). 
 * Zajišťuje umístění bloku ve světě, případné připojení k landmarkům
 * a spouští GUI při kliknutí. Veškerá logika je však v QuarryBlockEntity.
 */
public class Quarry extends BaseEntityBlock {
	public static final MapCodec<Quarry> CODEC = simpleCodec(Quarry::new);
	@Override protected @NotNull MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
	
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public Quarry(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override public @NotNull RenderShape getRenderShape(@NotNull BlockState bs) { return RenderShape.MODEL; }
	
	@Nullable @Override 
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState bs) { 
		return new QuarryBlockEntity(pos, bs); 
	}
	
	@Override 
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }
	
	@Nullable @Override 
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
	}

	// ── Událost po položení bloku hráčem ────────────────────────────────

	@Override
	public void onPlace(@NotNull BlockState bs, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState old, boolean moving) {
		super.onPlace(bs, level, pos, old, moving);
		if (level.isClientSide) return;
		
		// 1. Po položení se podívá kolem, jestli neleží vedle připravených Landmarků
		tryApplyNearbyLandmarks(level, pos);
		
		// 2. Pokud je uvnitř oblasti nezničitelný blok v cestě rámu (Y+1 až Y+3), těžba se zruší
		if (hasUnbreakableInFrameArea(level, pos)) {
			level.removeBlock(pos, false);
			Block.popResource(level, pos, new net.minecraft.world.item.ItemStack(this));
		}
	}

	/**
	 * Naskenuje okolí (až do maximálního dosahu Landmarků) a zjistí, 
	 * zda se nachází na hraně vytyčené oblasti. Pokud ano, oblast si přivlastní.
	 */
	private static void tryApplyNearbyLandmarks(Level level, BlockPos quarryPos) {
		int range = QuarryAreaManager.DEFAULT_RANGE * 25; // Odpovídá maximální velikosti oblasti
		int qx = quarryPos.getX(), qy = quarryPos.getY(), qz = quarryPos.getZ();
		
		for (int dx = -range; dx <= range; dx++) {
			for (int dz = -range; dz <= range; dz++) {
				BlockPos scanPos = new BlockPos(qx + dx, qy, qz + dz);
				
				if (!level.getBlockState(scanPos).is(DifModBlocks.QUARRY_LANDMARK.get())) continue;
				if (!(level.getBlockEntity(scanPos) instanceof QuarryLandmarkBlockEntity lm)) continue;
				if (!lm.isFormed()) continue;
				
				QuarryArea area = lm.getFormedArea();
				if (area == null) continue;
				
				// Zkontrolujeme, jestli je quarry postavené přesně jeden blok vně od hrany oblasti
				boolean onEdge =
					(qz == area.minZ() - 1 && qx >= area.minX() && qx <= area.maxX()) ||
					(qz == area.maxZ() + 1 && qx >= area.minX() && qx <= area.maxX()) ||
					(qx == area.minX() - 1 && qz >= area.minZ() && qz <= area.maxZ()) ||
					(qx == area.maxX() + 1 && qz >= area.minZ() && qz <= area.maxZ());
					
				if (!onEdge) continue;
				
				// Pokud ano, předáme data a smažeme landmarky
				lm.applyToQuarry(level, quarryPos);
				return;
			}
		}
	}

	/**
	 * Zkontroluje, zda není v místě, kde se bude stavět vrchní rám, nějaký bedrock nebo spawner.
	 */
	private static boolean hasUnbreakableInFrameArea(Level level, BlockPos quarryPos) {
		if (!(level.getBlockEntity(quarryPos) instanceof QuarryBlockEntity qe)) return false;
		QuarryAreaManager am = qe.getAreaManager();
		if (!am.hasArea()) return false;
		
		QuarryArea area = am.getArea();
		int yBase = quarryPos.getY(), yTop = yBase + 3; // Rám je vysoký 3 bloky
		
		for (int y = yBase; y <= yTop; y++) {
			for (int x = area.minX(); x <= area.maxX(); x++) {
				for (int z = area.minZ(); z <= area.maxZ(); z++) {
					BlockPos p = new BlockPos(x, y, z);
					BlockState s = level.getBlockState(p);
					if (!s.isAir() && s.getDestroySpeed(level, p) < 0) return true;
				}
			}
		}
		return false;
	}

	// ── Ostatní události ────────────────────────────────────────────────

	@Override
	public void onRemove(BlockState bs, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean moving) {
		if (!bs.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof QuarryBlockEntity qe) {
			qe.onQuarryRemoved();
		}
		super.onRemove(bs, level, pos, newState, moving);
	}

	@Override
	public @NotNull InteractionResult useWithoutItem(@NotNull BlockState bs, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
		if (!level.isClientSide) {
			if (level.getBlockEntity(pos) instanceof QuarryBlockEntity qe) player.openMenu(qe, pos);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Nullable @Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState bs, @NotNull BlockEntityType<T> type) {
		return createTickerHelper(type, DifModBlockEntities.QUARRY.get(), QuarryBlockEntity::tick);
	}
}