package cz.maxtechnik.dif.block.template;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class CustomTransparent extends Block{
	public CustomTransparent(SoundType sound,float hardness,float resistance,boolean requiresCorrectToolForDrops){
		super(requiresCorrectToolForDrops?Properties.of().strength(hardness,resistance).sound(sound).noOcclusion().requiresCorrectToolForDrops():Properties.of().strength(hardness,resistance).sound(sound).noOcclusion());
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public boolean propagatesSkylightDown(BlockState state,@NotNull BlockGetter reader,@NotNull BlockPos pos){
		return state.getFluidState().isEmpty();
	}
	@Override
	public float getShadeBrightness(@NotNull BlockState blockState,@NotNull BlockGetter blockGetter,@NotNull BlockPos pos){
		return 1.0f;
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState state,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public boolean skipRendering(@NotNull BlockState state,BlockState adjacentBlockState,@NotNull Direction side) {
		return adjacentBlockState.getBlock()==this||super.skipRendering(state,adjacentBlockState,side);
	}

}
