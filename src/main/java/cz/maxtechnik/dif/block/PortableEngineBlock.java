package cz.maxtechnik.dif.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import cz.maxtechnik.dif.block.entity.EngineBlockEntity;
import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
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

import java.util.List;
public class PortableEngineBlock extends KineticBlock implements EntityBlock, IWrenchable{
	public static final DirectionProperty FACING=Engine.FACING;
	public static final BooleanProperty ACTIVE=Engine.ACTIVE;
	public static final BooleanProperty INVERT=Engine.INVERT;
	public static final DirectionProperty ROTATION=DirectionProperty.create("rotation",Direction.Plane.HORIZONTAL);
	public PortableEngineBlock(Properties properties){
		super(properties.noOcclusion());
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING,Direction.NORTH).setValue(ROTATION,Direction.NORTH).setValue(INVERT,false).setValue(ACTIVE,false));
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
		builder.add(FACING,ROTATION,INVERT,ACTIVE);
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
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING,context.getNearestLookingDirection().getOpposite()).setValue(ROTATION,context.getHorizontalDirection());
	}
	@Override
	public InteractionResult onWrenched(BlockState state,UseOnContext context){
		Level level=context.getLevel();
		BlockPos pos=context.getClickedPos();
		Direction side=context.getClickedFace();
		Direction facing=state.getValue(FACING);
		if(side.getAxis()==facing.getAxis()){
			KineticBlockEntity.switchToBlockState(level,pos,state.cycle(INVERT));
			IWrenchable.playRotateSound(level,pos);
			return InteractionResult.SUCCESS;
		}
		Direction rot=state.getValue(ROTATION);
		BlockState rotated;
		if(facing.getAxis().isVertical()&&side.getAxis()!=rot.getAxis()){
			rotated=state.setValue(ROTATION,rot.getClockWise());
		}else{
			rotated=this.getRotatedBlockState(state,side);
			if(facing.getAxis().isHorizontal()) rotated=rotated.setValue(ROTATION,facing);
		}
		if(!rotated.canSurvive(level,pos)) return InteractionResult.PASS;
		KineticBlockEntity.switchToBlockState(level,pos,this.updateAfterWrenched(rotated,context));
		IWrenchable.playRotateSound(level,pos);
		return InteractionResult.SUCCESS;
	}
	@Override
	public void neighborChanged(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull Block block,@NotNull BlockPos fromPos,boolean isMoving){
		super.neighborChanged(state,level,pos,block,fromPos,isMoving);
		if(!level.isClientSide&&level.getBlockEntity(pos) instanceof EngineBlockEntity be) be.recalculateStats();
	}
	@Override
	public @NotNull BlockState rotate(@NotNull BlockState state,Rotation rotation){
		return state.setValue(FACING,rotation.rotate(state.getValue(FACING))).setValue(ROTATION,rotation.rotate(state.getValue(ROTATION)));
	}
	@Override
	public @NotNull BlockState mirror(@NotNull BlockState state,Mirror mirror){
		return state.setValue(FACING,mirror.mirror(state.getValue(FACING))).setValue(ROTATION,mirror.mirror(state.getValue(ROTATION)));
	}
	@Override
	public void appendHoverText(@NotNull ItemStack stack,Item.@NotNull TooltipContext context,@NotNull List<Component> tooltipComponents,@NotNull TooltipFlag tooltipFlag){
		super.appendHoverText(stack,context,tooltipComponents,tooltipFlag);
		tooltipComponents.add(Component.literal("Burn Rates:").withStyle(ChatFormatting.GRAY));
		Engine.addFuelTooltip(tooltipComponents,"Gasoline",DifModServerConfig.PORTABLE_ENGINE_GASOLINE_CONSUMPTION.get());
		Engine.addFuelTooltip(tooltipComponents,"LPG",DifModServerConfig.PORTABLE_ENGINE_LPG_CONSUMPTION.get());
	}
}
