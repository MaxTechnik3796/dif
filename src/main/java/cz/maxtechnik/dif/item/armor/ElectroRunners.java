package cz.maxtechnik.dif.item.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
public abstract class ElectroRunners extends ArmorItem{
	String texture="dif:textures/models/armor/electro_runners.png";
	public ElectroRunners(Type type,Properties properties){
		super(new ArmorMaterial(){
			@Override
			public int getDurabilityForType(@NotNull Type type){
				return new int[]{0,0,0,0}[type.getSlot().getIndex()]*128;
			}
			@Override
			public int getDefenseForType(@NotNull Type type){
				return new int[]{0,0,0,0}[type.getSlot().getIndex()];
			}
			@Override
			public int getEnchantmentValue(){
				return 0;
			}
			@Override
			public @NotNull SoundEvent getEquipSound(){
				return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath("minecraft","item.armor.equip_diamond")));
			}
			@Override
			public @NotNull Ingredient getRepairIngredient(){
				return Ingredient.of();
			}
			@Override
			public @NotNull String getName(){
				return "electro_runners";
			}
			@Override
			public float getToughness(){
				return 0F;
			}
			@Override
			public float getKnockbackResistance(){
				return 0F;
			}
		},type,properties);
	}
	public static class Boots extends ElectroRunners{
		public static final int MAX=1000;
		private static final UUID S_MOD=UUID.fromString("a4e29252-1234-4567-890a-1234567890ab");
		private static final UUID H_MOD=UUID.fromString("b5f39363-1234-4567-890a-1234567890ac");
		private static final UUID A_MOD=UUID.fromString("c6d49474-1234-4567-890a-1234567890ad");
		public Boots(){
			super(Type.BOOTS,new Properties().defaultDurability(0).setNoRepair().stacksTo(1));
		}
		@Override
		public String getArmorTexture(ItemStack stack,Entity entity,EquipmentSlot slot,String type){
			return texture;
		}
		@Override
		public boolean isEnchantable(@NotNull ItemStack itemStack){return false;}
		@Override
		public int getEnchantmentValue(){return 0;}
		@Override
		public int getDefense(){return 0;}
		@Override
		public Multimap<Attribute,AttributeModifier> getAttributeModifiers(EquipmentSlot slot,ItemStack stack){
			ImmutableMultimap.Builder<Attribute,AttributeModifier> b=ImmutableMultimap.builder();
			if(slot==EquipmentSlot.FEET&&getEnergy(stack)>0){
				b.putAll(super.getAttributeModifiers(slot,stack));
				b.put(Attributes.ARMOR,new AttributeModifier(A_MOD,"E-Armor",2.0,AttributeModifier.Operation.ADDITION));
				b.put(Attributes.MOVEMENT_SPEED,new AttributeModifier(S_MOD,"E-Speed",0.20,AttributeModifier.Operation.MULTIPLY_TOTAL));
				b.put(ForgeMod.STEP_HEIGHT_ADDITION.get(),new AttributeModifier(H_MOD,"E-Step",1.0,AttributeModifier.Operation.ADDITION));
			}
			return b.build();
		}
		@Override
		public boolean isBarVisible(@NotNull ItemStack itemStack){return true;}
		@Override
		public int getBarWidth(@NotNull ItemStack itemStack){return Math.round(13.0F*getEnergy(itemStack)/MAX);}
		@Override
		public int getBarColor(@NotNull ItemStack itemStack){return 0x00FFFF;}
		public static int getEnergy(ItemStack itemStack){return itemStack.getCapability(ForgeCapabilities.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);}
		public static void extract(ItemStack itemStack,int amount){itemStack.getCapability(ForgeCapabilities.ENERGY).ifPresent(e->e.extractEnergy(amount,false));}
		@Override
		public void appendHoverText(@NotNull ItemStack itemStack,@Nullable Level world,List<Component> list,@NotNull TooltipFlag flag){
			list.add(Component.literal(getEnergy(itemStack)+" / "+MAX+" FE").withStyle(ChatFormatting.AQUA));
		}
		@Override
		public @Nullable ICapabilityProvider initCapabilities(ItemStack stack,@Nullable CompoundTag nbt){
			return new ICapabilityProvider(){
				private final LazyOptional<IEnergyStorage> helper=LazyOptional.of(()->new EnergyStorage(MAX){
					@Override
					public int getEnergyStored(){
						return stack.getOrCreateTag().getInt("Energy");
					}
					@Override
					public int extractEnergy(int max,boolean sim){
						int e=getEnergyStored(), ext=Math.min(e,max);
						if(!sim) stack.getOrCreateTag().putInt("Energy",e-ext);
						return ext;
					}
					@Override
					public int receiveEnergy(int max,boolean sim){
						int e=getEnergyStored(), rec=Math.min(MAX-e,max);
						if(!sim) stack.getOrCreateTag().putInt("Energy",e+rec);
						return rec;
					}
				});
				@Override
				public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> c,@Nullable Direction direction){
					return c==ForgeCapabilities.ENERGY?helper.cast():LazyOptional.empty();
				}
			};
		}
	}
}