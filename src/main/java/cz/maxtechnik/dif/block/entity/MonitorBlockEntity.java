package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.CameraBlock;
import cz.maxtechnik.dif.block.MonitorBlock;
import cz.maxtechnik.dif.client.ClientCameraHandler;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.MonitorState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
			level.setBlock(worldPosition,getBlockState().setValue(MonitorBlock.STATE,MonitorState.INACTIVE),3);
		}
		setChanged();
	}
	public InteractionResult useMonitor(Player player){
		if(level==null) return InteractionResult.PASS;
		// Na serveru zkontrolujeme, jestli máme link
		if(!level.isClientSide){
			if(linkedCameraPos==null){
				player.displayClientMessage(Component.literal("No camera linked!"),true);
				return InteractionResult.FAIL;
			}
			// Kontrola, jestli kamera stále existuje
			if(!(level.getBlockState(linkedCameraPos).getBlock() instanceof CameraBlock)){
				player.displayClientMessage(Component.literal("Link lost!"),true);
				return InteractionResult.FAIL;
			}
		}
		if(level.isClientSide){
			// Klient teď díky onDataPacket už ví, kde je linkedCameraPos
			if(linkedCameraPos!=null){
				if(level.getBlockState(linkedCameraPos).getBlock() instanceof CameraBlock)
					ClientCameraHandler.enterCamera(linkedCameraPos,this.getBlockPos());
			}
		}else{
			level.setBlock(worldPosition,getBlockState().setValue(MonitorBlock.STATE,MonitorState.ACTIVE),3);
		}
		return InteractionResult.SUCCESS;
	}
	public void setInactive(){
		if(level!=null){
			level.setBlock(worldPosition,getBlockState().setValue(MonitorBlock.STATE,MonitorState.INACTIVE),3);
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