package cz.maxtechnik.dif.item.armor;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.config.DifModServerConfig;
import cz.maxtechnik.dif.init.fluid.DifModFluids;
import cz.maxtechnik.dif.init.other.DifModTiers;
import cz.maxtechnik.dif.model.ModelJetpack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
@SuppressWarnings("removal")
public abstract class Jetpack extends ArmorItem{
	public static int getCapacity(){
		return DifModServerConfig.JETPACK_CAPACITY.get();
	}
	public Jetpack(ArmorItem.Type type,Item.Properties properties){
		super(DifModTiers.ARMOR_MATERIAL_JETPACK,type,properties.stacksTo(1));
	}
	public static class Chestplate extends Jetpack{
		private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/models/armor/jetpack.png");
		public Chestplate(){
			super(Type.CHESTPLATE,new Item.Properties().stacksTo(1));
		}
		@Override
		public ResourceLocation getArmorTexture(@NotNull ItemStack stack,@NotNull Entity entity,@NotNull EquipmentSlot slot,ArmorMaterial.@NotNull Layer layer,boolean innerModel){
			return TEXTURE;
		}
		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer){
			consumer.accept(new IClientItemExtensions(){
				private HumanoidModel<?> armorModel;
				@Override
				@OnlyIn(Dist.CLIENT)
				public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity living,@NotNull ItemStack stack,@NotNull EquipmentSlot slot,@NotNull HumanoidModel<?> defaultModel){
					if(armorModel==null){
						ModelJetpack<LivingEntity> jetpackModel=new ModelJetpack<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelJetpack.LAYER_LOCATION));
						ModelPart empty=new ModelPart(Collections.emptyList(),Collections.emptyMap());
						armorModel=new HumanoidModel<>(new ModelPart(Collections.emptyList(),Map.of(
								"body",jetpackModel.Body,
								"left_arm",empty,"right_arm",empty,
								"head",empty,"hat",empty,
								"right_leg",empty,"left_leg",empty
						)));
					}
					armorModel.crouching=living.isShiftKeyDown();
					armorModel.riding=defaultModel.riding;
					armorModel.young=living.isBaby();
					return armorModel;
				}
			});
		}
		public static int getThrust(ItemStack itemStack){
			CustomData data=itemStack.get(DataComponents.CUSTOM_DATA);
			return data!=null?data.copyTag().getInt("Thrust"):0;
		}
		public static void setThrust(ItemStack itemStack,int value){
			itemStack.update(DataComponents.CUSTOM_DATA,CustomData.EMPTY,
					data->data.update(tag->tag.putInt("Thrust",Mth.clamp(value,0,getCapacity()))));
		}
		public static int getMax(){
			return getCapacity();
		}
		public static int getMode(ItemStack stack){
			CustomData data=stack.get(DataComponents.CUSTOM_DATA);
			return data!=null?data.copyTag().getInt("Mode"):0;
		}
		public static void setMode(ItemStack stack,int mode){
			stack.update(DataComponents.CUSTOM_DATA,CustomData.EMPTY,
					data->data.update(tag->tag.putInt("Mode",Mth.clamp(mode,0,2))));
		}
		public static boolean isHovering(ItemStack itemStack){
			return getMode(itemStack)==1;
		}
		public static boolean isOff(ItemStack itemStack){
			return getMode(itemStack)==2;
		}
		public static class FluidHandler implements IFluidHandlerItem{
			private final ItemStack container;
			public FluidHandler(ItemStack container){
				this.container=container;
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
				int amount=getThrust(container);
				return amount>0?new FluidStack(DifModFluids.JETPACK_FUEL.source.get(),amount):FluidStack.EMPTY;
			}
			@Override
			public int getTankCapacity(int tank){
				return getCapacity();
			}
			@Override
			public boolean isFluidValid(int tank,@NotNull FluidStack fluidStack){
				return fluidStack.getFluid()==DifModFluids.JETPACK_FUEL.source.get();
			}
			@Override
			public int fill(FluidStack resource,IFluidHandler.@NotNull FluidAction action){
				if(resource.isEmpty()||!isFluidValid(0,resource)) return 0;
				int current=getThrust(container);
				int accepted=Math.min(getCapacity()-current,resource.getAmount());
				if(accepted>0&&action.execute()) setThrust(container,current+accepted);
				return Math.max(0,accepted);
			}
			@Override
			public @NotNull FluidStack drain(FluidStack resource,IFluidHandler.@NotNull FluidAction action){
				if(resource.isEmpty()||!isFluidValid(0,resource)) return FluidStack.EMPTY;
				return drain(resource.getAmount(),action);
			}
			@Override
			public @NotNull FluidStack drain(int maxDrain,IFluidHandler.@NotNull FluidAction action){
				int current=getThrust(container);
				int drained=Math.min(current,maxDrain);
				if(drained<=0) return FluidStack.EMPTY;
				if(action.execute()) setThrust(container,current-drained);
				return new FluidStack(DifModFluids.JETPACK_FUEL.source.get(),drained);
			}
		}
		@Override
		public int getBarWidth(@NotNull ItemStack itemStack){
			return Math.round(13F*getThrust(itemStack)/Math.max(1F,getCapacity()));
		}
		@Override
		public int getBarColor(@NotNull ItemStack itemStack){
			float fill=Math.max(0F,(float)getThrust(itemStack)/Math.max(1F,getCapacity()));
			return Mth.hsvToRgb(fill*0.33F,1F,1F);
		}
		@Override
		public void appendHoverText(@NotNull ItemStack stack,@Nullable TooltipContext ctx,@NotNull List<Component> list,@NotNull TooltipFlag flag){
			list.add(Component.literal("Fuel: "+getThrust(stack)+" / "+getCapacity()+" mB").withStyle(ChatFormatting.AQUA));
			if(isOff(stack)) list.add(Component.literal("❌ OFF").withStyle(ChatFormatting.RED));
			else if(isHovering(stack)) list.add(Component.literal("⭐ HOVER").withStyle(ChatFormatting.GREEN));
		}
		@Override
		public boolean isEnchantable(@NotNull ItemStack stack){
			return false;
		}
		@Override
		public boolean isBarVisible(@NotNull ItemStack stack){
			return true;
		}
	}
}