package cz.maxtechnik.dif.block;

import com.mojang.serialization.MapCodec;
import cz.maxtechnik.dif.block.entity.CameraMonitorBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.CameraMonitorState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class CameraMonitor extends BaseEntityBlock{
	// Definice Codecu pro 1.21.1
	public static final MapCodec<CameraMonitor> CODEC=simpleCodec(CameraMonitor::new);
	public static final EnumProperty<CameraMonitorState> STATE=EnumProperty.create("state",CameraMonitorState.class);
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	@Override
	protected @NotNull MapCodec<? extends BaseEntityBlock> codec(){
		return CODEC;
	}
	public CameraMonitor(Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(STATE,CameraMonitorState.NO_SIGNAL).setValue(FACING,Direction.NORTH));
	}
	@Override
	protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state,Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		if(level.getBlockEntity(pos) instanceof CameraMonitorBlockEntity monitor){
			// Kontrola itemu v ruce
			if(player.getMainHandItem().is(DifModItems.CAMERA_LINK.get())) return InteractionResult.PASS;
			return monitor.useMonitor(player);
		}
		return InteractionResult.PASS;
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState blockState){
		return RenderShape.MODEL;
	}
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(STATE,FACING);
	}
	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return this.defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());
	}
	@Override
	public @NotNull BlockState rotate(BlockState state,Rotation rot){
		return state.setValue(FACING,rot.rotate(state.getValue(FACING)));
	}
	@Override
	public @NotNull BlockState mirror(BlockState state,Mirror mirrorIn){
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new CameraMonitorBlockEntity(pos,state);
	}
	@Override
	protected void onRemove(BlockState state,@NotNull Level level,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(!state.is(newState.getBlock())){
			super.onRemove(state,level,pos,newState,isMoving);
		}
	}
	@Override
	protected void tick(@NotNull BlockState blockstate,@NotNull ServerLevel world,@NotNull BlockPos pos,@NotNull RandomSource random){
		super.tick(blockstate,world,pos,random);
	}
	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,@NotNull BlockState state,@NotNull BlockEntityType<T> type){
		if(level.isClientSide) return null;
		return createTickerHelper(type,DifModBlockEntities.CAMERA_MONITOR.get(),(lvl,pos,st,be)->{
			if(st.getValue(STATE)==CameraMonitorState.ACTIVE){
				if(lvl.getGameTime()%20==0){
					if(lvl.getNearestPlayer(pos.getX(),pos.getY(),pos.getZ(),8,false)==null){
						be.setInactive();
					}
				}
			}
		});
	}
}