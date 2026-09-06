package cz.maxtechnik.dif.block.entity.barrel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

public abstract class BaseBarrelBlockEntity extends RandomizableContainerBlockEntity {
	private final int containerSize;
	private NonNullList<ItemStack> items;
	private final IItemHandler itemHandler = new InvWrapper(this);
	private final BooleanProperty openProperty;
	private final SoundEvent openSound;
	private final SoundEvent closeSound;

	private void playSound(Level level, BlockPos pos, BlockState blockState, SoundEvent sound) {
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.5;
		double z = pos.getZ() + 0.5;
		if (blockState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)) {
			var normal = blockState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING).getNormal();
			x += normal.getX() * 0.5;
			y += normal.getY() * 0.5;
			z += normal.getZ() * 0.5;
		}
		float pitch = level.random.nextFloat() * 0.1F + 0.9F;
		level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, 0.5F, pitch);
	}

	private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
		@Override
		protected void onOpen(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState blockState) {
			playSound(level, pos, blockState, openSound);
			if (openProperty != null) {
				level.setBlock(pos, blockState.setValue(openProperty, true), 3);
			}
		}

		@Override
		protected void onClose(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState blockState) {
			playSound(level, pos, blockState, closeSound);
			if (openProperty != null) {
				level.setBlock(pos, blockState.setValue(openProperty, false), 3);
			}
		}

		@Override
		protected void openerCountChanged(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState blockState, int prevOpenCount, int openCount) {
		}

		@Override
		protected boolean isOwnContainer(Player player) {
			if (player.containerMenu instanceof ChestMenu menu) {
				return menu.getContainer() == BaseBarrelBlockEntity.this;
			}
			return false;
		}
	};

	public BaseBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int containerSize, BooleanProperty openProperty, SoundEvent openSound, SoundEvent closeSound) {
		super(type, pos, state);
		this.containerSize = containerSize;
		this.openProperty = openProperty;
		this.openSound = openSound;
		this.closeSound = closeSound;
		this.items = NonNullList.withSize(containerSize, ItemStack.EMPTY);
	}

	public BaseBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int containerSize, BooleanProperty openProperty) {
		this(type, pos, state, containerSize, openProperty, SoundEvents.BARREL_OPEN, SoundEvents.BARREL_CLOSE);
	}

	public IItemHandler getInventory() {
		return itemHandler;
	}

	@Override
	public int getContainerSize() {
		return this.containerSize;
	}

	@Override
	protected @NotNull NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> itemStacks) {
		this.items = itemStacks;
	}

	@Override
	public void startOpen(@NotNull Player player) {
		if (!this.remove && !player.isSpectator()) {
			assert this.getLevel() != null;
			this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
		}
	}

	@Override
	public void stopOpen(@NotNull Player player) {
		if (!this.remove && !player.isSpectator()) {
			assert this.getLevel() != null;
			this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
		}
	}

	public void recheckOpen() {
		if (!this.remove) {
			assert this.getLevel() != null;
			this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
		}
	}

	@Override
	protected void loadAdditional(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider provider) {
		super.loadAdditional(compound, provider);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		if (!this.tryLoadLootTable(compound)) {
			if (compound.contains("Items", CompoundTag.TAG_LIST)) {
				ContainerHelper.loadAllItems(compound, this.items, provider);
			} else if (compound.contains("inventory")) {
				ItemStackHandler handler = new ItemStackHandler(this.getContainerSize());
				handler.deserializeNBT(provider, compound.getCompound("inventory"));
				for (int i = 0; i < handler.getSlots() && i < this.items.size(); i++) {
					this.items.set(i, handler.getStackInSlot(i));
				}
			}
		}
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider provider) {
		super.saveAdditional(compound, provider);
		if (!this.trySaveLootTable(compound)) {
			ContainerHelper.saveAllItems(compound, this.items, provider);
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider) {
		return this.saveWithFullMetadata(provider);
	}
}
