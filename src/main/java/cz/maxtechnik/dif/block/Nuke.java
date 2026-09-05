package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.entity.bomb.NuclearExplosionEntity;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
public class Nuke extends Block{
	public Nuke(BlockBehaviour.Properties properties){
		super(properties);
	}
	@Override
	public void neighborChanged(@NotNull BlockState state,Level level,@NotNull BlockPos pos,@NotNull Block block,@NotNull BlockPos fromPos,boolean isMoving){
		if(level.isClientSide) return;
		if(level.hasNeighborSignal(pos)){
			level.removeBlock(pos,false);
			spawnNuclearExplosion(level,pos);
		}
	}
	public static void spawnNuclearExplosion(Level level,BlockPos pos){
		double x=pos.getX()+0.5;
		double y=pos.getY();
		double z=pos.getZ()+0.5;
		NuclearExplosionEntity explosion=new NuclearExplosionEntity(DifModEntities.NUCLEAR_EXPLOSION.get(),level);
		explosion.setPos(x,y,z);
		level.addFreshEntity(explosion);
	}
}