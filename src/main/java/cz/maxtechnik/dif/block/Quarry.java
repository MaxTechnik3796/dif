package cz.maxtechnik.dif.block;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import cz.maxtechnik.dif.block.entity.QuarryBlockEntity;
import cz.maxtechnik.dif.block.entity.QuarryLandmarkBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.quarry.QuarryArea;
import cz.maxtechnik.dif.util.quarry.QuarryAreaManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
/**
 * Těžební zařízení (Quarry) jako Create Kinetic block.
 * Přebírá rotaci ze spodu (Direction.DOWN).
 */
public class Quarry extends KineticBlock implements EntityBlock, IWrenchable, IBE<QuarryBlockEntity>{
	public static final MapCodec<Quarry> CODEC=simpleCodec(Quarry::new);
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	@Override
	protected @NotNull MapCodec<? extends KineticBlock> codec(){
		return CODEC;
	}
	public Quarry(Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH));
	}
	@Override
	public Direction.Axis getRotationAxis(BlockState blockState){
		return Direction.Axis.Y;
	}
	@Override
	public boolean hasShaftTowards(LevelReader world,BlockPos pos,BlockState blockState,Direction face){
		return face==Direction.DOWN;
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState bs){
		return RenderShape.MODEL;
	}
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new QuarryBlockEntity(pos,blockState);
	}
	@Override
	public Class<QuarryBlockEntity> getBlockEntityClass(){
		return QuarryBlockEntity.class;
	}
	@Override
	public BlockEntityType<? extends QuarryBlockEntity> getBlockEntityType(){
		return DifModBlockEntities.QUARRY.get();
	}
	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState blockState,@NotNull BlockEntityType<T> type){
		if(type!=DifModBlockEntities.QUARRY.get()) return null;
		return (lvl,pos,state,be)->{
			if(be instanceof QuarryBlockEntity blockEntity) blockEntity.tick();
		};
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){
		b.add(FACING);
	}
	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
	}
	@Override
	public void onPlace(@NotNull BlockState bs,@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState old,boolean moving){
		super.onPlace(bs,level,pos,old,moving);
		if(level.isClientSide) return;
		tryApplyNearbyLandmarks(level,pos);
		if(level.getBlockEntity(pos) instanceof QuarryBlockEntity qe){
			qe.setChanged();
			qe.sendData();
		}
		if(hasUnbreakableInFrameArea(level,pos)){
			level.removeBlock(pos,false);
			Block.popResource(level,pos,new net.minecraft.world.item.ItemStack(this));
		}
	}
	private static void tryApplyNearbyLandmarks(Level level,BlockPos quarryPos){
		int range=QuarryAreaManager.DEFAULT_RANGE*25;
		int qx=quarryPos.getX(), qy=quarryPos.getY(), qz=quarryPos.getZ();
		for(int dx=-range;dx<=range;dx++){
			for(int dz=-range;dz<=range;dz++){
				BlockPos scanPos=new BlockPos(qx+dx,qy,qz+dz);
				if(!level.getBlockState(scanPos).is(DifModBlocks.QUARRY_LANDMARK.get())) continue;
				if(!(level.getBlockEntity(scanPos) instanceof QuarryLandmarkBlockEntity lm)) continue;
				if(!lm.isFormed()) continue;
				QuarryArea area=lm.getFormedArea();
				if(area==null) continue;
				boolean onEdge=
						(qx>=area.minX()-1&&qx<=area.maxX()+1)&&
								(qz>=area.minZ()-1&&qz<=area.maxZ()+1);
				if(!onEdge) continue;
				lm.applyToQuarry(level,quarryPos);
				return;
			}
		}
	}
	private static boolean hasUnbreakableInFrameArea(Level level,BlockPos quarryPos){
		if(!(level.getBlockEntity(quarryPos) instanceof QuarryBlockEntity qe)) return false;
		QuarryAreaManager am=qe.getAreaManager();
		if(!am.hasArea()) return false;
		QuarryArea area=am.getArea();
		int yBase=quarryPos.getY(), yTop=yBase+3;
		for(int y=yBase;y<=yTop;y++){
			for(int x=area.minX();x<=area.maxX();x++){
				for(int z=area.minZ();z<=area.maxZ();z++){
					BlockPos p=new BlockPos(x,y,z);
					BlockState s=level.getBlockState(p);
					if(!s.isAir()&&s.getDestroySpeed(level,p)<0) return true;
				}
			}
		}
		return false;
	}
	@Override
	public void onRemove(BlockState bs,@NotNull Level level,@NotNull BlockPos pos,BlockState newState,boolean moving){
		if(!bs.is(newState.getBlock())&&level.getBlockEntity(pos) instanceof QuarryBlockEntity blockEntity)
			blockEntity.onQuarryRemoved();
		super.onRemove(bs,level,pos,newState,moving);
	}
	@Override
	public @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		return InteractionResult.PASS;
	}
}