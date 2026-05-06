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
	public static final MapCodec<Quarry> CODEC = simpleCodec(Quarry::new);

	@Override
	protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public Quarry(Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH));
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
		return RenderShape.MODEL;
	}
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new QuarryBlockEntity(pos,state);
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING);
	}
	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx){
		return this.defaultBlockState().setValue(FACING,ctx.getHorizontalDirection().getOpposite());
	}
	// Zkontroluje jestli je v oblasti framu nějaký neničitelný blok
	private static boolean hasUnbreakableInFrameArea(Level level,BlockPos quarryPos){
		if(!(level.getBlockEntity(quarryPos) instanceof QuarryBlockEntity quarryEntity)) return false;
		BlockPos areaCenter=quarryEntity.getAreaCenter();
		if(areaCenter==null) return false;
		int halfX=quarryEntity.getFrameHalfX();
		int halfZ=quarryEntity.getFrameHalfZ();
		int yBase=quarryPos.getY();
		int yTop=yBase+3;
		for(int scanY=yBase;scanY<=yTop;scanY++)
			for(int scanX=areaCenter.getX()-halfX;scanX<=areaCenter.getX()+halfX;scanX++)
				for(int scanZ=areaCenter.getZ()-halfZ;scanZ<=areaCenter.getZ()+halfZ;scanZ++){
					BlockPos scanPos=new BlockPos(scanX,scanY,scanZ);
					BlockState scannedBlock=level.getBlockState(scanPos);
					if(!scannedBlock.isAir()&&scannedBlock.getDestroySpeed(level,scanPos)<0) return true;
				}
		return false;
	}
	@Override
	public void onPlace(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState oldState,boolean moving){
		super.onPlace(state,level,pos,oldState,moving);
		if(level.isClientSide) return;
		// Nejdřív aplikuj landmarky (nastaví oblast quarry)
		tryApplyNearbyLandmarks(level,pos);
		// Pak teprve zkontroluj neničitelné bloky ve výsledné oblasti
		if(hasUnbreakableInFrameArea(level,pos)){
			level.removeBlock(pos,false);
			Block.popResource(level,pos,new net.minecraft.world.item.ItemStack(this));
		}
	}
	// Hledá formed landmarky v okolí a aplikuje první vyhovující na tuto quarry
	private static void tryApplyNearbyLandmarks(Level level,BlockPos quarryPos){
		int searchRange=QuarryBlockEntity.MAX_AREA_SIDE;
		int baseY=quarryPos.getY();
		for(int dx=-searchRange;dx<=searchRange;dx++){
			for(int dz=-searchRange;dz<=searchRange;dz++){
				BlockPos scanPos=new BlockPos(quarryPos.getX()+dx,baseY,quarryPos.getZ()+dz);
				if(!level.getBlockState(scanPos).is(DifModBlocks.QUARRY_LANDMARK.get())) continue;
				if(!(level.getBlockEntity(scanPos) instanceof QuarryLandmarkBlockEntity lmEntity)) continue;
				if(lmEntity.isFormed()) continue;
				BlockPos areaCenter=lmEntity.getFormedCenter();
				int halfX=lmEntity.getFormedHalfX();
				int halfZ=lmEntity.getFormedHalfZ();
				if(areaCenter==null) continue;
				if(quarryPos.getY()!=areaCenter.getY()) continue;
				// Quarry musí být přesně 1 blok za hranou frame oblasti
				int qPosX=quarryPos.getX(), qPosZ=quarryPos.getZ();
				int edgeMinX=areaCenter.getX()-halfX, edgeMaxX=areaCenter.getX()+halfX;
				int edgeMinZ=areaCenter.getZ()-halfZ, edgeMaxZ=areaCenter.getZ()+halfZ;
				boolean onEdge=(qPosZ==edgeMinZ-1&&qPosX>=edgeMinX&&qPosX<=edgeMaxX)||(qPosZ==edgeMaxZ+1&&qPosX>=edgeMinX&&qPosX<=edgeMaxX)||(qPosX==edgeMinX-1&&qPosZ>=edgeMinZ&&qPosZ<=edgeMaxZ)||(qPosX==edgeMaxX+1&&qPosZ>=edgeMinZ&&qPosZ<=edgeMaxZ);
				if(!onEdge) continue;
				lmEntity.applyToQuarry(level,quarryPos);
				return;
			}
		}
	}
	@Override
	public void onRemove(BlockState state,@NotNull Level level,@NotNull BlockPos pos,BlockState newState,boolean moving){
		if(!state.is(newState.getBlock())&&level.getBlockEntity(pos) instanceof QuarryBlockEntity quarryEntity)
			quarryEntity.onQuarryRemoved();
		super.onRemove(state,level,pos,newState,moving);
	}
	@Override
	public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
		if (!level.isClientSide) {
			if (level.getBlockEntity(pos) instanceof QuarryBlockEntity quarryEntity) {
				player.openMenu(quarryEntity, pos);
			}
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type){
		return createTickerHelper(type,DifModBlockEntities.QUARRY.get(),QuarryBlockEntity::tick);
	}
}