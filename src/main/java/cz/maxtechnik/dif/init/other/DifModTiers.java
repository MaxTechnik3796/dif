package cz.maxtechnik.dif.init.other;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
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
        public @NotNull Ingredient getRepairIngredient() {return Ingredient.of(Items.COPPER_INGOT);}
    };
    public static final ArmorMaterial ARMOR_MATERIAL = new ArmorMaterial() {
        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
			return switch(type){
				case HELMET -> 121;
				case CHESTPLATE -> 176;
				case LEGGINGS -> 165;
				case BOOTS -> 143;
			};}
        @Override
        public int getDefenseForType(ArmorItem.Type type) {
			return switch(type){
				case HELMET -> 2;
				case CHESTPLATE -> 4;
				case LEGGINGS -> 3;
				case BOOTS -> 1;
			};}
        @Override
		public int getEnchantmentValue() {return 8;}
        @Override
        public @NotNull SoundEvent getEquipSound() {return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath("minecraft","item.armor.equip_iron")));}
        @Override
        public @NotNull Ingredient getRepairIngredient() {return Ingredient.of(Items.COPPER_INGOT);}
        @Override
        public @NotNull String getName() {return "dif:copper";}
        @Override
        public float getToughness() {return 0f;}
        @Override
        public float getKnockbackResistance() {return 0f;}
    };
}
