package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.entity.portal.PortalData;
import cz.maxtechnik.dif.entity.portal.PortalEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class PortalGun extends Item{

	public PortalGun(){
		super(new Properties().stacksTo(1));
	}

	// -------------------- NBT helpers --------------------

	private boolean isBlueMode(ItemStack gun){
		CustomData data=gun.get(DataComponents.CUSTOM_DATA);
		return data==null||data.copyTag().getBoolean("mode");
	}

	private void setMode(ItemStack gun,boolean blue){
		CustomData.update(DataComponents.CUSTOM_DATA,gun,tag->tag.putBoolean("mode",blue));
		gun.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(blue?0:1));
	}

	private int getEnergy(ItemStack gun){
		CustomData data=gun.get(DataComponents.CUSTOM_DATA);
		if(data==null) return DifModServerConfig.PORTAL_GUN_MAX_DURABILITY.get();
		return data.copyTag().getInt("energy");
	}

	private void setEnergy(ItemStack gun,int energy){
		int max=DifModServerConfig.PORTAL_GUN_MAX_DURABILITY.get();
		gun.update(DataComponents.CUSTOM_DATA,CustomData.EMPTY,cd->{
			var tag=cd.copyTag().copy();
			tag.putInt("energy",Math.clamp(energy,0,max));
			return CustomData.of(tag);
		});
	}

	// -------------------- use() --------------------

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world,Player player,@NotNull InteractionHand hand){
		ItemStack gun=player.getItemInHand(hand);

		// Inicializace dat
		CustomData data=gun.get(DataComponents.CUSTOM_DATA);
		if(data==null||!data.copyTag().contains("energy")){
			setMode(gun,true);
			setEnergy(gun,DifModServerConfig.PORTAL_GUN_MAX_DURABILITY.get());
		}

		boolean isBlue=isBlueMode(gun);
		int energy=getEnergy(gun);

		// Dobíjení ender perlou
		ItemStack off=player.getOffhandItem();
		if(off.is(Items.ENDER_PEARL)&&energy<DifModServerConfig.PORTAL_GUN_MAX_DURABILITY.get()){
			if(!world.isClientSide){
				setEnergy(gun,energy+DifModServerConfig.PORTAL_GUN_ENERGY_PER_PEARL.get());
				off.shrink(1);
				player.displayClientMessage(Component.literal("[+] Energy restored"),true);
			}
			return InteractionResultHolder.sidedSuccess(gun,world.isClientSide());
		}

		// Přepínání módu
		if(player.isShiftKeyDown()){
			if(!world.isClientSide){
				boolean m=!isBlue;
				setMode(gun,m);
				player.displayClientMessage(Component.literal(m?"Mode: Blue":"Mode: Orange"),true);
			}
			return InteractionResultHolder.sidedSuccess(gun,world.isClientSide());
		}

		// Střelba
		if(!world.isClientSide){
			if(energy>=1){
				if(firePortal((ServerLevel)world,player,isBlue)){
					setEnergy(gun,energy-1);
					player.getCooldowns().addCooldown(this,10);
				}
			}else{
				player.displayClientMessage(Component.literal("[!] Out of energy"),true);
			}
		}
		return InteractionResultHolder.success(gun);
	}

	// -------------------- Placement --------------------

	private boolean firePortal(ServerLevel world,Player player,boolean isBlue){
		Vec3 eye=player.getEyePosition();
		var hit=world.clip(new ClipContext(eye,eye.add(player.getLookAngle().scale(128)),
				ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
		if(hit.getType()!=HitResult.Type.BLOCK) return false;

		Direction face=hit.getDirection();
		BlockPos hitPos=hit.getBlockPos();
		Direction extDir=(face.getAxis()==Direction.Axis.Y)?player.getDirection():Direction.UP;

		Vec3 spawnPos=alignPortal(world,hitPos,face,extDir,hit.getLocation());
		if(spawnPos==null){
			player.displayClientMessage(Component.literal("[!] Invalid placement"),true);
			return false;
		}

		// Kontrola překryvu s cizími portály
		PortalEntity portal=new PortalEntity(world,player.getUUID(),isBlue,face,extDir,spawnPos);
		List<PortalEntity> nearby=world.getEntitiesOfClass(PortalEntity.class,portal.getBoundingBox().inflate(0.05));
		for(PortalEntity o: nearby){
			if(o.getOwner()!=null&&o.getOwner().equals(player.getUUID())&&o.isBlue()==isBlue) continue;
			if(portal.getBoundingBox().intersects(o.getBoundingBox())){
				player.displayClientMessage(Component.literal("[!] Invalid position"),true);
				return false;
			}
		}

		PortalEntity.removeOldPortal(world,player.getUUID(),isBlue);
		PortalData.get(world).set(player.getUUID(),isBlue,portal.blockPosition());
		world.addFreshEntity(portal);
		return true;
	}

	/**
	 * Pixel-grid zarovnání portálu (1/16 bloku).
	 * Zkouší pozice v pořadí: off-grid pixel snap → snap k horní hraně → snap k dolní hraně → střed.
	 * Každou pozici validuje přes kompletní footprint (podpora + volný prostor).
	 * Pokud žádná pozice nevyhovuje, vrací null.
	 */
	private Vec3 alignPortal(ServerLevel world,BlockPos hitPos,Direction face,Direction extDir,Vec3 hitLoc){
		Vec3 center=Vec3.atCenterOf(hitPos);
		Vec3 normal=Vec3.atLowerCornerOf(face.getNormal());
		Vec3 up=Vec3.atLowerCornerOf(extDir.getNormal());
		Vec3 right=normal.cross(up);

		double cU=center.dot(up);
		double cR=center.dot(right);
		double nVal=center.dot(normal)+0.5; // povrch stěny

		double hitU=hitLoc.dot(up);
		double hitR=hitLoc.dot(right);

		// Kandidáti pro výšku: off-grid snap, pak hrany bloku, pak střed
		double offU=snapToGrid(Math.clamp(hitU,cU-0.5,cU+0.5));
		double[] tryU={offU,cU+0.5,cU-0.5,cU};

		// Kandidáti pro šířku: off-grid snap, pak střed
		double offR=snapToGrid(Math.clamp(hitR,cR-0.5,cR+0.5));
		double[] tryR={offR,cR};

		for(double u: tryU){
			for(double r: tryR){
				Vec3 pos=normal.scale(nVal+0.02).add(up.scale(u)).add(right.scale(r));
				if(isValidPortalPos(world,pos,extDir,face)) return pos;
			}
		}
		return null;
	}

	/**
	 * Ověří kompletní footprint portálu: každý překrytý blok musí mít za sebou oporu a před sebou vzduch.
	 */
	private boolean isValidPortalPos(ServerLevel world,Vec3 pos,Direction upDir,Direction face){
		Set<BlockPos> blocks=PortalEntity.getPortalFootprint(pos,upDir,face);
		if(blocks.isEmpty()) return false;
		for(BlockPos p: blocks){
			BlockPos behind=p.relative(face.getOpposite());
			if(!world.getBlockState(behind).isFaceSturdy(world,behind,face)) return false;
			if(!world.isEmptyBlock(p)&&!world.getBlockState(p).canBeReplaced()) return false;
		}
		return true;
	}

	private static double snapToGrid(double v){
		return Math.round(v*16.0)/16.0;
	}

	// -------------------- Durability bar --------------------

	@Override
	public boolean isBarVisible(@NotNull ItemStack s){
		return getEnergy(s)<DifModServerConfig.PORTAL_GUN_MAX_DURABILITY.get();
	}

	@Override
	public int getBarWidth(@NotNull ItemStack s){
		return Math.round((float)getEnergy(s)/DifModServerConfig.PORTAL_GUN_MAX_DURABILITY.get()*13);
	}

	@Override
	public int getBarColor(@NotNull ItemStack s){
		float f=(float)getEnergy(s)/DifModServerConfig.PORTAL_GUN_MAX_DURABILITY.get();
		return FastColor.ARGB32.color(0,(int)(f*255),255-(int)(f*255),0);
	}

	@Override public boolean isEnchantable(@NotNull ItemStack s){ return false; }
	@Override public boolean isRepairable(@NotNull ItemStack s){ return false; }
	@Override public boolean isValidRepairItem(@NotNull ItemStack a,@NotNull ItemStack b){ return false; }
}