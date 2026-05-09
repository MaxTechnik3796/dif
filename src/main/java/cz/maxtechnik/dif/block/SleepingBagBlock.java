package cz.maxtechnik.dif.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
public class SleepingBagBlock extends BedBlock{
	protected static final VoxelShape SHAPE=Block.box(0D,0D,0D,16D,2D,16D);
	public SleepingBagBlock(){
		super(DyeColor.WHITE,BlockBehaviour.Properties.of().sound(SoundType.WOOL).strength(0.2F).pushReaction(PushReaction.BLOCK).noOcclusion());
	}
	@Override
	protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state,Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		if(level.isClientSide){
			return InteractionResult.CONSUME;
		}
		// Pokud klikneme na nohy spacáku, najdeme hlavu
		if(state.getValue(PART)!=BedPart.HEAD){
			pos=pos.relative(state.getValue(FACING));
			state=level.getBlockState(pos);
			if(!state.is(this)){
				return InteractionResult.FAIL;
			}
		}
		// Kontrola, zda lze v této dimenzi spát (výbuch v Netheru)
		if(!BedBlock.canSetSpawn(level)){
			level.explode(null,(double)pos.getX()+0.5D,(double)pos.getY()+0.5D,(double)pos.getZ()+0.5D,5.0F,true,Level.ExplosionInteraction.BLOCK);
			return InteractionResult.SUCCESS;
		}
		// Uložení hráče ke spánku
		player.startSleepInBed(pos).ifLeft((problem)->{
			if(problem!=null&&problem.getMessage()!=null){
				player.displayClientMessage(problem.getMessage(),true);
			}
		});
		return InteractionResult.SUCCESS;
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState state){
		return RenderShape.MODEL;
	}
	@Override
	public @NotNull VoxelShape getShape(@NotNull BlockState state,@NotNull BlockGetter level,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return SHAPE;
	}
	@Override
	public @NotNull VoxelShape getCollisionShape(@NotNull BlockState state,@NotNull BlockGetter level,@NotNull BlockPos pos,@NotNull CollisionContext context){
		return SHAPE;
	}
}