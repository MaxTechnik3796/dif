package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.entity.bomb.NuclearExplosionEntity;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
public class Nuke extends Block{
	public Nuke(){
		super(Properties.of().strength(5F,1200F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
	}
	@Override
	public int getLightBlock(@NotNull BlockState blockState,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public float getShadeBrightness(@NotNull BlockState blockState,@NotNull BlockGetter blockGetter,@NotNull BlockPos pos){
		return 1F;
	}
	@Override
	public void onPlace(@NotNull BlockState blockState,Level level,@NotNull BlockPos pos,@NotNull BlockState oldState,boolean isMoving){
		if(!level.isClientSide) checkAndDetonate(level,pos);
	}
	@Override
	public void neighborChanged(@NotNull BlockState blockState,Level level,@NotNull BlockPos pos,@NotNull Block block,@NotNull BlockPos fromPos,boolean isMoving){
		if(!level.isClientSide) checkAndDetonate(level,pos);
	}
	private static void checkAndDetonate(Level level,BlockPos pos){
		if(level.hasNeighborSignal(pos)){
			level.removeBlock(pos,false);
			spawnNuclearExplosion(level,pos);
		}
	}
	public static void spawnNuclearExplosion(Level level,BlockPos pos){
		NuclearExplosionEntity explosion=new NuclearExplosionEntity(DifModEntities.NUCLEAR_EXPLOSION.get(),level);
		explosion.setPos(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5);
		level.addFreshEntity(explosion);
	}
}