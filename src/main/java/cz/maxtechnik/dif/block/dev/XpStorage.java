package cz.maxtechnik.dif.block.dev;

import cz.maxtechnik.dif.block.entity.dev.XpStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@SuppressWarnings("deprecation")
public class XpStorage extends Block implements EntityBlock{
	public XpStorage(){
		super(Properties.of().strength(2F,2F));
	}
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new XpStorageBlockEntity(pos,blockState);
	}
	@Override
	public boolean triggerEvent(@NotNull BlockState state,@NotNull Level world,@NotNull BlockPos pos,int eventID,int eventParam){
		super.triggerEvent(state,world,pos,eventID,eventParam);
		BlockEntity blockEntity=world.getBlockEntity(pos);
		return blockEntity!=null&&blockEntity.triggerEvent(eventID,eventParam);
	}
	@Override
	public void onRemove(BlockState state,@NotNull Level world,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(state.getBlock()!=newState.getBlock()){
			super.onRemove(state,world,pos,newState,isMoving);
		}
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState blockstate,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		super.use(blockstate,world,pos,player,hand,hit);
		if(world.isClientSide()) return InteractionResult.SUCCESS;
		if(!(world.getBlockEntity(pos) instanceof XpStorageBlockEntity blockEntity)) return InteractionResult.SUCCESS;
		if(player.isShiftKeyDown()){
			player.giveExperiencePoints(blockEntity.xp);
			blockEntity.xp=0;
			world.sendBlockUpdated(pos,blockstate,blockstate,3);
		}else{
			blockEntity.xp+=player.totalExperience;
			player.giveExperiencePoints(-player.totalExperience);
			world.sendBlockUpdated(pos,blockstate,blockstate,3);
		}
		return InteractionResult.SUCCESS;
	}
}
