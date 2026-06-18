package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.entity.bomb.NuclearMushroomEntity;
import cz.maxtechnik.dif.entity.bomb.NuclearRadiationEntity;
import cz.maxtechnik.dif.entity.bomb.NuclearWaveEntity;
import cz.maxtechnik.dif.init.events.client.NukeSoundEffect;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
public class NukeSafe extends Block{
    public NukeSafe(BlockBehaviour.Properties properties){
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
    private void spawnNuclearExplosion(Level level,BlockPos pos){
        double x=pos.getX()+0.5;
        double y=pos.getY();
        double z=pos.getZ()+0.5;
        NuclearMushroomEntity mushroom=new NuclearMushroomEntity(DifModEntities.NUCLEAR_MUSHROOM.get(),level);
        mushroom.setPos(x,y,z);
        level.addFreshEntity(mushroom);
        NuclearWaveEntity wave=new NuclearWaveEntity(DifModEntities.NUCLEAR_WAVE.get(),level);
        wave.setPos(x,y,z);
        level.addFreshEntity(wave);
        NuclearRadiationEntity radiation=new NuclearRadiationEntity(DifModEntities.NUCLEAR_RADIATION.get(),level);
        radiation.setPos(x,y,z);
        level.addFreshEntity(radiation);
        if(level instanceof ServerLevel serverLevel) NukeSoundEffect.play(serverLevel,x,y,z);
    }
}