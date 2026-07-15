package cz.maxtechnik.dif.block;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.entity.NanoGlassBlockEntity;
import cz.maxtechnik.dif.config.DifModCommonConfig;
import cz.maxtechnik.dif.init.basic.DifModSounds;
import cz.maxtechnik.dif.init.other.DifModBlockEntities; // uprav podle své registrace
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.GAME)
public class NanoGlass extends TransparentBlock implements EntityBlock{
	public static final BooleanProperty DARK=BooleanProperty.create("dark");
	public static final long COOLDOWN_TICKS=20L;
	private static final int[][] NEIGHBOR_OFFSETS=buildNeighborOffsets();
	private static int[][] buildNeighborOffsets(){
		List<int[]> offsets=new ArrayList<>(26);
		for(int dx=-1;dx<=1;dx++){
			for(int dy=-1;dy<=1;dy++){
				for(int dz=-1;dz<=1;dz++){
					if(dx==0&&dy==0&&dz==0) continue;
					offsets.add(new int[]{dx,dy,dz});
				}
			}
		}
		return offsets.toArray(new int[0][]);
	}
	// ---------------------------------------------------------------
	// Wave scheduling (level-scoped, in-memory, ring-based)
	// ---------------------------------------------------------------
	private static final Map<ServerLevel,LevelWaveState> WAVE_STATES=new WeakHashMap<>();
	/** Stav rozpracovaných vln pro jeden level. Fronta prstenců čekajících na aplikaci. */
	private static final class LevelWaveState{
		/** Fronta prstenců (ring = množina pozic se stejnou vlnovou "vzdáleností"). */
		private final ArrayDeque<Ring> ringQueue=new ArrayDeque<>();
		/** Pozice, které už jsou zařazené v některém frontovém prstenci (proti duplicitám mezi souběžnými vlnami). */
		private final LongOpenHashSet scheduledPositions=new LongOpenHashSet();
		private boolean isEmpty(){
			return ringQueue.isEmpty();
		}
	}
	private record Ring(List<BlockPos> positions,boolean dark){
	}
	/**
	 * Spustí novou vlnu z originPos. Provede kompletní BFS "objevení" (levné -
	 * jen čtení BlockState) a rozdělí objevené bloky do prstenců podle BFS
	 * vzdálenosti. Prstence se pak aplikují postupně, jeden za tick, přes tick().
	 */
	private static void startWave(ServerLevel level,BlockPos originPos,boolean dark){
		int maxSpread=DifModCommonConfig.NANO_GLASS_MAX_SPREAD.get();
		LevelWaveState waveState=WAVE_STATES.computeIfAbsent(level,l->new LevelWaveState());
		level.playSound(null,originPos,DifModSounds.NANO_GLASS.get(),SoundSource.BLOCKS,1F,1F);
		LongOpenHashSet visited=new LongOpenHashSet();
		List<BlockPos> currentRing=new ArrayList<>();
		currentRing.add(originPos);
		visited.add(originPos.asLong());
		int discovered=0;
		while(!currentRing.isEmpty()&&discovered<maxSpread){
			List<BlockPos> ringToSchedule=new ArrayList<>();
			List<BlockPos> nextRing=new ArrayList<>();
			for(BlockPos pos: currentRing){
				if(discovered>=maxSpread) break;
				BlockState state=level.getBlockState(pos);
				if(!(state.getBlock() instanceof NanoGlass)) continue;
				if(state.getValue(DARK).equals(dark)) continue;
				ringToSchedule.add(pos);
				discovered++;
				for(int[] offset: NEIGHBOR_OFFSETS){
					BlockPos neighborPos=pos.offset(offset[0],offset[1],offset[2]);
					long key=neighborPos.asLong();
					if(visited.contains(key)) continue;
					visited.add(key);
					BlockState neighborState=level.getBlockState(neighborPos);
					if(neighborState.getBlock() instanceof NanoGlass&&!neighborState.getValue(DARK).equals(dark)){
						nextRing.add(neighborPos);
					}
				}
			}
			if(!ringToSchedule.isEmpty()){
				scheduleRing(waveState,ringToSchedule,dark);
			}
			currentRing=nextRing;
		}
	}
	private static void scheduleRing(LevelWaveState waveState,List<BlockPos> positions,boolean dark){
		List<BlockPos> filtered=new ArrayList<>(positions.size());
		for(BlockPos pos: positions){
			long key=pos.asLong();
			if(waveState.scheduledPositions.contains(key)) continue;
			waveState.scheduledPositions.add(key);
			filtered.add(pos.immutable());
		}
		if(!filtered.isEmpty()){
			waveState.ringQueue.add(new Ring(filtered,dark));
		}
	}
	/**
	 * Aplikuje jeden prstenec za tick - všechny jeho bloky se obarví najednou,
	 * takže vlna postupuje "po vrstvách" a je výrazně rychlejší než po jednom
	 * bloku, ale pořád vizuálně plynulá.
	 */
	private static void tickWaves(ServerLevel level){
		LevelWaveState waveState=WAVE_STATES.get(level);
		if(waveState==null||waveState.isEmpty()) return;
		long currentTime=level.getGameTime();
		Ring ring=waveState.ringQueue.poll();
		if(ring==null) return;
		List<BlockPos> retry=null;
		for(BlockPos pos: ring.positions()){
			waveState.scheduledPositions.remove(pos.asLong());
			BlockState currentState=level.getBlockState(pos);
			if(!(currentState.getBlock() instanceof NanoGlass)) continue;
			if(currentState.getValue(DARK).equals(ring.dark())) continue;
			BlockEntity blockEntity=level.getBlockEntity(pos);
			if(blockEntity instanceof NanoGlassBlockEntity nanoGlassBE&&nanoGlassBE.isOnCooldown(currentTime)){
				// ještě na cooldownu -> zkusit znovu příští tick jako samostatný "mini-ring"
				if(retry==null) retry=new ArrayList<>();
				retry.add(pos);
				continue;
			}
			level.setBlockAndUpdate(pos,currentState.setValue(DARK,ring.dark()));
			if(blockEntity instanceof NanoGlassBlockEntity nanoGlassBE){
				nanoGlassBE.setCooldown(currentTime,COOLDOWN_TICKS);
			}
		}
		if(retry!=null&&!retry.isEmpty()){
			scheduleRing(waveState,retry,ring.dark());
		}
	}
	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event){
		if(!(event.getLevel() instanceof ServerLevel serverLevel)) return;
		long gameTick=serverLevel.getGameTime();
		if(gameTick%2==0){  // aplikuj jen každý sudý tick (2x pomaleji)
			tickWaves(serverLevel);
		}
	}
	// ---------------------------------------------------------------
	// Block implementation
	// ---------------------------------------------------------------
	public NanoGlass(BlockBehaviour.Properties properties){
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(DARK,false));
	}
	@Override
	public void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
		builder.add(DARK);
	}
	@Override
	public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos,@NotNull BlockState state){
		return new NanoGlassBlockEntity(DifModBlockEntities.NANO_GLASS.get(),pos,state);
	}
	/**
	 * Klik prázdnou rukou -> ručně přepne tmavost a spustí plynulou vlnu.
	 */
	@Override
	public @NotNull InteractionResult useWithoutItem(@NotNull BlockState blockState,@NotNull Level level,@NotNull BlockPos pos,@NotNull Player player,@NotNull BlockHitResult hit){
		super.useWithoutItem(blockState,level,pos,player,hit);
		if(level.isClientSide) return InteractionResult.SUCCESS;
		if(!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
		if(!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;
		boolean nextDarkValue=!blockState.getValue(DARK);
		startWave(serverLevel,pos,nextDarkValue);
		return InteractionResult.SUCCESS;
	}
	/**
	 * Reakce POUZE na skutečnou změnu redstone signálu. Notifikace vyvolané
	 * jiným NanoGlass blokem (položení/odebrání sousedního skla, nebo naše
	 * vlastní vlna přes setBlockAndUpdate) se ignorují, jinak by docházelo
	 * k reentrancy/ping-pong šíření i bez skutečného redstone podnětu.
	 */
	@Override
	public void neighborChanged(@NotNull BlockState state,@NotNull Level level,@NotNull BlockPos pos,@NotNull Block block,@NotNull BlockPos fromPos,boolean isMoving){
		super.neighborChanged(state,level,pos,block,fromPos,isMoving);
		if(level.isClientSide) return;
		if(!(level instanceof ServerLevel serverLevel)) return;
		if(block instanceof NanoGlass) return;
		boolean powered=level.hasNeighborSignal(pos);
		if(state.getValue(DARK)==powered) return;
		startWave(serverLevel,pos,powered);
	}
}