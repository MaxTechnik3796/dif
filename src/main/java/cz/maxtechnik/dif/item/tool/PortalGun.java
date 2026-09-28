package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.entity.portal.PortalData;
import cz.maxtechnik.dif.entity.portal.PortalEntity;
import cz.maxtechnik.dif.entity.portal.PortalPlacement;
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
			if(amount<=0) return FluidStack.EMPTY;
			return new FluidStack(DifModFluids.ASTRAGEL.source.get(),amount);
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
			FluidStack current=getFluidInTank(0);
			int space=this.capacity-current.getAmount();
			if(space<=0) return 0;
			int toFill=Math.min(space,resource.getAmount());
			if(action.execute()){
				int newAmount=current.getAmount()+toFill;
				CustomData.update(DataComponents.CUSTOM_DATA,container,tag->tag.putInt("astragel_amount",newAmount));
			}
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
			FluidStack current=getFluidInTank(0);
			if(current.isEmpty()) return FluidStack.EMPTY;
			int toDrain=Math.min(current.getAmount(),maxDrain);
			if(action.execute()){
				int newAmount=current.getAmount()-toDrain;
				CustomData.update(DataComponents.CUSTOM_DATA,container,tag->{
					if(newAmount>0) tag.putInt("astragel_amount",newAmount);
					else tag.remove("astragel_amount");
				});
			}
			return new FluidStack(DifModFluids.ASTRAGEL.source.get(),toDrain);
		}
	}
	private boolean isBlueMode(ItemStack gun){
		CustomData data=gun.get(DataComponents.CUSTOM_DATA);
		CompoundTag tag=data!=null?data.copyTag():new CompoundTag();
		return !tag.contains("mode")||tag.getBoolean("mode");
	}
	private void setMode(ItemStack gun,boolean blue){
		CustomData.update(DataComponents.CUSTOM_DATA,gun,tag->tag.putBoolean("mode",blue));
		gun.set(DataComponents.CUSTOM_MODEL_DATA,new CustomModelData(blue?0:1));
	}
	public static int getAstragelAmount(ItemStack gun){
		IFluidHandlerItem handler=gun.getCapability(Capabilities.FluidHandler.ITEM);
		if(handler==null) return 0;
		return handler.getFluidInTank(0).getAmount();
	}
	public static void consumeAstragel(ItemStack gun,int amount){
		IFluidHandlerItem handler=gun.getCapability(Capabilities.FluidHandler.ITEM);
		if(handler==null) return;
		FluidStack drainedSimulated=handler.drain(amount,IFluidHandler.FluidAction.SIMULATE);
		if(drainedSimulated.getAmount()<amount) return;
		handler.drain(amount,IFluidHandler.FluidAction.EXECUTE);
	}
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world,Player player,@NotNull InteractionHand hand){
		ItemStack gun=player.getItemInHand(hand);
		boolean isBlue=isBlueMode(gun);
		int energy=getAstragelAmount(gun);
		if(player.isCrouching()){
			if(!world.isClientSide){
				boolean mode=!isBlue;
				setMode(gun,mode);
				player.displayClientMessage(Component.translatable("info.dif.portal_gun.mode").append(": ").append(Component.translatable(mode?"info.dif.portal_gun.blue":"info.dif.portal_gun.orange")),true);
			}
			return InteractionResultHolder.sidedSuccess(gun,world.isClientSide());
		}
		if(!world.isClientSide){
			if(energy>=1||player.isCreative()){
				if(firePortal((ServerLevel)world,player,isBlue)){
					if(!player.isCreative()) consumeAstragel(gun,1);
					player.getCooldowns().addCooldown(this,10);
				}
			}else player.displayClientMessage(Component.translatable("info.dif.portal_gun.out_of_astragel"),true);
		}
		return InteractionResultHolder.success(gun);
	}
	private boolean firePortal(ServerLevel world,Player player,boolean isBlue){
		Vec3 eye=player.getEyePosition();
		var hit=world.clip(new ClipContext(eye,eye.add(player.getLookAngle().scale(128)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
		if(hit.getType()!=HitResult.Type.BLOCK) return false;
		Direction face=hit.getDirection();
		BlockPos hitPos=hit.getBlockPos();
		Direction extDir=(face.getAxis()==Direction.Axis.Y)?player.getDirection():Direction.UP;
		Vec3 spawnPos=PortalPlacement.align(world,hitPos,face,extDir,hit.getLocation());
		if(spawnPos==null){
			player.displayClientMessage(Component.translatable("info.dif.portal_gun.invalid_placement"),true);
			return false;
		}
		PortalEntity portal=new PortalEntity(world,player.getUUID(),isBlue,face,extDir,spawnPos);
		if(PortalPlacement.hasOverlap(world,portal.getBoundingBox(),player.getUUID(),isBlue)){
			player.displayClientMessage(Component.translatable("info.dif.portal_gun.invalid_position"),true);
			return false;
		}
		PortalData data=PortalData.get(world);
		data.removeOldPortal(world,player.getUUID(),isBlue);
		data.set(player.getUUID(),isBlue,portal.blockPosition());
		world.addFreshEntity(portal);
		return true;
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
		IFluidHandlerItem handler=stack.getCapability(Capabilities.FluidHandler.ITEM);
		if(handler==null) return 0;
		int capacity=DifModServerConfig.PORTAL_GUN_CAPACITY.get();
		if(capacity<=0) return 0;
		FluidStack fluid=handler.getFluidInTank(0);
		int amount=fluid.getAmount();
		return Math.clamp(Math.round(13F*amount/(float)capacity),0,13);
	}
	@Override
	public int getBarColor(@NotNull ItemStack itemStack){
		return isBlueMode(itemStack)?0x00EBFF:0xFFBB00;
	}
}