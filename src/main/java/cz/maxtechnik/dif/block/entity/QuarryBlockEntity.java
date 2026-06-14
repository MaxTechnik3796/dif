package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.util.quarry.QuarryArea;
import cz.maxtechnik.dif.util.quarry.QuarryAreaManager;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
public class QuarryBlockEntity extends BlockEntity implements MenuProvider{
	// ── Konstanty ───────────────────────────────────────────────────────
	public enum State{NO_ENERGY,CLEARING,BUILDING_FRAME,MINING,DONE}
	private static final int ENERGY_CAPACITY=QuarryStats.QUARRY_ENERGY_CAPACITY;
	private static final int ENERGY_INPUT=QuarryStats.QUARRY_ENERGY_INPUT;
	private static final int FRAME_CHECK_INTERVAL=40;
	private static final int FE_TICK_INTERVAL=5;
	// ── Stav Quarry ─────────────────────────────────────────────────────
	private State quarryState=State.NO_ENERGY;
	private State activeState=State.CLEARING;
	private int frameCheckTimer=0;
	private boolean hasFEThisCycle=false;
	private float miningProgressAcc=0f;
	private int feTickTimer=0;
	private boolean chunksNeedReload=false;
	// ── Místo starých souřadnic nyní používáme QuarryAreaManager ─────────
	private final QuarryAreaManager areaManager=new QuarryAreaManager();
	// ── Pracovní fronta (pro čištění a stavbu rámu) ─────────────────────
	private final ArrayList<BlockPos> workQueue=new ArrayList<>();
	private int workIndex=0;
	// ── Upgrady a Inventář ──────────────────────────────────────────────
	private boolean hasSilkTouch=false;
	private boolean hasLiquidRemover=false;
	private final ItemStackHandler inventory=new ItemStackHandler(3){
		@Override
		protected void onContentsChanged(int index){
			rebuildUpgradeCache();
			setChanged();
		}
	};
	// ── Energie a GUI data ──────────────────────────────────────────────
	private final EnergyStorage energy=new EnergyStorage(ENERGY_CAPACITY,ENERGY_INPUT,ENERGY_CAPACITY);
	public final ContainerData dataAccess=new ContainerData(){
		@Override
		public int get(int index){
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
		@Override
		public void set(int index,int value){
		}
		@Override
		public int getCount(){
			return 6;
		}
	};
	public QuarryBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.QUARRY.get(),pos,blockState);
	}
	// ── Inicializace oblasti z vnějšku (při umístění nebo z landmarku) ──
	public void setArea(int minX,int maxX,int minZ,int maxZ){
		areaManager.setArea(new QuarryArea(minX,maxX,minZ,maxZ));
		setChanged();
		if(level!=null&&!level.isClientSide){
			level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
		}
	}
	private void ensureAreaInitialized(){
		if(!areaManager.hasArea()){
			Direction facing=getBlockState().getValue(Quarry.FACING);
			BlockPos center=worldPosition.relative(facing.getOpposite(),QuarryAreaManager.DEFAULT_RANGE+1);
			int cx=center.getX(), cz=center.getZ(), dr=QuarryAreaManager.DEFAULT_RANGE;
			areaManager.setArea(new QuarryArea(cx-dr,cx+dr,cz-dr,cz+dr));
		}
	}
	// ── Statistiky a výpočty enginů ─────────────────────────────────────
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
	private void rebuildUpgradeCache(){
		hasSilkTouch=false;
		hasLiquidRemover=false;
		for(int i=1;i<=2;i++){
			ItemStack s=inventory.getStackInSlot(i);
			if(s.isEmpty()) continue;
			if(s.getItem()==Items.SPONGE){
				hasLiquidRemover=true;
			}else if(s.is(Items.ENCHANTED_BOOK)){
				var stored=s.get(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS);
				if(stored!=null) stored.keySet().forEach(h->{
					if(h.is(Enchantments.SILK_TOUCH)) hasSilkTouch=true;
				});
			}
		}
	}
	// ══════════════════════════════════════════════════════════════════════
	// ── HLAVNÍ TICK ───────────────────────────────────────────────────────
	// ══════════════════════════════════════════════════════════════════════
	public static void tick(Level level,QuarryBlockEntity be){
		if(level.isClientSide) return;
		be.ensureAreaInitialized();
		// 1. Odběr energie
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
					be.sync();
				}
			}else if(be.quarryState==State.NO_ENERGY){
				if(feNeeded==0||be.energy.getEnergyStored()>=feNeeded){
					if(feNeeded>0) be.energy.extractEnergy(feNeeded,false);
					be.hasFEThisCycle=true;
					be.quarryState=be.activeState;
					be.sync();
				}
			}else{
				be.hasFEThisCycle=true;
			}
		}
		if(!be.hasFEThisCycle&&be.quarryState!=State.DONE) return;
		// 2. Provedení samotné operace (State machine)
		switch(be.quarryState){
			case CLEARING -> be.tickClearing(level);
			case BUILDING_FRAME -> be.tickBuildFrame(level);
			case MINING -> be.tickMine(level);
			default -> {
			}
		}
		// 3. Obnovení chunkloadingu po restartu serveru
		if(be.chunksNeedReload&&be.quarryState==State.MINING&&level instanceof ServerLevel sl){
			be.areaManager.loadMiningChunks(sl);
			be.chunksNeedReload=false;
		}
	}
	// ── ČIŠTĚNÍ OBLASTI PRO RÁM ───────────────────────────────────────────
	private void startClearing(Level level){
		QuarryArea area=areaManager.getArea();
		int yBase=worldPosition.getY();
		workQueue.clear();
		workIndex=0;
		for(int y=yBase+3;y>=yBase;y--){
			for(int x=area.minX();x<=area.maxX();x++){
				for(int z=area.minZ();z<=area.maxZ();z++){
					BlockPos bp=new BlockPos(x,y,z);
					if(!level.isEmptyBlock(bp)&&isOwnedFrame(level,bp)){
						workQueue.add(bp);
					}
				}
			}
		}
		if(workQueue.isEmpty()){
			startBuildingFrame();
			return;
		}
		quarryState=State.CLEARING;
		miningProgressAcc=0f;
		sync();
	}
	private void tickClearing(Level level){
		if(workQueue.isEmpty()){
			startClearing(level);
			return;
		}
		float progress=getProgressPerTick();
		if(progress<=0f) return;
		miningProgressAcc+=progress;
		float cost=10f; // Cena za zničení jednoho překážejícího bloku
		while(workIndex<workQueue.size()&&miningProgressAcc>=cost){
			BlockPos bp=workQueue.get(workIndex++);
			if(!level.isEmptyBlock(bp)&&isOwnedFrame(level,bp)){
				level.removeBlock(bp,false);
				miningProgressAcc-=cost;
			}
		}
		if(workIndex>=workQueue.size()){
			workQueue.clear();
			workIndex=0;
			miningProgressAcc=0f;
			startBuildingFrame();
		}
	}
	// ── STAVBA RÁMU ───────────────────────────────────────────────────────
	private void startBuildingFrame(){
		quarryState=State.BUILDING_FRAME;
		workQueue.clear();
		workQueue.addAll(areaManager.computeFramePositions(worldPosition.getY()));
		workIndex=0;
		sync();
	}
	private void tickBuildFrame(Level level){
		if(workQueue.isEmpty()){
			startBuildingFrame();
			return;
		}
		float progress=getProgressPerTick();
		if(progress<=0f) return;
		miningProgressAcc+=progress;
		float cost=15f; // Cena za stavbu jednoho bloku rámu
		while(workIndex<workQueue.size()&&miningProgressAcc>=cost){
			BlockPos fp=workQueue.get(workIndex++);
			if(isFrameBlock(level,fp)){
				level.setBlock(fp,DifModBlocks.QUARRY_FRAME.get().defaultBlockState(),3);
				if(level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity frame){
					frame.setOwner(worldPosition);
				}
				miningProgressAcc-=cost;
			}
		}
		if(workIndex>=workQueue.size()){
			workQueue.clear();
			workIndex=0;
			miningProgressAcc=0f;
			activeState=State.MINING;
			quarryState=State.MINING;
			areaManager.resetMiningPos(worldPosition.getY());
			if(level instanceof ServerLevel sl){
				areaManager.loadMiningChunks(sl);
			}
			sync();
		}
	}
	// ── TĚŽBA (delegováno na QuarryMiningLogic) ───────────────────────────
	private void tickMine(Level level){
		// Pravidelná kontrola celistvosti rámu
		if(++frameCheckTimer>=FRAME_CHECK_INTERVAL){
			frameCheckTimer=0;
			if(!isFrameIntact(level)){
				if(level instanceof ServerLevel sl) areaManager.unloadForcedChunks(sl);
				quarryState=State.CLEARING;
				activeState=State.CLEARING;
				workQueue.clear();
				workIndex=0;
				areaManager.setMiningPos(null);
				sync();
				return;
			}
		}
		float step=getProgressPerTick();
		if(step<=0f) return;
		// Delegujeme samotnou logiku ničení bloků a spotřeby progresu do QuarryMiningLogic
		miningProgressAcc=QuarryMiningLogic.doMiningTick(this,level,miningProgressAcc,step,hasSilkTouch,hasLiquidRemover);
	}
	public void finishMining(){
		quarryState=State.DONE;
		areaManager.setMiningPos(null);
		if(level instanceof ServerLevel sl) areaManager.unloadForcedChunks(sl);
		sync();
	}
	// ── Frame Utility ─────────────────────────────────────────────────────
	public boolean isFrameIntact(Level level){
		for(BlockPos fp: areaManager.computeFramePositions(worldPosition.getY())){
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
		if(level instanceof ServerLevel sl) areaManager.unloadForcedChunks(sl);
		quarryState=State.CLEARING;
		workQueue.clear();
		workIndex=0;
		areaManager.setMiningPos(null);
		sync();
	}
	public void onQuarryRemoved(){
		if(level==null||level.isClientSide) return;
		if(level instanceof ServerLevel sl) areaManager.unloadForcedChunks(sl);
		// Vypadne inventář
		for(int i=0;i<inventory.getSlots();i++){
			ItemStack s=inventory.getStackInSlot(i);
			if(!s.isEmpty()) Block.popResource(level,worldPosition,s);
		}
		// Naplánovat smazání rámu
		for(BlockPos fp: areaManager.computeFramePositions(worldPosition.getY())){
			if(level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity frame&&worldPosition.equals(frame.getOwnerPos())){
				frame.scheduleRemoval();
			}
		}
	}
	// ── Sync a Ukládání/Načítání (NBT) ────────────────────────────────────
	private void sync(){
		setChanged();
		if(level!=null&&!level.isClientSide){
			level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
		}
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider p){
		CompoundTag tag=new CompoundTag();
		tag.putInt("QS",quarryState.ordinal());
		BlockPos miningPos=areaManager.getMiningPos();
		if(miningPos!=null){
			tag.putInt("MineX",miningPos.getX());
			tag.putInt("MineY",miningPos.getY());
			tag.putInt("MineZ",miningPos.getZ());
		}
		if(areaManager.hasArea()){
			areaManager.getArea().save(tag);
		}
		return tag;
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider p){
		super.loadAdditional(tag,p);
		if(tag.contains("Inventory")) inventory.deserializeNBT(p,tag.getCompound("Inventory"));
		rebuildUpgradeCache();
		energy.receiveEnergy(tag.getInt("Energy")-energy.getEnergyStored(),false);
		int ord=tag.getInt("QS");
		quarryState=(ord>=0&&ord<State.values().length)?State.values()[ord]:State.NO_ENERGY;
		workIndex=tag.getInt("WI");
		if(tag.contains("MineX")){
			areaManager.setMiningPos(new BlockPos(tag.getInt("MineX"),tag.getInt("MineY"),tag.getInt("MineZ")));
		}
		// Načtení oblasti (nový i starý formát pro kompatibilitu)
		QuarryArea loadedArea=QuarryArea.load(tag);
		if(loadedArea==null){
			loadedArea=QuarryArea.loadLegacyHalf(tag,"LmHX","LmHZ","LmCX","LmCZ");
		}
		if(loadedArea!=null){
			areaManager.setArea(loadedArea);
		}
		if(quarryState==State.MINING){
			chunksNeedReload=true;
		}
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider p){
		super.saveAdditional(tag,p);
		tag.put("Inventory",inventory.serializeNBT(p));
		tag.putInt("Energy",energy.getEnergyStored());
		tag.putInt("QS",quarryState.ordinal());
		tag.putInt("WI",workIndex);
		BlockPos miningPos=areaManager.getMiningPos();
		if(miningPos!=null){
			tag.putInt("MineX",miningPos.getX());
			tag.putInt("MineY",miningPos.getY());
			tag.putInt("MineZ",miningPos.getZ());
		}
		if(areaManager.hasArea()){
			areaManager.getArea().save(tag);
		}
	}
	// ── Gettery (pro renderer, GUI a externí přístup) ─────────────────────
	public QuarryAreaManager getAreaManager(){
		return areaManager;
	}
	public BlockPos getMiningPos(){
		return areaManager.getMiningPos();
	}
	public State getQuarryState(){
		return quarryState;
	}
	public IItemHandler getInventory(){
		return inventory;
	}
	public IEnergyStorage getEnergyStorage(){
		return energy;
	}
	// GUI
	public int getAreaSizeX(){
		return areaManager.hasArea()?areaManager.getArea().sizeX():QuarryAreaManager.DEFAULT_RANGE*2+1;
	}
	public int getAreaSizeZ(){
		return areaManager.hasArea()?areaManager.getArea().sizeZ():QuarryAreaManager.DEFAULT_RANGE*2+1;
	}
	public int getAreaMinX(){
		return areaManager.hasArea()?areaManager.getArea().minX():getBlockPos().getX()-QuarryAreaManager.DEFAULT_RANGE;
	}
	public int getAreaMinZ(){
		return areaManager.hasArea()?areaManager.getArea().minZ():getBlockPos().getZ()-QuarryAreaManager.DEFAULT_RANGE;
	}
	public int getAreaMaxX(){
		return areaManager.hasArea()?areaManager.getArea().maxX():getBlockPos().getX()+QuarryAreaManager.DEFAULT_RANGE;
	}
	public int getAreaMaxZ(){
		return areaManager.hasArea()?areaManager.getArea().maxZ():getBlockPos().getZ()+QuarryAreaManager.DEFAULT_RANGE;
	}
	// ── Menu ──────────────────────────────────────────────────────────────
	@Override
	public @NotNull Component getDisplayName(){
		return Component.translatable("block.dif.quarry");
	}
	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id,@NotNull Inventory inv,@NotNull Player player){
		return new QuarryMenu(id,inv,this);
	}
}