package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.block.PortalBlock;
import cz.maxtechnik.dif.block.entity.PortalBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
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
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class PortalGun extends Item{
	public PortalGun(){
		super(new Properties().stacksTo(1));
	}

	private boolean isBlueMode(ItemStack gun){
		var data=gun.get(DataComponents.CUSTOM_DATA);
		return data==null||data.getUnsafe().getBoolean("mode");
	}

	private void setMode(ItemStack gun,boolean mode){
		gun.update(DataComponents.CUSTOM_DATA,
				net.minecraft.world.item.component.CustomData.EMPTY,
				cd->{
					var tag=cd.getUnsafe().copy();
					tag.putBoolean("mode",mode);
					tag.putInt("CustomModelData",mode?1:0);
					return net.minecraft.world.item.component.CustomData.of(tag);
				});
	}

	private int getEnergy(ItemStack gun){
		var data=gun.get(DataComponents.CUSTOM_DATA);
		if(data==null) return DifModCommonConfig.portalGunMaxDurability;
		return data.getUnsafe().getInt("energy");
	}

	private void setEnergy(ItemStack gun,int energy){
		gun.update(DataComponents.CUSTOM_DATA,
				net.minecraft.world.item.component.CustomData.EMPTY,
				cd->{
					var tag=cd.getUnsafe().copy();
					tag.putInt("energy",Math.max(0,Math.min(DifModCommonConfig.portalGunMaxDurability,energy)));
					return net.minecraft.world.item.component.CustomData.of(tag);
				});
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world,Player player,@NotNull InteractionHand hand){
		ItemStack gun=player.getItemInHand(hand);

		// Inicializace pokud chybí data
		var data=gun.get(DataComponents.CUSTOM_DATA);
		if(data==null||!data.getUnsafe().contains("energy")){
			setMode(gun,true);
			setEnergy(gun,DifModCommonConfig.portalGunMaxDurability);
		}

		boolean isBlue=isBlueMode(gun);
		int energy=getEnergy(gun);

		// Dobíjení ender pearlem
		ItemStack off=player.getOffhandItem();
		if(off.is(Items.ENDER_PEARL)&&energy<DifModCommonConfig.portalGunMaxDurability){
			if(!world.isClientSide){
				setEnergy(gun,energy+DifModCommonConfig.portalGunEnergyPerPearl);
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
			if(energy>=DifModCommonConfig.portalGunEnergyPerShot){
				if(firePortal((ServerLevel)world,player,isBlue)){
					setEnergy(gun,energy-DifModCommonConfig.portalGunEnergyPerShot);
					player.getCooldowns().addCooldown(this,DifModCommonConfig.portalGunShotCooldown);
				}
			}else{
				player.displayClientMessage(Component.literal("[!] Out of energy"),true);
			}
		}
		return InteractionResultHolder.success(gun);
	}

	private boolean firePortal(ServerLevel world,Player player,boolean isBlue){
		var start=player.getEyePosition();
		var hit=world.clip(new ClipContext(start,start.add(player.getLookAngle().scale(128.0)),
				ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
		if(hit.getType()!=HitResult.Type.BLOCK) return false;
		Direction face=hit.getDirection();
		BlockPos pos=hit.getBlockPos().relative(face);
		Direction extDir=(face.getAxis()==Direction.Axis.Y)?player.getDirection():Direction.UP;
		BlockPos extPos=pos.relative(extDir);
		boolean space=world.isEmptyBlock(pos)&&world.isEmptyBlock(extPos);
		boolean support=world.getBlockState(pos.relative(face.getOpposite()))
				.isFaceSturdy(world,pos.relative(face.getOpposite()),face)&&
				world.getBlockState(extPos.relative(face.getOpposite()))
						.isFaceSturdy(world,extPos.relative(face.getOpposite()),face);
		if(!space||!support){
			player.displayClientMessage(Component.literal("[!] Invalid placement"),true);
			return false;
		}
		PortalBlockEntity.removeOldPortal(world,player.getUUID(),isBlue);
		PortalBlockEntity.savePortal(world,player.getUUID(),isBlue,pos);
		var state=DifModBlocks.PORTAL_BLOCK.get().defaultBlockState()
				.setValue(PortalBlock.FACING,face)
				.setValue(PortalBlock.EXTENSION_DIR,extDir)
				.setValue(PortalBlock.IS_BLUE,isBlue);
		world.setBlock(pos,state.setValue(PortalBlock.HALF,DoubleBlockHalf.LOWER),3);
		world.setBlock(extPos,state.setValue(PortalBlock.HALF,DoubleBlockHalf.UPPER),3);
		if(world.getBlockEntity(pos) instanceof PortalBlockEntity be) be.setup(player.getUUID(),isBlue,face);
		return true;
	}

	@Override
	public boolean isEnchantable(@NotNull ItemStack itemStack){ return false; }

	@Override
	public int getEnchantmentValue(){ return 0; }

	@Override
	public boolean isRepairable(@NotNull ItemStack itemStack){ return false; }

	@Override
	public boolean isValidRepairItem(@NotNull ItemStack pToRepair,@NotNull ItemStack pRepair){ return false; }
}