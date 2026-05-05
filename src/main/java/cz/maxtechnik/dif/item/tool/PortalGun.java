package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.block.PortalBlock;
import cz.maxtechnik.dif.block.entity.PortalBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("deprecation")
public class PortalGun extends Item{
	public PortalGun(){
		super(new Properties().stacksTo(1).durability(24));
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world,Player player,@NotNull InteractionHand hand){
		ItemStack gun=player.getItemInHand(hand);
		CompoundTag nbt=gun.getOrCreateTag();
		if(!nbt.contains("mode")){
			nbt.putBoolean("mode",true);
			nbt.putInt("CustomModelData",1);
		}
		ItemStack off=player.getOffhandItem();
		if(off.is(Items.ENDER_PEARL)&&gun.getDamageValue()>0){
			if(!world.isClientSide){
				gun.setDamageValue(Math.max(0,gun.getDamageValue()-4));
				off.shrink(1);
				player.displayClientMessage(Component.literal("§a[+] Energy restored"),true);
			}
			return InteractionResultHolder.sidedSuccess(gun,world.isClientSide());
		}
		if(player.isShiftKeyDown()){
			if(!world.isClientSide){
				boolean mode=!nbt.getBoolean("mode");
				nbt.putBoolean("mode",mode);
				nbt.putInt("CustomModelData",mode?0:1);
				player.displayClientMessage(Component.literal(mode?"§bMode: Blue":"§6Mode: Orange"),true);
			}
			return InteractionResultHolder.sidedSuccess(gun,world.isClientSide());
		}
		if(!world.isClientSide){
			if(gun.getDamageValue()<24){
				if(firePortal((ServerLevel)world,player,nbt.getBoolean("mode"))){
					gun.setDamageValue(gun.getDamageValue()+1);
					player.getCooldowns().addCooldown(this,10);
				}
			}else player.displayClientMessage(Component.literal("§c[!] Out of energy"),true);
		}
		return InteractionResultHolder.success(gun);
	}
	private boolean firePortal(ServerLevel world,Player player,boolean isBlue){
		var start=player.getEyePosition();
		var hit=world.clip(new ClipContext(start,start.add(player.getLookAngle().scale(128.0)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
		if(hit.getType()!=HitResult.Type.BLOCK) return false;
		Direction face=hit.getDirection();
		BlockPos pos=hit.getBlockPos().relative(face);
		Direction extDir=(face.getAxis()==Direction.Axis.Y)?player.getDirection():Direction.UP;
		BlockPos extPos=pos.relative(extDir);
		boolean space=world.isEmptyBlock(pos)&&world.isEmptyBlock(extPos);
		boolean support=world.getBlockState(pos.relative(face.getOpposite())).isFaceSturdy(world,pos.relative(face.getOpposite()),face)&&
				world.getBlockState(extPos.relative(face.getOpposite())).isFaceSturdy(world,extPos.relative(face.getOpposite()),face);
		if(!space||!support){
			player.displayClientMessage(Component.literal("§c[!] Invalid placement"),true);
			return false;
		}
		PortalBlockEntity.removeOldPortal(world,player.getUUID(),isBlue);
		PortalBlockEntity.savePortal(world,player.getUUID(),isBlue,pos);
		var state=DifModBlocks.PORTAL_BLOCK.get().defaultBlockState().setValue(PortalBlock.FACING,face).setValue(PortalBlock.EXTENSION_DIR,extDir).setValue(PortalBlock.IS_BLUE,isBlue);
		world.setBlock(pos,state.setValue(PortalBlock.HALF,DoubleBlockHalf.LOWER),3);
		world.setBlock(extPos,state.setValue(PortalBlock.HALF,DoubleBlockHalf.UPPER),3);
		if(world.getBlockEntity(pos) instanceof PortalBlockEntity be) be.setup(player.getUUID(),isBlue,face);
		return true;
	}
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack itemStack,Enchantment enchantment){
		return false;
	}
	@Override
	public boolean isEnchantable(@NotNull ItemStack itemStack){
		return false;
	}
	@Override
	public int getEnchantmentValue(){
		return 0;
	}
	@Override
	public boolean isBookEnchantable(@NotNull ItemStack itemStack, @NotNull ItemStack book){
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