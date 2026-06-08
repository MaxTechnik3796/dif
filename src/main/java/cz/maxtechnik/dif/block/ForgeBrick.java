package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.AbstractMultiblockBrickBlockEntity;
import cz.maxtechnik.dif.block.entity.ForgeBrickBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class ForgeBrick extends AbstractMultiblockBrick{
	public ForgeBrick(BlockBehaviour.Properties properties){
		super(properties);
	}
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return DifModBlockEntities.FORGE_BRICK.get().create(pos,blockState);
	}
	@Override
	protected @Nullable AbstractMultiblockBrickBlockEntity getBlockEntityFromPos(Level level,BlockPos pos){
		return level.getBlockEntity(pos) instanceof ForgeBrickBlockEntity be?be:null;
	}
}