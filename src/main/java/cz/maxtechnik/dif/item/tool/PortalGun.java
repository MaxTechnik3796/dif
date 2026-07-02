package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.DifModCommonConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;

import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
public class PortalGun extends Item{
	public PortalGun(){
		super(new Properties().stacksTo(1));
	}
	private boolean isBlueMode(ItemStack gun){
		var data=gun.get(DataComponents.CUSTOM_DATA);
		if(data==null) return true;
		return data.copyTag().getBoolean("mode");
	}
	private void setMode(ItemStack gun,boolean mode){
		net.minecraft.world.item.component.CustomData.update(DataComponents.CUSTOM_DATA,gun,tag->tag.putBoolean("mode",mode));
		gun.set(DataComponents.CUSTOM_MODEL_DATA,new net.minecraft.world.item.component.CustomModelData(mode?0:1));
	}
	private int getEnergy(ItemStack gun){
		var data=gun.get(DataComponents.CUSTOM_DATA);
		if(data==null) return DifModCommonConfig.PORTAL_GUN_MAX_DURABILITY.get();
		return data.copyTag().getInt("energy");
	}
	private void setEnergy(ItemStack gun,int energy){
		gun.update(DataComponents.CUSTOM_DATA,
				net.minecraft.world.item.component.CustomData.EMPTY,
				cd->{
					var tag=cd.copyTag().copy();
					tag.putInt("energy",Math.clamp(energy,0,DifModCommonConfig.PORTAL_GUN_MAX_DURABILITY.get()));
					return net.minecraft.world.item.component.CustomData.of(tag);
				});
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world,Player player,@NotNull InteractionHand hand){
		ItemStack gun=player.getItemInHand(hand);
		// Inicializace pokud chybí data
		var data=gun.get(DataComponents.CUSTOM_DATA);
		if(data==null||!data.copyTag().contains("energy")){
			setMode(gun,true);
			setEnergy(gun,DifModCommonConfig.PORTAL_GUN_MAX_DURABILITY.get());
		}
		boolean isBlue=isBlueMode(gun);
		int energy=getEnergy(gun);
		// Dobíjení ender pearlem
		ItemStack off=player.getOffhandItem();
		if(off.is(Items.ENDER_PEARL)&&energy<DifModCommonConfig.PORTAL_GUN_MAX_DURABILITY.get()){
			if(!world.isClientSide){
				setEnergy(gun,energy+DifModCommonConfig.PORTAL_GUN_ENERGY_PER_PEARL.get());
				off.shrink(1);
				player.displayClientMessage(Component.literal("[+] Energy restored"),true);
			}
			return InteractionResultHolder.sidedSuccess(gun,world.isClientSide());
		}
		// Přepínání módu
		if(player.isShiftKeyDown()){
			if(!world.isClientSide){
				boolean newMode=!isBlue;
				setMode(gun,newMode);
				player.displayClientMessage(Component.literal(newMode?"Mode: Blue":"Mode: Orange"),true);
			}
			return InteractionResultHolder.sidedSuccess(gun,world.isClientSide());
		}
		// Výstřel
		if(!world.isClientSide){
			if(energy>=DifModCommonConfig.PORTAL_GUN_ENERGY_PER_SHOT.get()){
				if(firePortal((ServerLevel)world,player,isBlue)){
					setEnergy(gun,energy-DifModCommonConfig.PORTAL_GUN_ENERGY_PER_SHOT.get());
					player.getCooldowns().addCooldown(this,DifModCommonConfig.PORTAL_GUN_SHOT_COOLDOWN.get());
				}
			}else{
				player.displayClientMessage(Component.literal("[!] Out of energy"),true);
			}
		}
		return InteractionResultHolder.success(gun);
	}
	private net.minecraft.world.phys.Vec3 alignPortal(ServerLevel world, BlockPos hitPos, Direction face, Direction extDir, net.minecraft.world.phys.Vec3 hitLoc) {
		net.minecraft.world.phys.Vec3 C = net.minecraft.world.phys.Vec3.atCenterOf(hitPos);
		net.minecraft.world.phys.Vec3 normal = net.minecraft.world.phys.Vec3.atLowerCornerOf(face.getNormal());
		net.minecraft.world.phys.Vec3 up = net.minecraft.world.phys.Vec3.atLowerCornerOf(extDir.getNormal());
		net.minecraft.world.phys.Vec3 right = normal.cross(up);
		Direction rightDir = Direction.getNearest(right.x, right.y, right.z);

		// 1. Align along UP axis (height/length)
		double offsetUp = hitLoc.subtract(C).dot(up);
		double alignedUpVal;
		BlockPos adjPos;
		
		BlockPos tryPos1 = hitPos.relative(extDir);
		BlockPos tryPos2 = hitPos.relative(extDir.getOpposite());
		
		boolean try1Sturdy = world.getBlockState(tryPos1).isFaceSturdy(world, tryPos1, face);
		boolean try2Sturdy = world.getBlockState(tryPos2).isFaceSturdy(world, tryPos2, face);

		if (offsetUp > 0) {
			if (try1Sturdy) {
				alignedUpVal = C.dot(up) + 0.5;
				adjPos = tryPos1;
			} else if (try2Sturdy) {
				alignedUpVal = C.dot(up) - 0.5;
				adjPos = tryPos2;
			} else {
				return null; // No support
			}
		} else {
			if (try2Sturdy) {
				alignedUpVal = C.dot(up) - 0.5;
				adjPos = tryPos2;
			} else if (try1Sturdy) {
				alignedUpVal = C.dot(up) + 0.5;
				adjPos = tryPos1;
			} else {
				return null; // No support
			}
		}

		// 2. Align along RIGHT axis (width)
		double valRight = hitLoc.dot(right);
		double centerRight = C.dot(right);
		double offsetRight = valRight - centerRight;

		BlockPos sidePos1 = hitPos.relative(rightDir);
		BlockPos sidePos2 = hitPos.relative(rightDir.getOpposite());
		BlockPos adjSidePos1 = adjPos.relative(rightDir);
		BlockPos adjSidePos2 = adjPos.relative(rightDir.getOpposite());

		boolean rightSturdy = world.getBlockState(sidePos1).isFaceSturdy(world, sidePos1, face) &&
		                      world.getBlockState(adjSidePos1).isFaceSturdy(world, adjSidePos1, face);
		                      
		boolean leftSturdy = world.getBlockState(sidePos2).isFaceSturdy(world, sidePos2, face) &&
		                     world.getBlockState(adjSidePos2).isFaceSturdy(world, adjSidePos2, face);

		double alignedRightVal = valRight;
		if (offsetRight > 0 && !rightSturdy) {
			alignedRightVal = centerRight;
		} else if (offsetRight < 0 && !leftSturdy) {
			alignedRightVal = centerRight;
		}

		double valNormal = hitLoc.dot(normal);
		
		return normal.scale(valNormal).add(up.scale(alignedUpVal)).add(right.scale(alignedRightVal));
	}

	private boolean firePortal(ServerLevel world,Player player,boolean isBlue){
		var start=player.getEyePosition();
		var hit=world.clip(new ClipContext(start,start.add(player.getLookAngle().scale(128.0)),
				ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
		if(hit.getType()!=HitResult.Type.BLOCK) return false;
		net.minecraft.world.phys.BlockHitResult blockHit = (net.minecraft.world.phys.BlockHitResult) hit;
		Direction face=blockHit.getDirection();
		BlockPos hitPos=blockHit.getBlockPos();
		
		Direction extDir = (face.getAxis() == Direction.Axis.Y) ? player.getDirection() : Direction.UP;
		net.minecraft.world.phys.Vec3 extVec = net.minecraft.world.phys.Vec3.atLowerCornerOf(extDir.getNormal());
		
		net.minecraft.world.phys.Vec3 alignedLoc = alignPortal(world, hitPos, face, extDir, blockHit.getLocation());
		if (alignedLoc == null) {
			player.displayClientMessage(Component.literal("[!] Invalid placement (needs support)"), true);
			return false;
		}
		
		// Shift 0.02 blocks away from the wall to prevent z-fighting / texture glitching
		net.minecraft.world.phys.Vec3 spawnPos = alignedLoc.add(net.minecraft.world.phys.Vec3.atLowerCornerOf(face.getNormal()).scale(0.02));
		
		BlockPos pos1 = BlockPos.containing(spawnPos.subtract(extVec.scale(0.5)));
		BlockPos pos2 = BlockPos.containing(spawnPos.add(extVec.scale(0.5)));
		
		boolean space = (world.isEmptyBlock(pos1) || world.getBlockState(pos1).canBeReplaced()) && 
		                (world.isEmptyBlock(pos2) || world.getBlockState(pos2).canBeReplaced());
		
		if (!space) {
			player.displayClientMessage(Component.literal("[!] Invalid placement (no space)"), true);
			return false;
		}
		
		cz.maxtechnik.dif.entity.portal.PortalEntity.removeOldPortal(world,player.getUUID(),isBlue);
		
		cz.maxtechnik.dif.entity.portal.PortalEntity portal = new cz.maxtechnik.dif.entity.portal.PortalEntity(world, player.getUUID(), isBlue, face, extDir, spawnPos);
		
		world.addFreshEntity(portal);
		cz.maxtechnik.dif.entity.portal.PortalData.get(world).set(player.getUUID(), isBlue, portal.blockPosition());
		return true;
	}
	@Override
	public boolean isBarVisible(@NotNull ItemStack stack){
		return getEnergy(stack)<DifModCommonConfig.PORTAL_GUN_MAX_DURABILITY.get();
	}
	@Override
	public int getBarWidth(@NotNull ItemStack stack){
		return Math.round((float)getEnergy(stack)/DifModCommonConfig.PORTAL_GUN_MAX_DURABILITY.get()*13);
	}
	@Override
	public int getBarColor(@NotNull ItemStack stack){
		float f=(float)getEnergy(stack)/DifModCommonConfig.PORTAL_GUN_MAX_DURABILITY.get();
		return net.minecraft.util.FastColor.ARGB32.color(0,(int)(f*255),255-((int)(f*255)),0);
	}
	@Override
	public boolean isEnchantable(@NotNull ItemStack itemStack){
		return false;
	}
	@Override
	public boolean isRepairable(@NotNull ItemStack itemStack){
		return false;
	}
	@Override
	public boolean isValidRepairItem(@NotNull ItemStack pToRepair,@NotNull ItemStack pRepair){
		return false;
	}
}