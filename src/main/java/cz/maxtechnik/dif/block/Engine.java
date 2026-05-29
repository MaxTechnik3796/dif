package cz.maxtechnik.dif.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import cz.maxtechnik.dif.block.entity.EngineBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class Engine extends KineticBlock implements EntityBlock, IWrenchable{
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty ACTIVE=BooleanProperty.create("active");
	public static final BooleanProperty INVERT=BooleanProperty.create("invert");
	public Engine(Properties properties){
		super(properties.noOcclusion());
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING,Direction.NORTH).setValue(INVERT,false).setValue(ACTIVE,false));
	}
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new EngineBlockEntity(pos,blockState);
	}
	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState blockState,@NotNull BlockEntityType<T> type){
		if(type!=DifModBlockEntities.ENGINE.get()) return null;
		return (lvl,pos,state,be)->{
			if(be instanceof EngineBlockEntity blockEntity) blockEntity.tick();
		};
	}
	@Override
	public void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,INVERT,ACTIVE);
	}
	@Override
	public Direction.Axis getRotationAxis(BlockState blockState){
		return blockState.getValue(FACING).getAxis();
	}
	@Override
	public boolean hasShaftTowards(LevelReader world,BlockPos pos,BlockState blockState,Direction face){
		return face.getAxis().equals(getRotationAxis(blockState));
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		Direction dir=context.getNearestLookingDirection();
		if(dir.getAxis().isVertical()) dir=context.getHorizontalDirection().getOpposite();
		return this.defaultBlockState().setValue(FACING,dir);
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public InteractionResult onWrenched(BlockState state,UseOnContext context){
		Level level=context.getLevel();
		BlockPos pos=context.getClickedPos();
		Direction side=context.getClickedFace();
		BlockState rotated=this.getRotatedBlockState(state,context.getClickedFace());
		if(side.equals(level.getBlockState(pos).getValue(FACING))||side.equals(level.getBlockState(pos).getValue(FACING).getOpposite())){
			KineticBlockEntity.switchToBlockState(level,pos,level.getBlockState(pos).setValue(INVERT,!level.getBlockState(pos).getValue(INVERT)));
			if(level.getBlockState(pos)!=state) IWrenchable.playRotateSound(level,pos);
			return InteractionResult.SUCCESS;
		}else{
			if(!rotated.canSurvive(level,context.getClickedPos())){
				return InteractionResult.PASS;
			}else{
				KineticBlockEntity.switchToBlockState(level,pos,this.updateAfterWrenched(rotated,context));
				if(level.getBlockState(pos)!=state) IWrenchable.playRotateSound(level,pos);
				return InteractionResult.SUCCESS;
			}
		}
	}
}