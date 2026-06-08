package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.ForgeFurnaceController;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class ForgeBrickBlockEntity extends AbstractMultiblockBrickBlockEntity{
	private int integrity=100;
	public ForgeBrickBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.FORGE_BRICK.get(),pos,blockState);
	}
	@Override
	protected @Nullable AbstractMultiblockControllerBlockEntity<?> resolveController(BlockPos pos){
		if(level==null) return null;
		var state=level.getBlockState(pos);
		if(state.hasProperty(ForgeFurnaceController.FORMED)&&state.getValue(ForgeFurnaceController.FORMED)&&level.getBlockEntity(pos) instanceof ForgeControllerBlockEntity ctrl)
			return ctrl;
		return null;
	}
	@Override
	protected String getGoggleDisplayName(){
		return "◆ Forge Furnace";
	}
	@Override
	protected ChatFormatting getGoggleNameColor(){
		return ChatFormatting.GOLD;
	}
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.saveAdditional(tag,provider);
		tag.putInt("integrity",integrity);
	}
	@Override
	protected void loadAdditional(@NotNull CompoundTag tag,@NotNull HolderLookup.Provider provider){
		super.loadAdditional(tag,provider);
		integrity=tag.contains("integrity")?tag.getInt("integrity"):100;
	}
}