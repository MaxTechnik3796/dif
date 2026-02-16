package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.SpaceShipBE;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
@SuppressWarnings("deprecation")
public class Spaceship extends Block implements EntityBlock{
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public Spaceship(){
		super(Properties.of().strength(5F,6F).sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops().noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH));
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
			level.setBlock(finalMasterPos,blockState,3);
			for(BlockPos ghostPos: getGhostPositions(finalMasterPos)){
				level.setBlock(ghostPos,DifModBlocks.SPACESHIP_GHOST_BLOCK.get().defaultBlockState(),3);
			}
		}
	}
	@Override
	public void onRemove(BlockState state,@NotNull Level world,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(!state.is(newState.getBlock())&&!world.isClientSide()){
			for(BlockPos ghostPos: getGhostPositions(pos)){
				if(world.getBlockState(ghostPos).is(DifModBlocks.SPACESHIP_GHOST_BLOCK.get())){
					world.setBlock(ghostPos,Blocks.AIR.defaultBlockState(),3);
				}
			}
			if(world.getBlockEntity(pos) instanceof SpaceShipBE be) be.drops();
			super.onRemove(state,world,pos,newState,isMoving);
		}
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState blockState,Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		if(!level.isClientSide()&&level.getBlockEntity(pos) instanceof SpaceShipBE be){
			NetworkHooks.openScreen(((ServerPlayer)player),be,pos);
		}
		return InteractionResult.sidedSuccess(level.isClientSide());
	}
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new SpaceShipBE(pos,blockState);
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING);
	}
}