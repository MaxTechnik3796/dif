package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.item.modular.v2.ModularReforge;
import cz.maxtechnik.dif.item.modular.v2.ModularTool;
import cz.maxtechnik.dif.item.modular.v2.ModularTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
public class ModularReforgeTableBlockEntity extends BlockEntity{
	private long lastMergeTime=-20L;
	private final ItemStackHandler inventory=new ItemStackHandler(3){
		@Override
		public boolean isItemValid(int slot,@NotNull ItemStack itemStack){
			return switch(slot){
				case 0 -> itemStack.getItem() instanceof ModularTool;
				case 1 -> itemStack.is(DifModItems.MODULAR_TEMPLATE_NORMAL.get());
				case 2 -> itemStack.is(DifModItems.MODULAR_REFORGE_STONE.get());
				default -> false;
			};
		}
		@Override
		protected void onContentsChanged(int slot){
			super.onContentsChanged(slot);
			setChanged();
			if(level!=null)
				level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
		}
	};
	public ModularReforgeTableBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.MODULAR_REFORGE_TABLE.get(),pos,state);
	}
	public ItemStackHandler getInventory(){
		return this.inventory;
	}
	public boolean tryInsertItem(ItemStack stack,Player player,InteractionHand hand){
		if(stack.isEmpty()) return false;
		int slot=getPreferredSlot(stack);
		if(slot<0) return false;
		int before=stack.getCount();
		ItemStack remaining=this.inventory.insertItem(slot,stack,false);
		if(remaining.getCount()==before) return false;
		if(player!=null&&hand!=null)
			player.setItemInHand(hand,remaining);
		return true;
	}
	/**
	 * Vytáhne první neprázdný slot (v pořadí TOOL, TEMPLATE, CATALYST) - volá se
	 * při pravém kliku prázdnou rukou. Pokud právě běží merge (rituál), přeruší ho.
	 */
	public boolean tryExtractItem(Player player){
		for(int slot=0;slot<3;slot++){
			ItemStack stack=this.inventory.getStackInSlot(slot);
			if(!stack.isEmpty()){
				ItemStack extracted=this.inventory.extractItem(slot,stack.getCount(),false);
				if(player!=null){
					if(!player.getInventory().add(extracted))
						player.drop(extracted,false);
				}
				return true;
			}
		}
		return false;
	}
	/** Vysype všechny itemy do světa - volat při rozbití bloku (Block#onRemove). */
	public void dropAllItems(Level level,BlockPos pos){
		for(int slot=0;slot<3;slot++){
			ItemStack stack=this.inventory.getStackInSlot(slot);
			if(!stack.isEmpty()){
				ItemEntity itemEntity=new ItemEntity(level,pos.getX()+0.5D,pos.getY()+0.5D,pos.getZ()+0.5D,stack.copy());
				level.addFreshEntity(itemEntity);
				this.inventory.setStackInSlot(slot,ItemStack.EMPTY);
			}
		}
	}
	private int getPreferredSlot(ItemStack stack){
		if(stack.getItem() instanceof ModularTool)
			return this.inventory.getStackInSlot(0).isEmpty()?0:-1;
		if(stack.is(DifModItems.MODULAR_TEMPLATE_HYPER.get()))
			return this.inventory.getStackInSlot(1).isEmpty()?1:-1;
		if(stack.is(Items.IRON_INGOT))
			return this.inventory.getStackInSlot(2).isEmpty()?2:-1;
		return -1;
	}
	public boolean canMerge(){
		return !this.inventory.getStackInSlot(0).isEmpty() && !this.inventory.getStackInSlot(1).isEmpty() && !this.inventory.getStackInSlot(2).isEmpty();
	}

	public boolean finishMerge(){
		if(this.level==null) return false;
		long gameTime=this.level.getGameTime();
		if(gameTime-this.lastMergeTime<20L) return false;
		if(!canMerge()) return false;
		ItemStack toolStack=this.inventory.getStackInSlot(0);
		if(toolStack.isEmpty()||!(toolStack.getItem() instanceof ModularTool)) return false;
		ModularReforge reforge=pickRandomReforge(toolStack);
		if(reforge==ModularReforge.NONE) return false;
		ModularTool.setReforge(this.level.registryAccess(),toolStack,reforge);
		this.inventory.extractItem(1,1,false);
		this.inventory.extractItem(2,1,false);
		this.lastMergeTime=gameTime;
		setChanged();
		this.level.sendBlockUpdated(this.worldPosition,this.getBlockState(),this.getBlockState(),3);
		return true;
	}
	private ModularReforge pickRandomReforge(ItemStack toolStack){
		ModularTools toolType=ModularTool.getToolType(toolStack);
		List<ModularReforge> valid=Arrays.stream(ModularReforge.values())
				.filter(reforge->reforge!=ModularReforge.NONE&&Arrays.stream(reforge.getTools()).anyMatch(tool->tool==toolType))
				.toList();
		if(valid.isEmpty()) return ModularReforge.NONE;
		return valid.get(this.level!=null?this.level.getRandom().nextInt(valid.size()):0);
	}
	@Override
	public void loadAdditional(@NotNull CompoundTag tag,HolderLookup.@NotNull Provider provider){
		super.loadAdditional(tag,provider);
		this.inventory.deserializeNBT(provider,tag.getCompound("inventory"));
	}
	@Override
	public void saveAdditional(@NotNull CompoundTag tag,HolderLookup.@NotNull Provider provider){
		super.saveAdditional(tag,provider);
		tag.put("inventory",this.inventory.serializeNBT(provider));
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider provider){
		CompoundTag tag=new CompoundTag();
		saveAdditional(tag,provider);
		return tag;
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
}