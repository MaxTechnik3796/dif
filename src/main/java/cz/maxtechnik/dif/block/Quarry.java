package cz.maxtechnik.dif.block;

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

public class Quarry extends BaseEntityBlock{
	public static final MapCodec<Quarry> CODEC=simpleCodec(Quarry::new);
	@Override protected @NotNull MapCodec<? extends BaseEntityBlock> codec(){ return CODEC; }
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;

	public Quarry(Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH));
	}

	@Override public @NotNull RenderShape getRenderShape(@NotNull BlockState bs){ return RenderShape.MODEL; }
	@Nullable @Override public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState bs){ return new QuarryBlockEntity(pos,bs); }
	@Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){ b.add(FACING); }
	@Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext ctx){
		return this.defaultBlockState().setValue(FACING,ctx.getHorizontalDirection().getOpposite());
	}

	// ── Po položení: hledej landmark oblasti ────────────────────────────
	@Override
	public void onPlace(@NotNull BlockState bs,@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState old,boolean moving){
		super.onPlace(bs,level,pos,old,moving);
		if(level.isClientSide) return;
		tryApplyNearbyLandmarks(level,pos);
		if(hasUnbreakableInArea(level,pos)){
			level.removeBlock(pos,false);
			Block.popResource(level,pos,new net.minecraft.world.item.ItemStack(this));
		}
	}

	// ── Hledej formed landmark v dosahu a ověř edge-check ───────────────
	private static void tryApplyNearbyLandmarks(Level level,BlockPos quarryPos){
		int range=QuarryBlockEntity.MAX_AREA_SIDE;
		int qx=quarryPos.getX(), qy=quarryPos.getY(), qz=quarryPos.getZ();
		for(int dx=-range;dx<=range;dx++){
			for(int dz=-range;dz<=range;dz++){
				BlockPos scanPos=new BlockPos(qx+dx,qy,qz+dz);
				if(!level.getBlockState(scanPos).is(DifModBlocks.QUARRY_LANDMARK.get())) continue;
				if(!(level.getBlockEntity(scanPos) instanceof QuarryLandmarkBlockEntity lm)) continue;
				if(!lm.isFormed()) continue;
				var area=lm.getFormedArea();
				if(area==null) continue;
				// Quarry musí být na vnější hraně oblasti (1 blok za rámem)
				boolean onEdge=
					(qz==area.minZ()-1&&qx>=area.minX()&&qx<=area.maxX())||
					(qz==area.maxZ()+1&&qx>=area.minX()&&qx<=area.maxX())||
					(qx==area.minX()-1&&qz>=area.minZ()&&qz<=area.maxZ())||
					(qx==area.maxX()+1&&qz>=area.minZ()&&qz<=area.maxZ());
				if(!onEdge) continue;
				lm.applyToQuarry(level,quarryPos);
				return;
			}
		}
	}

	// ── Kontrola nezničitelných bloků v oblasti ─────────────────────────
	private static boolean hasUnbreakableInArea(Level level,BlockPos quarryPos){
		if(!(level.getBlockEntity(quarryPos) instanceof QuarryBlockEntity qe)) return false;
		int mnX=qe.getAreaMinX(), mxX=qe.getAreaMaxX();
		int mnZ=qe.getAreaMinZ(), mxZ=qe.getAreaMaxZ();
		int yBase=quarryPos.getY(), yTop=yBase+3;
		for(int y=yBase;y<=yTop;y++)
			for(int x=mnX;x<=mxX;x++)
				for(int z=mnZ;z<=mxZ;z++){
					BlockPos p=new BlockPos(x,y,z);
					BlockState s=level.getBlockState(p);
					if(!s.isAir()&&s.getDestroySpeed(level,p)<0) return true;
				}
		return false;
	}

	@Override
	public void onRemove(BlockState bs,@NotNull Level level,@NotNull BlockPos pos,BlockState newState,boolean moving){
		if(!bs.is(newState.getBlock())&&level.getBlockEntity(pos) instanceof QuarryBlockEntity qe) qe.onQuarryRemoved();
		super.onRemove(bs,level,pos,newState,moving);
	}
	@Override
	public @NotNull InteractionResult useWithoutItem(@NotNull BlockState bs,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		if(!level.isClientSide) if(level.getBlockEntity(pos) instanceof QuarryBlockEntity qe) player.openMenu(qe,pos);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}
	@Nullable @Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState bs,@NotNull BlockEntityType<T> type){
		return createTickerHelper(type,DifModBlockEntities.QUARRY.get(),QuarryBlockEntity::tick);
	}
}