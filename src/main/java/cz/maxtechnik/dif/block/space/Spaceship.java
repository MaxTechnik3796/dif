package cz.maxtechnik.dif.block.space;

import cz.maxtechnik.dif.block.entity.SpaceshipBlockEntity;
import cz.maxtechnik.dif.gui.menu.SpaceshipMenu;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
@SuppressWarnings("deprecation")
public class Spaceship extends Block implements EntityBlock{
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty MOVED=BooleanProperty.create("moved");
	public Spaceship(){
		super(Properties.of().strength(5F,6F).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().noOcclusion().pushReaction(PushReaction.BLOCK));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(MOVED,false));
	}
	public Set<BlockPos> getGhostPositions(BlockPos masterPos){
		Set<BlockPos> positions=new HashSet<>();
		// Spodní kříž (pod lodí)
		positions.add(masterPos.below());
		positions.add(masterPos.below().north());
		positions.add(masterPos.below().south());
		positions.add(masterPos.below().east());
		positions.add(masterPos.below().west());
		// Horní kříž (vedle lodi)
		positions.add(masterPos.north());
		positions.add(masterPos.south());
		positions.add(masterPos.east());
		positions.add(masterPos.west());
		return positions;
	}
	@Override
	public boolean canSurvive(@NotNull BlockState blockState,@NotNull LevelReader level,@NotNull BlockPos pos){
		if(pos.getY()==318) return false;
		BlockPos finalMasterPos=pos.above();
		boolean blocked=!level.getBlockState(finalMasterPos).canBeReplaced();
		for(BlockPos ghostPos: getGhostPositions(finalMasterPos)){
			if(ghostPos.equals(pos)) continue;
			if(!level.getBlockState(ghostPos).canBeReplaced()){
				blocked=true;
				break;
			}
		}
		return !blocked;
	}
	@Override
	public void setPlacedBy(Level level,@NotNull BlockPos pos,@NotNull BlockState blockState,@Nullable LivingEntity entity,@NotNull ItemStack itemStack){
		if(!level.isClientSide()){
			BlockPos finalMasterPos=pos.above();
			level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
			level.setBlock(finalMasterPos,blockState.setValue(MOVED,true),3);
			for(BlockPos ghostPos: getGhostPositions(finalMasterPos)){
				level.setBlock(ghostPos,DifModBlocks.SPACESHIP_GHOST_BLOCK.get().defaultBlockState(),3);
			}
		}
	}
	@Override
	public void onRemove(BlockState state,@NotNull Level world,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(!state.is(newState.getBlock())&&!world.isClientSide()&&state.getValue(MOVED)){
			for(BlockPos ghostPos: getGhostPositions(pos)){
				if(world.getBlockState(ghostPos).is(DifModBlocks.SPACESHIP_GHOST_BLOCK.get())){
					world.setBlock(ghostPos,Blocks.AIR.defaultBlockState(),3);
				}
			}
			if(world.getBlockEntity(pos) instanceof SpaceshipBlockEntity be) be.drops();
			super.onRemove(state,world,pos,newState,isMoving);
		}
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull Player entity,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		super.use(blockState,world,pos,entity,hand,hit);
		if(entity instanceof ServerPlayer player){
			NetworkHooks.openScreen(player,new MenuProvider(){
				@Override
				public @NotNull Component getDisplayName(){
					return Component.literal("Spaceship");
				}
				@Override
				public AbstractContainerMenu createMenu(int id,@NotNull Inventory inventory,@NotNull Player player){
					return new SpaceshipMenu(id,inventory,new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos));
				}
			},pos);
		}
		return InteractionResult.SUCCESS;
	}
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new SpaceshipBlockEntity(pos,blockState);
	}
	@Override
	public MenuProvider getMenuProvider(@NotNull BlockState state,Level worldIn,@NotNull BlockPos pos){
		BlockEntity tileEntity=worldIn.getBlockEntity(pos);
		return tileEntity instanceof MenuProvider menuProvider?menuProvider:null;
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,MOVED);
	}
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
	}
	public @NotNull BlockState rotate(BlockState state,Rotation rot){
		return state.setValue(FACING,rot.rotate(state.getValue(FACING)));
	}
	public @NotNull BlockState mirror(BlockState state,Mirror mirrorIn){
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}
}