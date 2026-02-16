package cz.maxtechnik.dif.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class BuddingEnergyBlock extends AmethystBlock{
	public static final int GROWTH_CHANCE=5;
	private static final Direction[] DIRECTIONS=Direction.values();
	public BuddingEnergyBlock(BlockBehaviour.Properties properties){
		super(properties);
	}
	@Override
	public void randomTick(@NotNull BlockState blockState,@NotNull ServerLevel world,@NotNull BlockPos pos,RandomSource source){
		if(source.nextInt(5)==0){
			Direction direction=DIRECTIONS[source.nextInt(DIRECTIONS.length)];
			BlockPos blockpos=pos.relative(direction);
			BlockState blockstate=world.getBlockState(blockpos);
			Block block=null;
			if(canClusterGrowAtState(blockstate)){
				block=DifModBlocks.SMALL_ENERGY_BUD.get();
			}else if(blockstate.is(DifModBlocks.SMALL_ENERGY_BUD.get())&&blockstate.getValue(AmethystClusterBlock.FACING)==direction){
				block=DifModBlocks.MEDIUM_ENERGY_BUD.get();
			}else if(blockstate.is(DifModBlocks.MEDIUM_ENERGY_BUD.get())&&blockstate.getValue(AmethystClusterBlock.FACING)==direction){
				block=DifModBlocks.LARGE_ENERGY_BUD.get();
			}else if(blockstate.is(DifModBlocks.LARGE_ENERGY_BUD.get())&&blockstate.getValue(AmethystClusterBlock.FACING)==direction){
				block=DifModBlocks.ENERGY_CLUSTER.get();
			}
			if(block!=null){
				BlockState blockstate1=block.defaultBlockState().setValue(AmethystClusterBlock.FACING,direction).setValue(AmethystClusterBlock.WATERLOGGED,blockstate.getFluidState().getType()==Fluids.WATER);
				world.setBlockAndUpdate(blockpos,blockstate1);
			}
		}
	}
	public static boolean canClusterGrowAtState(BlockState blockState){
		return blockState.isAir()||blockState.is(Blocks.WATER)&&blockState.getFluidState().getAmount()==8;
	}
}