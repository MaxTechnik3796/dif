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
	public QuarryLandmarkBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.QUARRY_LANDMARK.get(),pos,blockState);
	}
	public void onRightClick(Player player){
		if(level==null||level.isClientSide) return;
		if(formed) return;

		// Najdi landmarky ve 4 směrech
		List<BlockPos> found = new ArrayList<>();
		int myX = worldPosition.getX();
		int myY = worldPosition.getY();
		int myZ = worldPosition.getZ();

		boolean tooClose = false;
		for(int[] dir : new int[][]{{0,-1},{0,1},{-1,0},{1,0}}){
			ScanResult sr = scanDirectionFull(myX, myY, myZ, dir[0], dir[1]);
			if(sr == null) continue;
			if(sr.tooClose()){ tooClose = true; continue; }
			found.add(sr.pos());
		}
		if(tooClose){
			player.sendSystemMessage(Component.literal("§cA landmark is too close (min "+MIN_DISTANCE+" blocks)."));
			return;
		}

		// Pokud mám 2 — zkontroluj jestli tvoří L se mnou jako rohem
		if(found.size() >= 2){
			// Zkus všechny kombinace dvou z nalezených
			for(int a = 0; a < found.size(); a++){
				for(int b = a+1; b < found.size(); b++){
					List<BlockPos> candidate = new ArrayList<>();
					candidate.add(worldPosition);
					candidate.add(found.get(a));
					candidate.add(found.get(b));
					FormResult result = tryForm(candidate);
					if(result != null){
						applyFormation(candidate, result);
						player.sendSystemMessage(Component.literal("§aArea marked: §f"+result.sizeX()+"§ax§f"+result.sizeZ()+" §ablocks. Place Quarry at the edge."));
						return;
					}
				}
			}
		}

		// Pokud mám 1 — ten jeden zkontroluje své 2 kolmé směry
		if(!found.isEmpty()){
			for(BlockPos first : found){
				boolean firstOnX = first.getZ() == worldPosition.getZ();
				// Kolmé směry od prvního
				int[] perp1 = firstOnX ? new int[]{0,-1} : new int[]{-1,0};
				int[] perp2 = firstOnX ? new int[]{0, 1} : new int[]{1, 0};
				BlockPos third1 = scanDirectionFrom(first, perp1[0], perp1[1]);
				BlockPos third2 = scanDirectionFrom(first, perp2[0], perp2[1]);
				for(BlockPos third : new BlockPos[]{third1, third2}){
					if(third == null || third.equals(worldPosition)) continue;
					List<BlockPos> candidate = new ArrayList<>();
					candidate.add(worldPosition);
					candidate.add(first);
					candidate.add(third);
					FormResult result = tryForm(candidate);
					if(result != null){
						applyFormation(candidate, result);
						player.sendSystemMessage(Component.literal("§aArea marked: §f"+result.sizeX()+"§ax§f"+result.sizeZ()+" §ablocks. Place Quarry at the edge."));
						return;
					}
				}
			}
		}

		player.sendSystemMessage(Component.literal("§cNo valid area found. Place 3 landmarks in an L-shape on the same lines."));
	}

	// ZA:
	private static final int MIN_DISTANCE = 5;
	private record ScanResult(BlockPos pos, boolean tooClose){}

	private ScanResult scanDirectionFull(int fromX, int fromY, int fromZ, int dx, int dz){
		if(level == null) return null;
		for(int d = 1; d <= MAX_SEARCH; d++){
			BlockPos pos = new BlockPos(fromX + dx*d, fromY, fromZ + dz*d);
			if(level.getBlockState(pos).is(DifModBlocks.QUARRY_LANDMARK.get())){
				if(level.getBlockEntity(pos) instanceof QuarryLandmarkBlockEntity lm && lm.formed) return null;
				if(d < MIN_DISTANCE) return new ScanResult(pos, true);
				return new ScanResult(pos, false);
			}
		}
		return null;
	}

	private BlockPos scanDirectionFrom(BlockPos from, int dx, int dz){
		ScanResult sr = scanDirectionFull(from.getX(), from.getY(), from.getZ(), dx, dz);
		return (sr == null || sr.tooClose()) ? null : sr.pos();
	}
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
		if(isLShape(lmA, lmB, lmC) && isLShape(lmB, lmA, lmC) && isLShape(lmC, lmA, lmB)) return null;
		int halfX=spanX/2;
		int halfZ=spanZ/2;
		int centerX=minX+halfX;
		int centerZ=minZ+halfZ;
		return new FormResult(spanX+1,spanZ+1,halfX,halfZ,new BlockPos(centerX,lmA.getY(),centerZ));
	}
	private static boolean isLShape(BlockPos corner,BlockPos pos1,BlockPos pos2){
		return (corner.getX() == pos1.getX() && corner.getZ() == pos2.getZ()) || (corner.getX() == pos2.getX() && corner.getZ() == pos1.getZ());
	}
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
	public void handleUpdateTag(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider lookupProvider){
		loadAdditional(tag,lookupProvider);
		updateClientRenderer();
	}
	@Override
	public void onDataPacket(@NotNull Connection connection,ClientboundBlockEntityDataPacket packet,@NotNull HolderLookup.Provider lookupProvider){
		CompoundTag tag=packet.getTag();
		loadAdditional(tag,lookupProvider);
		updateClientRenderer();
	}
	private void updateClientRenderer(){
		if(level==null||!level.isClientSide) return;
		if(formed) LandmarkOverlayRenderer.register(this);
		else LandmarkOverlayRenderer.unregister(worldPosition);
	}
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
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		tag.putBoolean("Formed",formed);
		tag.putInt("FHX",formedHalfX);
		tag.putInt("FHZ",formedHalfZ);
		if(formedCenter!=null) tag.put("FC",NbtUtils.writeBlockPos(formedCenter));
		ListTag partnerList=new ListTag();
		for(BlockPos partnerPos: partnerPositions) partnerList.add(NbtUtils.writeBlockPos(partnerPos));
		tag.put("Partners",partnerList);
	}
	@Override
	public void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		formed=tag.getBoolean("Formed");
		formedHalfX=tag.getInt("FHX");
		formedHalfZ=tag.getInt("FHZ");
		formedCenter=tag.contains("FC")?NbtUtils.readBlockPos(tag,"FC").orElse(null):null;
		partnerPositions.clear();
		ListTag partnerList=tag.getList("Partners",Tag.TAG_COMPOUND);
		for(int i=0;i<partnerList.size();i++){
			CompoundTag entry=partnerList.getCompound(i);
			NbtUtils.readBlockPos(entry,"Pos").ifPresent(partnerPositions::add);
		}
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider provider){
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