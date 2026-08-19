package cz.maxtechnik.dif.block.entity;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.events.ChunkLoaderData;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
	private int radius=0;
	private UUID ownerUUID;
	private String ownerName="Unknown";
	private boolean active=true;

	public ChunkLoaderBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.CHUNK_LOADER_BE.get(),pos,blockState);
	}



	public void setOwner(UUID uuid,String name){
		this.ownerUUID=uuid;
		this.ownerName=name;
		this.setChanged();
	}

	public int getRadius(){
		return radius;
	}

	public int getLoadedChunksCount(){
		int side=2*radius+1;
		return side*side;
	}

	public String getRadiusText(){
		int side=2*radius+1;
		return side+"×"+side;
	}

	public void cycleRadius(Player player){
		if(level instanceof ServerLevel serverLevel){
			// 1. Zrušíme tickety starých chunků pro tuto konkrétní pozici
			forceChunksInRadius(serverLevel,this.radius,false);

			// 2. Cyklujeme radius: 0 (1x1) -> 1 (3x3) -> 2 (5x5) -> 0 (1x1)
			this.radius=(this.radius+1)%(MAX_RADIUS+1);

			// 3. Vytvoříme tickety pro nové chunky
			if(this.active){
				forceChunksInRadius(serverLevel,this.radius,true);
			}

			// 4. Aktualizujeme data a uložíme
			ChunkLoaderData.get(serverLevel).updateRecord(worldPosition,ownerUUID,ownerName,active,this.radius);
			this.setChanged();
			serverLevel.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);

			// 5. Zvuk a zpětná vazba pro hráče
			serverLevel.playSound(null,worldPosition,SoundEvents.EXPERIENCE_ORB_PICKUP,SoundSource.BLOCKS,1.0F,0.8F+this.radius*0.3F);
			if(player!=null){
				String chunksWord=this.radius==0?"chunk":(this.radius==1?"chunky":"chunků");
				player.displayClientMessage(
						Component.literal("§6§lChunk Loader: §b"+getRadiusText()+" §7("+getLoadedChunksCount()+" "+chunksWord+")"),
						true
				);
			}
		}
	}

	public void updateStatus(boolean newActive){
		this.active=newActive;
		if(level instanceof ServerLevel serverLevel){
			forceChunksInRadius(serverLevel,this.radius,this.active);
			ChunkLoaderData.get(serverLevel).updateRecord(worldPosition,ownerUUID,ownerName,this.active,this.radius);
			this.setChanged();
			serverLevel.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
		}
	}

	private void forceChunksInRadius(ServerLevel serverLevel,int r,boolean force){
		ChunkPos center=new ChunkPos(worldPosition);
		for(int x=-r;x<=r;x++){
			for(int z=-r;z<=r;z++){
				DifMod.CHUNK_LOADER_TICKETS.forceChunk(serverLevel,this.worldPosition,center.x+x,center.z+z,force,true);
			}
		}
	}

	public void handleRemoval(){
		if(level instanceof ServerLevel serverLevel){
			forceChunksInRadius(serverLevel,this.radius,false);
			ChunkLoaderData data=ChunkLoaderData.get(serverLevel);
			data.loaders.removeIf(r->r.pos().equals(this.worldPosition));
			data.setDirty();
		}
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip,boolean isPlayerSneaking){
		tooltip.add(Component.literal("     Chunk Loader").withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.literal("     Stav: ").withStyle(ChatFormatting.GRAY)
				.append(active?Component.literal("Aktivní").withStyle(ChatFormatting.GREEN):Component.literal("Vypnuto (Redstone)").withStyle(ChatFormatting.RED)));
		tooltip.add(Component.literal("     Rozsah: ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(getRadiusText()+" ("+getLoadedChunksCount()+" chunků)").withStyle(ChatFormatting.AQUA)));
		if(ownerName!=null&&!ownerName.isEmpty()){
			tooltip.add(Component.literal("     Majitel: ").withStyle(ChatFormatting.GRAY)
					.append(Component.literal(ownerName).withStyle(ChatFormatting.WHITE)));
		}
		return true;
	}

	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider registries){
		super.loadAdditional(tag,registries);
		if(tag.contains("radius")) this.radius=tag.getInt("radius");
		else if(tag.contains("is3x3")) this.radius=tag.getBoolean("is3x3")?1:0;
		if(tag.contains("active")) this.active=tag.getBoolean("active");
		if(tag.hasUUID("ownerUUID")) this.ownerUUID=tag.getUUID("ownerUUID");
		if(tag.contains("ownerName")) this.ownerName=tag.getString("ownerName");
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider registries){
		super.saveAdditional(tag,registries);
		tag.putInt("radius",this.radius);
		tag.putBoolean("active",this.active);
		if(this.ownerUUID!=null){
			tag.putUUID("ownerUUID",this.ownerUUID);
		}
		if(this.ownerName!=null){
			tag.putString("ownerName",this.ownerName);
		}
	}

	@Override
	public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries){
		CompoundTag tag=new CompoundTag();
		tag.putInt("radius",this.radius);
		tag.putBoolean("active",this.active);
		if(this.ownerUUID!=null) tag.putUUID("ownerUUID",this.ownerUUID);
		if(this.ownerName!=null) tag.putString("ownerName",this.ownerName);
		return tag;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket(){
		return ClientboundBlockEntityDataPacket.create(this);
	}
}