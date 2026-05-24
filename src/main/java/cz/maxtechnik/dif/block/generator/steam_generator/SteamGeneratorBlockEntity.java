package cz.maxtechnik.dif.block.generator.steam_generator;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public class SteamGeneratorBlockEntity extends GeneratingKineticBlockEntity{
	public SteamGeneratorBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.STEAM_GENERATOR.get(),pos,blockState);
	}
	// Rychlost generování (96 RPM)
	@Override
	public float getGeneratedSpeed(){
		return 96.0F;
	}
	// Síla / Stress kapacita (512 SU)
	@Override
	public float calculateAddedStressCapacity(){
		this.lastCapacityProvided=512.0F;
		return this.lastCapacityProvided;
	}
	// Probudí Create síť hned po položení bloku do světa
	@Override
	public void initialize(){
		super.initialize();
		if(level!=null&&!level.isClientSide){
			updateGeneratedRotation();
		}
	}
}
