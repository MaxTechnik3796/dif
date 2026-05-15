package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.entity.NuclearExplosionEntity;
import cz.maxtechnik.dif.entity.NuclearMushroomEntity;
import cz.maxtechnik.dif.entity.NuclearWaveEntity;
import cz.maxtechnik.dif.init.other.DifModEntities;
import cz.maxtechnik.dif.init.events.client.NukeSoundEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
		return InteractionResult.PASS;
	}
	@Override
	public void neighborChanged(BlockState state,Level level,BlockPos pos,
	                            Block block,BlockPos fromPos,boolean isMoving){
		if(level.isClientSide) return;
		if(level.hasNeighborSignal(pos)){
			level.removeBlock(pos,false);
			spawnNuclearExplosion(level,pos);
		}
	}
	private void spawnNuclearExplosion(Level level,BlockPos pos){
		double x=pos.getX()+0.5;
		double y=pos.getY();
		double z=pos.getZ()+0.5;

		NuclearExplosionEntity explosion=new NuclearExplosionEntity(DifModEntities.NUCLEAR_EXPLOSION.get(),level);
		explosion.setPos(x,y,z);
		explosion.setRadius(40);
		level.addFreshEntity(explosion);

		NuclearMushroomEntity mushroom=new NuclearMushroomEntity(DifModEntities.NUCLEAR_MUSHROOM.get(),level);
		mushroom.setPos(x,y,z);
		level.addFreshEntity(mushroom);

		NuclearWaveEntity wave=new NuclearWaveEntity(DifModEntities.NUCLEAR_WAVE.get(),level);
		wave.setPos(x,y,z);
		level.addFreshEntity(wave);

		if(level instanceof ServerLevel serverLevel){
			NukeSoundEffect.play(serverLevel,x,y,z);
		}
	}
}