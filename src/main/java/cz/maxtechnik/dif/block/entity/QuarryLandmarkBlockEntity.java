package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.renderer.QuarryRenderer;
import cz.maxtechnik.dif.util.quarry.QuarryArea;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
public class QuarryLandmarkBlockEntity extends BlockEntity implements IHaveGoggleInformation{
	public static final int MAX_SEARCH=128;
	public static final int MIN_SPAN=2;
	private static final int[][] DIRS={{1,0},{-1,0},{0,1},{0,-1}};
	private final List<BlockPos> partnerPositions=new ArrayList<>(2);
	private boolean formed=false;
	@Nullable
	private QuarryArea formedArea=null;
	public QuarryLandmarkBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.QUARRY_LANDMARK.get(),pos,blockState);
	}
	public void onRightClick(){
		if(level==null||level.isClientSide||formed) return;
		List<BlockPos> match=findFormableGroup();
		if(match!=null){
			QuarryArea area=tryForm(match.get(0),match.get(1),match.get(2));
			if(area!=null) applyFormation(match,area);
		}
	}
	@Nullable
	private List<BlockPos> findFormableGroup(){
		if(level==null||formed) return null;
		List<BlockPos> nearby=new ArrayList<>();
		for(int[] dir: DIRS){
			BlockPos found=scanDirection(worldPosition,dir[0],dir[1]);
			if(found!=null) nearby.add(found);
		}
		if(nearby.size()>=2) for(int a=0;a<nearby.size();a++) for(int b=a+1;b<nearby.size();b++) if(tryForm(worldPosition,nearby.get(a),nearby.get(b))!=null) return List.of(worldPosition,nearby.get(a),nearby.get(b));
		for(BlockPos first: nearby){
			boolean onX=first.getZ()==worldPosition.getZ();
			int[][] perps=onX?new int[][]{{0,-1},{0,1}}:new int[][]{{-1,0},{1,0}};
			for(int[] p: perps){
				BlockPos third=scanDirection(first,p[0],p[1]);
				if(third!=null&&!third.equals(worldPosition)&&tryForm(worldPosition,first,third)!=null) return List.of(worldPosition,first,third);
			}
		}
		return null;
	}
	@Nullable
	private BlockPos scanDirection(BlockPos from,int dx,int dz){
		if(level==null) return null;
		for(int d=1;d<=MAX_SEARCH;d++){
			BlockPos p=from.offset(dx*d,0,dz*d);
			if(!level.getBlockState(p).is(DifModBlocks.QUARRY_LANDMARK.get())) continue;
			if(level.getBlockEntity(p) instanceof QuarryLandmarkBlockEntity lm&&lm.formed) return null;
			return d>=MIN_SPAN?p:null;
		}
		return null;
	}
	@Nullable
	public static QuarryArea tryForm(BlockPos pos1,BlockPos pos2,BlockPos pos3){
		if(isNotCorner(pos1,pos2,pos3)&&isNotCorner(pos2,pos1,pos3)&&isNotCorner(pos3,pos1,pos2)) return null;
		int minX=Math.min(pos1.getX(),Math.min(pos2.getX(),pos3.getX()));
		int maxX=Math.max(pos1.getX(),Math.max(pos2.getX(),pos3.getX()));
		int minZ=Math.min(pos1.getZ(),Math.min(pos2.getZ(),pos3.getZ()));
		int maxZ=Math.max(pos1.getZ(),Math.max(pos2.getZ(),pos3.getZ()));
		int spanX=maxX-minX, spanZ=maxZ-minZ;
		if(spanX<MIN_SPAN||spanZ<MIN_SPAN||spanX>MAX_SEARCH||spanZ>MAX_SEARCH) return null;
		return new QuarryArea(minX,maxX,minZ,maxZ);
	}
	private static boolean isNotCorner(BlockPos corner,BlockPos pos1,BlockPos pos2){
		return (corner.getX()!=pos1.getX()||corner.getZ()!=pos2.getZ())&&(corner.getX()!=pos2.getX()||corner.getZ()!=pos1.getZ());
	}
	private void applyFormation(List<BlockPos> landmarks,QuarryArea area){
		if(level==null) return;
		for(BlockPos lmPos: landmarks){
			if(!(level.getBlockEntity(lmPos) instanceof QuarryLandmarkBlockEntity blockEntity)) continue;
			blockEntity.partnerPositions.clear();
			for(BlockPos other: landmarks) if(!other.equals(lmPos)) blockEntity.partnerPositions.add(other);
			blockEntity.formed=true;
			blockEntity.formedArea=area;
			blockEntity.setChanged();
			level.sendBlockUpdated(lmPos,blockEntity.getBlockState(),blockEntity.getBlockState(),3);
		}
	}
	public void applyToQuarry(Level level,BlockPos quarryPos){
		if(!formed||formedArea==null||!(level.getBlockEntity(quarryPos) instanceof QuarryBlockEntity blockEntity)) return;
		blockEntity.setArea(formedArea.minX(),formedArea.maxX(),formedArea.minZ(),formedArea.maxZ());
		List<BlockPos> all=new ArrayList<>(partnerPositions);
		all.add(worldPosition);
		for(BlockPos lmPos: all){
			if(level.getBlockState(lmPos).is(DifModBlocks.QUARRY_LANDMARK.get())){
				Block.popResource(level,lmPos,new ItemStack(DifModBlocks.QUARRY_LANDMARK.get().asItem()));
				level.removeBlock(lmPos,false);
			}
		}
	}
	public void onRemoved(){
		if(level==null||level.isClientSide||!formed) return;
		for(BlockPos pp: new ArrayList<>(partnerPositions)){
			if(!(level.getBlockEntity(pp) instanceof QuarryLandmarkBlockEntity blockEntity)) continue;
			blockEntity.formed=false;
			blockEntity.formedArea=null;
			blockEntity.partnerPositions.clear();
			blockEntity.setChanged();
			level.sendBlockUpdated(pp,blockEntity.getBlockState(),blockEntity.getBlockState(),3);
		}
	}
	@Override
	public void onLoad(){
		super.onLoad();
		if(level!=null&&level.isClientSide) QuarryRenderer.register(this);
	}
	@Override
	public void setRemoved(){
		super.setRemoved();
		if(level!=null&&level.isClientSide) QuarryRenderer.unregister(worldPosition);
	}
	@Override
	public void handleUpdateTag(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		loadAdditional(tag,provider);
		if(level!=null&&level.isClientSide) QuarryRenderer.register(this);
	}
	@Override
	public void onDataPacket(@NotNull Connection connection,ClientboundBlockEntityDataPacket pkt,@NotNull HolderLookup.Provider provider){
		loadAdditional(pkt.getTag(),provider);
		if(level!=null&&level.isClientSide) QuarryRenderer.register(this);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip,boolean isPlayerSneaking){
		Component statusComponent;
		QuarryArea canFormArea=getFormableArea();
		if(formed&&formedArea!=null) statusComponent=Component.literal("Formed").withStyle(ChatFormatting.GREEN);
		else if(canFormArea!=null) statusComponent=Component.literal("Can Be Formed (3/3)").withStyle(ChatFormatting.GOLD);
		else{
			int count=countConnectedLandmarks()+1;
			statusComponent=Component.literal("Cannot Be Formed ("+count+"/3)").withStyle(ChatFormatting.RED);
		}
		tooltip.add(DifMod.fixedComponent(Component.literal("Status: ").withStyle(ChatFormatting.GRAY).append(statusComponent)));
		QuarryArea displayArea=formed?formedArea:canFormArea;
		if(displayArea!=null){
			QuarryArea mining=displayArea.miningBounds();
			tooltip.add(DifMod.fixedComponent(Component.literal("Area: ").withStyle(ChatFormatting.GRAY).append(Component.literal(displayArea.sizeX()+" x "+displayArea.sizeZ()+" blocks").withStyle(ChatFormatting.WHITE))));
			tooltip.add(DifMod.fixedComponent(Component.literal("Mining: ").withStyle(ChatFormatting.GRAY).append(Component.literal(mining.sizeX()+" x "+mining.sizeZ()+" blocks").withStyle(ChatFormatting.WHITE))));
		}
		return true;
	}
	@Nullable
	public QuarryArea getFormableArea(){
		List<BlockPos> match=findFormableGroup();
		return match!=null?tryForm(match.get(0),match.get(1),match.get(2)):null;
	}
	public int countConnectedLandmarks(){
		if(level==null) return 0;
		int count=0;
		for(int[] dir: DIRS) if(scanDirection(worldPosition,dir[0],dir[1])!=null) count++;
		return count;
	}
	public boolean isFormed(){
		return formed;
	}
	@Nullable
	public QuarryArea getFormedArea(){
		return formedArea;
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider p){
		super.saveAdditional(tag,p);
		tag.putBoolean("Formed",formed);
		if(formedArea!=null) formedArea.save(tag);
		tag.putLongArray("Partners",partnerPositions.stream().mapToLong(BlockPos::asLong).toArray());
	}
	@Override
	public void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider p){
		super.loadAdditional(tag,p);
		formed=tag.getBoolean("Formed");
		formedArea=QuarryArea.load(tag);
		partnerPositions.clear();
		if(tag.contains("Partners")) for(long l: tag.getLongArray("Partners")) partnerPositions.add(BlockPos.of(l));
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider p){
		CompoundTag tag=new CompoundTag();
		saveAdditional(tag,p);
		return tag;
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
}