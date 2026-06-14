package cz.maxtechnik.dif.block.template;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
public class CustomTransparent extends Block{
	public CustomTransparent(BlockBehaviour.Properties properties){
		super(properties);
	}
	@Override
	public int getLightBlock(@NotNull BlockState blockState,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public boolean propagatesSkylightDown(BlockState blockState,@NotNull BlockGetter reader,@NotNull BlockPos pos){
		return blockState.getFluidState().isEmpty();
	}
	@Override
	public float getShadeBrightness(@NotNull BlockState blockState,@NotNull BlockGetter blockGetter,@NotNull BlockPos pos){
		return 1F;
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public boolean skipRendering(@NotNull BlockState state,BlockState adjacentBlockState,@NotNull Direction side){
		return adjacentBlockState.getBlock().equals(this)||super.skipRendering(state,adjacentBlockState,side);
	}
}
