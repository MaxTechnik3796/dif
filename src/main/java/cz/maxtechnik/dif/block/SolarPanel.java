package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class SolarPanel extends Block implements SimpleWaterloggedBlock{
	public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
	public SolarPanel(){
		super(Properties.of().strength(5F).sound(SoundType.NETHERITE_BLOCK).noOcclusion().isRedstoneConductor((bs,br,bp)->false).requiresCorrectToolForDrops());
		this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED,false));
	}
	@Override
	public boolean skipRendering(@NotNull BlockState state,BlockState adjacentBlockState,@NotNull Direction side){
		return adjacentBlockState.getBlock()==this||super.skipRendering(state,adjacentBlockState,side);
	}
	@Override
	public boolean propagatesSkylightDown(BlockState state,@NotNull BlockGetter reader,@NotNull BlockPos pos){
		return state.getFluidState().isEmpty();
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState state,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return box(0,0,0,16,3,16);
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(WATERLOGGED);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		boolean flag=context.getLevel().getFluidState(context.getClickedPos()).getType()==Fluids.WATER;
		return this.defaultBlockState().setValue(WATERLOGGED,flag);
	}
	@Override
	public @NotNull FluidState getFluidState(BlockState state){
		return state.getValue(WATERLOGGED)?Fluids.WATER.getSource(false):super.getFluidState(state);
	}
	@Override
	public @NotNull BlockState updateShape(BlockState state,@NotNull Direction facing,@NotNull BlockState facingState,@NotNull LevelAccessor world,@NotNull BlockPos currentPos,@NotNull BlockPos facingPos){
		if(state.getValue(WATERLOGGED)){
			world.scheduleTick(currentPos,Fluids.WATER,Fluids.WATER.getTickDelay(world));
		}
		return super.updateShape(state,facing,facingState,world,currentPos,facingPos);
	}
	@Override
	public void onPlace(@NotNull BlockState blockstate,@NotNull Level world,@NotNull BlockPos pos,@NotNull BlockState oldState,boolean moving){
		super.onPlace(blockstate,world,pos,oldState,moving);
		world.scheduleTick(pos,this,1);
	}
	@Override
	public void tick(@NotNull BlockState blockstate,@NotNull ServerLevel world,@NotNull BlockPos pos,@NotNull RandomSource random){
		super.tick(blockstate,world,pos,random);
		BlockEntity ent=world.getBlockEntity(pos.below());
		if(world.canSeeSky(pos)&&world.isDay()&&world.dimension().equals(Level.OVERWORLD)&&ent!=null){
			Block block=blockstate.getBlock();
			if(block.equals(DifModBlocks.SOLAR_PANEL_00.get())){
				generate(ent,DifModCommonConfig.solarPanel_00);
			}else if(block.equals(DifModBlocks.SOLAR_PANEL_01.get())){
				generate(ent,DifModCommonConfig.solarPanel_01);
			}else if(block.equals(DifModBlocks.SOLAR_PANEL_02.get())){
				generate(ent,DifModCommonConfig.solarPanel_02);
			}else if(block.equals(DifModBlocks.SOLAR_PANEL_03.get())){
				generate(ent,DifModCommonConfig.solarPanel_03);
			}else if(block.equals(DifModBlocks.SOLAR_PANEL_04.get())){
				generate(ent,DifModCommonConfig.solarPanel_04);
			}
		}
		world.scheduleTick(pos,this,1);
	}
	private void generate(BlockEntity ent,int amount){
		ent.getCapability(ForgeCapabilities.ENERGY,Direction.UP).ifPresent(capability->capability.receiveEnergy(amount,false));
	}
}
