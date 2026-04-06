package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.CameraMonitor;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.events.client.ClientCameraHandler;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.CameraMonitorState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
public class MonitorBlockEntity extends BlockEntity{
	private BlockPos linkedCameraPos=null;
	public MonitorBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.MONITOR.get(),pos,state); // Tady doplň svůj BlockEntityType!
	}
	public void linkCamera(BlockPos camPos){
		this.linkedCameraPos=camPos;
		if(level!=null){
			level.setBlock(worldPosition,getBlockState().setValue(CameraMonitor.STATE,CameraMonitorState.INACTIVE),3);
		}
		setChanged();
	}
	public InteractionResult useMonitor(Player player) {
		if (level == null) return InteractionResult.PASS;

		if (!level.isClientSide && player instanceof ServerPlayer) {
			if (linkedCameraPos == null) return InteractionResult.FAIL;

			// VYNUCENÍ NAČTENÍ CHUNKU PRO HRÁČE
			// Tento příkaz řekne serveru, aby posílal data z okolí kamery tomuto hráči,
			// i když je jeho tělo fyzicky daleko.
			ServerLevel serverLevel = (ServerLevel) level;
			ChunkPos chunkPos = new ChunkPos(linkedCameraPos);
			serverLevel.getChunkSource().addRegionTicket(TicketType.FORCED, chunkPos, 3, chunkPos);

			// Nastavíme stav monitoru
			if(level.getBlockState(linkedCameraPos).getBlock().equals(DifModBlocks.CAMERA.get()))
				level.setBlock(worldPosition, getBlockState().setValue(CameraMonitor.STATE, CameraMonitorState.ACTIVE), 3);
		}

		if (level.isClientSide) {
			// Na klientovi už jen vstoupíme do handleru
			if (linkedCameraPos != null) {
				if(level.getBlockState(linkedCameraPos).getBlock().equals(DifModBlocks.CAMERA.get()))
					ClientCameraHandler.enterCamera(linkedCameraPos, this.getBlockPos());
				else player.displayClientMessage(Component.literal("Camera is not available!"),true);
			}
		}
		return InteractionResult.SUCCESS;
	}
	public void setInactive() {
		if (level != null) {
			// Změna stavu bloku
			level.setBlock(worldPosition, getBlockState().setValue(CameraMonitor.STATE, CameraMonitorState.INACTIVE), 3);

			// ODEBRÁNÍ TICKETU (pouze na serveru)
			if (!level.isClientSide && level instanceof ServerLevel serverLevel && linkedCameraPos != null) {
				ChunkPos chunkPos = new ChunkPos(linkedCameraPos);
				// Musí to být stejný typ a parametry jako při addRegionTicket
				serverLevel.getChunkSource().removeRegionTicket(TicketType.FORCED, chunkPos, 3, chunkPos);
			}
		}
	}
	@Override
	public void load(@NotNull CompoundTag tag){
		super.load(tag);
		if(tag.contains("CamX")){
			this.linkedCameraPos=new BlockPos(tag.getInt("CamX"),tag.getInt("CamY"),tag.getInt("CamZ"));
		}
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag){
		super.saveAdditional(tag);
		if(linkedCameraPos!=null){
			tag.putInt("CamX",linkedCameraPos.getX());
			tag.putInt("CamY",linkedCameraPos.getY());
			tag.putInt("CamZ",linkedCameraPos.getZ());
		}
	}
	@Override
	public @NotNull CompoundTag getUpdateTag(){
		CompoundTag tag=new CompoundTag();
		saveAdditional(tag);
		return tag;
	}
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
	@Override
	public void onDataPacket(net.minecraft.network.Connection net,net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt){
		assert pkt.getTag()!=null;
		this.load(pkt.getTag());
	}
}