package cz.maxtechnik.dif.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import cz.maxtechnik.dif.block.entity.ChunkLoaderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class ChunkLoader extends Block implements EntityBlock, IWrenchable{
	public static final BooleanProperty LIT=BlockStateProperties.LIT;
	public ChunkLoader(){
		super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5F).sound(SoundType.LODESTONE).requiresCorrectToolForDrops().pushReaction(PushReaction.BLOCK).lightLevel(s->s.getValue(LIT)?12:0));
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT,true));
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(LIT);
	}
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return new ChunkLoaderBlockEntity(pos,blockState);
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState blockState){
		return RenderShape.MODEL;
	}
	@Override
	public InteractionResult onWrenched(BlockState blockState,UseOnContext context){
		Level level=context.getLevel();
		BlockPos pos=context.getClickedPos();
		Player player=context.getPlayer();
		if(!level.isClientSide){
			if(level.getBlockEntity(pos) instanceof ChunkLoaderBlockEntity loader)
				loader.cycleRadius(player);
		}
		return InteractionResult.SUCCESS;
	}
	@Override
	public void animateTick(BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull RandomSource random){
		if(!blockState.getValue(LIT)) return;
		double centerX=pos.getX()+0.5;
		double centerY=pos.getY()+0.5;
		double centerZ=pos.getZ()+0.5;
		double time=(double)level.getGameTime()*0.2;
		int radiusIndex=0;
		if(level.getBlockEntity(pos) instanceof ChunkLoaderBlockEntity blockEntity)
			radiusIndex=blockEntity.getRadius();
		double radius=1.1+radiusIndex*0.25;
		for(int orbit=0;orbit<3;orbit++){
			double orbitOffset=(orbit*Math.PI*2)/3;
			for(int segment=0;segment<12;segment++){
				double sTime=time-(segment*0.08);
				double x, y, z;
				double v=Math.cos(sTime+orbitOffset)*radius;
				double v1=Math.sin(sTime+orbitOffset)*radius;
				double x1=centerX+v;
				if(orbit==0){
					x=x1;
					y=centerY+v1;
					z=centerZ;
				}else if(orbit==1){
					x=x1;
					y=centerY;
					z=centerZ+v1;
				}else{
					x=centerX;
					y=centerY+v;
					z=centerZ+v1;
				}
				if(segment==0)
					level.addParticle(ParticleTypes.END_ROD,x,y,z,0,0,0);
				else
					level.addParticle(ParticleTypes.PORTAL,x,y,z,0,0,0);
			}
		}
		if(random.nextFloat()<0.3F){
			level.addParticle(ParticleTypes.WITCH,
					pos.getX()+random.nextDouble(),
					pos.getY()+random.nextDouble(),
					pos.getZ()+random.nextDouble(),0,0,0);
		}
	}
	@Override
	public void onRemove(BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(!blockState.is(newState.getBlock())){
			if(level.getBlockEntity(pos) instanceof ChunkLoaderBlockEntity loader)
				loader.handleRemoval();
			super.onRemove(blockState,level,pos,newState,isMoving);
		}
	}
	@Override
	public void setPlacedBy(Level level,@NotNull BlockPos pos,@NotNull BlockState blockState,@Nullable LivingEntity placer,@NotNull ItemStack itemStack){
		if(!level.isClientSide&&placer instanceof Player player){
			if(level.getBlockEntity(pos) instanceof ChunkLoaderBlockEntity blockEntity){
				blockEntity.setOwner(player.getUUID(),player.getName().getString());
				blockEntity.updateStatus(!level.hasNeighborSignal(pos));
			}
		}
	}
	@Override
	public void neighborChanged(@NotNull BlockState blockState,Level level,@NotNull BlockPos pos,@NotNull Block block,@NotNull BlockPos fromPos,boolean isMoving){
		if(!level.isClientSide){
			boolean shouldBeLit=!level.hasNeighborSignal(pos);
			if(blockState.getValue(LIT)!=shouldBeLit){
				level.setBlock(pos,blockState.setValue(LIT,shouldBeLit),3);
				if(level.getBlockEntity(pos) instanceof ChunkLoaderBlockEntity blockEntity) blockEntity.updateStatus(shouldBeLit);
			}
		}
	}
}