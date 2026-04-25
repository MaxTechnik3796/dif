package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.DifMod;
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
	public static final BooleanProperty NORTH=BooleanProperty.create("north");
	public static final BooleanProperty EAST=BooleanProperty.create("east");
	public static final BooleanProperty SOUTH=BooleanProperty.create("south");
	public static final BooleanProperty WEST=BooleanProperty.create("west");
	public static final BooleanProperty UP=BooleanProperty.create("up");
	public static final BooleanProperty DOWN=BooleanProperty.create("down");
	public QuarryFrame(){
		super(Properties.of().strength(1F,1F).noLootTable().noOcclusion().sound(SoundType.METAL));
		this.registerDefaultState(this.stateDefinition.any().setValue(NORTH,false).setValue(EAST,false).setValue(SOUTH,false).setValue(WEST,false).setValue(UP,false).setValue(DOWN,false));
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
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type){
		return level.isClientSide?null:createTickerHelper(type,DifModBlockEntities.QUARRY_FRAME.get(),(level1,pos,state1,be)->QuarryFrameBlockEntity.tick(level1,pos,be));
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
	public boolean skipRendering(@NotNull BlockState state,BlockState adjacentBlockState,@NotNull Direction side) {
		return adjacentBlockState.getBlock()==this||super.skipRendering(state,adjacentBlockState,side);
	}

	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public void neighborChanged(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull Block neighborBlock,@NotNull BlockPos fromPos,boolean moving){
		super.neighborChanged(blockState,world,pos,neighborBlock,fromPos,moving);
		BlockState northState=world.getBlockState(pos.relative(Direction.NORTH));
		BlockState eastState=world.getBlockState(pos.relative(Direction.EAST));
		BlockState southState=world.getBlockState(pos.relative(Direction.SOUTH));
		BlockState westState=world.getBlockState(pos.relative(Direction.WEST));
		BlockState upState=world.getBlockState(pos.relative(Direction.UP));
		BlockState downState=world.getBlockState(pos.relative(Direction.DOWN));

		blockState.setValue(NORTH,(northState.is(this)||northState.is(DifModBlocks.QUARRY.get())));
		blockState.setValue(EAST,(eastState.is(this)||eastState.is(DifModBlocks.QUARRY.get())));
		blockState.setValue(SOUTH,(southState.is(this)||southState.is(DifModBlocks.QUARRY.get())));
		blockState.setValue(WEST,(westState.is(this)||westState.is(DifModBlocks.QUARRY.get())));
		blockState.setValue(UP,(upState.is(this)||upState.is(DifModBlocks.QUARRY.get())));
		blockState.setValue(DOWN,(downState.is(this)||downState.is(DifModBlocks.QUARRY.get())));
		world.setBlock(pos,blockState,3);
	}
}