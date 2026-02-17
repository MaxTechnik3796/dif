package cz.maxtechnik.dif.item.armor;

import cz.maxtechnik.dif.DifModCommonConfig;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.model.ModelJetpack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
public abstract class Jetpack extends ArmorItem{
	String texture="dif:textures/models/armor/jetpack.png";
	public Jetpack(ArmorItem.Type type,Item.Properties properties){
		super(new ArmorMaterial(){
			@Override
			public int getDurabilityForType(@NotNull ArmorItem.Type type){
				return new int[]{0,2,0,0}[type.getSlot().getIndex()]*37;
			}
			@Override
			public int getDefenseForType(@NotNull ArmorItem.Type type){
				return new int[]{0,2,0,0}[type.getSlot().getIndex()];
			}
			@Override
			public int getEnchantmentValue(){
				return 0;
			}
			@Override
			public @NotNull SoundEvent getEquipSound(){
				return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath("minecraft","item.armor.equip_iron")));
			}
			@Override
			public @NotNull Ingredient getRepairIngredient(){
				return Ingredient.of();
			}
			@Override
			public @NotNull String getName(){return "jetpack";}
			@Override
			public float getToughness(){return 2F;}
			@Override
			public float getKnockbackResistance(){return 0F;}
		},type,properties);
	}
	public static class Chestplate extends Jetpack{
		public Chestplate(){
			super(Type.CHESTPLATE,new Item.Properties().stacksTo(1));
		}
		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer) {
			consumer.accept(new IClientItemExtensions() {
				@Override
				@OnlyIn(Dist.CLIENT)
				public HumanoidModel getHumanoidArmorModel(LivingEntity living,ItemStack stack,EquipmentSlot slot,HumanoidModel defaultModel) {
					HumanoidModel armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(),
							Map.of("body", new ModelJetpack(Minecraft.getInstance().getEntityModels().bakeLayer(ModelJetpack.LAYER_LOCATION)).Body,
									"left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
									"left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
					armorModel.crouching = living.isShiftKeyDown();
					armorModel.riding = defaultModel.riding;
					armorModel.young = living.isBaby();
					return armorModel;
				}
			});
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type) {
			return texture;
		}
		@Override
		public @Nullable SoundEvent getEquipSound() {
			return null;
		}
		public static int getMainFuel(ItemStack itemStack){
			if(!itemStack.hasTag()||!Objects.requireNonNull(itemStack.getTag()).contains("MainFuel")) return 0;
			return itemStack.getTag().getInt("MainFuel");
		}
		public static void setMainFuel(ItemStack itemStack,int value){
			assert itemStack.getTag()!=null;
			itemStack.getTag().putInt("MainFuel",value);
		}
		public static int getThrustFuel(ItemStack itemStack){
			if(!itemStack.hasTag()||!Objects.requireNonNull(itemStack.getTag()).contains("ThrustFuel")) return 0;
			return itemStack.getTag().getInt("ThrustFuel");
		}
		public static void setThrustFuel(ItemStack itemStack,int value){
			assert itemStack.getTag()!=null;
			itemStack.getTag().putInt("ThrustFuel",value);
		}
		public static boolean getTurbo(ItemStack itemStack){
			if(!itemStack.hasTag()||!Objects.requireNonNull(itemStack.getTag()).contains("Turbo")) return false;
			return itemStack.getTag().getBoolean("Turbo");
		}
		public static void setTurbo(ItemStack itemStack,boolean value){
			assert itemStack.getTag()!=null;
			itemStack.getTag().putBoolean("Turbo",value);
		}
		@Override
		public int getBarWidth(@NotNull ItemStack itemStack){
			assert itemStack.getTag()!=null;
			return Math.round(13F*(float)getMainFuel(itemStack)/(float)DifModCommonConfig.jetpackMaxBasic);
		}
		@Override
		public int getBarColor(@NotNull ItemStack itemStack){
			assert itemStack.getTag()!=null;
			float f=Math.max(0,(float)getMainFuel(itemStack)/(float)DifModCommonConfig.jetpackMaxBasic);
			return Mth.hsvToRgb(f*0.33F,1F,1F);
		}
		@Override
		public void appendHoverText(@NotNull ItemStack itemStack,@Nullable Level world,@NotNull List<Component> list,@NotNull TooltipFlag flag){
			list.add(Component.literal("Main Storage: "+getMainFuel(itemStack)+" / "+DifModCommonConfig.jetpackMaxBasic).withStyle(ChatFormatting.GRAY));
			list.add(Component.literal("Thrust Tank: "+getThrustFuel(itemStack)+" / "+(getTurbo(itemStack)?DifModCommonConfig.jetpackMaxTurbo:DifModCommonConfig.jetpackMaxThrust)).withStyle(ChatFormatting.AQUA));
			if(getTurbo(itemStack)) list.add(Component.literal("TURBO").withStyle(ChatFormatting.RED));
		}
		public static boolean isFuel(ItemStack itemStack){
			return itemStack.getItem().equals(DifModItems.JETPACK_FUEL.get());
		}
		public static boolean isTurboFuel(ItemStack itemStack){
			return itemStack.getItem().equals(DifModItems.JETPACK_TURBO_FUEL.get());
		}
		@Override
		public boolean isEnchantable(@NotNull ItemStack itemStack){
			return false;
		}
		@Override
		public boolean isBookEnchantable(ItemStack itemStack,ItemStack book){
			return false;
		}
		@Override
		public boolean shouldCauseReequipAnimation(ItemStack oldStack,ItemStack newStack,boolean slotChanged){
			return slotChanged||oldStack.getItem()!=newStack.getItem();
		}
		@Override
		public boolean isBarVisible(@NotNull ItemStack itemStack){
			return true;
		}
	}
}




