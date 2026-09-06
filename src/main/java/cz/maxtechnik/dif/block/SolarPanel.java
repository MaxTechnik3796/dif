package cz.maxtechnik.dif.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;
public class SolarPanel extends Block implements SimpleWaterloggedBlock{
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	private final Supplier<Integer> energyPerTick;
	public SolarPanel(Supplier<Integer> energyPerTick){
		super(Properties.of().strength(5F).sound(SoundType.NETHERITE_BLOCK).noOcclusion().isRedstoneConductor((bs,br,bp)->false).requiresCorrectToolForDrops());
		this.energyPerTick=energyPerTick;
		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false));
	}
	@Override
	public boolean skipRendering(@NotNull BlockState blockState,BlockState adjacentBlockState,@NotNull Direction side){
		return adjacentBlockState.getBlock() instanceof SolarPanel||super.skipRendering(blockState,adjacentBlockState,side);
	}
	@Override
	public boolean propagatesSkylightDown(BlockState blockState,@NotNull BlockGetter reader,@NotNull BlockPos pos){
		return blockState.getFluidState().isEmpty();
	}
	@Override
	public int getLightBlock(@NotNull BlockState blockState,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return box(0,0,0,16,3,16);
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(WATERLOGGED);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		boolean flag=context.getLevel().getFluidState(context.getClickedPos()).getType().equals(Fluids.WATER);
		return this.defaultBlockState().setValue(WATERLOGGED,flag);
	}
	@Override
	public @NotNull FluidState getFluidState(BlockState blockState){
		return blockState.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(blockState);
	}
	@Override
	public @NotNull BlockState updateShape(BlockState blockState,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
		if(blockState.getValue(WATERLOGGED)){
			world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
		}
		return super.updateShape(blockState,facing,facingState,world,currentPos,facingPos);
	}
	@Override
	public void onPlace(@NotNull BlockState blockstate,@NotNull Level world,@NotNull BlockPos pos,@NotNull BlockState oldState,boolean moving){
		super.onPlace(blockstate,world,pos,oldState,moving);
		world.scheduleTick(pos,this,1);
	}
	@Override
	protected void neighborChanged(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull Block neighborBlock,@NotNull BlockPos neighborPos,boolean movedByPiston){
		super.neighborChanged(state,level,pos,neighborBlock,neighborPos,movedByPiston);
		if(!level.isClientSide&&neighborPos.equals(pos.below())){
			level.scheduleTick(pos,this,1);
		}
	}
	@Override
	public void tick(@NotNull BlockState blockstate,@NotNull ServerLevel world,@NotNull BlockPos pos,@NotNull RandomSource random){
		super.tick(blockstate,world,pos,random);
		if(!world.dimensionType().hasSkyLight()||!world.isDay()){
			world.scheduleTick(pos,this,20);
			return;
		}
		if(world.canSeeSky(pos)){
			BlockEntity ent=world.getBlockEntity(pos.below());
			if(ent!=null){
				int amount=this.energyPerTick.get();
				if(amount>0) generate(ent,amount);
			}
		}
		world.scheduleTick(pos,this,1);
	}
	private void generate(BlockEntity blockEntity,int amount){
		Level level=blockEntity.getLevel();
		if(level==null||level.isClientSide) return;
		IEnergyStorage energyHandler=level.getCapability(Capabilities.EnergyStorage.BLOCK,blockEntity.getBlockPos(),blockEntity.getBlockState(),blockEntity,Direction.UP);
		if(energyHandler!=null) energyHandler.receiveEnergy(amount,false);
	}
}
