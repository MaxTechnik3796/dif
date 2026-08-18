package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import cz.maxtechnik.dif.block.Quarry;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.quarry.QuarryArea;
import cz.maxtechnik.dif.util.quarry.QuarryAreaManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static cz.maxtechnik.dif.DifMod.goggleTooltipFix;
/**
 * BlockEntity pro těžební zařízení (Quarry).
 * Připojeno na Create kinetic síť (shaft zdola).
 * 1 RPM = 128 SU zátěže. Těžební rychlost odvislá přímo od RPM.
 * Podporuje vyžadované Goggles info a Redstone zastavení.
 */
public class QuarryBlockEntity extends KineticBlockEntity{
	public enum State{NO_ENERGY,CLEARING,BUILDING_FRAME,MINING,DONE}
	private static final int FRAME_CHECK_INTERVAL=40;
	private static final int POS_CHECK_INTERVAL=10;
	// -------------------- Stav Quarry --------------------
	private State quarryState=State.NO_ENERGY;
	private State activeState=State.CLEARING;
	private int frameCheckTimer=0;
	private int posCheckTimer=0;
	private float miningProgressAcc=0F;
	private boolean chunksNeedReload=false;
	private boolean lastRedstoneState=false;
	private BlockPos originPos;
	// -------------------- QuarryAreaManager --------------------
	private final QuarryAreaManager areaManager=new QuarryAreaManager();
	// -------------------- Pracovní fronta (pro čištění a stavbu rámu) --------------------
	private final ArrayList<BlockPos> workQueue=new ArrayList<>();
	private int workIndex=0;
	public QuarryBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.QUARRY.get(),pos,blockState);
	}
	public boolean isRedstonePowered(){
		return level!=null&&level.hasNeighborSignal(worldPosition);
	}
	// -------------------- Create Kinetic Stress (0 při redstonu) --------------------
	@Override
	public float calculateStressApplied(){
		if(isRedstonePowered()){
			this.lastStressApplied=0F;
			return 0F;
		}
		float impact=cz.maxtechnik.dif.config.DifModServerConfig.QUARRY_STRESS_IMPACT.get().floatValue();
		this.lastStressApplied=impact;
		return impact;
	}
	// -------------------- Goggles Tooltip (Engineer's Goggles Info) --------------------
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip,boolean isPlayerSneaking){
		super.addToGoggleTooltip(tooltip,isPlayerSneaking);
		// 1. Status
		Component statusComponent;
		if(isRedstonePowered()){
			statusComponent=Component.literal("Stopped").withStyle(ChatFormatting.GOLD);
		}else if(quarryState==State.DONE){
			statusComponent=Component.literal("Finished").withStyle(ChatFormatting.AQUA);
		}else if(isOverStressed()){
			statusComponent=Component.literal("Overstressed").withStyle(ChatFormatting.RED);
		}else if(Math.abs(getSpeed())==0){
			statusComponent=Component.literal("No Power").withStyle(ChatFormatting.RED);
		}else{
			String actionName=switch(quarryState){
				case CLEARING -> "Clearing Area";
				case BUILDING_FRAME -> "Building Frame";
				case MINING -> "Mining";
				default -> "Active";
			};
			statusComponent=Component.literal(actionName).withStyle(ChatFormatting.GREEN);
		}
		tooltip.add(Component.literal(goggleTooltipFix+"Status: ").withStyle(ChatFormatting.GRAY).append(statusComponent));
		// 2. Area Size & Mining Size
		if(areaManager.hasArea()){
			QuarryArea area=areaManager.getArea();
			QuarryArea mining=area.miningBounds();
			tooltip.add(Component.literal(goggleTooltipFix+"Area: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(area.sizeX()+" x "+area.sizeZ()+" blocks").withStyle(ChatFormatting.WHITE)));
			tooltip.add(Component.literal(goggleTooltipFix+"Mining: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(mining.sizeX()+" x "+mining.sizeZ()+" blocks").withStyle(ChatFormatting.WHITE)));
		}
		return true;
	}
	// -------------------- Inicializace oblasti --------------------
	public void setArea(int minX,int maxX,int minZ,int maxZ){
		originPos=worldPosition;
		areaManager.setArea(new QuarryArea(minX,maxX,minZ,maxZ));
		sendData();
	}
	public void ensureAreaInitialized(){
		if(originPos==null){
			originPos=worldPosition;
		}
		if(!areaManager.hasArea()){
			Direction facing=getBlockState().getValue(Quarry.FACING);
			BlockPos center=worldPosition.relative(facing.getOpposite(),QuarryAreaManager.DEFAULT_RANGE+1);
			int cx=center.getX(), cz=center.getZ(), dr=QuarryAreaManager.DEFAULT_RANGE;
			areaManager.setArea(new QuarryArea(cx-dr,cx+dr,cz-dr,cz+dr));
		}
	}
	// Rychlost těžby podle RPM sítě
	public float getProgressPerTick(){
		float speed=Math.abs(getSpeed());
		if(speed<=0F||isOverStressed()||isRedstonePowered()) return 0F;
		return Math.clamp(speed/12.8F,0.1F,20F);
	}
	// -------------------- HLAVNÍ TICK --------------------
	@Override
	public void tick(){
		super.tick();
		if(level==null||level.isClientSide) return;
		// Kontrola pohybu / kontraptce každých 10 ticků
		if(++posCheckTimer>=POS_CHECK_INTERVAL){
			posCheckTimer=0;
			if(isVirtual()||(originPos!=null&&!worldPosition.equals(originPos))){
				resetAreaDueToMovement();
				return;
			}
		}
		ensureAreaInitialized();
		boolean redstoneStopped=isRedstonePowered();
		if(lastRedstoneState!=redstoneStopped){
			lastRedstoneState=redstoneStopped;
			if(hasNetwork())
				getOrCreateNetwork().updateStress();
			sendData();
		}
		float speed=Math.abs(getSpeed());
		boolean hasPower=speed>0F&&!isOverStressed()&&!redstoneStopped;
		if(!hasPower&&quarryState!=State.DONE){
			if(quarryState!=State.NO_ENERGY){
				activeState=quarryState;
				quarryState=State.NO_ENERGY;
				sendData();
			}
			return;
		}
		if(hasPower&&quarryState==State.NO_ENERGY){
			quarryState=activeState;
			sendData();
		}
		switch(quarryState){
			case CLEARING -> tickClearing(level);
			case BUILDING_FRAME -> tickBuildFrame(level);
			case MINING -> tickMine(level);
			default -> {
			}
		}
		if(chunksNeedReload&&quarryState==State.MINING&&level instanceof ServerLevel sl){
			areaManager.loadMiningChunks(sl);
			chunksNeedReload=false;
		}
	}
	// -------------------- ČIŠTĚNÍ OBLASTI PRO RÁM --------------------
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
		miningProgressAcc=0F;
		sendData();
	}
	private void tickClearing(Level level){
		if(workQueue.isEmpty()){
			startClearing(level);
			return;
		}
		float progress=getProgressPerTick();
		if(progress<=0F) return;
		miningProgressAcc+=progress;
		int safety=0;
		while(workIndex<workQueue.size()&&safety++<1000){
			BlockPos bp=workQueue.get(workIndex);
			BlockState state=level.getBlockState(bp);
			if(state.isAir()||!isOwnedFrame(level,bp)){
				workIndex++;
				continue;
			}
			float hardness=state.getDestroySpeed(level,bp);
			if(hardness<0){
				workIndex++;
				continue;
			}
			float required=Math.max(1F,hardness*10F);
			if(miningProgressAcc<required){
				break;
			}
			miningProgressAcc-=required;
			level.removeBlock(bp,false);
			workIndex++;
		}
		if(workIndex>=workQueue.size()){
			workQueue.clear();
			workIndex=0;
			miningProgressAcc=0f;
			startBuildingFrame();
		}
	}
	// -------------------- STAVBA RÁMU --------------------
	private void startBuildingFrame(){
		quarryState=State.BUILDING_FRAME;
		workQueue.clear();
		workQueue.addAll(areaManager.computeFramePositions(worldPosition.getY()));
		workIndex=0;
		sendData();
	}
	private void tickBuildFrame(Level level){
		if(workQueue.isEmpty()){
			startBuildingFrame();
			return;
		}
		float progress=getProgressPerTick();
		if(progress<=0F) return;
		miningProgressAcc+=progress;
		float cost=15F;
		while(workIndex<workQueue.size()&&miningProgressAcc>=cost){
			BlockPos fp=workQueue.get(workIndex++);
			if(isFrameBlock(level,fp)){
				level.setBlock(fp,DifModBlocks.QUARRY_FRAME.get().defaultBlockState(),3);
				if(level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity frame)
					frame.setOwner(worldPosition);
				miningProgressAcc-=cost;
			}
		}
		if(workIndex>=workQueue.size()){
			workQueue.clear();
			workIndex=0;
			miningProgressAcc=0F;
			activeState=State.MINING;
			quarryState=State.MINING;
			areaManager.resetMiningPos(worldPosition.getY());
			if(level instanceof ServerLevel sl){
				areaManager.loadMiningChunks(sl);
			}
			sendData();
		}
	}
	// -------------------- TĚŽBA --------------------
	private void tickMine(Level level){
		if(++frameCheckTimer>=FRAME_CHECK_INTERVAL){
			frameCheckTimer=0;
			if(!isFrameIntact(level)){
				if(level instanceof ServerLevel sl) areaManager.unloadForcedChunks(sl);
				quarryState=State.CLEARING;
				activeState=State.CLEARING;
				workQueue.clear();
				workIndex=0;
				areaManager.setMiningPos(null);
				sendData();
				return;
			}
		}
		float step=getProgressPerTick();
		if(step<=0F) return;
		miningProgressAcc=QuarryMiningLogic.doMiningTick(this,level,miningProgressAcc,step);
	}
	public void finishMining(){
		quarryState=State.DONE;
		areaManager.setMiningPos(null);
		if(level instanceof ServerLevel sl) areaManager.unloadForcedChunks(sl);
		sendData();
	}
	// -------------------- Frame Utility --------------------
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
		sendData();
	}
	public void resetAreaDueToMovement(){
		try{
			if(level instanceof ServerLevel sl&&!level.isClientSide)
				areaManager.unloadForcedChunks(sl);
			BlockPos checkPos=originPos!=null?originPos:worldPosition;
			if(areaManager.hasArea()&&level!=null&&!level.isClientSide){
				int yBase=checkPos.getY();
				List<BlockPos> frames=areaManager.computeFramePositions(yBase);
				for(BlockPos fp: frames){
					if(level.isLoaded(fp)&&level.getBlockEntity(fp) instanceof QuarryFrameBlockEntity frame){
						if(checkPos.equals(frame.getOwnerPos()))
							frame.scheduleRemoval();
					}
				}
			}
		}catch(Exception ignored){
			// Zabezpečení proti padání při neplatném nebo přechodném stavu sveta/kontraptce
		}
		areaManager.setArea(null);
		areaManager.setMiningPos(null);
		workQueue.clear();
		workIndex=0;
		miningProgressAcc=0f;
		originPos=null;
		quarryState=State.NO_ENERGY;
		activeState=State.CLEARING;
		try{
			sendData();
		}catch(Exception ignored){
		}
	}
	public void onQuarryRemoved(){
		if(level==null||level.isClientSide) return;
		resetAreaDueToMovement();
	}
	// -------------------- Create Kinetic NBT (read / write) --------------------
	@Override
	protected void read(CompoundTag tag,HolderLookup.Provider registries,boolean clientPacket){
		super.read(tag,registries,clientPacket);
		int ord=tag.getInt("QS");
		quarryState=(ord>=0&&ord<State.values().length)?State.values()[ord]:State.NO_ENERGY;
		workIndex=tag.getInt("WI");
		if(tag.contains("OrigX"))
			originPos=new BlockPos(tag.getInt("OrigX"),tag.getInt("OrigY"),tag.getInt("OrigZ"));
		else
			originPos=worldPosition;
		if(tag.contains("MineX"))
			areaManager.setMiningPos(new BlockPos(tag.getInt("MineX"),tag.getInt("MineY"),tag.getInt("MineZ")));
		QuarryArea loadedArea=QuarryArea.load(tag);
		if(loadedArea!=null)
			areaManager.setArea(loadedArea);
		if(quarryState==State.MINING)
			chunksNeedReload=true;
	}
	@Override
	protected void write(CompoundTag tag,HolderLookup.Provider registries,boolean clientPacket){
		super.write(tag,registries,clientPacket);
		tag.putInt("QS",quarryState.ordinal());
		tag.putInt("WI",workIndex);
		if(originPos!=null){
			tag.putInt("OrigX",originPos.getX());
			tag.putInt("OrigY",originPos.getY());
			tag.putInt("OrigZ",originPos.getZ());
		}
		BlockPos miningPos=areaManager.getMiningPos();
		if(miningPos!=null){
			tag.putInt("MineX",miningPos.getX());
			tag.putInt("MineY",miningPos.getY());
			tag.putInt("MineZ",miningPos.getZ());
		}
		if(areaManager.hasArea())
			areaManager.getArea().save(tag);
	}
	// -------------------- Gettery --------------------
	public QuarryAreaManager getAreaManager(){
		return areaManager;
	}
	public BlockPos getMiningPos(){
		return areaManager.getMiningPos();
	}
	public State getQuarryState(){
		return quarryState;
	}
	public int getAreaMinX(){
		ensureAreaInitialized();
		return areaManager.hasArea()?areaManager.getArea().minX():getBlockPos().getX()-QuarryAreaManager.DEFAULT_RANGE;
	}
	public int getAreaMinZ(){
		ensureAreaInitialized();
		return areaManager.hasArea()?areaManager.getArea().minZ():getBlockPos().getZ()-QuarryAreaManager.DEFAULT_RANGE;
	}
	public int getAreaMaxX(){
		ensureAreaInitialized();
		return areaManager.hasArea()?areaManager.getArea().maxX():getBlockPos().getX()+QuarryAreaManager.DEFAULT_RANGE;
	}
	public int getAreaMaxZ(){
		ensureAreaInitialized();
		return areaManager.hasArea()?areaManager.getArea().maxZ():getBlockPos().getZ()+QuarryAreaManager.DEFAULT_RANGE;
	}
}