package cz.maxtechnik.dif.block.entity.barrel;

import cz.maxtechnik.dif.gui.menu.AndesiteBarrelMenu;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.stream.IntStream;
public class AndesiteBarrelBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer{
	private NonNullList<ItemStack> stacks=NonNullList.withSize(36,ItemStack.EMPTY);
	private final ContainerOpenersCounter openersCounter=new ContainerOpenersCounter(){
		@Override
		protected void onOpen(@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState state){
			level.playSound(null,pos,SoundEvents.BARREL_OPEN,SoundSource.BLOCKS,1.0F,1.0F);
			level.setBlock(pos,state.setValue(cz.maxtechnik.dif.block.barrel.AndesiteBarrel.OPEN,true),3);
		}
		@Override
		protected void onClose(@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState state){
			level.playSound(null,pos,SoundEvents.BARREL_CLOSE,SoundSource.BLOCKS,1.0F,1.0F);
			level.setBlock(pos,state.setValue(cz.maxtechnik.dif.block.barrel.AndesiteBarrel.OPEN,false),3);
		}
		@Override
		protected void openerCountChanged(@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState state,int p_155062_,int p_155063_){
		}
		@Override
		protected boolean isOwnContainer(Player player){
			if(player.containerMenu instanceof AndesiteBarrelMenu menu){
				return menu.getBlockEntity()==AndesiteBarrelBlockEntity.this;
			}
			return false;
		}
	};
	public AndesiteBarrelBlockEntity(BlockPos position,BlockState state){
		super(DifModBlockEntities.ANDESITE_BARREL.get(),position,state);
	}
	@Override
	public void startOpen(@NotNull Player player){
		super.startOpen(player);
		if(this.level!=null&&!this.level.isClientSide){
			assert this.getLevel()!=null;
			this.openersCounter.incrementOpeners(player,this.getLevel(),this.getBlockPos(),this.getBlockState());
		}
	}
	@Override
	public void stopOpen(@NotNull Player player){
		super.stopOpen(player);
		if(this.level!=null&&!this.level.isClientSide){
			assert this.getLevel()!=null;
			this.openersCounter.decrementOpeners(player,this.getLevel(),this.getBlockPos(),this.getBlockState());
		}
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider provider){
		super.loadAdditional(compound, provider);
		if(!this.tryLoadLootTable(compound))
			this.stacks=NonNullList.withSize(this.getContainerSize(),ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound,this.stacks, provider);
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag compound, @NotNull HolderLookup.Provider provider){
		super.saveAdditional(compound, provider);
		if(!this.trySaveLootTable(compound)){
			ContainerHelper.saveAllItems(compound,this.stacks, provider);
		}
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider){
		return this.saveWithFullMetadata(provider);
	}
	@Override
	public int getContainerSize(){
		return stacks.size();
	}
	@Override
	public boolean isEmpty(){
		for(ItemStack itemstack: this.stacks)
			if(!itemstack.isEmpty())
				return false;
		return true;
	}
	@Override
	public @NotNull Component getDefaultName(){
		return Component.translatable("container.dif.andesite_barrel");
	}
	@Override
	public @NotNull AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory,@NotNull Player player){
		return new AndesiteBarrelMenu(id,inventory,new RegistryFriendlyByteBuf(Unpooled.buffer(), this.level.registryAccess()).writeBlockPos(this.worldPosition));
	}
	@Override
	public @NotNull Component getDisplayName(){
		return Component.translatable("container.dif.andesite_barrel");
	}
	@Override
	protected @NotNull NonNullList<ItemStack> getItems(){
		return this.stacks;
	}
	@Override
	protected void setItems(@NotNull NonNullList<ItemStack> stacks){
		this.stacks=stacks;
	}
	@Override
	public boolean canPlaceItem(int index,@NotNull ItemStack stack){
		return true;
	}
	@Override
	public int @NotNull [] getSlotsForFace(@NotNull Direction side){
		return IntStream.range(0,this.getContainerSize()).toArray();
	}
	@Override
	public boolean canPlaceItemThroughFace(int index,@NotNull ItemStack stack,@Nullable Direction direction){
		return this.canPlaceItem(index,stack);
	}
	@Override
	public boolean canTakeItemThroughFace(int index,@NotNull ItemStack stack,@NotNull Direction direction){
		return true;
	}
}