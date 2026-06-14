package cz.maxtechnik.dif.block.entity;

import cz.maxtechnik.dif.block.CokeOvenController;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
public class CokeOvenBlockEntity extends AbstractMultiblockBrickBlockEntity{
	public CokeOvenBlockEntity(BlockPos pos,BlockState blockState){
		super(DifModBlockEntities.COKE_OVEN.get(),pos,blockState);
	}
	@Override
	protected @Nullable AbstractMultiblockControllerBlockEntity<?> resolveController(BlockPos pos){
		if(level==null) return null;
		var state=level.getBlockState(pos);
		if(state.hasProperty(CokeOvenController.FORMED)
				&&state.getValue(CokeOvenController.FORMED)
				&&level.getBlockEntity(pos) instanceof CokeOvenControllerBlockEntity ctrl){
			return ctrl;
		}
		return null;
	}
	@Override
	protected String getGoggleDisplayName(){
		return "◆ Coke Oven";
	}
	@Override
	protected ChatFormatting getGoggleNameColor(){
		return ChatFormatting.GOLD;
	}
}