package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.item.modular.v2.ModularReforge;
import cz.maxtechnik.dif.item.modular.v2.ModularTool;
import cz.maxtechnik.dif.item.modular.v2.ModularTools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.Arrays;
import java.util.List;

public class ModularReforgeTableBlockEntity extends BlockEntity{
	public static final int SLOT_TOOL=0;
	public static final int SLOT_TEMPLATE=1;
	public static final int SLOT_CATALYST=2;
	public static final int SLOT_COUNT=3;
	public static final int ANIMATION_DURATION=40;
	private final ItemStackHandler inventory=new ItemStackHandler(SLOT_COUNT){
		@Override
		public boolean isItemValid(int slot,ItemStack stack){
			return switch(slot){
				case SLOT_TOOL -> stack.getItem() instanceof ModularTool;
				case SLOT_TEMPLATE -> stack.is(DifModItems.MODULAR_TEMPLATE_HYPER.get());
				case SLOT_CATALYST -> stack.is(Items.IRON_INGOT);
				default -> false;
			};
		}
		@Override
		public int getSlotLimit(int slot){
			return 1;
		}
		@Override
		protected void onContentsChanged(int slot){
			super.onContentsChanged(slot);
			setChanged();
			if(level!=null)
				level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
		}
	};
	private boolean mergeActive=false;
	private int mergeProgress=0;
	public ModularReforgeTableBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.MODULAR_REFORGE_TABLE.get(),pos,state);
	}
	public ItemStackHandler getInventory(){
		return this.inventory;
	}
	public boolean isMerging(){
		return this.mergeActive;
	}
	public int getMergeProgress(){
		return this.mergeProgress;
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
		maybeStartMerge();
		return true;
	}
	/**
	 * Vytáhne první neprázdný slot (v pořadí TOOL, TEMPLATE, CATALYST) - volá se
	 * při pravém kliku prázdnou rukou. Pokud právě běží merge (rituál), přeruší ho.
	 */
	public boolean tryExtractItem(Player player){
		for(int slot=0;slot<SLOT_COUNT;slot++){
			ItemStack stack=this.inventory.getStackInSlot(slot);
			if(!stack.isEmpty()){
				ItemStack extracted=this.inventory.extractItem(slot,stack.getCount(),false);
				cancelMerge();
				if(player!=null){
					if(!player.getInventory().add(extracted))
						player.drop(extracted,false);
				}
				return true;
			}
		}
		return false;
	}
	private void cancelMerge(){
		if(!this.mergeActive) return;
		this.mergeActive=false;
		this.mergeProgress=0;
		setChanged();
		if(this.level!=null)
			this.level.sendBlockUpdated(this.worldPosition,this.getBlockState(),this.getBlockState(),3);
	}
	/** Vysype všechny itemy do světa - volat při rozbití bloku (Block#onRemove). */
	public void dropAllItems(Level level,BlockPos pos){
		for(int slot=0;slot<SLOT_COUNT;slot++){
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
			return this.inventory.getStackInSlot(SLOT_TOOL).isEmpty()?SLOT_TOOL:-1;
		if(stack.is(DifModItems.MODULAR_TEMPLATE_HYPER.get()))
			return this.inventory.getStackInSlot(SLOT_TEMPLATE).isEmpty()?SLOT_TEMPLATE:-1;
		if(stack.is(Items.IRON_INGOT))
			return this.inventory.getStackInSlot(SLOT_CATALYST).isEmpty()?SLOT_CATALYST:-1;
		return -1;
	}
	private void maybeStartMerge(){
		if(this.mergeActive) return;
		if(this.inventory.getStackInSlot(SLOT_TOOL).isEmpty()||this.inventory.getStackInSlot(SLOT_TEMPLATE).isEmpty()||this.inventory.getStackInSlot(SLOT_CATALYST).isEmpty()) return;
		this.mergeActive=true;
		this.mergeProgress=0;
		setChanged();
		if(this.level!=null)
			this.level.sendBlockUpdated(this.worldPosition,this.getBlockState(),this.getBlockState(),3);
	}
	public void serverTick(){
		if(!this.mergeActive) return;
		this.mergeProgress++;
		if(this.mergeProgress>=ANIMATION_DURATION){
			finishMerge();
			return;
		}
		if(this.level!=null&&this.level.getGameTime()%4L==0L)
			spawnParticles(0.03D,0.2D,0.02D,4);
		setChanged();
	}
	private void finishMerge(){
		if(this.level==null) return;
		ItemStack toolStack=this.inventory.getStackInSlot(SLOT_TOOL);
		if(!toolStack.isEmpty()&&toolStack.getItem() instanceof ModularTool){
			ModularReforge reforge=pickRandomReforge(toolStack);
			if(reforge!=ModularReforge.NONE){
				ModularTool.setReforge(this.level.registryAccess(),toolStack,reforge);
				this.level.playSound(null,this.worldPosition,SoundEvents.ENCHANTMENT_TABLE_USE,SoundSource.BLOCKS,1.0F,1.0F);
				spawnParticles(0.08D,0.25D,0.05D,24);
				spawnEndRodSplash(8);
			}
		}
		this.inventory.setStackInSlot(SLOT_TEMPLATE,ItemStack.EMPTY);
		this.inventory.setStackInSlot(SLOT_CATALYST,ItemStack.EMPTY);
		this.mergeActive=false;
		this.mergeProgress=0;
		setChanged();
		this.level.sendBlockUpdated(this.worldPosition,this.getBlockState(),this.getBlockState(),3);
	}
	private ModularReforge pickRandomReforge(ItemStack toolStack){
		ModularTools toolType=ModularTool.getToolType(toolStack);
		List<ModularReforge> valid=Arrays.stream(ModularReforge.values())
				.filter(reforge->reforge!=ModularReforge.NONE&&Arrays.stream(reforge.getTools()).anyMatch(tool->tool==toolType))
				.toList();
		if(valid.isEmpty()) return ModularReforge.NONE;
		return valid.get(this.level!=null?this.level.getRandom().nextInt(valid.size()):0);
	}
	/** Výraznější "splash" pop efekt v okamžiku dokončení - END_ROD částice létající do všech stran. */
	private void spawnEndRodSplash(int count){
		if(this.level==null) return;
		for(int i=0;i<count;i++)
			this.level.addParticle(ParticleTypes.END_ROD,
					this.worldPosition.getX()+0.5D,
					this.worldPosition.getY()+1.1D,
					this.worldPosition.getZ()+0.5D,
					(this.level.random.nextDouble()-0.5D)*0.3D,
					this.level.random.nextDouble()*0.3D+0.05D,
					(this.level.random.nextDouble()-0.5D)*0.3D);
	}
	private void spawnParticles(double spreadX,double spreadY,double spreadZ,int count){
		if(this.level==null) return;
		for(int i=0;i<count;i++)
			this.level.addParticle(ParticleTypes.ENCHANT,
					this.worldPosition.getX()+0.5D,
					this.worldPosition.getY()+1.0D,
					this.worldPosition.getZ()+0.5D,
					(this.level.random.nextDouble()-0.5D)*spreadX,
					this.level.random.nextDouble()*spreadY,
					(this.level.random.nextDouble()-0.5D)*spreadZ);
	}
	@Override
	public void loadAdditional(CompoundTag tag,HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		this.inventory.deserializeNBT(provider,tag.getCompound("inventory"));
		this.mergeActive=tag.getBoolean("mergeActive");
		this.mergeProgress=tag.getInt("mergeProgress");
	}
	@Override
	public void saveAdditional(CompoundTag tag,HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		tag.put("inventory",this.inventory.serializeNBT(provider));
		tag.putBoolean("mergeActive",this.mergeActive);
		tag.putInt("mergeProgress",this.mergeProgress);
	}
	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider){
		CompoundTag tag=new CompoundTag();
		saveAdditional(tag,provider);
		return tag;
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
}