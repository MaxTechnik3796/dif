package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.config.DifModCommonConfig;
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
	private net.minecraft.world.phys.Vec3 alignPortal(ServerLevel world,BlockPos hitPos,Direction face,Direction extDir,net.minecraft.world.phys.Vec3 hitLoc){
		net.minecraft.world.phys.Vec3 C=net.minecraft.world.phys.Vec3.atCenterOf(hitPos);
		net.minecraft.world.phys.Vec3 normal=net.minecraft.world.phys.Vec3.atLowerCornerOf(face.getNormal());
		net.minecraft.world.phys.Vec3 up=net.minecraft.world.phys.Vec3.atLowerCornerOf(extDir.getNormal());
		net.minecraft.world.phys.Vec3 right=normal.cross(up);
		Direction rightDir=Direction.getNearest(right.x,right.y,right.z);
		double valUp=hitLoc.dot(up);
		double centerUp=C.dot(up);
		BlockPos tryPos1=hitPos.relative(extDir); // UP
		BlockPos tryPos2=hitPos.relative(extDir.getOpposite()); // DOWN
		boolean upSturdy=world.getBlockState(tryPos1).isFaceSturdy(world,tryPos1,face);
		boolean downSturdy=world.getBlockState(tryPos2).isFaceSturdy(world,tryPos2,face);
		double alignedUpVal;
		if(upSturdy&&downSturdy){
			alignedUpVal=valUp;
		}else if(upSturdy){
			alignedUpVal=centerUp+0.5;
		}else if(downSturdy){
			alignedUpVal=centerUp-0.5;
		}else{
			return null;
		}
		double valRight=hitLoc.dot(right);
		double centerRight=C.dot(right);
		double offsetRight=valRight-centerRight;
		double alignedRightVal=valRight;
		boolean rightSturdy=true;
		boolean leftSturdy=true;
		net.minecraft.world.phys.Vec3 wallPos=normal.scale(hitLoc.dot(normal))
				.add(up.scale(alignedUpVal)).add(right.scale(centerRight))
				.subtract(normal.scale(0.1));
		for(double h: new double[]{-0.9,0.0,0.9}){
			BlockPos bPos=BlockPos.containing(wallPos.add(up.scale(h)));
			BlockPos rPos=bPos.relative(rightDir);
			BlockPos lPos=bPos.relative(rightDir.getOpposite());
			if(!world.getBlockState(rPos).isFaceSturdy(world,rPos,face)){
				rightSturdy=false;
			}
			if(!world.getBlockState(lPos).isFaceSturdy(world,lPos,face)){
				leftSturdy=false;
			}
		}
		if(offsetRight>0&&!rightSturdy){
			alignedRightVal=centerRight;
		}else if(offsetRight<0&&!leftSturdy){
			alignedRightVal=centerRight;
		}
		double valNormal=hitLoc.dot(normal);
		return normal.scale(valNormal).add(up.scale(alignedUpVal)).add(right.scale(alignedRightVal));
	}
	private boolean firePortal(ServerLevel world,Player player,boolean isBlue){
		var start=player.getEyePosition();
		var hit=world.clip(new ClipContext(start,start.add(player.getLookAngle().scale(128.0)),
				ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
		if(hit.getType()!=HitResult.Type.BLOCK) return false;
		Direction face=hit.getDirection();
		BlockPos hitPos=hit.getBlockPos();
		Direction extDir=(face.getAxis()==Direction.Axis.Y)?player.getDirection():Direction.UP;
		net.minecraft.world.phys.Vec3 alignedLoc=alignPortal(world,hitPos,face,extDir,hit.getLocation());
		if(alignedLoc==null){
			player.displayClientMessage(Component.literal("[!] Invalid placement (needs support)"),true);
			return false;
		}
		// Shift 0.02 blocks away from the wall to prevent z-fighting / texture glitching
		net.minecraft.world.phys.Vec3 spawnPos=alignedLoc.add(net.minecraft.world.phys.Vec3.atLowerCornerOf(face.getNormal()).scale(0.02));
		java.util.Set<BlockPos> uniquePositions=cz.maxtechnik.dif.entity.portal.PortalEntity.getPortalSupportBlocks(spawnPos,extDir,face);
		// 1. Check space in all unique block positions (up to 4, strictly on the air side)
		boolean space=true;
		for(BlockPos p: uniquePositions){
			if(!world.isEmptyBlock(p)&&!world.getBlockState(p).canBeReplaced()){
				space=false;
				break;
			}
		}
		if(!space){
			player.displayClientMessage(Component.literal("[!] Invalid placement (no space)"),true);
			return false;
		}
		// 2. Check support behind all unique block positions
		boolean support=true;
		for(BlockPos p: uniquePositions){
			BlockPos supPos=p.relative(face.getOpposite());
			if(!world.getBlockState(supPos).isFaceSturdy(world,supPos,face)){
				support=false;
				break;
			}
		}
		if(!support){
			player.displayClientMessage(Component.literal("[!] Invalid placement (needs support)"),true);
			return false;
		}
		cz.maxtechnik.dif.entity.portal.PortalEntity portal=new cz.maxtechnik.dif.entity.portal.PortalEntity(world,player.getUUID(),isBlue,face,extDir,spawnPos);
		boolean overlaps=false;
		java.util.List<cz.maxtechnik.dif.entity.portal.PortalEntity> existingPortals=world.getEntitiesOfClass(
				cz.maxtechnik.dif.entity.portal.PortalEntity.class,
				portal.getBoundingBox().inflate(0.01)
		);
		for(cz.maxtechnik.dif.entity.portal.PortalEntity other: existingPortals){
			if(other.getOwner()!=null&&other.getOwner().equals(player.getUUID())&&other.isBlue()==isBlue){
				continue;
			}
			if(portal.getBoundingBox().inflate(0.01).intersects(other.getBoundingBox())){
				overlaps=true;
				break;
			}
		}
		if(overlaps){
			player.displayClientMessage(Component.literal("[!] Invalid position"),true);
			return false;
		}
		cz.maxtechnik.dif.entity.portal.PortalEntity.removeOldPortal(world,player.getUUID(),isBlue);
		// Save position in PortalData BEFORE spawning entity so that updateLinks during onAddedToLevel finds it!
		cz.maxtechnik.dif.entity.portal.PortalData.get(world).set(player.getUUID(),isBlue,portal.blockPosition());
		world.addFreshEntity(portal);
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