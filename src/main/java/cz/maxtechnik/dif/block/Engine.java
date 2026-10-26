package cz.maxtechnik.dif.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import cz.maxtechnik.dif.block.entity.EngineBlockEntity;
import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
public class Engine extends KineticBlock implements EntityBlock, IWrenchable{
	public static final DirectionProperty FACING=BlockStateProperties.FACING;
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
		return this.defaultBlockState().setValue(FACING,context.getNearestLookingDirection().getOpposite());
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
			if(!rotated.canSurvive(level,context.getClickedPos())) return InteractionResult.PASS;
			else{
				KineticBlockEntity.switchToBlockState(level,pos,this.updateAfterWrenched(rotated,context));
				if(level.getBlockEntity(pos) instanceof EngineBlockEntity be) be.updateExtenders();
				if(level.getBlockState(pos)!=state) IWrenchable.playRotateSound(level,pos);
				return InteractionResult.SUCCESS;
			}
		}
	}
	@Override
	public void neighborChanged(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull Block block,@NotNull BlockPos fromPos,boolean isMoving){
		super.neighborChanged(state,level,pos,block,fromPos,isMoving);
		if(level.getBlockEntity(pos) instanceof EngineBlockEntity blockEntity) blockEntity.updateExtenders();
	}
	@Override
	public void appendHoverText(@NotNull ItemStack stack,Item.@NotNull TooltipContext context,@NotNull List<Component> tooltipComponents,@NotNull TooltipFlag tooltipFlag){
		super.appendHoverText(stack,context,tooltipComponents,tooltipFlag);
		boolean isPortable=this.equals(DifModBlocks.ENGINE_PORTABLE.get());
		tooltipComponents.add(Component.literal("Burn Rates:").withStyle(ChatFormatting.GRAY));
		if(isPortable){
			addFuelTooltip(tooltipComponents,"Gasoline",DifModServerConfig.PORTABLE_ENGINE_GASOLINE_CONSUMPTION.get());
			addFuelTooltip(tooltipComponents,"LPG",DifModServerConfig.PORTABLE_ENGINE_LPG_CONSUMPTION.get());
		}else{
			addFuelTooltip(tooltipComponents,"Gasoline",DifModServerConfig.LARGE_ENGINE_GASOLINE_CONSUMPTION.get());
			addFuelTooltip(tooltipComponents,"Diesel",DifModServerConfig.LARGE_ENGINE_DIESEL_CONSUMPTION.get());
			addFuelTooltip(tooltipComponents,"Heavy Fuel Oil",DifModServerConfig.LARGE_ENGINE_HEAVY_FUEL_OIL_CONSUMPTION.get());
		}
	}
	private void addFuelTooltip(List<Component> tooltipComponents,String name,double rate){
		tooltipComponents.add(Component.literal(" • "+name+": ").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal(String.format(Locale.US,"%.1f mB/s",rate)).withStyle(ChatFormatting.GOLD)));
	}
}