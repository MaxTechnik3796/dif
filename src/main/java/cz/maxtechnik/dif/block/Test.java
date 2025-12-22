package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.entity.TestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
public class Test extends Block implements EntityBlock{
	public Test(){
		super(Properties.of().strength(5F,6F));
	}
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new TestBlockEntity(pos,blockState);
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
	public void attack(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player player){
		super.attack(blockState,world,pos,player);
		if(world.isClientSide())return;
		if(player instanceof ServerPlayer serverPlayer) if(DifMod.playerGameModeIsCreativeCategory(serverPlayer))return;
		if(!(world.getBlockEntity(pos) instanceof TestBlockEntity blockEntity))return;
		player.displayClientMessage(Component.literal("Xp: "+blockEntity.getPersistentData().getInt("xp")),true);//Broken
	}

	@Override
	public @NotNull InteractionResult use(@NotNull BlockState blockstate,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		super.use(blockstate,world,pos,player,hand,hit);
		if(world.isClientSide())return InteractionResult.SUCCESS;
		if(!(world.getBlockEntity(pos) instanceof TestBlockEntity blockEntity))return InteractionResult.SUCCESS;
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
