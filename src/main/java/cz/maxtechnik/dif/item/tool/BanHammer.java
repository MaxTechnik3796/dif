package cz.maxtechnik.dif.item.tool;

import net.minecraft.world.item.Item;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.EquipmentSlot;

import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap;
import org.jetbrains.annotations.NotNull;

public class BanHammer extends Item{
	public BanHammer(){
		super(new Item.Properties().stacksTo(1).fireResistant());
	}
	@Override
	public Multimap<Attribute,AttributeModifier>getDefaultAttributeModifiers(@NotNull EquipmentSlot equipmentSlot){
		if (equipmentSlot==EquipmentSlot.MAINHAND){
			ImmutableMultimap.Builder<Attribute,AttributeModifier>builder=ImmutableMultimap.builder();
			builder.putAll(super.getDefaultAttributeModifiers(equipmentSlot));
			builder.put(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,"Item modifier",Integer.MAX_VALUE,AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_UUID,"Item modifier",-1,AttributeModifier.Operation.ADDITION));
			return builder.build();
		}
		return super.getDefaultAttributeModifiers(equipmentSlot);
	}
}
