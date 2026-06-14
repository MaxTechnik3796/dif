package cz.maxtechnik.dif.block.space;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
public class SpaceshipGhostBlock extends Block{
	public SpaceshipGhostBlock(){
		super(Properties.of().strength(5F,6F).noOcclusion().noLootTable().requiresCorrectToolForDrops().pushReaction(PushReaction.BLOCK));
	}
	@Override
	public boolean skipRendering(@NotNull BlockState blockState,BlockState adjacentBlockState,@NotNull Direction side){
		return adjacentBlockState.getBlock().equals(this)||super.skipRendering(blockState,adjacentBlockState,side);
	}
	@Override
	public boolean propagatesSkylightDown(@NotNull BlockState blockState,@NotNull BlockGetter reader,@NotNull BlockPos pos){
		return true;
	}
	@Override
	public float getShadeBrightness(@NotNull BlockState blockState,@NotNull BlockGetter blockGetter,@NotNull BlockPos pos){
		return 1.0f;
	}
	private BlockPos findMyMaster(Level level,BlockPos myPos){
		for(int x=-1;x<=1;x++){
			for(int y=0;y<=1;y++){
				for(int z=-1;z<=1;z++){
					BlockPos checkPos=myPos.offset(x,y,z);
					if(level.getBlockState(checkPos).getBlock() instanceof Spaceship master)
						if(master.getGhostPositions(checkPos).contains(myPos)) return checkPos;
				}
			}
		}
		return null;
	}
	@Override
	public void onRemove(BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(!blockState.is(newState.getBlock())&&!world.isClientSide()){
			BlockPos masterPos=findMyMaster(world,pos);
			if(masterPos!=null) world.destroyBlock(masterPos,true);
		}
		super.onRemove(blockState,world,pos,newState,isMoving);
	}
	@Override
	public @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		BlockPos masterPos=findMyMaster(world,pos);
		if(masterPos!=null)
			return world.getBlockState(masterPos).useWithoutItem(world,player,hit.withPosition(masterPos));
		return InteractionResult.PASS;
	}
}