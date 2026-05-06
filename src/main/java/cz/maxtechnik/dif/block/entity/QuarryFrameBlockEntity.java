package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
public class QuarryFrameBlockEntity extends BlockEntity{
	private BlockPos ownerQuarryPos=null;
	private boolean scheduledForRemoval=false;
	public QuarryFrameBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.QUARRY_FRAME.get(),pos,state);
	}
	public static void tick(Level level,BlockPos pos,QuarryFrameBlockEntity frameEntity){
		if(!level.isClientSide&&frameEntity.scheduledForRemoval) level.removeBlock(pos,false);
	}
	// Nastaví vlastníka – lze nastavit pouze jednou (ochrana před přepsáním cizí quarry)
	public void setOwner(BlockPos quarryPos){
		if(ownerQuarryPos!=null) return;
		ownerQuarryPos=quarryPos;
		setChanged();
	}
	// Označí frame pro odstranění v příštím ticku
	public void scheduleRemoval(){
		scheduledForRemoval=true;
		ownerQuarryPos=null;
		setChanged();
	}
	public BlockPos getOwnerPos(){
		return ownerQuarryPos;
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag, provider);
		if(ownerQuarryPos!=null){
			tag.putInt("OwnX",ownerQuarryPos.getX());
			tag.putInt("OwnY",ownerQuarryPos.getY());
			tag.putInt("OwnZ",ownerQuarryPos.getZ());
		}
		tag.putBoolean("Dying",scheduledForRemoval);
	}
	@Override
	public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag, provider);
		if(tag.contains("OwnX")) ownerQuarryPos=new BlockPos(tag.getInt("OwnX"),tag.getInt("OwnY"),tag.getInt("OwnZ"));
		scheduledForRemoval=tag.getBoolean("Dying");
	}
}