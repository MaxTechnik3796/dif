package cz.maxtechnik.dif.init.other;
 
import net.minecraft.resources.ResourceLocation;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DifModTiers{
    public static final Tier COPPER = new Tier() {
        @Override
        public int getUses() {return 190;}
		@Override
        public float getSpeed() {return 5.0F;}
		@Override
        public float getAttackDamageBonus() {return 2.0F;}
		@Override
		public int getLevel() {return 1;}
        @Override
		public int getEnchantmentValue() {return 13;}
		@Override
        public Ingredient getRepairIngredient() {return Ingredient.of(Items.COPPER_INGOT);}
    };

    public static final Holder<ArmorMaterial> ARMOR_MATERIAL = Holder.direct(new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 2);
                map.put(ArmorItem.Type.CHESTPLATE, 4);
                map.put(ArmorItem.Type.LEGGINGS, 3);
                map.put(ArmorItem.Type.BOOTS, 1);
            }),
            8,
            SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(Items.COPPER_INGOT),
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath("dif", "copper"))),
            0.0F,
            0.0F
    ));
}
