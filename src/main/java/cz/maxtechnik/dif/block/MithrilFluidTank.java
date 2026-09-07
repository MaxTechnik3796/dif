package cz.maxtechnik.dif.block;

import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class MithrilFluidTank extends FluidTankBlock {

	public MithrilFluidTank() {
		super(BlockBehaviour.Properties.of()
				.strength(4.0F, 5.0F)
				.sound(SoundType.METAL)
				.noOcclusion()
				.isRedstoneConductor((p1, p2, p3) -> true)
				.requiresCorrectToolForDrops(), false);
	}

	@Override
	public BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType() {
		return DifModBlockEntities.MITHRIL_FLUID_TANK.get();
	}

	// ==================== BLOCK ENTITY ====================
	public static class Entity extends FluidTankBlockEntity {
		public Entity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
			super(type, pos, state);
		}

		public static int getCapacityMultiplier() {
			try {
				return DifModServerConfig.MITHRIL_FLUID_TANK_CAPACITY.get() * 1000;
			} catch (Exception e) {
				return 32000;
			}
		}

		@Override
		protected SmartFluidTank createInventory() {
			return new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
		}

		@Override
		public void applyFluidTankSize(int blocks) {
			tankInventory.setCapacity(blocks * getCapacityMultiplier());
			int overflow = tankInventory.getFluidAmount() - tankInventory.getCapacity();
			if (overflow > 0)
				tankInventory.drain(overflow, FluidAction.EXECUTE);
			forceFluidLevelUpdate = true;
		}

		@Override
		public int getTankSize(int tank) {
			return getCapacityMultiplier();
		}

		@Override
		protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
			super.read(compound, registries, clientPacket);
			if (isController()) {
				tankInventory.setCapacity(getTotalTankSize() * getCapacityMultiplier());
			}
		}

		public IFluidHandler getFluidCapability() {
			return fluidCapability;
		}
	}
}
