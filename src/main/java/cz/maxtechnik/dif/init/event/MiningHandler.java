package cz.maxtechnik.dif.init.event;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;
import java.util.function.Predicate;
@EventBusSubscriber(modid=DifMod.MODID)
public final class MiningHandler{
	private static boolean isBreaking=false;
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event){
		if(isBreaking) return;
		Level level=(Level)event.getLevel();
		if(level.isClientSide||!(event.getPlayer() instanceof ServerPlayer player)) return;
		ItemStack tool=player.getMainHandItem();
		if(tool.isEmpty()) return;
		boolean isDestroyer=tool.is(DifModItems.DESTROYER.get());
		boolean isDeforester=tool.is(DifModItems.DEFORESTER.get())||tool.is(DifModItems.DEFORESTRATOR.get());
		if(!isDestroyer&&!isDeforester) return;
		BlockPos center=event.getPos();
		BlockState centerState=event.getState();
		isBreaking=true;
		try{
			if(isDestroyer) break3x3(level,player,center,state->state.is(BlockTags.MINEABLE_WITH_PICKAXE)||state.is(BlockTags.MINEABLE_WITH_SHOVEL)||tool.isCorrectToolForDrops(state));
			else if(!centerState.is(BlockTags.LOGS)||!fellTree(level,player,center)) break3x3(level,player,center,state->state.is(BlockTags.MINEABLE_WITH_AXE)||state.is(BlockTags.LOGS)||tool.isCorrectToolForDrops(state));
		}finally{
			isBreaking=false;
		}
	}
	private static void break3x3(Level level,ServerPlayer player,BlockPos center,Predicate<BlockState> canMine){
		Direction face=getPlayerFacingFace(player);
		for(BlockPos pos: get3x3Plane(center,face)){
			if(player.getMainHandItem().isEmpty()) break;
			BlockState state=level.getBlockState(pos);
			if(state.isAir()||state.getDestroySpeed(level,pos)<0) continue;
			if(canMine.test(state)&&player.gameMode.destroyBlock(pos)){
				if(!(state.getBlock() instanceof BaseFireBlock)) level.levelEvent(2001,pos,Block.getId(state));
			}
		}
	}
	private static boolean fellTree(Level level,ServerPlayer player,BlockPos start){
		int maxLogs=DifModServerConfig.ELECTRUM_DEFORESTER_MAX_LOGS.get();
		List<BlockPos> logs=new ArrayList<>();
		Set<BlockPos> visited=new HashSet<>();
		Queue<BlockPos> queue=new ArrayDeque<>();
		queue.add(start);
		visited.add(start);
		boolean hasNaturalLeaves=false;
		while(!queue.isEmpty()&&logs.size()<maxLogs){
			BlockPos cur=queue.poll();
			logs.add(cur);
			for(int dx=-1;dx<=1;dx++){
				for(int dy=-1;dy<=1;dy++){
					for(int dz=-1;dz<=1;dz++){
						if(dx==0&&dy==0&&dz==0) continue;
						BlockPos next=cur.offset(dx,dy,dz);
						if(!visited.add(next)) continue;
						BlockState state=level.getBlockState(next);
						if(state.is(BlockTags.LOGS)) queue.add(next);
						else if(!hasNaturalLeaves&&state.is(BlockTags.LEAVES)&&state.hasProperty(LeavesBlock.PERSISTENT)&&!state.getValue(LeavesBlock.PERSISTENT)) hasNaturalLeaves=true;
					}
				}
			}
		}
		if(!hasNaturalLeaves) return false;
		logs.sort(Comparator.comparingInt(BlockPos::getY));
		for(BlockPos pos: logs){
			if(pos.equals(start)) continue;
			if(player.getMainHandItem().isEmpty()) break;
			BlockState state=level.getBlockState(pos);
			if(!state.isAir()&&player.gameMode.destroyBlock(pos)) level.levelEvent(2001,pos,Block.getId(state));
		}
		return true;
	}
	public static List<BlockPos> get3x3Plane(BlockPos center,Direction face){
		List<BlockPos> list=getAoePlane(center,face,1);
		list.remove(center);
		return list;
	}
	public static List<BlockPos> getAoePlane(BlockPos center,Direction face,int radius){
		List<BlockPos> result=new ArrayList<>();
		for(int a=-radius;a<=radius;a++){
			for(int b=-radius;b<=radius;b++){
				BlockPos pos=switch(face.getAxis()){
					case Y -> center.offset(a,0,b);
					case Z -> center.offset(a,b,0);
					case X -> center.offset(0,b,a);
				};
				result.add(pos);
			}
		}
		return result;
	}
	private static Direction getPlayerFacingFace(Player player){
		HitResult hit=player.pick(5D,0F,false);
		if(hit instanceof BlockHitResult blockHit) return blockHit.getDirection();
		if(player.getXRot()>45F) return Direction.DOWN;
		if(player.getXRot()<-45F) return Direction.UP;
		return player.getDirection();
	}
}
