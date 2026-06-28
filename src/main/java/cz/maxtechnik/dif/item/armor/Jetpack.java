package cz.maxtechnik.dif.item.armor;

import cz.maxtechnik.dif.DifMod;
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
	// Kapacita nádrže v mB
	public static final int CAPACITY=16000;
	public Jetpack(ArmorItem.Type type,Item.Properties properties){
		super(DifModTiers.ARMOR_MATERIAL_JETPACK,type,properties.stacksTo(1));
	}
	public static class Chestplate extends Jetpack{
		public Chestplate(){
			super(Type.CHESTPLATE,new Item.Properties().stacksTo(1));
		}
		@Override
		public ResourceLocation getArmorTexture(@NotNull ItemStack stack,@NotNull Entity entity,@NotNull EquipmentSlot slot,ArmorMaterial.@NotNull Layer layer,boolean innerModel){
			return ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"textures/models/armor/jetpack.png");
		}
		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer){
			consumer.accept(new IClientItemExtensions(){
				@Override
				@OnlyIn(Dist.CLIENT)
				public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity living,@NotNull ItemStack stack,@NotNull EquipmentSlot slot,@NotNull HumanoidModel<?> defaultModel){
					ModelJetpack<LivingEntity> jetpackModel=new ModelJetpack<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelJetpack.LAYER_LOCATION));
					HumanoidModel<?> armorModel=new HumanoidModel<>(new ModelPart(Collections.emptyList(),
							Map.of("body",jetpackModel.Body,
									"left_arm",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"right_arm",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"head",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"hat",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"right_leg",new ModelPart(Collections.emptyList(),Collections.emptyMap()),
									"left_leg",new ModelPart(Collections.emptyList(),Collections.emptyMap()))));
					armorModel.crouching=living.isShiftKeyDown();
					armorModel.riding=defaultModel.riding;
					armorModel.young=living.isBaby();
					return armorModel;
				}
			});
		}
		// === Palivo (Thrust) v mB ===
		public static int getThrust(ItemStack stack){
			CustomData data=stack.get(DataComponents.CUSTOM_DATA);
			if(data==null||!data.copyTag().contains("Thrust")) return 0;
			return data.copyTag().getInt("Thrust");
		}
		public static void setThrust(ItemStack stack,int value){
			stack.update(DataComponents.CUSTOM_DATA,CustomData.EMPTY,
					data->data.update(tag->tag.putInt("Thrust",Mth.clamp(value,0,CAPACITY))));
		}
		public static int getMax(){
			return CAPACITY;
		}
		// === Stav jetpacku: 0=let, 1=hover, 2=vypnuto ===
		public static int getMode(ItemStack stack){
			CustomData data=stack.get(DataComponents.CUSTOM_DATA);
			if(data==null||!data.copyTag().contains("Mode")) return 0;
			return data.copyTag().getInt("Mode");
		}
		public static void setMode(ItemStack stack,int mode){
			stack.update(DataComponents.CUSTOM_DATA,CustomData.EMPTY,
					data->data.update(tag->tag.putInt("Mode",Mth.clamp(mode,0,2))));
		}
		public static boolean isHovering(ItemStack stack){
			return getMode(stack)==1;
		}
		public static boolean isOff(ItemStack stack){
			return getMode(stack)==2;
		}
		// === Fluid capability ===
		// Registruje se v DifMod.registerCapabilities přes Capabilities.FluidHandler.ITEM.
		// Spout (i jakýkoliv tank/stroj) tím pádem může jetpack plnit, fill() bere přesně
		// tolik mB kolik chybí.
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
				if(amount<=0) return FluidStack.EMPTY;
				return new FluidStack(DifModFluids.JETPACK_FUEL.get(),amount);
			}
			@Override
			public int getTankCapacity(int tank){
				return CAPACITY;
			}
			@Override
			public boolean isFluidValid(int tank,@NotNull FluidStack stack){
				return stack.getFluid()==DifModFluids.JETPACK_FUEL.get();
			}
			@Override
			public int fill(FluidStack resource,IFluidHandler.@NotNull FluidAction action){
				if(resource.isEmpty()||!isFluidValid(0,resource)) return 0;
				int current=getThrust(container);
				int accepted=Math.min(CAPACITY-current,resource.getAmount());
				if(accepted<=0) return 0;
				if(action.execute()) setThrust(container,current+accepted);
				return accepted;
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
				return new FluidStack(DifModFluids.JETPACK_FUEL.get(),drained);
			}
		}
		// Bar ukazuje naplnění
		@Override
		public int getBarWidth(@NotNull ItemStack stack){
			return Math.round(13F*(float)getThrust(stack)/(float)CAPACITY);
		}
		@Override
		public int getBarColor(@NotNull ItemStack stack){
			float f=Math.max(0,(float)getThrust(stack)/(float)CAPACITY);
			return Mth.hsvToRgb(f*0.33F,1F,1F);
		}
		@Override
		public void appendHoverText(@NotNull ItemStack stack,@Nullable TooltipContext ctx,@NotNull List<Component> list,@NotNull TooltipFlag flag){
			list.add(Component.literal("Fuel: "+getThrust(stack)+" / "+CAPACITY+" mB").withStyle(ChatFormatting.AQUA));
			if(isHovering(stack)){
				list.add(Component.literal("⭐ HOVER").withStyle(ChatFormatting.GREEN));
			}
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