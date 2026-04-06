package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.block.entity.MonitorBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.other.DifModBlockEntities;
import cz.maxtechnik.dif.util.CameraMonitorState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
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
@SuppressWarnings("deprecation")
public class CameraMonitor extends BaseEntityBlock{
	public static final EnumProperty<CameraMonitorState> STATE=EnumProperty.create("state",CameraMonitorState.class);
	public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
	public CameraMonitor(Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(STATE,CameraMonitorState.NO_SIGNAL).setValue(FACING,Direction.NORTH));
	}
	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state,Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull InteractionHand hand,@NotNull BlockHitResult hit){
		if(level.getBlockEntity(pos) instanceof MonitorBlockEntity monitor){
			if(player.getMainHandItem().getItem().equals(DifModItems.CAMERA_LINK.get())) return InteractionResult.PASS;
			return monitor.useMonitor(player);
		}
		return InteractionResult.PASS;
	}
	@Override
	public @NotNull RenderShape getRenderShape(@NotNull BlockState blockState) {
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
	public @NotNull BlockState rotate(BlockState state,Rotation rot){
		return state.setValue(FACING,rot.rotate(state.getValue(FACING)));
	}
	public @NotNull BlockState mirror(BlockState state,Mirror mirrorIn){
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new MonitorBlockEntity(pos,state);
	}
	@Override
	public void onRemove(BlockState state,@NotNull Level level,@NotNull BlockPos pos,BlockState newState,boolean isMoving){
		if(!state.is(newState.getBlock())){
			super.onRemove(state,level,pos,newState,isMoving);
		}
	}
	@Override
	public void tick(@NotNull BlockState blockstate,@NotNull ServerLevel world,@NotNull BlockPos pos,@NotNull RandomSource random){
		super.tick(blockstate,world,pos,random);
	}
	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
		// Ticker nás zajímá jen na serveru, klient si stav synchronizuje přes blockstates
		if (level.isClientSide) return null;

		return createTickerHelper(type, DifModBlockEntities.MONITOR.get(), (lvl, pos, st, be) -> {
			// Kontrolujeme jen pokud je monitor ACTIVE
			if (st.getValue(STATE) == CameraMonitorState.ACTIVE) {
				// Každých 20 ticků (1 sekunda) zkontrolujeme okolí
				if (lvl.getGameTime() % 20 == 0) {
					// Pokud v okruhu 8 bloků není žádný hráč, vypneme to
					if (lvl.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 8, false) == null) {
						be.setInactive();
					}
				}
			}
		});
	}
}