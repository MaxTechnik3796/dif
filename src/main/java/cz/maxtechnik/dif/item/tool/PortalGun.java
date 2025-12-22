package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.DifModCommonConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
public class PortalGun extends Item{
	public PortalGun(){
		super(new Properties().stacksTo(1));
	}
	@Override
	public void onCraftedBy(@NotNull ItemStack itemstack,@NotNull Level world,@NotNull Player player){
		super.onCraftedBy(itemstack,world,player);
		setCharge(itemstack,itemstack.getOrCreateTag().getInt("ammo"));
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world,@NotNull Player player,@NotNull InteractionHand hand){
		super.use(world,player,hand);
		ItemStack itemStack=player.getItemInHand(hand);
		CompoundTag nbt=itemStack.getOrCreateTag();
		if(!world.isClientSide()&&nbt.getInt("ammo")>0){
			if(player.isShiftKeyDown()){
				nbt.putBoolean("needReset",true);
				itemStack.getOrCreateTag().putUUID("uuid",player.getUUID());
			}else{
				nbt.putInt("ammo",nbt.getInt("ammo")-1);
				if(DifModCommonConfig.portalGunCooldown!=0)
					player.getCooldowns().addCooldown(itemStack.getItem(),DifModCommonConfig.portalGunCooldown);
				if(itemStack.getOrCreateTag().getBoolean("needReset")){
					itemStack.getOrCreateTag().putUUID("uuid",player.getUUID());
				}
				Entity owner;
				if(nbt.contains("uuid")){
					UUID entityUuid=nbt.getUUID("uuid");
					Entity potentialOwner=null;
					if(world instanceof ServerLevel serverLevel){
						potentialOwner=serverLevel.getEntity(entityUuid);
					}
					owner=Objects.requireNonNullElse(potentialOwner,player);
				}else{
					owner=player;
				}
				if(player.isShiftKeyDown()){
					return InteractionResultHolder.fail(player.getItemInHand(hand));
				}
				Projectile entityToSpawn=new Object(){
					public Projectile getProjectile(Level level){
						Projectile entityToSpawn=new ThrownEnderpearl(EntityType.ENDER_PEARL,level);
						entityToSpawn.setOwner(owner);
						return entityToSpawn;
					}
				}.getProjectile(world);
				entityToSpawn.setPos(player.getX(),player.getEyeY()-0.1,player.getZ());
				entityToSpawn.shoot(player.getLookAngle().x,player.getLookAngle().y,player.getLookAngle().z,DifModCommonConfig.portalGunMaxRange,0);
				world.addFreshEntity(entityToSpawn);
				nbt.putBoolean("needReset",true);
				nbt.putUUID("uuid",player.getUUID());
			}
		}
		if(nbt.getInt("ammo")==0){
			nbt.putInt("CustomModelData",1);
		}else{
			nbt.putInt("CustomModelData",0);
		}
		return InteractionResultHolder.consume(player.getItemInHand(hand));
	}
	@Override
	public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack itemStack,@NotNull Player player,@NotNull LivingEntity entity,@NotNull InteractionHand hand){
		if(!player.level().isClientSide()&&player.isShiftKeyDown()){
			itemStack.getOrCreateTag().putUUID("uuid",entity.getUUID());
			itemStack.getOrCreateTag().putBoolean("needReset",false);
			player.setItemInHand(hand,itemStack);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.FAIL;
	}
	public static int getCharge(ItemStack itemStack){
		return itemStack.getOrCreateTag().getInt("charge");
	}
	public static void setCharge(ItemStack itemStack,int value){
		itemStack.getOrCreateTag().putInt("charge",Math.min(value,DifModCommonConfig.portalGunMaxAmmo));
	}
	@Override
	public boolean isBarVisible(@NotNull ItemStack itemStack){
		setCharge(itemStack,itemStack.getOrCreateTag().getInt("ammo"));
		return true;
	}
	@Override
	public int getBarWidth(@NotNull ItemStack itemStack){
		int charge=getCharge(itemStack);
		return Math.round(13.0f*charge/DifModCommonConfig.portalGunMaxAmmo);
	}
	@Override
	public int getBarColor(@NotNull ItemStack itemStack){
		return 0x00FF00;
	}
}