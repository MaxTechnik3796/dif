package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.event.ChunkLoaderData;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
public class ChunkLoaderBlockEntity extends BlockEntity implements IHaveGoggleInformation{
	public static final int MAX_RADIUS=2; // 0 = 1x1, 1 = 3x3, 2 = 5x5
	private static final int CYCLE_COOLDOWN_TICKS=10;
	private int radius=0;
	private UUID ownerUUID;
	private String ownerName="Unknown";
	private boolean active=true;
	private long lastCycleTime=0;
	public ChunkLoaderBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.CHUNK_LOADER_BE.get(),pos,blockState);
	}
	public void setOwner(UUID uuid,String name){
		this.ownerUUID=uuid;
		this.ownerName=name;
		this.setChanged();
	}
	public int getLoadedChunksCount(){
		int side=2*radius+1;
		return side*side;
	}
	public String getRadiusLabel(){
		int side=2*radius+1;
		return side+"x"+side;
	}
	private void forceChunks(ServerLevel serverLevel,int r,boolean force){
		ChunkPos center=new ChunkPos(worldPosition);
		for(int dx=-r;dx<=r;dx++) for(int dz=-r;dz<=r;dz++) DifMod.CHUNK_LOADER_TICKETS.forceChunk(serverLevel,worldPosition,center.x+dx,center.z+dz,force,true);
	}
	public void cycleRadius(Player player){
		if(!(level instanceof ServerLevel serverLevel)) return;
		long gameTime=serverLevel.getGameTime();
		if(gameTime-lastCycleTime<CYCLE_COOLDOWN_TICKS) return;
		lastCycleTime=gameTime;
		forceChunks(serverLevel,this.radius,false);
		this.radius=(this.radius+1)%(MAX_RADIUS+1);
		if(this.active) forceChunks(serverLevel,this.radius,true);
		syncAndSave(serverLevel);
		serverLevel.playSound(null,worldPosition,SoundEvents.EXPERIENCE_ORB_PICKUP,SoundSource.BLOCKS,1F,0.8F+this.radius*0.3F);
		if(player!=null) player.displayClientMessage(Component.literal("§6§lChunk Loader: §b"+getRadiusLabel()+" §7("+getLoadedChunksCount()+" chunks)"),true);
	}
	public void updateStatus(boolean newActive){
		if(this.active==newActive) return;
		this.active=newActive;
		if(level instanceof ServerLevel serverLevel){
			forceChunks(serverLevel,this.radius,this.active);
			syncAndSave(serverLevel);
		}
	}
	public void forceInitialLoad(boolean initialActive){
		this.active=initialActive;
		if(level instanceof ServerLevel serverLevel){
			if(this.active) forceChunks(serverLevel,this.radius,true);
			syncAndSave(serverLevel);
		}
	}
	public void handleRemoval(){
		if(level instanceof ServerLevel serverLevel){
			forceChunks(serverLevel,this.radius,false);
			ChunkLoaderData data=ChunkLoaderData.get(serverLevel);
			ResourceLocation dimId=serverLevel.dimension().location();
			data.loaders.removeIf(loaderRecord->loaderRecord.pos().equals(this.worldPosition)&&loaderRecord.dimension().equals(dimId));
			data.setDirty();
		}
	}
	private void syncAndSave(ServerLevel serverLevel){
		ChunkLoaderData.get(serverLevel).updateRecord(worldPosition,ownerUUID,ownerName,active,this.radius,serverLevel.dimension().location());
		this.setChanged();
		serverLevel.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip,boolean isPlayerSneaking){
		tooltip.add(DifMod.fixedComponent(Component.literal("Chunk Loader").withStyle(ChatFormatting.GOLD)));
		tooltip.add(DifMod.fixedComponent(Component.literal("Status: ").withStyle(ChatFormatting.GRAY).append(active?Component.literal("Active").withStyle(ChatFormatting.GREEN):Component.literal("Disabled (Redstone)").withStyle(ChatFormatting.RED))));
		tooltip.add(DifMod.fixedComponent(DifMod.fixedComponent(Component.literal("Range: ").withStyle(ChatFormatting.GRAY).append(Component.literal(getRadiusLabel()+" ("+getLoadedChunksCount()+" chunks)").withStyle(ChatFormatting.AQUA)))));
		if(ownerName!=null&&!ownerName.isEmpty()) tooltip.add(DifMod.fixedComponent(Component.literal("Owner: ").withStyle(ChatFormatting.GRAY).append(Component.literal(ownerName).withStyle(ChatFormatting.WHITE))));
		return true;
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider registries){
		super.loadAdditional(tag,registries);
		if(tag.contains("radius")) radius=tag.getInt("radius");
		if(tag.contains("active")) active=tag.getBoolean("active");
		if(tag.hasUUID("ownerUUID")) ownerUUID=tag.getUUID("ownerUUID");
		if(tag.contains("ownerName")) ownerName=tag.getString("ownerName");
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider registries){
		super.saveAdditional(tag,registries);
		tag.putInt("radius",radius);
		tag.putBoolean("active",active);
		if(ownerUUID!=null) tag.putUUID("ownerUUID",ownerUUID);
		if(ownerName!=null) tag.putString("ownerName",ownerName);
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries){
		CompoundTag tag=new CompoundTag();
		tag.putInt("radius",radius);
		tag.putBoolean("active",active);
		if(ownerUUID!=null) tag.putUUID("ownerUUID",ownerUUID);
		if(ownerName!=null) tag.putString("ownerName",ownerName);
		return tag;
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
}