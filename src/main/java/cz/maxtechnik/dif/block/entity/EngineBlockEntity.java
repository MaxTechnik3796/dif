package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.Engine;
import cz.maxtechnik.dif.block.EngineExtender;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.FuelType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import static cz.maxtechnik.dif.block.Engine.*;
import static cz.maxtechnik.dif.config.DifModServerConfig.*;
import static cz.maxtechnik.dif.init.basic.DifModBlocks.ENGINE_BASE;
import static cz.maxtechnik.dif.init.basic.DifModBlocks.ENGINE_PORTABLE;

public class EngineBlockEntity extends GeneratingKineticBlockEntity {
	public static final net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> DIESEL_TAG = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.FLUID, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "diesel"));
	public static final net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> GASOLINE_TAG = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.FLUID, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "gasoline"));
	public static final net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> LPG_TAG = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.FLUID, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "lpg"));
	public static final net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> HEAVY_FUEL_OIL_TAG = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.FLUID, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "heavy_fuel_oil"));

	boolean generating = false;
	float speed = 0F;
	float su = 0F;

	private boolean ext0 = false, ext1 = false, ext2 = false;
	private int cachedExtenders = 0;
	private boolean redstonePowered = false;
	private double consumptionPerTick = 0.0D;
	private boolean isDrainingInternally = false;

	private static final int FUEL_TICK_INTERVAL = 10;
	private int fuelTickCounter = 0;
	private double fuelAccumulator = 0.0D;

	public EngineBlockEntity(BlockPos pos, BlockState blockState) {
		super(DifModBlockEntities.ENGINE.get(), pos, blockState);
	}

	public final FluidTank fluidTank = new FluidTank(1000, stack -> {
		if (stack.isEmpty()) return false;
		boolean isPortable = isEngineBlockPortable(getBlockState().getBlock());
		if (stack.is(DIESEL_TAG)) return true;
		if (stack.is(GASOLINE_TAG)) return true;
		if (stack.is(LPG_TAG)) return true;
		return !isPortable && stack.is(HEAVY_FUEL_OIL_TAG);
	}) {
		@Override
		protected void onContentsChanged() {
			super.onContentsChanged();
			setChanged();
			if (!isDrainingInternally && level != null && !level.isClientSide) {
				recalculateStats();
			}
		}
	};

	@Override
	public float getGeneratedSpeed() {
		if (level == null) return 0F;
		BlockState state = getBlockState();
		if (!(state.getBlock() instanceof Engine)) return 0F;
		return generating ? speed * (state.getValue(INVERT) ? -1F : 1F) : 0F;
	}

	@Override
	public float calculateAddedStressCapacity() {
		return generating ? su : 0F;
	}

	@Override
	public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.read(tag, registries, clientPacket);
		if (tag.get("fluidTank") instanceof CompoundTag fluidTag) fluidTank.readFromNBT(registries, fluidTag);
		generating = tag.getBoolean("generating");
		speed = tag.getFloat("speed");
		su = tag.getFloat("su");
		ext0 = tag.getBoolean("ext0");
		ext1 = tag.getBoolean("ext1");
		ext2 = tag.getBoolean("ext2");
		cachedExtenders = tag.getInt("extenders");
	}

	@Override
	public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
		super.write(tag, registries, clientPacket);
		tag.put("fluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
		tag.putBoolean("generating", generating);
		tag.putFloat("speed", speed);
		tag.putFloat("su", su);
		tag.putBoolean("ext0", ext0);
		tag.putBoolean("ext1", ext1);
		tag.putBoolean("ext2", ext2);
		tag.putInt("extenders", cachedExtenders);
	}

	@Override
	public void initialize() {
		super.initialize();
		updateExtenders();
		if (level != null && !level.isClientSide) {
			recalculateStats();
		}
	}

	public void updateExtenders() {
		if (level == null) return;
		BlockState ownState = getBlockState();
		if (!(ownState.getBlock() instanceof Engine)) return;
		if (isEngineBlock(ownState.getBlock())) {
			Direction.Axis axis = ownState.getValue(FACING).getAxis();
			ext0 = isEngineExtender(worldPosition.above());
			if (axis == Direction.Axis.Z) {
				ext1 = isEngineExtender(worldPosition.east());
				ext2 = isEngineExtender(worldPosition.west());
			} else if (axis == Direction.Axis.X) {
				ext1 = isEngineExtender(worldPosition.north());
				ext2 = isEngineExtender(worldPosition.south());
			} else {
				ext1 = false;
				ext2 = false;
			}
			cachedExtenders = (ext0 ? 1 : 0) + (ext1 ? 1 : 0) + (ext2 ? 1 : 0);
		} else {
			ext0 = false;
			ext1 = false;
			ext2 = false;
			cachedExtenders = 1;
		}
		if (!level.isClientSide) {
			recalculateStats();
		}
	}

	public void recalculateStats() {
		if (level == null) return;
		boolean wasGenerating = generating;
		float prevSpeed = speed;
		float prevSu = su;

		redstonePowered = level.hasNeighborSignal(worldPosition);
		FuelType fuel = getFuelType();

		if (redstonePowered || fuel == FuelType.INVALID || fluidTank.isEmpty()) {
			generating = false;
			speed = 0F;
			su = 0F;
			consumptionPerTick = 0.0D;
		} else {
			boolean isPortable = isEngineBlockPortable(getBlockState().getBlock());
			int extenders = cachedExtenders;
			double baseRpm = 0.0D;
			double calculatedSu = 0.0D;
			double burnRatePerSec = 0.0D;

			if (fuel == FuelType.DIESEL) {
				baseRpm = ENGINE_DIESEL_RPM.get();
				calculatedSu = isPortable ? ENGINE_DIESEL_PORTABLE_SU.get() : ENGINE_DIESEL_SU.get() * extenders;
				burnRatePerSec = isPortable ? ENGINE_DIESEL_PORTABLE_CONSUMPTION.get() : ENGINE_DIESEL_CONSUMPTION.get() * (1.0 + (extenders - 1) * 0.5);
			} else if (fuel == FuelType.HEAVY_FUEL_OIL) {
				baseRpm = ENGINE_HEAVY_FUEL_OIL_RPM.get();
				calculatedSu = ENGINE_HEAVY_FUEL_OIL_SU.get() * extenders;
				burnRatePerSec = ENGINE_HEAVY_FUEL_OIL_CONSUMPTION.get() * (1.0 + (extenders - 1) * 0.5);
			} else if (fuel == FuelType.GASOLINE) {
				baseRpm = ENGINE_GASOLINE_RPM.get();
				calculatedSu = isPortable ? ENGINE_GASOLINE_PORTABLE_SU.get() : ENGINE_GASOLINE_SU.get() * extenders;
				burnRatePerSec = isPortable ? ENGINE_GASOLINE_PORTABLE_CONSUMPTION.get() : ENGINE_GASOLINE_CONSUMPTION.get() * (1.0 + (extenders - 1) * 0.5);
			} else if (fuel == FuelType.LPG) {
				baseRpm = ENGINE_LPG_RPM.get();
				calculatedSu = isPortable ? ENGINE_LPG_PORTABLE_SU.get() : ENGINE_LPG_SU.get() * extenders;
				burnRatePerSec = isPortable ? ENGINE_LPG_PORTABLE_CONSUMPTION.get() : ENGINE_LPG_CONSUMPTION.get() * (1.0 + (extenders - 1) * 0.5);
			}

			speed = (float) baseRpm;
			su = (float) calculatedSu;
			consumptionPerTick = burnRatePerSec / 20.0D;
			generating = true;
		}

		if (wasGenerating != generating || prevSpeed != speed || prevSu != su) {
			if (!level.isClientSide) {
				BlockState state = getBlockState();
				if (state.getBlock() instanceof Engine && state.getValue(ACTIVE) != generating) {
					KineticBlockEntity.switchToBlockState(level, worldPosition, state.setValue(ACTIVE, generating));
				}
				updateGeneratedRotation();
				sendData();
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (level == null || !(getBlockState().getBlock() instanceof Engine)) return;
		if (reActivateSource) {
			updateGeneratedRotation();
			reActivateSource = false;
		}
		if (!level.isClientSide) {
			if (generating && !redstonePowered) {
				fuelAccumulator += consumptionPerTick;
				if (++fuelTickCounter >= FUEL_TICK_INTERVAL) {
					fuelTickCounter = 0;
					int toDrain = (int) Math.floor(fuelAccumulator);
					if (toDrain > 0) {
						int available = fluidTank.getFluidAmount();
						int drainAmount = Math.min(toDrain, available);
						if (drainAmount > 0) {
							isDrainingInternally = true;
							try {
								fluidTank.drain(drainAmount, IFluidHandler.FluidAction.EXECUTE);
							} finally {
								isDrainingInternally = false;
							}
							fuelAccumulator -= drainAmount;
							if (fuelAccumulator < 0D) fuelAccumulator = 0D;
							if (fluidTank.isEmpty()) {
								recalculateStats();
							}
						}
					}
				}
			}
		}
		if (level.isClientSide && getBlockState().getValue(ACTIVE)) clientTick();
	}

	public void clientTick() {
		if (level == null) return;
		Direction.Axis axis = getBlockState().getValue(FACING).getAxis();
		double vel = 0.007;
		Block ownBlock = getBlockState().getBlock();
		if (axis.equals(Direction.Axis.Z)) {
			if (isEngineBlock(ownBlock)) {
				if (ext0) {
					particle(new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 2, worldPosition.getZ() + 0.3), new Vec3(0, vel, 0));
					particle(new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 2, worldPosition.getZ() + 0.5), new Vec3(0, vel, 0));
					particle(new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 2, worldPosition.getZ() + 0.7), new Vec3(0, vel, 0));
				}
				if (ext1) {
					particle(new Vec3(worldPosition.getX() + 2, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.3), new Vec3(vel * 2, vel, 0));
					particle(new Vec3(worldPosition.getX() + 2, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5), new Vec3(vel * 2, vel, 0));
					particle(new Vec3(worldPosition.getX() + 2, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.7), new Vec3(vel * 2, vel, 0));
				}
				if (ext2) {
					particle(new Vec3(worldPosition.getX() - 1, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.3), new Vec3(-vel * 2, vel, 0));
					particle(new Vec3(worldPosition.getX() - 1, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5), new Vec3(-vel * 2, vel, 0));
					particle(new Vec3(worldPosition.getX() - 1, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.7), new Vec3(-vel * 2, vel, 0));
				}
			} else if (isEngineBlockPortable(ownBlock)) {
				particle(new Vec3(worldPosition.getX() + 0.15, worldPosition.getY() + 0.99, worldPosition.getZ() + 0.22), new Vec3(-vel * 2, vel, 0));
				particle(new Vec3(worldPosition.getX() + 0.15, worldPosition.getY() + 0.99, worldPosition.getZ() + 0.78), new Vec3(-vel * 2, vel, 0));
				particle(new Vec3(worldPosition.getX() + 0.85, worldPosition.getY() + 0.99, worldPosition.getZ() + 0.22), new Vec3(vel * 2, vel, 0));
				particle(new Vec3(worldPosition.getX() + 0.85, worldPosition.getY() + 0.99, worldPosition.getZ() + 0.78), new Vec3(vel * 2, vel, 0));
			}
		} else if (axis.equals(Direction.Axis.X)) {
			if (isEngineBlock(ownBlock)) {
				if (ext0) {
					particle(new Vec3(worldPosition.getX() + 0.3, worldPosition.getY() + 2, worldPosition.getZ() + 0.5), new Vec3(0, vel, 0));
					particle(new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 2, worldPosition.getZ() + 0.5), new Vec3(0, vel, 0));
					particle(new Vec3(worldPosition.getX() + 0.7, worldPosition.getY() + 2, worldPosition.getZ() + 0.5), new Vec3(0, vel, 0));
				}
				if (ext1) {
					particle(new Vec3(worldPosition.getX() + 0.3, worldPosition.getY() + 0.5, worldPosition.getZ() - 1), new Vec3(0, vel, -vel * 2));
					particle(new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() - 1), new Vec3(0, vel, -vel * 2));
					particle(new Vec3(worldPosition.getX() + 0.7, worldPosition.getY() + 0.5, worldPosition.getZ() - 1), new Vec3(0, vel, -vel * 2));
				}
				if (ext2) {
					particle(new Vec3(worldPosition.getX() + 0.3, worldPosition.getY() + 0.5, worldPosition.getZ() + 2), new Vec3(0, vel, vel * 2));
					particle(new Vec3(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 2), new Vec3(0, vel, vel * 2));
					particle(new Vec3(worldPosition.getX() + 0.7, worldPosition.getY() + 0.5, worldPosition.getZ() + 2), new Vec3(0, vel, vel * 2));
				}
			} else if (isEngineBlockPortable(ownBlock)) {
				particle(new Vec3(worldPosition.getX() + 0.22, worldPosition.getY() + 0.99, worldPosition.getZ() + 0.15), new Vec3(0, vel, -vel * 2));
				particle(new Vec3(worldPosition.getX() + 0.75, worldPosition.getY() + 0.99, worldPosition.getZ() + 0.15), new Vec3(0, vel, -vel * 2));
				particle(new Vec3(worldPosition.getX() + 0.22, worldPosition.getY() + 0.99, worldPosition.getZ() + 0.85), new Vec3(0, vel, vel * 2));
				particle(new Vec3(worldPosition.getX() + 0.78, worldPosition.getY() + 0.99, worldPosition.getZ() + 0.85), new Vec3(0, vel, vel * 2));
			}
		}
	}

	public FuelType getFuelFromTank() {
		if (fluidTank.isEmpty()) return FuelType.INVALID;
		net.neoforged.neoforge.fluids.FluidStack stack = fluidTank.getFluid();
		if (stack.is(DIESEL_TAG)) return FuelType.DIESEL;
		if (stack.is(GASOLINE_TAG)) return FuelType.GASOLINE;
		if (stack.is(LPG_TAG)) return FuelType.LPG;
		if (stack.is(HEAVY_FUEL_OIL_TAG)) return FuelType.HEAVY_FUEL_OIL;
		return FuelType.INVALID;
	}

	public FuelType getFuelType() {
		if (fluidTank.isEmpty()) return FuelType.INVALID;
		Block ownBlock = getBlockState().getBlock();
		if (isEngineBlock(ownBlock) && cachedExtenders == 0) return FuelType.INVALID;
		FuelType fuel = getFuelFromTank();
		if (isEngineBlockPortable(ownBlock) && fuel == FuelType.HEAVY_FUEL_OIL) return FuelType.INVALID;
		return fuel;
	}

	public boolean isEngineExtender(BlockPos pos) {
		if (level == null || !level.isLoaded(pos)) return false;
		return level.getBlockState(pos).getBlock() instanceof EngineExtender;
	}

	private boolean isEngineBlock(Block block) {
		return block.equals(ENGINE_BASE.get());
	}

	private boolean isEngineBlockPortable(Block block) {
		return block.equals(ENGINE_PORTABLE.get());
	}

	public void particle(Vec3 pos, Vec3 velocity) {
		if (level == null) return;
		if (DifMod.rouletteBoolean(4))
			level.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
	}

	@Override
	public boolean addToGoggleTooltip(java.util.List<net.minecraft.network.chat.Component> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		if (!fluidTank.isEmpty()) {
			String fluidName = fluidTank.getFluid().getHoverName().getString();
			tooltip.add(net.minecraft.network.chat.Component.literal("     Fuel: ").withStyle(net.minecraft.ChatFormatting.GRAY)
					.append(net.minecraft.network.chat.Component.literal(fluidName + " (" + fluidTank.getFluidAmount() + " / " + fluidTank.getCapacity() + " mB)").withStyle(net.minecraft.ChatFormatting.AQUA)));
		} else {
			tooltip.add(net.minecraft.network.chat.Component.literal("     Fuel: ").withStyle(net.minecraft.ChatFormatting.GRAY)
					.append(net.minecraft.network.chat.Component.literal("Empty (0 / " + fluidTank.getCapacity() + " mB)").withStyle(net.minecraft.ChatFormatting.DARK_GRAY)));
		}
		FuelType fuel = getFuelType();
		if (fuel != FuelType.INVALID) {
			boolean isPortable = isEngineBlockPortable(getBlockState().getBlock());
			int extenders = cachedExtenders;
			double burnRatePerSec = 0.0D;
			if (fuel == FuelType.DIESEL) {
				burnRatePerSec = isPortable ? ENGINE_DIESEL_PORTABLE_CONSUMPTION.get() : ENGINE_DIESEL_CONSUMPTION.get() * (1.0 + (extenders - 1) * 0.5);
			} else if (fuel == FuelType.GASOLINE) {
				burnRatePerSec = isPortable ? ENGINE_GASOLINE_PORTABLE_CONSUMPTION.get() : ENGINE_GASOLINE_CONSUMPTION.get() * (1.0 + (extenders - 1) * 0.5);
			} else if (fuel == FuelType.LPG) {
				burnRatePerSec = isPortable ? ENGINE_LPG_PORTABLE_CONSUMPTION.get() : ENGINE_LPG_CONSUMPTION.get() * (1.0 + (extenders - 1) * 0.5);
			} else if (fuel == FuelType.HEAVY_FUEL_OIL) {
				burnRatePerSec = ENGINE_HEAVY_FUEL_OIL_CONSUMPTION.get() * (1.0 + (extenders - 1) * 0.5);
			}
			String burnRateStr = String.format(java.util.Locale.US, "%.2f", burnRatePerSec);
			tooltip.add(net.minecraft.network.chat.Component.literal("     Burn Rate: ").withStyle(net.minecraft.ChatFormatting.GRAY)
					.append(net.minecraft.network.chat.Component.literal(burnRateStr + " mB/s").withStyle(net.minecraft.ChatFormatting.GOLD)));
		} else {
			tooltip.add(net.minecraft.network.chat.Component.literal("     Burn Rate: ").withStyle(net.minecraft.ChatFormatting.GRAY)
					.append(net.minecraft.network.chat.Component.literal("0.00 mB/s").withStyle(net.minecraft.ChatFormatting.DARK_GRAY)));
		}
		return true;
	}
}