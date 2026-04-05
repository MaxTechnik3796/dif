package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.init.events.DivineDamageUtils;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BanHammer extends Item {

	// UUID pro atributy (aby se nepraly s ostatními itemy)
	private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
	private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

	public BanHammer() {
		super(new Item.Properties().stacksTo(1).fireResistant());
	}

	// Tato metoda zajistí "Božské" chování při kliknutí (Ban + 100% DMG + Křupnutí totemu)
	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
		if (entity instanceof LivingEntity target) {
			// Zavolá naši společnou logiku (true = je to ban hammer)
			DivineDamageUtils.applyDivineDamage(target, player, true);
			return true; // Zruší běžný útok Minecraftu, aby proběhl jen náš kód
		}
		return false;
	}

	// Zobrazení obrovského poškození v tooltipu (jako jsi měl v původním souboru)
	@Override
	public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot slot) {
		if (slot == EquipmentSlot.MAINHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(DAMAGE_MODIFIER_UUID, "Weapon modifier", 9999.0D, AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(SPEED_MODIFIER_UUID, "Weapon modifier", -2.4D, AttributeModifier.Operation.ADDITION));
			return builder.build();
		}
		return super.getDefaultAttributeModifiers(slot);
	}
}