package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.entity.NuclearCountdownEntity;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
public class NuclearBombBlock extends Block{
	public NuclearBombBlock(){
		super(BlockBehaviour.Properties.of()
				.strength(5.0f,1200.0f)
				.sound(SoundType.METAL)
				.noOcclusion()
		);
	}
	@Override
	public InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,
	                                        Player player,BlockHitResult hit){
		if(level.isClientSide) return InteractionResult.SUCCESS;
		level.removeBlock(pos,false);
		spawnCountdown(level,pos,200);
		return InteractionResult.SUCCESS;
	}
	@Override
	public void neighborChanged(BlockState state,Level level,BlockPos pos,
	                            Block block,BlockPos fromPos,boolean isMoving){
		if(level.isClientSide) return;
		if(level.hasNeighborSignal(pos)){
			level.removeBlock(pos,false);
			spawnCountdown(level,pos,40);
		}
	}
	private void spawnCountdown(Level level,BlockPos pos,int countdown){
		NuclearCountdownEntity bomb=new NuclearCountdownEntity(DifModEntities.NUCLEAR_COUNTDOWN.get(),level);
		bomb.setPos(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5);
		bomb.setCountdown(countdown);
		level.addFreshEntity(bomb);
	}
}