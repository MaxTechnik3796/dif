package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.QuarryFrameBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@SuppressWarnings("deprecation")
public class QuarryFrame extends BaseEntityBlock{
	private static final VoxelShape CORE_SHAPE=Block.box(4,4,4,12,12,12);
	private static final VoxelShape NORTH_SHAPE=Block.box(4,4,0,12,12,4);
	private static final VoxelShape SOUTH_SHAPE=Block.box(4,4,12,12,12,16);
	private static final VoxelShape EAST_SHAPE=Block.box(12,4,4,16,12,12);
	private static final VoxelShape WEST_SHAPE=Block.box(0,4,4,4,12,12);
	private static final VoxelShape UP_SHAPE=Block.box(4,12,4,12,16,12);
	private static final VoxelShape DOWN_SHAPE=Block.box(4,0,4,12,4,12);
	public static final BooleanProperty NORTH=BooleanProperty.create("north");
	public static final BooleanProperty EAST=BooleanProperty.create("east");
	public static final BooleanProperty SOUTH=BooleanProperty.create("south");
	public static final BooleanProperty WEST=BooleanProperty.create("west");
	public static final BooleanProperty UP=BooleanProperty.create("up");
	public static final BooleanProperty DOWN=BooleanProperty.create("down");
	public QuarryFrame(){
		super(Properties.of().strength(0.2F,0.8F).noLootTable().noOcclusion().sound(SoundType.METAL));
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(NORTH,false).setValue(EAST,false)
				.setValue(SOUTH,false).setValue(WEST,false)
				.setValue(UP,false).setValue(DOWN,false));
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
		return RenderShape.MODEL;
	}
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new QuarryFrameBlockEntity(pos,state);
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(NORTH,EAST,SOUTH,WEST,UP,DOWN);
	}
	@Override
	public float getShadeBrightness(@NotNull BlockState blockState,@NotNull BlockGetter blockGetter,@NotNull BlockPos pos){
		return 1F;
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState state,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public boolean skipRendering(@NotNull BlockState blockState,BlockState adjacentBlockState,@NotNull Direction side){
		return adjacentBlockState.getBlock()==this||super.skipRendering(blockState,adjacentBlockState,side);
	}
	@Override
	public int getLightBlock(@NotNull BlockState blockState,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	// Aktualizuje spojovací stav dle sousedních frame/quarry bloků
	@Override
	public void neighborChanged(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,
	                            @NotNull Block neighborBlock,@NotNull BlockPos fromPos,boolean moving){
		super.neighborChanged(blockState,world,pos,neighborBlock,fromPos,moving);
		BlockState updatedState=blockState
				.setValue(NORTH,isFrameOrQuarry(world,pos.north()))
				.setValue(EAST,isFrameOrQuarry(world,pos.east()))
				.setValue(SOUTH,isFrameOrQuarry(world,pos.south()))
				.setValue(WEST,isFrameOrQuarry(world,pos.west()))
				.setValue(UP,isFrameOrQuarry(world,pos.above()))
				.setValue(DOWN,isFrameOrQuarry(world,pos.below()));
		if(updatedState!=blockState) world.setBlock(pos,updatedState,3);
	}
	// Vrátí true pokud je na dané pozici frame nebo quarry blok
	private static boolean isFrameOrQuarry(Level world,BlockPos pos){
		Block block=world.getBlockState(pos).getBlock();
		return block.equals(DifModBlocks.QUARRY_FRAME.get())||block.equals(DifModBlocks.QUARRY.get());
	}
	@Override
	public @NotNull VoxelShape getShape(BlockState state,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		VoxelShape shape=CORE_SHAPE;
		if(state.getValue(NORTH)) shape=Shapes.or(shape,NORTH_SHAPE);
		if(state.getValue(SOUTH)) shape=Shapes.or(shape,SOUTH_SHAPE);
		if(state.getValue(EAST)) shape=Shapes.or(shape,EAST_SHAPE);
		if(state.getValue(WEST)) shape=Shapes.or(shape,WEST_SHAPE);
		if(state.getValue(UP)) shape=Shapes.or(shape,UP_SHAPE);
		if(state.getValue(DOWN)) shape=Shapes.or(shape,DOWN_SHAPE);
		return shape;
	}
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type){
		return level.isClientSide?null:createTickerHelper(type,DifModBlockEntities.QUARRY_FRAME.get(),(lvl,pos,st,frameEntity)->QuarryFrameBlockEntity.tick(lvl,pos,frameEntity));
	}
}