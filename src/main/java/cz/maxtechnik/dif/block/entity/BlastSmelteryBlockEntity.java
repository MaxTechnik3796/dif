package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.BlastSmelteryController;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
public class BlastSmelteryBlockEntity extends AbstractMultiblockBrickBlockEntity{
	public BlastSmelteryBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.BLAST_SMELTERY.get(),pos,blockState);
	}
	@Override
	protected @Nullable AbstractMultiblockControllerBlockEntity<?> resolveController(BlockPos pos){
		if(level==null) return null;
		var state=level.getBlockState(pos);
		if(state.hasProperty(BlastSmelteryController.FORMED)
				&&state.getValue(BlastSmelteryController.FORMED)
				&&level.getBlockEntity(pos) instanceof BlastSmelteryControllerBlockEntity ctrl){
			return ctrl;
		}
		return null;
	}
	@Override
	protected String getGoggleDisplayName(){
		return "◆ Blast Smeltery";
	}
	@Override
	protected ChatFormatting getGoggleNameColor(){
		return ChatFormatting.RED;
	}
}