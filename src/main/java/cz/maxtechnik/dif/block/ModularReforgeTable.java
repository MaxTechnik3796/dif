package cz.maxtechnik.dif.block;

import com.mojang.serialization.MapCodec;
import cz.maxtechnik.dif.block.entity.ModularReforgeTableBlockEntity;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModularReforgeTable extends BaseEntityBlock{
	public static final MapCodec<ModularReforgeTable> CODEC=simpleCodec(ModularReforgeTable::new);
	@Override
	protected @NotNull MapCodec<? extends BaseEntityBlock> codec(){
		return CODEC;
	}
	public ModularReforgeTable(BlockBehaviour.Properties properties){
		super(properties);
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
		return RenderShape.MODEL;
	}
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new ModularReforgeTableBlockEntity(pos,state);
	}
	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type){
		return level.isClientSide?null:createTickerHelper(type,DifModBlockEntities.MODULAR_REFORGE_TABLE.get(),(world,pos,blockState,be)->be.serverTick());
	}
	@Override
	protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack heldItem,@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		if(level.isClientSide) return ItemInteractionResult.sidedSuccess(true);
		BlockEntity be=level.getBlockEntity(pos);
		if(!(be instanceof ModularReforgeTableBlockEntity table)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if(!heldItem.isEmpty()&&table.tryInsertItem(heldItem,player,hand)) return ItemInteractionResult.sidedSuccess(level.isClientSide);
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
	@Override
	protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		if(level.isClientSide) return InteractionResult.SUCCESS;
		BlockEntity be=level.getBlockEntity(pos);
		if(!(be instanceof ModularReforgeTableBlockEntity table)) return InteractionResult.PASS;
		return table.tryExtractItem(player)?InteractionResult.SUCCESS:InteractionResult.PASS;
	}
	@Override
	protected void onRemove(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull BlockState newState,boolean movedByPiston){
		if(!state.is(newState.getBlock())){
			if(level.getBlockEntity(pos) instanceof ModularReforgeTableBlockEntity table)
				table.dropAllItems(level,pos);
		}
		super.onRemove(state,level,pos,newState,movedByPiston);
	}
}