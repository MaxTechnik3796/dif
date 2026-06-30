package cz.maxtechnik.dif.item.modular.v2;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.dif.DifMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.joml.Matrix4f;

import java.util.*;
/**
 * AOE mining logic for {@link ModularTools#HAMMER}, {@link ModularTools#EXCAVATOR},
 * {@link ModularTools#TIMBER_AXE} and {@link ModularTools#HOE} (CULTIVATOR reforge).
 *
 * <h3>Hammer / Excavator / Hoe – plane mining</h3>
 * <ul>
 *   <li>3×3 plane perpendicular to hit face (Hoe always 3×3, even at MYTHIC).</li>
 *   <li>2 blocks = 1 durability (rounded up). Bonus: if remaining == 8, 9th block is free.</li>
 *   <li>Hardness protection: neighbor skipped when hardness {@code > centre × 2}.</li>
 * </ul>
 *
 * <h3>Timber Axe</h3>
 * <ul>
 *   <li>BFS through connected {@code LOGS} (6-directional), max {@value TIMBER_MAX_BLOCKS}.</li>
 *   <li>Natural tree (adjacent {@code PERSISTENT=false} leaves) → fell whole tree.</li>
 *   <li>Player-placed wood → 3×3 axe-plane fallback.</li>
 *   <li>2 logs = 1 durability (rounded up). Budget-capped: remaining logs stay.</li>
 * </ul>
 *
 * <h3>Hoe – CULTIVATOR reforge (RMB tilling)</h3>
 * <ul>
 *   <li>EPIC / LEGENDARY → till 3×3.</li>
 *   <li>MYTHIC → till 5×5.</li>
 *   <li>Block-breaking (haybale etc.) always 3×3 regardless of tier.</li>
 * </ul>
 */
@EventBusSubscriber(modid=DifMod.MODID)
public final class ModularMiningHandler{
	private static final float HARDNESS_MULTIPLIER=2.0f;
	private static final int AOE_SIZE=8;
	private static final int TIMBER_MAX_BLOCKS=64;
	/** Prevents recursive {@code BlockEvent.BreakEvent} from re-triggering AOE. */
	private static final ThreadLocal<Boolean> BREAKING=new ThreadLocal<>();
	// -------------------------------------------------------------------------
	// Server-side break event
	// -------------------------------------------------------------------------
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event){
		if(Boolean.TRUE.equals(BREAKING.get())) return;
		Level level=(Level)event.getLevel();
		if(level.isClientSide) return;
		if(!(event.getPlayer() instanceof ServerPlayer player)) return;
		ItemStack tool=player.getMainHandItem();
		if(!(tool.getItem() instanceof ModularTool modularTool)) return;
		if(modularTool.isBroken(tool)) return;
		String type=ModularTool.getProps(tool).toolType();
		boolean isHammer=type.equals(ModularTools.HAMMER.getName());
		boolean isExcavator=type.equals(ModularTools.EXCAVATOR.getName());
		boolean isTimberAxe=type.equals(ModularTools.TIMBER_AXE.getName());
		boolean isHoe=type.equals(ModularTools.HOE.getName());
		if(!isHammer&&!isExcavator&&!isTimberAxe&&!isHoe) return;
		// HOE: only with CULTIVATOR at EPIC+ and always 3×3 for block-breaking
		if(isHoe){
			if(ModularTool.getReforge(tool)!=ModularReforge.CULTIVATOR) return;
			ModularTier tier=ModularTool.getTier(tool);
			if(tier!=ModularTier.EPIC&&tier!=ModularTier.LEGENDARY&&tier!=ModularTier.MYTHIC) return;
		}
		BlockPos centre=event.getPos();
		BlockState centreState=level.getBlockState(centre);
		BREAKING.set(Boolean.TRUE);
		try{
			if(isTimberAxe) handleTimberAxe(level,player,tool,modularTool,centre,centreState);
			else handleAoeMining(level,player,tool,modularTool,centre,centreState,isExcavator);
		}finally{
			BREAKING.remove();
		}
	}
	// -------------------------------------------------------------------------
	// Hammer / Excavator / Hoe (block-break) – 3×3 plane
	// -------------------------------------------------------------------------
	private static void handleAoeMining(Level level,ServerPlayer player,ItemStack tool,ModularTool modularTool,BlockPos centre,BlockState centreState,boolean isExcavator){
		Direction face=getPlayerFacingFace(player);
		List<BlockPos> neighbours=get3x3Plane(centre,face);
		float centreHardness=centreState.getDestroySpeed(level,centre);
		int maxDmg=modularTool.getMaxDamage(tool);
		int remaining=Math.max(0,(maxDmg-1)-tool.getDamageValue());
		boolean bonusBlock=(remaining==AOE_SIZE); // 9th block free when exactly 8 dur left
		int budget=remaining*2;                   // 2 blocks per 1 damage point
		int minedCount=0;
		for(BlockPos pos: neighbours){
			if(minedCount>=AOE_SIZE) break;
			BlockState state=level.getBlockState(pos);
			if(state.isAir()) continue;
			if(!canToolMine(modularTool,tool,state,isExcavator)) continue;
			// Hardness protection: skip blocks more than 2× harder than center
			float nHardness=state.getDestroySpeed(level,pos);
			if(centreHardness>0f&&nHardness>centreHardness*HARDNESS_MULTIPLIER) continue;
			boolean isBonusBlock=bonusBlock&&(minedCount==AOE_SIZE-1);
			if(!isBonusBlock&&budget<=0) break;
			level.destroyBlock(pos,true,player);
			if(!isBonusBlock&&!player.isCreative()){
				minedCount++;
				if(minedCount%2==0) modularTool.damageTool(tool,1,player);
				budget--;
				if(modularTool.isBroken(tool)) break;
			}else{
				minedCount++;
			}
		}
	}
	// -------------------------------------------------------------------------
	// Timber Axe – tree felling
	// -------------------------------------------------------------------------
	private static void handleTimberAxe(Level level,ServerPlayer player,ItemStack tool,ModularTool modularTool,BlockPos centre,BlockState centreState){
		if(!centreState.is(BlockTags.LOGS)){
			handleAoeMining(level,player,tool,modularTool,centre,centreState,false);
			return;
		}
		List<BlockPos> logs=collectLogs(level,centre,true);
		if(!isNaturalTree(level,logs)){
			handleAoeMining(level,player,tool,modularTool,centre,centreState,false);
			return;
		}
		int maxDmg=modularTool.getMaxDamage(tool);
		int remaining=Math.max(0,(maxDmg-1)-tool.getDamageValue());
		int toBreak=Math.min(logs.size(),Math.min(TIMBER_MAX_BLOCKS,remaining*2));
		int damageApplied=0;
		for(int i=1;i<toBreak;i++){
			BlockState state=level.getBlockState(logs.get(i));
			if(state.isAir()) continue;
			level.destroyBlock(logs.get(i),true,player);
			if(!player.isCreative()){
				int expectedDmg=(i+2)/2; // cell((i+1)/2) – i+1 total including center
				int dmgNow=expectedDmg-damageApplied;
				if(dmgNow>0){
					modularTool.damageTool(tool,dmgNow,player);
					damageApplied+=dmgNow;
				}
				if(modularTool.isBroken(tool)) break;
			}
		}
	}
	// -------------------------------------------------------------------------
	// Public API – called from ModularTool.useOn
	// -------------------------------------------------------------------------
	/**
	 * Excavator RMB: flatten a 3×3 area via {@code SHOVEL_FLATTEN}.
	 * @return {@code true} if at least one block was modified
	 */
	public static boolean excavatorFlatten(UseOnContext context,ModularTool modularTool){
		return applyToolModifiedState(context,modularTool,1,ItemAbilities.SHOVEL_FLATTEN);
	}
	/**
	 * Hoe CULTIVATOR RMB: till a NxN area via {@code HOE_TILL}.
	 * @param radius 1 = 3×3, 2 = 5×5
	 * @return {@code true} if at least one block was modified
	 */
	public static boolean hoeCultivatorTill(UseOnContext context,ModularTool modularTool,int radius){
		return applyToolModifiedState(context,modularTool,radius,ItemAbilities.HOE_TILL);
	}
	/**
	 * Generic AOE {@code getToolModifiedState} applicator used by both
	 * {@link #excavatorFlatten} and {@link #hoeCultivatorTill}.
	 * Only fires on the top face ({@code Direction.UP}).
	 */
	private static boolean applyToolModifiedState(UseOnContext context,ModularTool modularTool,int radius,ItemAbility ability){
		ItemStack tool=context.getItemInHand();
		if(modularTool.isBroken(tool)) return false;
		if(context.getClickedFace()!=Direction.UP) return false;
		Level level=context.getLevel();
		BlockPos centre=context.getClickedPos();
		boolean anyModified=false;
		for(BlockPos pos: getAoePlane(centre,Direction.UP,radius)){
			BlockState state=level.getBlockState(pos);
			assert context.getPlayer()!=null;
			BlockState modified=state.getToolModifiedState(new UseOnContext(context.getPlayer(),context.getHand(),new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false)),ability,false);
			if(modified==null) continue;
			if(!level.isClientSide){
				level.setBlock(pos,modified,11);
				modularTool.damageTool(tool,1,context.getPlayer());
				anyModified=true;
				if(modularTool.isBroken(tool)) break;
			}
		}
		return anyModified;
	}
	// -------------------------------------------------------------------------
	// Shared helpers
	// -------------------------------------------------------------------------
	/**
	 * 3×3 plane excluding the center block (used for break-event AOE).
	 * Center is already broken by the vanilla event that triggered the handler.
	 */
	public static List<BlockPos> get3x3Plane(BlockPos centre,Direction face){
		List<BlockPos> all=getAoePlane(centre,face,1);
		all.remove(centre);
		return all;
	}
	/**
	 * Full (2*radius+1)² plane including center (used for RMB tilling / overlay).
	 */
	public static List<BlockPos> getAoePlane(BlockPos centre,Direction face,int radius){
		Direction[] axes=perpendicularAxes(face);
		Direction a=axes[0], b=axes[1];
		int side=radius*2+1;
		List<BlockPos> result=new ArrayList<>(side*side);
		for(int da=-radius;da<=radius;da++)
			for(int db=-radius;db<=radius;db++)
				result.add(centre.relative(a,da).relative(b,db));
		return result;
	}
	private static Direction[] perpendicularAxes(Direction face){
		return switch(face){
			case UP,DOWN -> new Direction[]{Direction.EAST,Direction.SOUTH};
			case NORTH,SOUTH -> new Direction[]{Direction.EAST,Direction.UP};
			case EAST,WEST -> new Direction[]{Direction.SOUTH,Direction.UP};
		};
	}
	private static Direction getPlayerFacingFace(Player player){
		HitResult hit=player.pick(5.0,0f,false);
		if(hit instanceof BlockHitResult blockHit) return blockHit.getDirection();
		float pitch=player.getXRot();
		if(pitch>45f) return Direction.DOWN;
		if(pitch<-45f) return Direction.UP;
		return player.getDirection();
	}
	private static boolean canToolMine(ModularTool tool,ItemStack stack,BlockState state,boolean isExcavator){
		if(isExcavator) return state.is(BlockTags.MINEABLE_WITH_SHOVEL)&&tool.isCorrectToolForDrops(stack,state);
		return tool.isCorrectToolForDrops(stack,state);
	}
	/**
	 * BFS collecting connected log blocks from {@code start}.
	 * @param sortBottomUp sort result Y-ascending (for tree felling order)
	 */
	private static List<BlockPos> collectLogs(Level level,BlockPos start,boolean sortBottomUp){
		List<BlockPos> result=new ArrayList<>();
		Set<BlockPos> visited=new HashSet<>();
		Deque<BlockPos> queue=new ArrayDeque<>();
		queue.add(start);
		visited.add(start);
		while(!queue.isEmpty()&&result.size()<TIMBER_MAX_BLOCKS){
			BlockPos cur=queue.poll();
			result.add(cur);
			for(Direction dir: Direction.values()){
				BlockPos nb=cur.relative(dir);
				if(!visited.contains(nb)&&level.getBlockState(nb).is(BlockTags.LOGS)){
					visited.add(nb);
					queue.add(nb);
				}
			}
		}
		if(sortBottomUp) result.sort(Comparator.comparingInt(BlockPos::getY));
		return result;
	}
	/**
	 * Tree is "natural" when at least one neighboring leaf block has {@code PERSISTENT=false}
	 * (placed by WorldWind, not by a player).
	 */
	private static boolean isNaturalTree(Level level,List<BlockPos> logs){
		for(BlockPos logPos: logs)
			for(Direction dir: Direction.values()){
				BlockState adj=level.getBlockState(logPos.relative(dir));
				if(adj.is(BlockTags.LEAVES)&&adj.hasProperty(LeavesBlock.PERSISTENT)&&!adj.getValue(LeavesBlock.PERSISTENT)) return true;
			}
		return false;
	}
	// =========================================================================
	// Client – wireframe overlay
	// =========================================================================
	@EventBusSubscriber(modid=DifMod.MODID, value=Dist.CLIENT)
	public static final class ClientOverlay{
		private ClientOverlay(){
		}
		// Vanilla default block hover outline color
		private static final float OR=0f, OG=0f, OB=0f, OA=0.4f;
		@SubscribeEvent
		public static void onRenderLevel(RenderLevelStageEvent event){
			if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
			Minecraft mc=Minecraft.getInstance();
			Player player=mc.player;
			if(player==null||mc.level==null) return;
			ItemStack tool=player.getMainHandItem();
			if(!(tool.getItem() instanceof ModularTool modularTool)) return;
			if(modularTool.isBroken(tool)) return;
			String type=ModularTool.getProps(tool).toolType();
			boolean isHammer=type.equals(ModularTools.HAMMER.getName());
			boolean isExcavator=type.equals(ModularTools.EXCAVATOR.getName());
			boolean isTimberAxe=type.equals(ModularTools.TIMBER_AXE.getName());
			boolean isHoe=type.equals(ModularTools.HOE.getName());
			if(!isHammer&&!isExcavator&&!isTimberAxe&&!isHoe) return;
			// HOE overlay only for CULTIVATOR at EPIC+
			if(isHoe&&(ModularTool.getReforge(tool)!=ModularReforge.CULTIVATOR||ModularTool.getTier(tool)==ModularTier.COMMON||ModularTool.getTier(tool)==ModularTier.RARE)) return;
			HitResult hit=player.pick(5.0,event.getPartialTick().getGameTimeDeltaPartialTick(false),false);
			if(!(hit instanceof BlockHitResult blockHit)) return;
			if(blockHit.getType()==HitResult.Type.MISS) return;
			Level level=mc.level;
			BlockPos centre=blockHit.getBlockPos();
			Direction face=blockHit.getDirection();
			Vec3 camPos=event.getCamera().getPosition();
			PoseStack ps=event.getPoseStack();
			ps.pushPose();
			ps.translate(-camPos.x,-camPos.y,-camPos.z);
			MultiBufferSource.BufferSource buf=mc.renderBuffers().bufferSource();
			VertexConsumer lines=buf.getBuffer(RenderType.lines());
			if(isTimberAxe){
				if(level.getBlockState(centre).is(BlockTags.LOGS)){
					List<BlockPos> logs=collectLogs(level,centre,false);
					if(isNaturalTree(level,logs)){
						// Only highlight logs at or above the hit block – below will remain standing
						for(BlockPos pos: logs)
							if(pos.getY()>=centre.getY()) renderBlockOutline(ps,lines,pos);
					}else{
						renderPlane(ps,lines,level,get3x3Plane(centre,face),centre);
					}
				}else{
					renderPlane(ps,lines,level,get3x3Plane(centre,face),centre);
				}
			}else if(isHoe){
				if(face==Direction.UP){
					ModularTier tier=ModularTool.getTier(tool);
					int radius=(tier==ModularTier.MYTHIC)?2:1;
					for(BlockPos pos: getAoePlane(centre,Direction.UP,radius))
						if(!level.getBlockState(pos).isAir()) renderBlockOutline(ps,lines,pos);
				}else{
					renderPlane(ps,lines,level,get3x3Plane(centre,face),centre);
				}
			}else{
				renderPlane(ps,lines,level,get3x3Plane(centre,face),centre);
			}
			buf.endBatch(RenderType.lines());
			ps.popPose();
		}
		/** Renders center + all non-air blocks in {@code plane} with the shared gray color. */
		private static void renderPlane(PoseStack ps,VertexConsumer lines,Level level,List<BlockPos> plane,BlockPos centre){
			if(!level.getBlockState(centre).isAir()) renderBlockOutline(ps,lines,centre);
			for(BlockPos pos: plane)
				if(!level.getBlockState(pos).isAir()) renderBlockOutline(ps,lines,pos);
		}
		private static void renderBlockOutline(PoseStack ps,VertexConsumer lines,BlockPos pos){
			Matrix4f mat=ps.last().pose();
			float x=pos.getX(), y=pos.getY(), z=pos.getZ();
			float x1=x+1f, y1=y+1f, z1=z+1f;
			line(lines,mat,x,y,z,x1,y,z);
			line(lines,mat,x1,y,z,x1,y,z1);
			line(lines,mat,x1,y,z1,x,y,z1);
			line(lines,mat,x,y,z1,x,y,z);
			line(lines,mat,x,y1,z,x1,y1,z);
			line(lines,mat,x1,y1,z,x1,y1,z1);
			line(lines,mat,x1,y1,z1,x,y1,z1);
			line(lines,mat,x,y1,z1,x,y1,z);
			line(lines,mat,x,y,z,x,y1,z);
			line(lines,mat,x1,y,z,x1,y1,z);
			line(lines,mat,x1,y,z1,x1,y1,z1);
			line(lines,mat,x,y,z1,x,y1,z1);
		}
		private static void line(VertexConsumer vc,Matrix4f mat,float x0,float y0,float z0,float x1,float y1,float z1){
			float nx=x1-x0, ny=y1-y0, nz=z1-z0;
			float len=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);
			if(len==0f) return;
			nx/=len;
			ny/=len;
			nz/=len;
			vc.addVertex(mat,x0,y0,z0).setColor(OR,OG,OB,OA).setNormal(nx,ny,nz);
			vc.addVertex(mat,x1,y1,z1).setColor(OR,OG,OB,OA).setNormal(nx,ny,nz);
		}
	}
}
