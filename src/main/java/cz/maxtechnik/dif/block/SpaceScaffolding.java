package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.block.entity.SpaceScaffoldingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@SuppressWarnings("deprecation")
public class SpaceScaffolding extends Block implements EntityBlock{
	public SpaceScaffolding(){
		super(Properties.of().strength(0F,0F).sound(SoundType.COPPER).noOcclusion().pushReaction(PushReaction.DESTROY));
	}
	@Override
	public int getLightBlock(@NotNull BlockState state,@NotNull BlockGetter worldIn,@NotNull BlockPos pos){
		return 0;
	}
	@Override
	public boolean propagatesSkylightDown(BlockState state,@NotNull BlockGetter reader,@NotNull BlockPos pos){
		return state.getFluidState().isEmpty();
	}
	@Override
	public float getShadeBrightness(@NotNull BlockState blockState,@NotNull BlockGetter blockGetter,@NotNull BlockPos pos){
		return 1F;
	}
	@Override
	public @NotNull VoxelShape getVisualShape(@NotNull BlockState state,@NotNull BlockGetter world,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return Shapes.empty();
	}
	@Override
	public boolean skipRendering(@NotNull BlockState state,BlockState adjacentBlockState,@NotNull Direction side){
		return adjacentBlockState.getBlock()==this||super.skipRendering(state,adjacentBlockState,side);
	}
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new SpaceScaffoldingBlockEntity(pos,blockState);
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
	public void onPlace(@NotNull BlockState blockState,@NotNull Level world,@NotNull BlockPos pos,@NotNull BlockState oldState,boolean moving){
		super.onPlace(blockState,world,pos,oldState,moving);
		world.scheduleTick(pos,this,1);
	}
	@Override
	public void tick(@NotNull BlockState blockState,@NotNull ServerLevel world,@NotNull BlockPos pos,@NotNull RandomSource random){
		super.tick(blockState,world,pos,random);
		if(!world.isClientSide()){
			BlockEntity blockEntity=world.getBlockEntity(pos);
			if(blockEntity!=null){
				if(blockEntity instanceof SpaceScaffoldingBlockEntity scaffolding){
					if(scaffolding.lifeTime>=DifModCommonConfig.spaceScaffoldingLifeTime){
						world.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
					}else{
						scaffolding.lifeTime+=1;
					}
				}
			}
		}
		world.scheduleTick(pos,this,1);
	}
}
