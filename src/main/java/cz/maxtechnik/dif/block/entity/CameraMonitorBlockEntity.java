package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.CameraMonitor;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.events.client.ClientCameraHandler;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.CameraMonitorState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
public class CameraMonitorBlockEntity extends BlockEntity{
	private BlockPos linkedCameraPos=null;
	public CameraMonitorBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.CAMERA_MONITOR.get(),pos,blockState);
	}
	public void linkCamera(BlockPos camPos){
		this.linkedCameraPos=camPos;
		if(level!=null) level.setBlock(worldPosition,getBlockState().setValue(CameraMonitor.STATE,CameraMonitorState.INACTIVE),3);
		setChanged();
	}
	public InteractionResult useMonitor(Player player){
		if(level==null) return InteractionResult.PASS;
		if(!level.isClientSide&&player instanceof ServerPlayer){
			if(linkedCameraPos==null) return InteractionResult.FAIL;
			ServerLevel serverLevel=(ServerLevel)level;
			ChunkPos chunkPos=new ChunkPos(linkedCameraPos);
			serverLevel.getChunkSource().addRegionTicket(TicketType.FORCED,chunkPos,3,chunkPos);
			if(level.getBlockState(linkedCameraPos).getBlock().equals(DifModBlocks.CAMERA.get())) level.setBlock(worldPosition,getBlockState().setValue(CameraMonitor.STATE,CameraMonitorState.ACTIVE),3);
			else player.displayClientMessage(Component.literal("Camera is not available!"),true);
		}
		if(level.isClientSide){
			if(linkedCameraPos!=null) ClientCameraHandler.enterCamera(linkedCameraPos,this.getBlockPos());
			else player.displayClientMessage(Component.literal("No camera connected!"),true);
		}
		return InteractionResult.SUCCESS;
	}
	public void setInactive(){
		if(level!=null){
			level.setBlock(worldPosition,getBlockState().setValue(CameraMonitor.STATE,CameraMonitorState.INACTIVE),3);
			if(!level.isClientSide&&level instanceof ServerLevel serverLevel&&linkedCameraPos!=null){
				ChunkPos chunkPos=new ChunkPos(linkedCameraPos);
				serverLevel.getChunkSource().removeRegionTicket(TicketType.FORCED,chunkPos,3,chunkPos);
			}
		}
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider registries){
		super.loadAdditional(tag,registries);
		if(tag.contains("CamX")){
			this.linkedCameraPos=new BlockPos(tag.getInt("CamX"),tag.getInt("CamY"),tag.getInt("CamZ"));
		}
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider registries){
		super.saveAdditional(tag,registries);
		if(linkedCameraPos!=null){
			tag.putInt("CamX",linkedCameraPos.getX());
			tag.putInt("CamY",linkedCameraPos.getY());
			tag.putInt("CamZ",linkedCameraPos.getZ());
		}
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries){
		CompoundTag tag=new CompoundTag();
		saveAdditional(tag,registries);
		return tag;
	}
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public void onDataPacket(@NotNull Connection net,ClientboundBlockEntityDataPacket pkt,@NotNull HolderLookup.Provider registries){
		CompoundTag tag=pkt.getTag();
		this.loadAdditional(tag,registries);
	}
}