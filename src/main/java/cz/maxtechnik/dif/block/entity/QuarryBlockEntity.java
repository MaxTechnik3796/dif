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
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class QuarryBlockEntity extends BlockEntity implements MenuProvider{

	// ── Konstanty ───────────────────────────────────────────────────────
	public enum State{NO_ENERGY,CLEARING,BUILDING_FRAME,MINING,DONE}
	private static final int ENERGY_CAPACITY=QuarryStats.QUARRY_ENERGY_CAPACITY;
	private static final int ENERGY_INPUT=QuarryStats.QUARRY_ENERGY_INPUT;
	private static final int FRAME_CHECK_INTERVAL=40;
	private static final int FE_TICK_INTERVAL=5;
	private static final int FRAME_HEIGHT=3;
	public static final int DEFAULT_RANGE=5;
	public static final int MAX_AREA_SIDE=128;

	// ── Stav ────────────────────────────────────────────────────────────
	private State quarryState=State.NO_ENERGY;
	private State activeState=State.CLEARING;
	private BlockPos miningPos;
	private int frameCheckTimer=0;
	private boolean hasFEThisCycle=false;
	private float miningProgressAcc=0f;
	private int feTickTimer=0;

	// ── Oblast (přímo min/max, žádné half/center) ────────────────────────
	private int areaMinX=Integer.MIN_VALUE,areaMaxX,areaMinZ,areaMaxZ;
	private boolean hasArea(){ return areaMinX!=Integer.MIN_VALUE; }

	// ── Těžební hranice (uvnitř rámu) ───────────────────────────────────
	private int mineMinX,mineMaxX,mineMinZ,mineMaxZ;

	// ── Chunkloading ────────────────────────────────────────────────────
	private final List<ChunkPos> forcedChunks=new ArrayList<>();
	private boolean chunksNeedReload=false;

	// ── Pracovní fronta (clearing/building) ─────────────────────────────
	private final ArrayList<BlockPos> workQueue=new ArrayList<>();
	private int workIndex=0;

	// ── Cache pro frame pozice ──────────────────────────────────────────
	@Nullable private List<BlockPos> cachedFramePos=null;
	@Nullable private BlockPos cachedCenter=null;

	// ── Upgrady ─────────────────────────────────────────────────────────
	private boolean hasSilkTouch=false;
	private boolean hasLiquidRemover=false;
	private final ItemStackHandler inventory=new ItemStackHandler(3){
		@Override protected void onContentsChanged(int index){
			rebuildUpgradeCache();
			setChanged();
		}
	};

	// ── Energy + GUI data ───────────────────────────────────────────────
	private final EnergyStorage energy=new EnergyStorage(ENERGY_CAPACITY,ENERGY_INPUT,ENERGY_CAPACITY);
	public final ContainerData dataAccess=new ContainerData(){
		@Override public int get(int index){
			return switch(index){
				case 0 -> quarryState.ordinal();
				case 1 -> getTotalQP();
				case 2 -> getTotalFECost();
				case 3 -> getAreaSizeX();
				case 4 -> getAreaSizeZ();
				case 5 -> getTotalQP()==0?1:0;
				default -> 0;
			};
		}
		@Override public void set(int index,int value){}
		@Override public int getCount(){ return 6; }
	};

	public QuarryBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.QUARRY.get(),pos,blockState);
	}

	// ── Engine stats ────────────────────────────────────────────────────
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

	// ── Upgrade cache ───────────────────────────────────────────────────
	private void rebuildUpgradeCache(){
		hasSilkTouch=false; hasLiquidRemover=false;
		for(int i=1;i<=2;i++){
			ItemStack s=inventory.getStackInSlot(i);
			if(s.isEmpty()) continue;
			if(s.getItem()==Items.SPONGE) hasLiquidRemover=true;
			else if(s.is(Items.ENCHANTED_BOOK)){
				var stored=s.get(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS);
				if(stored!=null) stored.keySet().forEach(h->{ if(h.is(Enchantments.SILK_TOUCH)) hasSilkTouch=true; });
			}
		}
	}
	private ItemStack buildSimulatedTool(){
		ItemStack tool=new ItemStack(Items.NETHERITE_PICKAXE);
		if(hasSilkTouch&&level!=null){
			var lookup=level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
			tool.enchant(lookup.getOrThrow(Enchantments.SILK_TOUCH),1);
		}
		return tool;
	}

	// ── Oblast ───────────────────────────────────────────────────────────
	public void setArea(int minX,int maxX,int minZ,int maxZ){
		areaMinX=minX; areaMaxX=maxX; areaMinZ=minZ; areaMaxZ=maxZ;
		cachedFramePos=null; cachedCenter=null;
		setChanged();
		if(level!=null&&!level.isClientSide) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
	}
	/** Velikost oblasti (pro GUI) */
	public int getAreaSizeX(){ return hasArea()?areaMaxX-areaMinX+1:DEFAULT_RANGE*2+1; }
	public int getAreaSizeZ(){ return hasArea()?areaMaxZ-areaMinZ+1:DEFAULT_RANGE*2+1; }
	public int getAreaMinX(){ return hasArea()?areaMinX:getDefaultCenter().getX()-DEFAULT_RANGE; }
	public int getAreaMaxX(){ return hasArea()?areaMaxX:getDefaultCenter().getX()+DEFAULT_RANGE; }
	public int getAreaMinZ(){ return hasArea()?areaMinZ:getDefaultCenter().getZ()-DEFAULT_RANGE; }
	public int getAreaMaxZ(){ return hasArea()?areaMaxZ:getDefaultCenter().getZ()+DEFAULT_RANGE; }

	private BlockPos getDefaultCenter(){
		Direction facing=getBlockState().getValue(Quarry.FACING);
		return worldPosition.relative(facing.getOpposite(),DEFAULT_RANGE+1);
	}

	// ── Frame pozice (cache) ────────────────────────────────────────────
	public List<BlockPos> computeFramePositions(BlockState blockState){
		if(cachedFramePos!=null) return cachedFramePos;
		int mnX=getAreaMinX(), mxX=getAreaMaxX(), mnZ=getAreaMinZ(), mxZ=getAreaMaxZ();
		cachedCenter=new BlockPos((mnX+mxX)/2,worldPosition.getY(),(mnZ+mxZ)/2);
		List<BlockPos> result=new ArrayList<>();
		int yBase=worldPosition.getY();
		for(int x=mnX;x<=mxX;x++){
			for(int z=mnZ;z<=mxZ;z++){
				boolean edgeX=x==mnX||x==mxX;
				boolean edgeZ=z==mnZ||z==mxZ;
				if(!edgeX&&!edgeZ) continue;
				result.add(new BlockPos(x,yBase,z));
				result.add(new BlockPos(x,yBase+FRAME_HEIGHT,z));
				if(edgeX&&edgeZ){
					result.add(new BlockPos(x,yBase+1,z));
					result.add(new BlockPos(x,yBase+2,z));
				}
			}
		}
		cachedFramePos=result;
		return result;
	}
	public BlockPos getAreaCenter(BlockState blockState){
		computeFramePositions(blockState);
		return cachedCenter;
	}
	public BlockPos getAreaCenter(){ return getAreaCenter(getBlockState()); }
	/** Zpětná kompatibilita pro renderer */
	public int getFrameHalfX(){ return(getAreaSizeX()-1)/2; }
	public int getFrameHalfZ(){ return(getAreaSizeZ()-1)/2; }

	// ══════════════════════════════════════════════════════════════════════
	// ── TICK ─────────────────────────────────────────────────────────────
	// ══════════════════════════════════════════════════════════════════════
	public static void tick(Level level,BlockPos pos,BlockState blockState,QuarryBlockEntity be){
		if(level.isClientSide) return;
		// ── Energy management ──
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
					be.sync(level,pos,blockState);
				}
			}else if(be.quarryState==State.NO_ENERGY){
				if(feNeeded==0||be.energy.getEnergyStored()>=feNeeded){
					if(feNeeded>0) be.energy.extractEnergy(feNeeded,false);
					be.hasFEThisCycle=true;
					be.quarryState=be.activeState;
					be.sync(level,pos,blockState);
				}
			}else be.hasFEThisCycle=true;
		}
		if(!be.hasFEThisCycle&&be.quarryState!=State.DONE) return;
		// ── State machine ──
		switch(be.quarryState){
			case CLEARING -> be.tickClearing(level,pos,blockState);
			case BUILDING_FRAME -> be.tickBuildFrame(level,pos,blockState);
			case MINING -> be.tickMine(level,pos,blockState);
			default -> {}
		}
		// ── Reload chunků po loadu světa ──
		if(be.chunksNeedReload&&be.quarryState==State.MINING&&level instanceof ServerLevel sl){
			be.computeMineBounds();
			be.loadMiningChunks(sl);
			be.chunksNeedReload=false;
		}
	}

	// ── CLEARING ────────────────────────────────────────────────────────
	private void startClearing(Level level,BlockState bs,BlockPos pos){
		int mnX=getAreaMinX(),mxX=getAreaMaxX(),mnZ=getAreaMinZ(),mxZ=getAreaMaxZ();
		int yBase=worldPosition.getY();
		workQueue.clear(); workIndex=0;
		for(int y=yBase+FRAME_HEIGHT;y>=yBase;y--)
			for(int x=mnX;x<=mxX;x++)
				for(int z=mnZ;z<=mxZ;z++){
					BlockPos bp=new BlockPos(x,y,z);
					if(!level.isEmptyBlock(bp)&&isOwnedFrame(level,bp)) workQueue.add(bp);
				}
		if(workQueue.isEmpty()){ startBuildingFrame(level,bs,pos); return; }
		quarryState=State.CLEARING; miningProgressAcc=0f;
		sync(level,pos,bs);
	}
	private void tickClearing(Level level,BlockPos pos,BlockState bs){
		if(workQueue.isEmpty()){ startClearing(level,bs,pos); return; }
		float progress=getProgressPerTick(); if(progress<=0f) return;
		miningProgressAcc+=progress;
		float cost=10f;
		while(workIndex<workQueue.size()&&miningProgressAcc>=cost){
			BlockPos bp=workQueue.get(workIndex++);
			if(!level.isEmptyBlock(bp)&&isOwnedFrame(level,bp)){
				level.removeBlock(bp,false); miningProgressAcc-=cost;
			}
		}
		if(workIndex>=workQueue.size()){
			workQueue.clear(); workIndex=0; miningProgressAcc=0f;
			startBuildingFrame(level,bs,pos);
		}
	}

	// ── BUILDING FRAME ──────────────────────────────────────────────────
	private void startBuildingFrame(Level level,BlockState bs,BlockPos pos){
		quarryState=State.BUILDING_FRAME;
		workQueue.clear();
		workQueue.addAll(computeFramePositions(bs));
		workIndex=0;
		sync(level,pos,bs);
	}
	private void tickBuildFrame(Level level,BlockPos pos,BlockState bs){
		if(workQueue.isEmpty()){ startBuildingFrame(level,bs,pos); return; }
		float progress=getProgressPerTick(); if(progress<=0f) return;
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
			computeMineBounds();
			miningPos=new BlockPos(mineMinX,worldPosition.getY()-1,mineMinZ);
			if(level instanceof ServerLevel sl) loadMiningChunks(sl);
			sync(level,pos,bs);
		}
	}

	// ── MINING ──────────────────────────────────────────────────────────
	private void tickMine(Level level,BlockPos pos,BlockState bs){
		// Periodický frame check
		if(++frameCheckTimer>=FRAME_CHECK_INTERVAL){
			frameCheckTimer=0;
			if(!isFrameIntact(level,bs)){
				if(level instanceof ServerLevel sl) unloadForcedChunks(sl);
				quarryState=State.CLEARING; activeState=State.CLEARING;
				workQueue.clear(); workIndex=0; miningPos=null;
				sync(level,pos,bs); return;
			}
		}
		float step=getProgressPerTick(); if(step<=0f) return;
		if(!(level instanceof ServerLevel sl)) return;
		if(miningPos==null) miningPos=new BlockPos(mineMinX,worldPosition.getY()-1,mineMinZ);
		ItemStack tool=buildSimulatedTool();
		miningProgressAcc+=step;
		int safety=0;
		while(safety++<1000){
			// Přeskočit prázdné bloky
			while(level.isEmptyBlock(miningPos)&&level.getBlockState(miningPos).getFluidState().isEmpty())
				if(!advanceMiningPos()){ finishMining(level,pos,bs); return; }
			BlockState target=level.getBlockState(miningPos);
			// Kapaliny
			if(!target.getFluidState().isEmpty()){
				if(hasLiquidRemover){
					float fluidCost=target.getFluidState().isSource()?5f:1f;
					if(miningProgressAcc>=fluidCost){
						miningProgressAcc-=fluidCost;
						level.setBlock(miningPos,Blocks.AIR.defaultBlockState(),2);
						if(!advanceMiningPos()){ finishMining(level,pos,bs); return; }
						continue;
					}else return;
				}
				if(!advanceMiningPos()){ finishMining(level,pos,bs); return; }
				continue;
			}
			// Nezničitelné
			float hardness=target.getDestroySpeed(level,miningPos);
			if(hardness<0){
				miningProgressAcc=0f;
				if(!advanceMiningPos()){ finishMining(level,pos,bs); return; }
				continue;
			}
			// Těžení
			float required=Math.max(1f,hardness*10f);
			if(miningProgressAcc<required) return;
			miningProgressAcc-=required;
			List<ItemStack> drops=Block.getDrops(target,sl,miningPos,sl.getBlockEntity(miningPos),null,tool);
			level.removeBlock(miningPos,false);
			if(!drops.isEmpty()) distributeDrops(level,drops);
			if(!advanceMiningPos()){ finishMining(level,pos,bs); return; }
		}
	}

	// ── Mining helpers ──────────────────────────────────────────────────
	private void computeMineBounds(){
		mineMinX=getAreaMinX()+1; mineMaxX=getAreaMaxX()-1;
		mineMinZ=getAreaMinZ()+1; mineMaxZ=getAreaMaxZ()-1;
	}
	/** Posun na další blok. Vrací true = lze pokračovat, false = hotovo. */
	private boolean advanceMiningPos(){
		if(miningPos==null||level==null) return false;
		int nx=miningPos.getX()+1, nz=miningPos.getZ(), ny=miningPos.getY();
		if(nx>mineMaxX){ nx=mineMinX; nz++; }
		if(nz>mineMaxZ){ nz=mineMinZ; ny--; }
		miningPos=new BlockPos(nx,ny,nz);
		setChanged();
		return ny>level.getMinBuildHeight();
	}
	private void finishMining(Level level,BlockPos pos,BlockState bs){
		quarryState=State.DONE; miningPos=null;
		if(level instanceof ServerLevel sl) unloadForcedChunks(sl);
		sync(level,pos,bs);
	}
	private void distributeDrops(Level level,List<ItemStack> drops){
		List<IItemHandler> handlers=new ArrayList<>(6);
		for(Direction dir:Direction.values()){
			IItemHandler h=level.getCapability(Capabilities.ItemHandler.BLOCK,worldPosition.relative(dir),dir.getOpposite());
			if(h!=null) handlers.add(h);
		}
		for(ItemStack drop:drops){
			if(drop.isEmpty()) continue;
			ItemStack rem=drop;
			for(IItemHandler h:handlers){ if(rem.isEmpty()) break; rem=ItemHandlerHelper.insertItemStacked(h,rem,false); }
			if(!rem.isEmpty()) Block.popResource(level,worldPosition.above(),rem);
		}
	}

	// ── Chunkloading (jen těžební oblast) ───────────────────────────────
	private void loadMiningChunks(ServerLevel sl){
		unloadForcedChunks(sl);
		int mnCx=mineMinX>>4, mxCx=mineMaxX>>4, mnCz=mineMinZ>>4, mxCz=mineMaxZ>>4;
		for(int cx=mnCx;cx<=mxCx;cx++)
			for(int cz=mnCz;cz<=mxCz;cz++){
				sl.setChunkForced(cx,cz,true);
				forcedChunks.add(new ChunkPos(cx,cz));
			}
	}
	private void unloadForcedChunks(ServerLevel sl){
		for(ChunkPos cp:forcedChunks) sl.setChunkForced(cp.x,cp.z,false);
		forcedChunks.clear();
	}

	// ── Frame utility ───────────────────────────────────────────────────
	public boolean isFrameIntact(Level level,BlockState bs){
		for(BlockPos fp:computeFramePositions(bs)){
			if(!level.isLoaded(fp)) continue;
			if(isFrameBlock(level,fp)) return false;
		}
		return true;
	}
	private boolean isFrameBlock(Level level,BlockPos pos){
		return !level.getBlockState(pos).is(DifModBlocks.QUARRY_FRAME.get());
	}
	private boolean isOwnedFrame(Level level,BlockPos pos){
		return isFrameBlock(level,pos)||!(level.getBlockEntity(pos) instanceof QuarryFrameBlockEntity frame)||!worldPosition.equals(frame.getOwnerPos());
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
		for(BlockPos fp:computeFramePositions(getBlockState()))
			if(level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity frame&&worldPosition.equals(frame.getOwnerPos()))
				frame.scheduleRemoval();
	}

	// ── Sync ────────────────────────────────────────────────────────────
	private void sync(Level level,BlockPos pos,BlockState bs){
		level.sendBlockUpdated(pos,bs,bs,3); setChanged();
	}

	// ── NBT ─────────────────────────────────────────────────────────────
	@Override public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider p){
		CompoundTag tag=new CompoundTag();
		tag.putInt("QS",quarryState.ordinal());
		if(miningPos!=null){
			tag.putInt("MineX",miningPos.getX()); tag.putInt("MineY",miningPos.getY()); tag.putInt("MineZ",miningPos.getZ());
		}
		if(hasArea()){
			tag.putInt("AMnX",areaMinX); tag.putInt("AMxX",areaMaxX);
			tag.putInt("AMnZ",areaMinZ); tag.putInt("AMxZ",areaMaxZ);
		}
		return tag;
	}
	@Override public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override public void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider p){
		super.loadAdditional(tag,p);
		if(tag.contains("Inventory")) inventory.deserializeNBT(p,tag.getCompound("Inventory"));
		rebuildUpgradeCache();
		energy.receiveEnergy(tag.getInt("Energy")-energy.getEnergyStored(),false);
		int ord=tag.getInt("QS");
		quarryState=(ord>=0&&ord<State.values().length)?State.values()[ord]:State.NO_ENERGY;
		if(tag.contains("MineX")) miningPos=new BlockPos(tag.getInt("MineX"),tag.getInt("MineY"),tag.getInt("MineZ"));
		workIndex=tag.getInt("WI");
		// Oblast — nový formát
		if(tag.contains("AMnX")){
			areaMinX=tag.getInt("AMnX"); areaMaxX=tag.getInt("AMxX");
			areaMinZ=tag.getInt("AMnZ"); areaMaxZ=tag.getInt("AMxZ");
		}
		// Zpětná kompatibilita — starý halfX/center formát
		else if(tag.contains("LmHX")&&tag.contains("LmCX")){
			int hx=tag.getInt("LmHX"), hz=tag.getInt("LmHZ");
			int cx=tag.getInt("LmCX"), cz=tag.getInt("LmCZ");
			areaMinX=cx-hx; areaMaxX=cx+hx; areaMinZ=cz-hz; areaMaxZ=cz+hz;
		}else areaMinX=Integer.MIN_VALUE;
		// Těžební hranice
		if(tag.contains("MMnX")){
			mineMinX=tag.getInt("MMnX"); mineMaxX=tag.getInt("MMxX");
			mineMinZ=tag.getInt("MMnZ"); mineMaxZ=tag.getInt("MMxZ");
		}
		// Zpětná kompatibilita — staré sekce → přepočítat mine bounds
		else if(quarryState==State.MINING&&hasArea()) computeMineBounds();
		cachedFramePos=null; cachedCenter=null;
		if(quarryState==State.MINING) chunksNeedReload=true;
	}
	@Override protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider p){
		super.saveAdditional(tag,p);
		tag.put("Inventory",inventory.serializeNBT(p));
		tag.putInt("Energy",energy.getEnergyStored());
		tag.putInt("QS",quarryState.ordinal());
		tag.putInt("WI",workIndex);
		if(miningPos!=null){
			tag.putInt("MineX",miningPos.getX()); tag.putInt("MineY",miningPos.getY()); tag.putInt("MineZ",miningPos.getZ());
		}
		if(hasArea()){
			tag.putInt("AMnX",areaMinX); tag.putInt("AMxX",areaMaxX);
			tag.putInt("AMnZ",areaMinZ); tag.putInt("AMxZ",areaMaxZ);
		}
		tag.putInt("MMnX",mineMinX); tag.putInt("MMxX",mineMaxX);
		tag.putInt("MMnZ",mineMinZ); tag.putInt("MMxZ",mineMaxZ);
	}

	// ── Gettery ─────────────────────────────────────────────────────────
	public BlockPos getMiningPos(){ return miningPos; }
	public State getQuarryState(){ return quarryState; }
	public IItemHandler getInventory(){ return inventory; }
	public IEnergyStorage getEnergyStorage(){ return energy; }

	// ── Menu ────────────────────────────────────────────────────────────
	@Override public @NotNull Component getDisplayName(){ return Component.translatable("block.dif.quarry"); }
	@Nullable @Override public AbstractContainerMenu createMenu(int id,@NotNull Inventory inv,@NotNull Player player){
		return new QuarryMenu(id,inv,this);
	}
}