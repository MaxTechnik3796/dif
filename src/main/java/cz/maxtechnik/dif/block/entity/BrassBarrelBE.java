package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.BrassBarrel;
import cz.maxtechnik.dif.gui.menu.BrassBarrelMenu; // Důležitý import
import cz.maxtechnik.dif.init.misc.DifModBlockEntities;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container; // Důležitý import
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level; // Důležitý import
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class BrassBarrelBE extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> stacks = NonNullList.withSize(54, ItemStack.EMPTY);
    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());

    // --- OPRAVA ZDE: 'ContainerOpenersCounter' je nyní anonymní třída ---
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        // Tato metoda se zavolá, když se počet diváků změní z 0 na 1
        @Override
        protected void onOpen(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
            level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlock(pos, state.setValue(BrassBarrel.OPEN, true), 3);
        }

        // Tato metoda se zavolá, když se počet diváků změní na 0
        @Override
        protected void onClose(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
            level.playSound(null, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlock(pos, state.setValue(BrassBarrel.OPEN, false), 3);
        }

        // Tuto můžeme nechat prázdnou
        @Override
        protected void onOpenCountChanged(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, int p_155062_, int p_155063_) {
        }

        // Kontroluje, zda menu, které má hráč otevřené, patří k tomuto sudu
        @Override
        protected boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof BrassBarrelMenu menu) {
                // Musíme přidat getBlockEntity() do BrassBarrelMenu
                return menu.getBlockEntity() == BrassBarrelBE.this;
            }
            return false;
        }
    };
    // --- KONEC OPRAVY POČÍTADLA ---


    public BrassBarrelBE(BlockPos position, BlockState state) {
        super(DifModBlockEntities.BRASS_BARREL.get(), position, state);
    }


    // --- PŘEPSANÁ METODA: Zavolá se, když hráč otevře menu (z BrassBarrelMenu) ---
    @Override
    public void startOpen(Player player) {
        super.startOpen(player);
        if (this.level != null && !this.level.isClientSide) {
            // Jen řekneme počítadlu, aby se zvýšilo
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    // --- PŘEPSANÁ METODA: Zavolá se, když hráč zavře menu (z BrassBarrelMenu) ---
    @Override
    public void stopOpen(Player player) {
        super.stopOpen(player);
        if (this.level != null && !this.level.isClientSide) {
            // Jen řekneme počítadlu, aby se snížilo
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    // --- SMAZANÁ METODA 'recheckOpen' ---
    // Metoda 'recheckOpen' zde nebyla správně, smazal jsem ji.


    // ... (Zbytek tvých metod: load, saveAdditional, atd. zůstává stejný) ...

    @Override
    public void load(@NotNull CompoundTag compound) {
        super.load(compound);
        if (!this.tryLoadLootTable(compound))
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compound, this.stacks);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag compound) {
        super.saveAdditional(compound);
        if (!this.trySaveLootTable(compound)) {
            ContainerHelper.saveAllItems(compound, this.stacks);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    @Override
    public int getContainerSize() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks)
            if (!itemstack.isEmpty())
                return false;
        return true;
    }

    @Override
    public @NotNull Component getDefaultName() {
        return Component.translatable("container.tvujmod.brass_barrel");
    }

    @Override
    public @NotNull AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory) {
        return new BrassBarrelMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.tvujmod.brass_barrel");
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
        return true;
    }

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        return true;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == ForgeCapabilities.ITEM_HANDLER)
            return handlers[facing.ordinal()].cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<? extends IItemHandler> handler : handlers)
            handler.invalidate();
    }
}