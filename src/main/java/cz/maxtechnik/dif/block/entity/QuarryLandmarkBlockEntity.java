package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.renderer.QuarryRenderer;
import cz.maxtechnik.dif.util.quarry.QuarryArea;
import cz.maxtechnik.dif.util.quarry.QuarryAreaManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
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
	public static final int MAX_SEARCH=QuarryAreaManager.DEFAULT_RANGE*25; // Odpovídá zhruba 125, v originále QuarryBlockEntity.MAX_AREA_SIDE
	public static final int MIN_SPAN=4; // min 5x5 oblast = span >= 4
	private final List<BlockPos> partnerPositions=new ArrayList<>(2);
	private boolean formed=false;
	@Nullable
	private QuarryArea formedArea=null;
	public QuarryLandmarkBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.QUARRY_LANDMARK.get(),pos,blockState);
	}
	// ── Klik na landmark ────────────────────────────────────────────────
	public void onRightClick(Player player){
		if(level==null||level.isClientSide||formed) return;
		// Scan 4 směry, najdi sousední landmarky
		List<BlockPos> nearby=new ArrayList<>();
		for(int[] dir: new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){
			BlockPos found=scanDirection(worldPosition,dir[0],dir[1]);
			if(found!=null) nearby.add(found);
		}
		// 2+ nalezeny → zkus každou dvojici se mnou
		if(nearby.size()>=2){
			for(int a=0;a<nearby.size();a++){
				for(int b=a+1;b<nearby.size();b++){
					QuarryArea area=tryForm(worldPosition,nearby.get(a),nearby.get(b));
					if(area!=null){
						applyFormation(List.of(worldPosition,nearby.get(a),nearby.get(b)),area,player);
						return;
					}
				}
			}
		}
		// 1 nalezen → z něj hledej 3. kolmo
		for(BlockPos first: nearby){
			boolean onX=first.getZ()==worldPosition.getZ();
			int[][] perps=onX?new int[][]{{0,-1},{0,1}}:new int[][]{{-1,0},{1,0}};
			for(int[] p: perps){
				BlockPos third=scanDirection(first,p[0],p[1]);
				if(third!=null&&!third.equals(worldPosition)){
					QuarryArea area=tryForm(worldPosition,first,third);
					if(area!=null){
						applyFormation(List.of(worldPosition,first,third),area,player);
						return;
					}
				}
			}
		}
		player.sendSystemMessage(Component.literal("§cNo valid area found. Place 3 landmarks in an L-shape."));
	}
	// ── Scan jedním směrem ──────────────────────────────────────────────
	@Nullable
	private BlockPos scanDirection(BlockPos from,int dx,int dz){
		if(level==null) return null;
		for(int d=1;d<=MAX_SEARCH;d++){
			BlockPos p=new BlockPos(from.getX()+dx*d,from.getY(),from.getZ()+dz*d);
			if(!level.getBlockState(p).is(DifModBlocks.QUARRY_LANDMARK.get())) continue;
			if(level.getBlockEntity(p) instanceof QuarryLandmarkBlockEntity lm&&lm.formed) return null;
			return d>=MIN_SPAN?p:null; // příliš blízko = null
		}
		return null;
	}
	// ── Zkus vytvořit oblast ze 3 bodů ──────────────────────────────────
	@Nullable
	public static QuarryArea tryForm(BlockPos a,BlockPos b,BlockPos c){
		// Musí tvořit L-tvar: přesně jeden bod musí sdílet X s jedním a Z s druhým
		if(isLCorner(a,b,c)&&isLCorner(b,a,c)&&isLCorner(c,a,b)) return null;
		int minX=Math.min(a.getX(),Math.min(b.getX(),c.getX()));
		int maxX=Math.max(a.getX(),Math.max(b.getX(),c.getX()));
		int minZ=Math.min(a.getZ(),Math.min(b.getZ(),c.getZ()));
		int maxZ=Math.max(a.getZ(),Math.max(b.getZ(),c.getZ()));
		int spanX=maxX-minX, spanZ=maxZ-minZ;
		if(spanX<MIN_SPAN||spanZ<MIN_SPAN) return null;
		if(spanX>MAX_SEARCH||spanZ>MAX_SEARCH) return null;
		return new QuarryArea(minX,maxX,minZ,maxZ);
	}
	private static boolean isLCorner(BlockPos corner,BlockPos p1,BlockPos p2){
		return (corner.getX()!=p1.getX()||corner.getZ()!=p2.getZ())
				&&(corner.getX()!=p2.getX()||corner.getZ()!=p1.getZ());
	}
	// ── Aplikuj formaci ─────────────────────────────────────────────────
	private void applyFormation(List<BlockPos> landmarks,QuarryArea area,Player player){
		if(level==null) return;
		for(BlockPos lmPos: landmarks){
			if(!(level.getBlockEntity(lmPos) instanceof QuarryLandmarkBlockEntity lm)) continue;
			lm.partnerPositions.clear();
			for(BlockPos other: landmarks) if(!other.equals(lmPos)) lm.partnerPositions.add(other);
			lm.formed=true;
			lm.formedArea=area;
			lm.setChanged();
			level.sendBlockUpdated(lmPos,lm.getBlockState(),lm.getBlockState(),3);
		}
		player.sendSystemMessage(Component.literal("§aArea marked: §f"+area.sizeX()+"§ax§f"+area.sizeZ()+" §ablocks. Place Quarry at the edge."));
	}
	// ── Předat oblast quarry ────────────────────────────────────────────
	public void applyToQuarry(Level level,BlockPos quarryPos){
		if(!formed||formedArea==null) return;
		if(!(level.getBlockEntity(quarryPos) instanceof QuarryBlockEntity qe)) return;
		qe.setArea(formedArea.minX(),formedArea.maxX(),formedArea.minZ(),formedArea.maxZ());
		List<BlockPos> all=new ArrayList<>(partnerPositions);
		all.add(worldPosition);
		for(BlockPos lmPos: all){
			if(level.getBlockState(lmPos).is(DifModBlocks.QUARRY_LANDMARK.get())){
				Block.popResource(level,lmPos,new ItemStack(DifModBlocks.QUARRY_LANDMARK.get().asItem()));
				level.removeBlock(lmPos,false);
			}
		}
	}
	// ── Odstranění landmarku ────────────────────────────────────────────
	public void onRemoved(){
		if(level==null||level.isClientSide||!formed) return;
		for(BlockPos pp: new ArrayList<>(partnerPositions)){
			if(!(level.getBlockEntity(pp) instanceof QuarryLandmarkBlockEntity pe)) continue;
			pe.formed=false;
			pe.formedArea=null;
			pe.partnerPositions.clear();
			pe.setChanged();
			level.sendBlockUpdated(pp,pe.getBlockState(),pe.getBlockState(),3);
		}
	}
	// ── Client rendering ────────────────────────────────────────────────
	@Override
	public void onLoad(){
		super.onLoad();
		if(level!=null&&level.isClientSide) updateClientRenderer();
	}
	@Override
	public void setRemoved(){
		super.setRemoved();
		if(level!=null&&level.isClientSide) QuarryRenderer.unregister(worldPosition);
	}
	@Override
	public void handleUpdateTag(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider p){
		loadAdditional(tag,p);
		updateClientRenderer();
	}
	@Override
	public void onDataPacket(@NotNull net.minecraft.network.Connection c,ClientboundBlockEntityDataPacket pkt,@NotNull HolderLookup.Provider p){
		loadAdditional(pkt.getTag(),p);
		updateClientRenderer();
	}
	private void updateClientRenderer(){
		if(level==null||!level.isClientSide) return;
		if(formed) QuarryRenderer.register(this);
		else QuarryRenderer.unregister(worldPosition);
	}
	// ── Gettery ─────────────────────────────────────────────────────────
	public boolean isFormed(){
		return formed;
	}
	@Nullable
	public QuarryArea getFormedArea(){
		return formedArea;
	}
	/** Zpětná kompatibilita pro renderer */
	public int getFormedHalfX(){
		return formedArea!=null?(formedArea.maxX()-formedArea.minX())/2:0;
	}
	public int getFormedHalfZ(){
		return formedArea!=null?(formedArea.maxZ()-formedArea.minZ())/2:0;
	}
	@Nullable
	public BlockPos getFormedCenter(){
		if(formedArea==null) return null;
		return new BlockPos((formedArea.minX()+formedArea.maxX())/2,worldPosition.getY(),(formedArea.minZ()+formedArea.maxZ())/2);
	}
	// ── NBT ─────────────────────────────────────────────────────────────
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider p){
		super.saveAdditional(tag,p);
		tag.putBoolean("Formed",formed);
		if(formedArea!=null){
			formedArea.save(tag);
		}
		ListTag pl=new ListTag();
		for(BlockPos pp: partnerPositions) pl.add(NbtUtils.writeBlockPos(pp));
		tag.put("Partners",pl);
	}
	@Override
	public void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider p){
		super.loadAdditional(tag,p);
		formed=tag.getBoolean("Formed");
		QuarryArea area=QuarryArea.load(tag);
		if(area==null){
			area=QuarryArea.loadLegacyHalf(tag,"FHX","FHZ","FC","FC");
			// Původní načítání center: var center=tag.contains("FC")?NbtUtils.readBlockPos(tag,"FC").orElse(null):null;
			// Pokud to bylo jinak, můžeme jen ponechat původní kompatibilitu nebo předpokládat fallback
		}
		// Vylepšený fallback pro starý formát (pokud byl BlockPos center uložen jako FC tag)
		if(area==null&&tag.contains("FHX")){
			int hx=tag.getInt("FHX"), hz=tag.getInt("FHZ");
			var center=tag.contains("FC")?NbtUtils.readBlockPos(tag,"FC").orElse(null):null;
			if(center!=null){
				area=new QuarryArea(center.getX()-hx,center.getX()+hx,center.getZ()-hz,center.getZ()+hz);
			}
		}
		this.formedArea=area;
		partnerPositions.clear();
		ListTag pl=tag.getList("Partners",Tag.TAG_COMPOUND);
		for(int i=0;i<pl.size();i++){
			NbtUtils.readBlockPos(pl.getCompound(i),"Pos").ifPresent(partnerPositions::add);
		}
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider p){
		CompoundTag tag=new CompoundTag();
		tag.putBoolean("Formed",formed);
		if(formedArea!=null){
			formedArea.save(tag);
		}
		ListTag pl=new ListTag();
		for(BlockPos pp: partnerPositions) pl.add(NbtUtils.writeBlockPos(pp));
		tag.put("Partners",pl);
		return tag;
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
}