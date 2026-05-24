package cz.maxtechnik.dif.block.generator.steam_generator;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SteamGeneratorBlockEntity extends GeneratingKineticBlockEntity {

	public SteamGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
		super(DifModBlockEntities.STEAM_GENERATOR.get(), pos, blockState);
	}

	// Rychlost generování (96 RPM)
	@Override
	public float getGeneratedSpeed() {
		return 96.0F;
	}

	// Síla (Torque) generování (512 SU)
	@Override
	public float calculateAddedStressCapacity() {
		this.lastCapacityProvided = 512.0F;
		return this.lastCapacityProvided;
	}

	// KLÍČOVÉ PRO RENDER: Vynutí zobrazení a rotaci standardní Create hřídele (shaftu)
	@Override
	public BlockState getRenderedBlockState() {
		if (getBlockState().hasProperty(SteamGeneratorBlock.AXIS)) {
			return AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarBlock.AXIS, getBlockState().getValue(SteamGeneratorBlock.AXIS));
		}
		return AllBlocks.SHAFT.getDefaultState();
	}

	// Probudí Create síť hned po položení bloku do světa
	@Override
	public void initialize() {
		super.initialize();
		assert level!=null;
		if (!level.isClientSide) {
			updateGeneratedRotation();
		}
	}
}