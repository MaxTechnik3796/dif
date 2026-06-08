package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
public class ForgeGlassBlockEntity extends BlockEntity{
	@Nullable
	private BlockPos controllerPos;
	public ForgeGlassBlockEntity(BlockPos pos,BlockState state){
		super(DifModBlockEntities.FORGE_GLASS.get(),pos,state);
	}
	public @Nullable BlockPos getControllerPos(){
		return controllerPos;
	}
	public void setControllerPos(@Nullable BlockPos pos){
		if(!Objects.equals(pos,controllerPos)){
			this.controllerPos=pos;
			setChanged();
		}
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,HolderLookup.@NotNull Provider provider){
		super.saveAdditional(tag,provider);
		if(controllerPos!=null){
			tag.putInt("ctrl_x",controllerPos.getX());
			tag.putInt("ctrl_y",controllerPos.getY());
			tag.putInt("ctrl_z",controllerPos.getZ());
		}
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,HolderLookup.@NotNull Provider provider){
		super.loadAdditional(tag,provider);
		if(tag.contains("ctrl_x")){
			controllerPos=new BlockPos(
					tag.getInt("ctrl_x"),
					tag.getInt("ctrl_y"),
					tag.getInt("ctrl_z")
			);
		}else controllerPos=null;
	}
}
