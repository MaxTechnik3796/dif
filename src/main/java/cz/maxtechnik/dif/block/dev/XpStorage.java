package cz.maxtechnik.dif.block.dev;

import cz.maxtechnik.dif.block.entity.dev.XpStorageBlockEntity;
import net.minecraft.core.BlockPos;
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
	public @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockstate,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		if(world.isClientSide()) return InteractionResult.SUCCESS;
		if(!(world.getBlockEntity(pos) instanceof XpStorageBlockEntity blockEntity)) return InteractionResult.SUCCESS;
		if(player.isShiftKeyDown()){
			// Vytahování XP z tanku — přesné na bod
			if(blockEntity.xp>0){
				player.giveExperiencePoints(blockEntity.xp);
				blockEntity.xp=0;
				blockEntity.setChanged();
				world.sendBlockUpdated(pos,blockstate,blockstate,3);
			}
		}else{
			// Vkládání XP do tanku — přesné na bod
			int playerXp=getActualPlayerXP(player);
			if(playerXp>0){
				blockEntity.xp+=playerXp;
				setExactPlayerXP(player,0);
				blockEntity.setChanged();
				world.sendBlockUpdated(pos,blockstate,blockstate,3);
			}
		}
		return InteractionResult.SUCCESS;
	}
	// Přesný výpočet celkového XP hráče
	private static int getActualPlayerXP(Player player){
		int levelXp=totalXpForLevel(player.experienceLevel);
		int progressXp=Math.round(player.experienceProgress*xpBarCap(player.experienceLevel));
		return levelXp+progressXp;
	}
	// Přesné nastavení XP hráče bez zaokrouhlovacích chyb
	private static void setExactPlayerXP(Player player,int xp){
		player.experienceLevel=0;
		player.experienceProgress=0;
		player.totalExperience=0;
		if(xp>0) player.giveExperiencePoints(xp);
	}
	private static int totalXpForLevel(int level){
		if(level<=0) return 0;
		if(level<=16) return level*level+6*level;
		if(level<=31) return (int)(2.5*level*level-40.5*level+360);
		return (int)(4.5*level*level-162.5*level+2220);
	}
	private static int xpBarCap(int level){
		if(level>=30) return 112+(level-30)*9;
		if(level>=15) return 37+(level-15)*5;
		return 7+level*2;
	}
}