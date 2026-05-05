package cz.maxtechnik.dif.block.space;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
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
	public boolean skipRendering(@NotNull BlockState state,BlockState adjacentBlockState,@NotNull Direction side){
		return adjacentBlockState.getBlock()==this||super.skipRendering(state,adjacentBlockState,side);
	}
	@Override
	public boolean propagatesSkylightDown(@NotNull BlockState state,@NotNull BlockGetter reader,@NotNull BlockPos pos){
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
					if(level.getBlockState(checkPos).getBlock() instanceof Spaceship master){
						// Ověříme, zda tento ghost patří právě k této lodi
						if(master.getGhostPositions(checkPos).contains(myPos)){
							return checkPos;
						}
					}
				}
			}
		}
		return null;
	}
	@Override
	public void onRemove(BlockState state,@NotNull Level world,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(!state.is(newState.getBlock())&&!world.isClientSide()){
			BlockPos masterPos=findMyMaster(world,pos);
			if(masterPos!=null){
				// Ghost nespouští řetězové mazání, jen zničí Mastera.
				// Veškerou práci s mazáním ostatních ghostů provede Master v onRemove.
				world.destroyBlock(masterPos,true);
			}
		}
		super.onRemove(state,world,pos,newState,isMoving);
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		BlockPos masterPos=findMyMaster(world,pos);
		if(masterPos!=null){
			return world.getBlockState(masterPos).use(world,player,hand,hit.withPosition(masterPos));
		}
		return InteractionResult.PASS;
	}
}