package cz.maxtechnik.dif.item.armor;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.List;
@SuppressWarnings("removal")
@EventBusSubscriber(bus=EventBusSubscriber.Bus.MOD)
public abstract class CarbonSuit extends ArmorItem{
	public static Holder<ArmorMaterial> ARMOR_MATERIAL=null;
	@SubscribeEvent
	public static void registerArmorMaterial(RegisterEvent event){
		event.register(Registries.ARMOR_MATERIAL,helper->{
			ArmorMaterial material=new ArmorMaterial(
					Util.make(new EnumMap<>(ArmorItem.Type.class),map->{
						map.put(ArmorItem.Type.HELMET,4);
						map.put(ArmorItem.Type.CHESTPLATE,9);
						map.put(ArmorItem.Type.LEGGINGS,7);
						map.put(ArmorItem.Type.BOOTS,4);
					}),
					15,
					SoundEvents.ARMOR_EQUIP_DIAMOND,
					Ingredient::of,
					List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("dif","carbon_suit"))),
					3.0F,
					0.2F
			);
			helper.register(ResourceLocation.fromNamespaceAndPath("dif","carbon_suit"),material);
			ARMOR_MATERIAL=BuiltInRegistries.ARMOR_MATERIAL.wrapAsHolder(material);
		});
	}
	public CarbonSuit(ArmorItem.Type type,Item.Properties properties){
		super(ARMOR_MATERIAL,type,properties.stacksTo(1));
	}
	@Override
	public boolean isEnchantable(@NotNull ItemStack stack){
		return true;
	}
	@Override
	public int getEnchantmentValue(){
		return 15;
	}
	public static class Helmet extends CarbonSuit{
		public Helmet(){
			super(ArmorItem.Type.HELMET,new Item.Properties());
		}
	}
	public static class Chestplate extends CarbonSuit{
		public Chestplate(){
			super(ArmorItem.Type.CHESTPLATE,new Item.Properties());
		}
	}
	public static class Leggings extends CarbonSuit{
		public Leggings(){
			super(ArmorItem.Type.LEGGINGS,new Item.Properties());
		}
	}
	public static class Boots extends CarbonSuit{
		public Boots(){
			super(ArmorItem.Type.BOOTS,new Item.Properties());
		}
	}
}