package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.gui.menu.SpecialCraftingMenu;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
/**
 Out of service (Unfinished)
 **/
public class SpecialCrafting extends Block implements EntityBlock{
	private static final Component CONTAINER_TITLE=Component.literal("Special Crafting");
	public SpecialCrafting(){
		super(Properties.of());
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState blockstate,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player entity,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		super.use(blockstate,world,pos,entity,hand,hit);
		if(entity instanceof ServerPlayer player){
			NetworkHooks.openScreen(player,new MenuProvider(){
				@Override
				public @NotNull Component getDisplayName(){
					return CONTAINER_TITLE;
				}
				@Override
				public AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory,@NotNull Player player){
					return new SpecialCraftingMenu(id,inventory,new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos));
				}
			},pos);
		}
		return InteractionResult.SUCCESS;
	}
	@Override
	public MenuProvider getMenuProvider(@NotNull BlockState state,Level worldIn,@NotNull BlockPos pos){
		BlockEntity tileEntity=worldIn.getBlockEntity(pos);
		return tileEntity instanceof MenuProvider menuProvider?menuProvider:null;
	}
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new cz.maxtechnik.dif.block.entity.SpecialCrafting(pos,state);
	}
	@Override
	public void onRemove(BlockState state,@NotNull Level world,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(state.getBlock()!=newState.getBlock()){
			BlockEntity blockEntity=world.getBlockEntity(pos);
			if(blockEntity instanceof cz.maxtechnik.dif.block.entity.SpecialCrafting be){
				Containers.dropContents(world,pos,be);
				world.updateNeighbourForOutputSignal(pos,this);
			}
			super.onRemove(state,world,pos,newState,isMoving);
		}
	}
	@Override
	public boolean triggerEvent(@NotNull BlockState state,@NotNull Level world,@NotNull BlockPos pos,int eventID,int eventParam){
		super.triggerEvent(state,world,pos,eventID,eventParam);
		BlockEntity blockEntity=world.getBlockEntity(pos);
		return blockEntity!=null&&blockEntity.triggerEvent(eventID,eventParam);
	}
}
