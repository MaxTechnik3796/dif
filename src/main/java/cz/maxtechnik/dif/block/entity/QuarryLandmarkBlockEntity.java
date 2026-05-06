package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.renderer.LandmarkOverlayRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
public class QuarryLandmarkBlockEntity extends BlockEntity{
	public static final int MAX_SEARCH=QuarryBlockEntity.MAX_AREA_SIDE;
	private final List<BlockPos> partnerPositions=new ArrayList<>(2);
	private boolean formed=false;
	private int formedHalfX=0;
	private int formedHalfZ=0;
	@Nullable
	private BlockPos formedCenter=null;
	public QuarryLandmarkBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.QUARRY_LANDMARK.get(),pos,state);
	}
	// Pravý klik – spustí scan a pokusí se o formaci
	public void onRightClick(Player player){
		if(level==null||level.isClientSide) return;
		// Ověř že partneři stále existují, jinak resetuj
		if(formed&&!partnerPositions.isEmpty()){
			for(BlockPos partnerPos: partnerPositions){
				if(!level.getBlockState(partnerPos).is(DifModBlocks.QUARRY_LANDMARK.get())){
					resetFormation();
					break;
				}
			}
		}
		List<BlockPos> foundLandmarks=scanLandmarks();
		int totalCount=foundLandmarks.size()+1;
		if(totalCount==1){
			player.sendSystemMessage(Component.literal("§eNalezen §f1§e/§f3§e landmark. Chybí §f2§e."));
			return;
		}
		if(totalCount==2){
			player.sendSystemMessage(Component.literal("§eNalezeny §f2§e/§f3§e landmarky. Chybí §f1§e."));
			return;
		}
		if(totalCount>3){
			player.sendSystemMessage(Component.literal("§cV okolí je více než 3 landmarky – nelze určit oblast."));
			return;
		}
		List<BlockPos> allLandmarks=new ArrayList<>(foundLandmarks);
		allLandmarks.add(worldPosition);
		FormResult formResult=tryForm(allLandmarks);
		if(formResult==null){
			player.sendSystemMessage(Component.literal("§cLandmarky netvoří správný L-tvar nebo je vzdálenost mimo rozsah "+"(min 3, max §f"+MAX_SEARCH+"§c bloků)."));
			return;
		}
		applyFormation(allLandmarks,formResult);
		player.sendSystemMessage(Component.literal("§aFormace: §f"+formResult.sizeX()+"§ax§f"+formResult.sizeZ()+" §abloků. Polož Quarry ke kraji oblasti."));
	}
	// Prohledá okolí na ostatní landmarky na stejné Y úrovni
	private List<BlockPos> scanLandmarks(){
		List<BlockPos> found=new ArrayList<>(3);
		if(level==null) return found;
		int scanY=worldPosition.getY();
		for(int dx=-MAX_SEARCH;dx<=MAX_SEARCH&&found.size()<3;dx++){
			for(int dz=-MAX_SEARCH;dz<=MAX_SEARCH&&found.size()<3;dz++){
				if(dx==0&&dz==0) continue;
				BlockPos scanPos=new BlockPos(worldPosition.getX()+dx,scanY,worldPosition.getZ()+dz);
				if(level.getBlockState(scanPos).is(DifModBlocks.QUARRY_LANDMARK.get())) found.add(scanPos);
			}
		}
		return found;
	}
	// Zkusí zformovat oblast ze tří landmarků v L-tvaru
	@Nullable
	public static FormResult tryForm(List<BlockPos> allLandmarks){
		if(allLandmarks.size()!=3) return null;
		BlockPos lmA=allLandmarks.get(0);
		BlockPos lmB=allLandmarks.get(1);
		BlockPos lmC=allLandmarks.get(2);
		int minX=Math.min(lmA.getX(),Math.min(lmB.getX(),lmC.getX()));
		int maxX=Math.max(lmA.getX(),Math.max(lmB.getX(),lmC.getX()));
		int minZ=Math.min(lmA.getZ(),Math.min(lmB.getZ(),lmC.getZ()));
		int maxZ=Math.max(lmA.getZ(),Math.max(lmB.getZ(),lmC.getZ()));
		int spanX=maxX-minX;
		int spanZ=maxZ-minZ;
		if(spanX<2||spanZ<2) return null;
		if(spanX>MAX_SEARCH||spanZ>MAX_SEARCH) return null;
		// Ověř L-tvar – jeden z rohů musí sdílet X s jedním a Z s druhým
		if(isLShape(lmA,lmB,lmC)&&isLShape(lmB,lmA,lmC)&&isLShape(lmC,lmA,lmB)) return null;
		int centerX=(minX+maxX)/2;
		int centerZ=(minZ+maxZ)/2;
		// halfX/Z = max vzdálenost středu k okraji (ceiling pro liché rozměry)
		int halfX=Math.max(centerX-minX,maxX-centerX);
		int halfZ=Math.max(centerZ-minZ,maxZ-centerZ);
		int sizeX=spanX-1;
		int sizeZ=spanZ-1;
		return new FormResult(sizeX,sizeZ,halfX,halfZ,new BlockPos(centerX,lmA.getY(),centerZ));
	}
	// Zkontroluje jestli 'corner' tvoří roh L-tvaru s p1 a p2
	private static boolean isLShape(BlockPos corner,BlockPos pos1,BlockPos pos2){
		return (corner.getX()!=pos1.getX()||corner.getZ()!=pos2.getZ())&&(corner.getX()!=pos2.getX()||corner.getZ()!=pos1.getZ());
	}
	// Aplikuje formaci na všechny tři landmarky
	private void applyFormation(List<BlockPos> allLandmarks,FormResult formResult){
		if(level==null) return;
		for(BlockPos lmPos: allLandmarks){
			if(!(level.getBlockEntity(lmPos) instanceof QuarryLandmarkBlockEntity lmEntity)) continue;
			lmEntity.partnerPositions.clear();
			for(BlockPos other: allLandmarks) if(!other.equals(lmPos)) lmEntity.partnerPositions.add(other);
			lmEntity.formed=true;
			lmEntity.formedCenter=formResult.center();
			lmEntity.formedHalfX=formResult.halfX();
			lmEntity.formedHalfZ=formResult.halfZ();
			lmEntity.setChanged();
			level.sendBlockUpdated(lmPos,lmEntity.getBlockState(),lmEntity.getBlockState(),3);
		}
	}
	// Resetuje formaci tohoto landmarku
	private void resetFormation(){
		formed=false;
		formedCenter=null;
		formedHalfX=formedHalfZ=0;
		partnerPositions.clear();
		setChanged();
		if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
	}
	// Voláno z Quarry.onPlace – aplikuje oblast na quarry a zničí všechny landmarky
	public void applyToQuarry(Level level,BlockPos quarryPos){
		if(!formed||formedCenter==null) return;
		if(!(level.getBlockEntity(quarryPos) instanceof QuarryBlockEntity quarryEntity)) return;
		quarryEntity.setLandmarkArea(formedHalfX,formedHalfZ,formedCenter);
		List<BlockPos> allLandmarks=new ArrayList<>(partnerPositions);
		allLandmarks.add(worldPosition);
		for(BlockPos lmPos: allLandmarks){
			if(level.getBlockState(lmPos).is(DifModBlocks.QUARRY_LANDMARK.get())){
				Block.popResource(level,lmPos,new ItemStack(DifModBlocks.QUARRY_LANDMARK.get().asItem()));
				level.removeBlock(lmPos,false);
			}
		}
	}
	// Při zničení informuj partnerské landmarky aby ztratili formaci
	public void onRemoved(){
		if(level==null||level.isClientSide||!formed) return;
		for(BlockPos partnerPos: new ArrayList<>(partnerPositions)){
			if(!(level.getBlockEntity(partnerPos) instanceof QuarryLandmarkBlockEntity partnerEntity)) continue;
			partnerEntity.formed=false;
			partnerEntity.formedCenter=null;
			partnerEntity.formedHalfX=partnerEntity.formedHalfZ=0;
			partnerEntity.partnerPositions.clear();
			partnerEntity.setChanged();
			level.sendBlockUpdated(partnerPos,partnerEntity.getBlockState(),partnerEntity.getBlockState(),3);
		}
	}
	// Klientský tracking pro LandmarkOverlayRenderer
	@Override
	public void onLoad(){
		super.onLoad();
		if(level!=null&&level.isClientSide) updateClientRenderer();
	}
	@Override
	public void setRemoved(){
		super.setRemoved();
		if(level!=null&&level.isClientSide) LandmarkOverlayRenderer.unregister(worldPosition);
	}
	@Override
	public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider lookupProvider){
		loadAdditional(tag, lookupProvider);
		updateClientRenderer();
	}
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, @NotNull HolderLookup.Provider lookupProvider){
		CompoundTag tag=pkt.getTag();
		if(tag!=null) loadAdditional(tag, lookupProvider);
		updateClientRenderer();
	}
	private void updateClientRenderer(){
		if(level==null||!level.isClientSide) return;
		if(formed) LandmarkOverlayRenderer.register(this);
		else LandmarkOverlayRenderer.unregister(worldPosition);
	}
	// Gettery
	public boolean isFormed(){
		return !formed;
	}
	public int getFormedHalfX(){
		return formedHalfX;
	}
	public int getFormedHalfZ(){
		return formedHalfZ;
	}
	@Nullable
	public BlockPos getFormedCenter(){
		return formedCenter;
	}
	// NBT serializace
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag, provider);
		tag.putBoolean("Formed",formed);
		tag.putInt("FHX",formedHalfX);
		tag.putInt("FHZ",formedHalfZ);
		if(formedCenter!=null) tag.put("FC",NbtUtils.writeBlockPos(formedCenter));
		ListTag partnerList=new ListTag();
		for(BlockPos partnerPos: partnerPositions) partnerList.add(NbtUtils.writeBlockPos(partnerPos));
		tag.put("Partners",partnerList);
	}
	@Override
	public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag, provider);
		formed=tag.getBoolean("Formed");
		formedHalfX=tag.getInt("FHX");
		formedHalfZ=tag.getInt("FHZ");
		formedCenter=tag.contains("FC")?NbtUtils.readBlockPos(tag.getCompound("FC")):null;
		partnerPositions.clear();
		ListTag partnerList=tag.getList("Partners",Tag.TAG_COMPOUND);
		for(int i=0;i<partnerList.size();i++) partnerPositions.add(NbtUtils.readBlockPos(partnerList.getCompound(i)));
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(){
		CompoundTag tag=new CompoundTag();
		tag.putBoolean("Formed",formed);
		tag.putInt("FHX",formedHalfX);
		tag.putInt("FHZ",formedHalfZ);
		if(formedCenter!=null) tag.put("FC",NbtUtils.writeBlockPos(formedCenter));
		ListTag partnerList=new ListTag();
		for(BlockPos partnerPos: partnerPositions) partnerList.add(NbtUtils.writeBlockPos(partnerPos));
		tag.put("Partners",partnerList);
		return tag;
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	public record FormResult(int sizeX,int sizeZ,int halfX,int halfZ,BlockPos center){
	}
}