package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
public class NanoGlass extends TransparentBlock{
	public static final BooleanProperty DARK=BooleanProperty.create("dark");
	public NanoGlass(BlockBehaviour.Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(DARK,false));
	}
	@Override
	public void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(DARK);
	}
	@Override
	public @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		super.useWithoutItem(blockState,level,pos,player,hit);
		if(level.isClientSide) return InteractionResult.SUCCESS;
		updateGlass(blockState,level,pos,!level.getBlockState(pos).getValue(DARK));
		return InteractionResult.SUCCESS;
	}
	public void updateGlass(BlockState blockState,Level level,BlockPos pos,boolean dark){
		level.setBlockAndUpdate(pos,blockState.setValue(DARK,dark));
		DifMod.queueServerWork(2,()->{
			for(Direction direction: Direction.values()){
				BlockState block=level.getBlockState(pos.relative(direction));
				if(block.getBlock() instanceof NanoGlass glass)
					glass.updateGlass(blockState,level,pos.relative(direction),dark);
			}
		});
	}
}
