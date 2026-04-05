package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.init.basic.DifModItems;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
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

@SuppressWarnings("deprecation")
public class BanHammer extends Item {

	// UUID pro atributy, aby se nemíchaly s jinými itemy
	private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
	private static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

	public BanHammer() {
		super(new Item.Properties().stacksTo(1).fireResistant());
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player attacker, Entity entity) {
		if (!attacker.level().isClientSide && entity instanceof LivingEntity target) {
			// 1. ZÍSKÁNÍ DAMAGE SOURCE (Bypassuje vše)
			DamageSource divineSource = new DamageSource(
					target.level().registryAccess()
							.registryOrThrow(Registries.DAMAGE_TYPE)
							.getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD),
					attacker
			);
			// 2. KONTROLA GOD TOTEMU (Křupnutí)
			if (target instanceof Player targetPlayer) {
				ItemStack main = targetPlayer.getMainHandItem();
				ItemStack off = targetPlayer.getOffhandItem();
				if (main.getItem() == DifModItems.GOD_TOTEM.get() || off.getItem() == DifModItems.GOD_TOTEM.get()) {
					if (main.getItem() == DifModItems.GOD_TOTEM.get()) {
						targetPlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
					} else {
						targetPlayer.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
					}
					// Animace totemu
					targetPlayer.level().broadcastEntityEvent(targetPlayer, (byte) 35);
					return true; // První hit jen zničí totem
				}
			}
			// 3. SERVER BAN LOGIKA
			if (target instanceof Player targetPlayer) {
				MinecraftServer server = target.getServer();
				if (server != null) {
					String name = targetPlayer.getGameProfile().getName();
					// Provede ban přes konzoli
					server.getCommands().performPrefixedCommand(
							server.createCommandSourceStack(),
							"ban " + name + " Zabanován Božským Kladivem!"
					);
				}
			}
			// 4. ABSOLUTNÍ KILL (Bypass Avaritia & Lost Depths)
			target.setInvulnerable(false); // Vypne nesmrtelnost entity
			if (target instanceof Player targetPlayer) {
				targetPlayer.getAbilities().invulnerable = false; // Vypne nesmrtelnost hráče
				targetPlayer.onUpdateAbilities();
			}
			// Pokus o poškození (pro hroby/corpse mody)
			target.hurt(divineSource, Float.MAX_VALUE);
			// Pokud stále žije (Avaritia Armor), vynutíme konec
			if (target.isAlive()) {
				target.setHealth(0.0f);
				target.die(divineSource);
				// Pokud ani to nepomohlo, vymažeme ho ze světa
				if (target.isAlive()) {
					target.discard();
				}
			}
			return true;
		}
		return false;
	}
	// Zobrazení nekonečného damage v tooltipu
	@Override
	public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot slot) {
		if (slot == EquipmentSlot.MAINHAND) {
			ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
			builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 999999.0D, AttributeModifier.Operation.ADDITION));
			builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4D, AttributeModifier.Operation.ADDITION));
			return builder.build();
		}
		return super.getDefaultAttributeModifiers(slot);
	}
}