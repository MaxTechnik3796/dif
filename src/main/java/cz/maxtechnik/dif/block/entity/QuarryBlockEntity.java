package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.Quarry;
import cz.maxtechnik.dif.gui.menu.QuarryMenu;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.events.QuarryStats;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.item.quarry.EngineItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class QuarryBlockEntity extends BlockEntity implements MenuProvider{
	public enum State{NO_ENERGY,CLEARING,BUILDING_FRAME,MINING,DONE}
	private static final int ENERGY_CAPACITY=QuarryStats.QUARRY_ENERGY_CAPACITY;
	private static final int ENERGY_INPUT=QuarryStats.QUARRY_ENERGY_INPUT;
	private static final int FRAME_CHECK_INTERVAL=40;
	private static final int FE_TICK_INTERVAL=5;
	private static final int FRAME_HEIGHT=3;
	public static final int DEFAULT_RANGE=5;
	public static final int MAX_AREA_SIDE=128;

	private State quarryState=State.NO_ENERGY;
	private State activeState=State.CLEARING;
	private BlockPos miningPos;
	private int frameCheckTimer=0;
	private boolean hasFEThisCycle=false;
	private float miningProgressAcc=0f;

	private int currentSection=0;
	private int totalSections=1;
	private int sectionsX=1,sectionsZ=1;
	private int sectionMinX,sectionMaxX,sectionMinZ,sectionMaxZ;
	private final List<ChunkPos> forcedChunks=new ArrayList<>();
	private boolean chunksNeedReload=false;
	private final ArrayList<BlockPos> workQueue=new ArrayList<>();
	private int workIndex=0;
	private int customHalfX=-1;
	private int customHalfZ=-1;
	@Nullable private BlockPos customCenter=null;
	@Nullable private List<BlockPos> cachedFramePos=null;
	@Nullable private BlockPos cachedCenter=null;
	@Nullable private Direction cachedFacing=null;
	private int cachedHalfX=Integer.MIN_VALUE;
	private int cachedHalfZ=Integer.MIN_VALUE;

	private boolean hasSilkTouch=false;
	private boolean hasLiquidRemover=false;

	private final ItemStackHandler inventory=new ItemStackHandler(3){
		@Override
		protected void onContentsChanged(int slot){
			rebuildUpgradeCache();
			setChanged();
		}
	};

	// NOVÉ – prochází sloty 1 i 2, nekončí po prvním nálezu:
	private void rebuildUpgradeCache(){
		hasSilkTouch=false;
		hasLiquidRemover=false;
		for(int i=1;i<=2;i++){
			ItemStack stack=inventory.getStackInSlot(i);
			if(stack.isEmpty()) continue;
			if(stack.getItem()==Items.SPONGE){
				hasLiquidRemover=true;
			} else if(stack.is(Items.ENCHANTED_BOOK)){
				var stored=stack.get(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS);
				if(stored!=null) stored.keySet().forEach(holder->{
					if(holder.is(Enchantments.SILK_TOUCH)) hasSilkTouch=true;
				});
			}
		}
	}

	private int feTickTimer=0;
	private final EnergyStorage energy=new EnergyStorage(ENERGY_CAPACITY,ENERGY_INPUT,ENERGY_CAPACITY);

	public final ContainerData dataAccess=new ContainerData(){
		@Override
		public int get(int index){
			return switch(index){
				case 0 -> quarryState.ordinal();
				case 1 -> getTotalQP();
				case 2 -> getTotalFECost();
				case 3 -> getFrameHalfX()*2+1;
				case 4 -> getFrameHalfZ()*2+1;
				case 5 -> getTotalQP()==0 ? 1 : 0;
				default -> 0;
			};
		}
		@Override public void set(int index,int value){}
		@Override public int getCount(){ return 6; }
	};

	public QuarryBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.QUARRY.get(),pos,state);
	}

	public int getTotalQP(){
		int qp=0;
		for(int i=0;i<3;i++){
			ItemStack s=inventory.getStackInSlot(i);
			if(s.getItem() instanceof EngineItem eng) qp+=eng.qpGen;
		}
		return qp;
	}

	public int getTotalFECost(){
		int fe=0;
		for(int i=0;i<3;i++){
			ItemStack s=inventory.getStackInSlot(i);
			if(s.getItem() instanceof EngineItem eng) fe+=eng.feCost;
		}
		return fe;
	}

	public float getProgressPerTick(){
		int qp=getTotalQP();
		if(qp<=0) return 0f;
		float t=Math.min(1f,(float)qp/QuarryStats.MAX_QP);
		return QuarryStats.MIN_PROGRESS_PER_TICK+t*(QuarryStats.MAX_PROGRESS_PER_TICK-QuarryStats.MIN_PROGRESS_PER_TICK);
	}

	private ItemStack buildSimulatedTool(){
		ItemStack tool=new ItemStack(Items.NETHERITE_PICKAXE);
		if(hasSilkTouch&&level!=null){
			var lookup=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
			tool.enchant(lookup.getOrThrow(Enchantments.SILK_TOUCH),1);
		}
		return tool;
	}

	public static void tick(Level level,BlockPos pos,BlockState state,QuarryBlockEntity be){
		if(level.isClientSide) return;
		if(++be.feTickTimer>=FE_TICK_INTERVAL){
			be.feTickTimer=0;
			int feNeeded=be.getTotalFECost()*FE_TICK_INTERVAL;
			if(be.quarryState!=State.DONE&&be.quarryState!=State.NO_ENERGY){
				if(feNeeded==0||be.energy.getEnergyStored()>=feNeeded){
					if(feNeeded>0) be.energy.extractEnergy(feNeeded,false);
					be.hasFEThisCycle=true;
				}else{
					be.hasFEThisCycle=false;
					be.activeState=be.quarryState;
					be.quarryState=State.NO_ENERGY;
					be.sync(level,pos,state);
				}
			}else if(be.quarryState==State.NO_ENERGY){
				if(feNeeded==0||be.energy.getEnergyStored()>=feNeeded){
					if(feNeeded>0) be.energy.extractEnergy(feNeeded,false);
					be.hasFEThisCycle=true;
					be.quarryState=be.activeState;
					be.sync(level,pos,state);
				}
			}else be.hasFEThisCycle=true;
		}
		if(!be.hasFEThisCycle&&be.quarryState!=State.DONE) return;
		switch(be.quarryState){
			case CLEARING -> be.tickClearing(level,pos,state);
			case BUILDING_FRAME -> be.tickBuildFrame(level,pos,state);
			case MINING -> be.tickMine(level,pos,state);
			default -> {}
		}
		if(be.chunksNeedReload&&be.quarryState==State.MINING&&level instanceof ServerLevel sl){
			if(be.sectionMaxX<=be.sectionMinX||be.sectionMaxZ<=be.sectionMinZ) be.initSections(state);
			be.loadSectionChunks(sl);
			be.chunksNeedReload=false;
		}
	}

	private int halfX(){ return customHalfX>0?customHalfX:DEFAULT_RANGE; }
	private int halfZ(){ return customHalfZ>0?customHalfZ:DEFAULT_RANGE; }

	public void setLandmarkArea(int halfX,int halfZ,BlockPos center){
		this.customHalfX=Math.max(2,Math.min(halfX,MAX_AREA_SIDE/2));
		this.customHalfZ=Math.max(2,Math.min(halfZ,MAX_AREA_SIDE/2));
		this.customCenter=center;
		invalidateCache();
		setChanged();
		if(level!=null&&!level.isClientSide) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
	}

	private void invalidateCache(){
		cachedFramePos=null; cachedCenter=null; cachedFacing=null;
		cachedHalfX=Integer.MIN_VALUE; cachedHalfZ=Integer.MIN_VALUE;
	}

	public List<BlockPos> computeFramePositions(BlockState state){
		Direction facing=state.getValue(Quarry.FACING);
		int hx=halfX(),hz=halfZ();
		if(cachedFramePos!=null&&facing==cachedFacing&&hx==cachedHalfX&&hz==cachedHalfZ) return cachedFramePos;
		cachedFacing=facing; cachedHalfX=hx; cachedHalfZ=hz;
		cachedCenter=(customCenter!=null)?customCenter:worldPosition.relative(facing.getOpposite(),hx+1);
		cachedFramePos=buildFramePosList(hx,hz,worldPosition.getY());
		return cachedFramePos;
	}

	private @NotNull List<BlockPos> buildFramePosList(int hx,int hz,int yBase){
		List<BlockPos> result=new ArrayList<>();
		assert cachedCenter!=null;
		for(int x=cachedCenter.getX()-hx;x<=cachedCenter.getX()+hx;x++){
			for(int z=cachedCenter.getZ()-hz;z<=cachedCenter.getZ()+hz;z++){
				boolean edgeX=(x==cachedCenter.getX()-hx||x==cachedCenter.getX()+hx);
				boolean edgeZ=(z==cachedCenter.getZ()-hz||z==cachedCenter.getZ()+hz);
				if(!edgeX&&!edgeZ) continue;
				result.add(new BlockPos(x,yBase,z));
				result.add(new BlockPos(x,yBase+FRAME_HEIGHT,z));
				if(edgeX&&edgeZ){
					result.add(new BlockPos(x,yBase+1,z));
					result.add(new BlockPos(x,yBase+2,z));
				}
			}
		}
		return result;
	}

	private BlockPos getAreaCenter(BlockState state){ computeFramePositions(state); return cachedCenter; }

	private void startClearing(Level level,BlockState state,BlockPos pos){
		BlockPos center=getAreaCenter(state);
		int hx=halfX(),hz=halfZ(),yBase=worldPosition.getY();
		workQueue.clear(); workIndex=0;
		for(int y=yBase+FRAME_HEIGHT;y>=yBase;y--)
			for(int x=center.getX()-hx;x<=center.getX()+hx;x++)
				for(int z=center.getZ()-hz;z<=center.getZ()+hz;z++){
					BlockPos bp=new BlockPos(x,y,z);
					if(!level.isEmptyBlock(bp)&&isOwnedFrame(level,bp)) workQueue.add(bp);
				}
		if(workQueue.isEmpty()){ startBuildingFrame(level,state,pos); return; }
		quarryState=State.CLEARING; miningProgressAcc=0f; sync(level,pos,state);
	}

	private void tickClearing(Level level,BlockPos pos,BlockState state){
		if(workQueue.isEmpty()){ startClearing(level,state,pos); return; }
		float progress=getProgressPerTick();
		if(progress<=0f) return;
		miningProgressAcc+=progress;
		float cost=10f;
		while(workIndex<workQueue.size()&&miningProgressAcc>=cost){
			BlockPos bp=workQueue.get(workIndex++);
			if(!level.isEmptyBlock(bp)&&isOwnedFrame(level,bp)){ level.removeBlock(bp,false); miningProgressAcc-=cost; }
		}
		if(workIndex>=workQueue.size()){ workQueue.clear(); workIndex=0; miningProgressAcc=0f; startBuildingFrame(level,state,pos); }
	}

	private void startBuildingFrame(Level level,BlockState state,BlockPos pos){
		quarryState=State.BUILDING_FRAME;
		workQueue.clear(); workQueue.addAll(computeFramePositions(state)); workIndex=0;
		sync(level,pos,state);
	}

	private void tickBuildFrame(Level level,BlockPos pos,BlockState state){
		if(workQueue.isEmpty()){ startBuildingFrame(level,state,pos); return; }
		float progress=getProgressPerTick();
		if(progress<=0f) return;
		miningProgressAcc+=progress;
		float cost=15f;
		while(workIndex<workQueue.size()&&miningProgressAcc>=cost){
			BlockPos fp=workQueue.get(workIndex++);
			if(isFrameBlock(level,fp)){
				level.setBlock(fp,DifModBlocks.QUARRY_FRAME.get().defaultBlockState(),3);
				if(level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity frame) frame.setOwner(worldPosition);
				miningProgressAcc-=cost;
			}
		}
		if(workIndex>=workQueue.size()){
			workQueue.clear(); workIndex=0; miningProgressAcc=0f;
			activeState=State.MINING; quarryState=State.MINING;
			initSections(state); resetMiningPos();
			if(level instanceof ServerLevel sl) loadSectionChunks(sl);
			sync(level,pos,state);
		}
	}

	private void tickMine(Level level,BlockPos pos,BlockState state){
		if(++frameCheckTimer>=FRAME_CHECK_INTERVAL){
			frameCheckTimer=0;
			if(!isFrameIntact(level,state)){
				if(level instanceof ServerLevel sl) unloadForcedChunks(sl);
				quarryState=State.CLEARING; activeState=State.CLEARING;
				workQueue.clear(); workIndex=0; miningPos=null;
				sync(level,pos,state); return;
			}
		}
		float progressStep=getProgressPerTick();
		if(progressStep<=0f) return;
		if(!(level instanceof ServerLevel sl)) return;
		if(miningPos==null) resetMiningPos();
		if(miningPos==null) return;
		ItemStack tool=buildSimulatedTool();
		miningProgressAcc+=progressStep;

		int safety=0;
		while(safety++<1000){
			while(level.isEmptyBlock(miningPos)&&level.getBlockState(miningPos).getFluidState().isEmpty())
				if(handleMiningAdvance(level,pos,state)) return;

			BlockState target=level.getBlockState(miningPos);

			if(!target.getFluidState().isEmpty()){
				if(hasLiquidRemover){
					float fluidCost=target.getFluidState().isSource()?5f:1f;
					if(miningProgressAcc>=fluidCost){
						miningProgressAcc-=fluidCost;
						level.setBlock(miningPos,Blocks.AIR.defaultBlockState(),2);
						if(handleMiningAdvance(level,pos,state)) return;
						continue;
					}else return;
				}
				if(handleMiningAdvance(level,pos,state)) return;
				continue;
			}

			float hardness=target.getDestroySpeed(level,miningPos);
			if(hardness<0){
				miningProgressAcc=0f;
				if(handleMiningAdvance(level,pos,state)) return;
				continue;
			}

			float requiredProgress=Math.max(1f,hardness*10f);
			if(miningProgressAcc<requiredProgress) return;
			miningProgressAcc-=requiredProgress;

			// Block.getDrops volá loot table správně, sám řeší correct tool podle requiresCorrectToolForDrops
			List<ItemStack> drops=Block.getDrops(target,sl,miningPos,sl.getBlockEntity(miningPos),null,tool);
			level.removeBlock(miningPos,false);
			if(!drops.isEmpty()) distributeDrops(level,drops);

			if(handleMiningAdvance(level,pos,state)) return;
		}
	}

	private void distributeDrops(Level level,List<ItemStack> drops){
		// Fresh check inventářů – žádný cache, vždy aktuální
		List<IItemHandler> handlers=new ArrayList<>(6);
		for(Direction dir: Direction.values()){
			IItemHandler h=level.getCapability(Capabilities.ItemHandler.BLOCK,worldPosition.relative(dir),dir.getOpposite());
			if(h!=null) handlers.add(h);
		}
		for(ItemStack drop: drops){
			if(drop.isEmpty()) continue;
			ItemStack remaining=drop;
			for(IItemHandler h: handlers){
				if(remaining.isEmpty()) break;
				remaining=ItemHandlerHelper.insertItemStacked(h,remaining,false);
			}
			if(!remaining.isEmpty()) Block.popResource(level,worldPosition.above(),remaining);
		}
	}

	private boolean handleMiningAdvance(Level level,BlockPos pos,BlockState state){
		if(advanceMiningPos()) return false;
		if(advanceToNextSection(level,state)) return false;
		finishMining(level,pos,state); return true;
	}

	private void finishMining(Level level,BlockPos pos,BlockState state){
		quarryState=State.DONE; miningPos=null;
		if(level instanceof ServerLevel sl) unloadForcedChunks(sl);
		sync(level,pos,state);
	}

	public void onFrameDestroyed(Level level){
		if(level==null||level.isClientSide) return;
		if(level instanceof ServerLevel sl) unloadForcedChunks(sl);
		quarryState=State.CLEARING; workQueue.clear(); workIndex=0; miningPos=null;
		setChanged(); level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
	}

	public void onQuarryRemoved(){
		if(level==null||level.isClientSide) return;
		if(level instanceof ServerLevel sl) unloadForcedChunks(sl);
		for(int i=0;i<inventory.getSlots();i++){
			ItemStack s=inventory.getStackInSlot(i);
			if(!s.isEmpty()) Block.popResource(level,worldPosition,s);
		}
		for(BlockPos fp: computeFramePositions(getBlockState()))
			if(level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity frame&&worldPosition.equals(frame.getOwnerPos()))
				frame.scheduleRemoval();
	}

	public boolean isFrameIntact(Level level,BlockState state){
		for(BlockPos fp: computeFramePositions(state)){
			if(!level.isLoaded(fp)) continue;
			if(isFrameBlock(level,fp)) return false;
		}
		return true;
	}

	private boolean isFrameBlock(Level level,BlockPos pos){ return !level.getBlockState(pos).is(DifModBlocks.QUARRY_FRAME.get()); }
	private boolean isOwnedFrame(Level level,BlockPos pos){
		return isFrameBlock(level,pos)||!(level.getBlockEntity(pos) instanceof QuarryFrameBlockEntity frame)||!worldPosition.equals(frame.getOwnerPos());
	}

	private void resetMiningPos(){ miningPos=new BlockPos(sectionMinX,worldPosition.getY()-1,sectionMinZ); setChanged(); }

	private boolean advanceMiningPos(){
		if(miningPos==null||level==null) return false;
		int nx=miningPos.getX()+1,nz=miningPos.getZ(),ny=miningPos.getY();
		if(nx>sectionMaxX){ nx=sectionMinX; nz++; }
		if(nz>sectionMaxZ){ nz=sectionMinZ; ny--; }
		miningPos=new BlockPos(nx,ny,nz); setChanged();
		return ny>level.getMinBuildHeight();
	}

	private void initSections(BlockState state){
		BlockPos center=getAreaCenter(state); if(center==null) return;
		int ihx=halfX()-1,ihz=halfZ()-1;
		sectionsX=(ihx*2+1>64)?2:1; sectionsZ=(ihz*2+1>64)?2:1;
		totalSections=sectionsX*sectionsZ; currentSection=0;
		applySectionBounds(state);
	}

	private void applySectionBounds(BlockState state){
		BlockPos center=getAreaCenter(state); if(center==null) return;
		int ihx=halfX()-1,ihz=halfZ()-1;
		int fmx=center.getX()-ihx,fxx=center.getX()+ihx;
		int fmz=center.getZ()-ihz,fxz=center.getZ()+ihz;
		int col=currentSection%sectionsX,row=currentSection/sectionsX;
		if(sectionsX==1){ sectionMinX=fmx; sectionMaxX=fxx; }
		else{ int wa=(fxx-fmx+1)/2; sectionMinX=(col==0)?fmx:fmx+wa; sectionMaxX=(col==0)?fmx+wa-1:fxx; }
		if(sectionsZ==1){ sectionMinZ=fmz; sectionMaxZ=fxz; }
		else{ int da=(fxz-fmz+1)/2; sectionMinZ=(row==0)?fmz:fmz+da; sectionMaxZ=(row==0)?fmz+da-1:fxz; }
	}

	private boolean advanceToNextSection(Level level,BlockState state){
		currentSection++; if(currentSection>=totalSections) return false;
		applySectionBounds(state);
		if(level instanceof ServerLevel sl) loadSectionChunks(sl);
		miningPos=new BlockPos(sectionMinX,worldPosition.getY()-1,sectionMinZ);
		miningProgressAcc=0f; setChanged(); return true;
	}

	private void loadSectionChunks(ServerLevel level){
		unloadForcedChunks(level);
		int mnx=sectionMinX>>4,mxx=sectionMaxX>>4,mnz=sectionMinZ>>4,mxz=sectionMaxZ>>4;
		for(int cx=mnx;cx<=mxx;cx++) for(int cz=mnz;cz<=mxz;cz++){ level.setChunkForced(cx,cz,true); forcedChunks.add(new ChunkPos(cx,cz)); }
	}

	private void unloadForcedChunks(ServerLevel level){ for(ChunkPos cp: forcedChunks) level.setChunkForced(cp.x,cp.z,false); forcedChunks.clear(); }

	private void sync(Level level,BlockPos pos,BlockState state){ level.sendBlockUpdated(pos,state,state,3); setChanged(); }

	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider){
		CompoundTag tag=new CompoundTag();
		tag.putInt("QS",quarryState.ordinal());
		if(miningPos!=null){ tag.putInt("MineX",miningPos.getX()); tag.putInt("MineY",miningPos.getY()); tag.putInt("MineZ",miningPos.getZ()); }
		if(customHalfX>0){ tag.putInt("LmHX",customHalfX); tag.putInt("LmHZ",customHalfZ); }
		if(customCenter!=null){ tag.putInt("LmCX",customCenter.getX()); tag.putInt("LmCY",customCenter.getY()); tag.putInt("LmCZ",customCenter.getZ()); }
		return tag;
	}

	@Override public ClientboundBlockEntityDataPacket getUpdatePacket(){ return ClientboundBlockEntityDataPacket.create(this); }

	@Override
	public void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		if(tag.contains("Inventory")) inventory.deserializeNBT(provider,tag.getCompound("Inventory"));
		rebuildUpgradeCache();
		int storedFE=tag.getInt("Energy");
		energy.receiveEnergy(storedFE-energy.getEnergyStored(),false);
		int ord=tag.getInt("QS");
		quarryState=(ord>=0&&ord<State.values().length)?State.values()[ord]:State.NO_ENERGY;
		if(tag.contains("MineX")) miningPos=new BlockPos(tag.getInt("MineX"),tag.getInt("MineY"),tag.getInt("MineZ"));
		workIndex=tag.getInt("WI");
		if(tag.contains("LmHX")){ customHalfX=tag.getInt("LmHX"); customHalfZ=tag.getInt("LmHZ"); }
		customCenter=tag.contains("LmCX")?new BlockPos(tag.getInt("LmCX"),tag.getInt("LmCY"),tag.getInt("LmCZ")):null;
		currentSection=tag.getInt("SecCur"); totalSections=Math.max(1,tag.getInt("SecTot"));
		sectionsX=Math.max(1,tag.getInt("SecGX")); sectionsZ=Math.max(1,tag.getInt("SecGZ"));
		sectionMinX=tag.getInt("SecMnX"); sectionMaxX=tag.getInt("SecMxX");
		sectionMinZ=tag.getInt("SecMnZ"); sectionMaxZ=tag.getInt("SecMxZ");
		if(quarryState==State.MINING) chunksNeedReload=true;
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		tag.put("Inventory",inventory.serializeNBT(provider));
		tag.putInt("Energy",energy.getEnergyStored());
		tag.putInt("QS",quarryState.ordinal());
		tag.putInt("WI",workIndex);
		if(miningPos!=null){ tag.putInt("MineX",miningPos.getX()); tag.putInt("MineY",miningPos.getY()); tag.putInt("MineZ",miningPos.getZ()); }
		if(customHalfX>0){ tag.putInt("LmHX",customHalfX); tag.putInt("LmHZ",customHalfZ); }
		if(customCenter!=null){ tag.putInt("LmCX",customCenter.getX()); tag.putInt("LmCY",customCenter.getY()); tag.putInt("LmCZ",customCenter.getZ()); }
		tag.putInt("SecCur",currentSection); tag.putInt("SecTot",totalSections);
		tag.putInt("SecGX",sectionsX); tag.putInt("SecGZ",sectionsZ);
		tag.putInt("SecMnX",sectionMinX); tag.putInt("SecMxX",sectionMaxX);
		tag.putInt("SecMnZ",sectionMinZ); tag.putInt("SecMxZ",sectionMaxZ);
	}

	public BlockPos getMiningPos(){ return miningPos; }
	public State getQuarryState(){ return quarryState; }
	public BlockPos getAreaCenter(){ return getAreaCenter(getBlockState()); }
	public int getFrameHalfX(){ return halfX(); }
	public int getFrameHalfZ(){ return halfZ(); }

	@Override public @NotNull Component getDisplayName(){ return Component.translatable("block.dif.quarry"); }
	@Nullable @Override
	public AbstractContainerMenu createMenu(int id,@NotNull Inventory playerInv,@NotNull Player player){ return new QuarryMenu(id,playerInv,this); }

	public IItemHandler getInventory(){ return inventory; }
	public net.neoforged.neoforge.energy.IEnergyStorage getEnergyStorage(){ return energy; }
}