package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public class SteamGeneratorBlockEntity extends GeneratingKineticBlockEntity{
	public SteamGeneratorBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.STEAM_GENERATOR.get(),pos,blockState);
	}
	@Override
	public float getGeneratedSpeed(){
		return 96F;
	}
	@Override
	public float calculateAddedStressCapacity(){
		this.lastCapacityProvided=512F;
		return this.lastCapacityProvided;
	}
	@Override
	public void initialize(){
		super.initialize();
		if(level!=null&&!level.isClientSide) updateGeneratedRotation();
	}
}