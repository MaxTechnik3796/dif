package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.entity.portal.PortalData;
import cz.maxtechnik.dif.entity.portal.PortalEntity;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
public class PortalGun extends Item{
	public PortalGun(Item.Properties properties){
		super(properties.stacksTo(1));
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world,Player player,@NotNull InteractionHand hand){
		ItemStack gun=player.getItemInHand(hand);
		boolean isBlue=isBlueMode(gun);
		if(player.isShiftKeyDown()){
			if(!world.isClientSide){
				setMode(gun,!isBlue);
				Component modeName=Component.translatable(!isBlue?"info.dif.portal_gun.blue":"info.dif.portal_gun.orange");
				player.displayClientMessage(Component.translatable("info.dif.portal_gun.mode").append(": ").append(modeName),true);
			}
			return InteractionResultHolder.sidedSuccess(gun,world.isClientSide());
		}
		if(!world.isClientSide&&world instanceof ServerLevel serverLevel){
			if(getAstragelAmount(gun)>=1||player.isCreative()){
				if(shootPortal(serverLevel,player,isBlue)){
					if(!player.isCreative()) consumeAstragel(gun,DifModServerConfig.PORTAL_GUN_CONSUMPTION.get());
					player.getCooldowns().addCooldown(this,10);
				}
			}else player.displayClientMessage(Component.translatable("info.dif.portal_gun.out_of_astragel"),true);
		}
		return InteractionResultHolder.success(gun);
	}
	private boolean shootPortal(ServerLevel world,Player player,boolean isBlue){
		Vec3 start=player.getEyePosition();
		Vec3 end=start.add(player.getLookAngle().scale(128));
		BlockHitResult hit=world.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
		if(hit.getType()!=HitResult.Type.BLOCK) return false;
		Direction face=hit.getDirection();
		BlockPos hitPos=hit.getBlockPos();
		Direction upDir=(face.getAxis()==Direction.Axis.Y)?player.getDirection():Direction.UP;
		Vec3 spawnPos=PortalEntity.findSpawnPos(world,hitPos,face,upDir,hit.getLocation());
		if(spawnPos==null){
			player.displayClientMessage(Component.translatable("info.dif.portal_gun.invalid_placement"),true);
			return false;
		}
		PortalEntity portal=new PortalEntity(world,player.getUUID(),isBlue,face,upDir,spawnPos);
		if(PortalEntity.hasOverlap(world,portal.getBoundingBox(),player.getUUID(),isBlue)){
			player.displayClientMessage(Component.translatable("info.dif.portal_gun.invalid_position"),true);
			return false;
		}
		PortalData data=PortalData.get(world);
		data.removeOldPortal(world,player.getUUID(),isBlue);
		data.set(player.getUUID(),isBlue,portal.blockPosition());
		world.addFreshEntity(portal);
		return true;
	}
	private boolean isBlueMode(ItemStack gun){
		CustomData data=gun.get(DataComponents.CUSTOM_DATA);
		if(data==null) return true;
		CompoundTag tag=data.copyTag();
		return !tag.contains("mode")||tag.getBoolean("mode");
	}
	private void setMode(ItemStack gun,boolean blue){
		CustomData.update(DataComponents.CUSTOM_DATA,gun,tag->tag.putBoolean("mode",blue));
		gun.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(blue?0:1));
	}
	public static int getAstragelAmount(ItemStack gun){
		IFluidHandlerItem handler=gun.getCapability(Capabilities.FluidHandler.ITEM);
		return handler!=null?handler.getFluidInTank(0).getAmount():0;
	}
	public static void consumeAstragel(ItemStack gun,int amount){
		IFluidHandlerItem handler=gun.getCapability(Capabilities.FluidHandler.ITEM);
		if(handler!=null&&handler.drain(amount,IFluidHandler.FluidAction.SIMULATE).getAmount()>=amount) handler.drain(amount,IFluidHandler.FluidAction.EXECUTE);
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
	public boolean isValidRepairItem(@NotNull ItemStack itemStack,@NotNull ItemStack stack){
		return false;
	}
	@Override
	public boolean isBarVisible(@NotNull ItemStack itemStack){
		return true;
	}
	@Override
	public int getBarWidth(@NotNull ItemStack stack){
		int capacity=DifModServerConfig.PORTAL_GUN_CAPACITY.get();
		if(capacity<=0) return 0;
		return Math.clamp(Math.round(13F*getAstragelAmount(stack)/(float)capacity),0,13);
	}
	@Override
	public int getBarColor(@NotNull ItemStack itemStack){
		return isBlueMode(itemStack)?0x00EBFF:0xFFBB00;
	}
	public static class FluidHandler implements IFluidHandlerItem{
		private final ItemStack container;
		private final int capacity;
		public FluidHandler(ItemStack container){
			this.container=container;
			this.capacity=DifModServerConfig.PORTAL_GUN_CAPACITY.get();
		}
		@Override
		public @NotNull ItemStack getContainer(){
			return container;
		}
		@Override
		public int getTanks(){
			return 1;
		}
		@Override
		public @NotNull FluidStack getFluidInTank(int tank){
			if(tank!=0) return FluidStack.EMPTY;
			CustomData customData=container.get(DataComponents.CUSTOM_DATA);
			if(customData==null) return FluidStack.EMPTY;
			int amount=customData.copyTag().getInt("astragel_amount");
			return amount>0?new FluidStack(DifModFluids.ASTRAGEL.source.get(),amount):FluidStack.EMPTY;
		}
		@Override
		public int getTankCapacity(int tank){
			return capacity;
		}
		@Override
		public boolean isFluidValid(int tank,@NotNull FluidStack fluidStack){
			return fluidStack.getFluid().equals(DifModFluids.ASTRAGEL.source.get());
		}
		@Override
		public int fill(FluidStack resource,@NotNull FluidAction action){
			if(resource.isEmpty()||!isFluidValid(0,resource)) return 0;
			int current=getFluidInTank(0).getAmount();
			int toFill=Math.min(capacity-current,resource.getAmount());
			if(toFill<=0) return 0;
			if(action.execute()) CustomData.update(DataComponents.CUSTOM_DATA,container,tag->tag.putInt("astragel_amount",current+toFill));
			return toFill;
		}
		@Override
		public @NotNull FluidStack drain(FluidStack resource,@NotNull FluidAction action){
			if(resource.isEmpty()||!isFluidValid(0,resource)) return FluidStack.EMPTY;
			return drain(resource.getAmount(),action);
		}
		@Override
		public @NotNull FluidStack drain(int maxDrain,@NotNull FluidAction action){
			if(maxDrain<=0) return FluidStack.EMPTY;
			int current=getFluidInTank(0).getAmount();
			int toDrain=Math.min(current,maxDrain);
			if(toDrain<=0) return FluidStack.EMPTY;
			if(action.execute()){
				int remaining=current-toDrain;
				CustomData.update(DataComponents.CUSTOM_DATA,container,tag->{
					if(remaining>0) tag.putInt("astragel_amount",remaining);
					else tag.remove("astragel_amount");
				});
			}
			return new FluidStack(DifModFluids.ASTRAGEL.source.get(),toDrain);
		}
	}
}