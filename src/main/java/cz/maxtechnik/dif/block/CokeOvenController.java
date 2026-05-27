package cz.maxtechnik.dif.block;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
@SuppressWarnings("deprecation")
public class CokeOvenController extends Block implements EntityBlock, IWrenchable{
	public static BooleanProperty ACTIVE=BooleanProperty.create("active");
	public static BooleanProperty FORMED=BooleanProperty.create("formed");
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public CokeOvenController(Properties properties){
		super(properties.lightLevel((bs)->bs.getValue(ACTIVE)?12:0));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(ACTIVE,false).setValue(FORMED,false));
	}
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState blockState){
		return DifModBlockEntities.COKE_OVEN_CONTROLLER.get().create(pos,blockState);
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(FACING,ACTIVE,FORMED);
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
	@Override
	public InteractionResult onWrenched(BlockState blockState,UseOnContext ctx){
		Level level=ctx.getLevel();
		BlockPos pos=ctx.getClickedPos();
		Player player=ctx.getPlayer();
		if(level.isClientSide||blockState.getValue(FORMED))return InteractionResult.PASS;


		return InteractionResult.CONSUME;
	}
}