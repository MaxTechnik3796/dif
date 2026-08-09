package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
@SuppressWarnings("unused")
@EventBusSubscriber(modid = DifMod.MODID)
public class BanHammer extends Item {

	public BanHammer() {
		super(new Item.Properties().stacksTo(1).fireResistant());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
		super.appendHoverText(stack, context, list, flag);

		list.add(Component.empty());

		// Header - System Override Glitch
		list.add(Component.literal("» SYSTEM OVERRIDE « ").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
				.append(Component.literal("ERR_BAN").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.RED)));

		// Obfuskovaný blikající řádek
		list.add(Component.literal("# ").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
				.append(Component.literal("###").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.DARK_RED))
				.append(Component.literal(" CEASE TO EXIST ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
				.append(Component.literal("###").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.DARK_RED))
				.append(Component.literal(" #").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)));

		list.add(Component.empty());

		// English Lore Text
		list.add(Component.literal("\"You were never ").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC)
				.append(Component.literal("here.\"").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC, ChatFormatting.BOLD)));
		list.add(Component.empty());

		// Modifier Header & Attack Damage
		list.add(Component.translatable("item.modifiers.mainhand").withStyle(ChatFormatting.BLUE));
		list.add(Component.literal(" ")
				.append(Component.literal("∞ ").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
				.append(Component.translatable("attribute.name.generic.attack_damage").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)));

		// Extra Ban Status
		list.add(Component.literal(" ")
				.append(Component.literal("⚠ ").withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal("PERMANENT BAN: ").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD))
				.append(Component.literal("INSTANT").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
	}

	@Override
	public boolean onLeftClickEntity(@NotNull ItemStack stack, @NotNull Player attacker, @NotNull Entity entity) {
		executeBanHammerEffect(attacker, entity);
		return true;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onAttackEntity(AttackEntityEvent event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide && player.getMainHandItem().is(DifModItems.BAN_HAMMER.get())) {
			executeBanHammerEffect(player, event.getTarget());
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		handleRaycastAttack(event.getEntity());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		handleRaycastAttack(event.getEntity());
	}

	private static void handleRaycastAttack(Player player) {
		if (player != null && !player.level().isClientSide && player.getMainHandItem().is(DifModItems.BAN_HAMMER.get())) {
			Entity raycastedTarget = getTargetEntity(player);
			if (raycastedTarget != null) {
				executeBanHammerEffect(player, raycastedTarget);
			}
		}
	}

	public static void executeBanHammerEffect(Player attacker, Entity target) {
		if (attacker == null || attacker.level().isClientSide || target == null) return;

		// 1. Zrušení God Totemu & Ban příkaz pro hráče
		if (target instanceof Player targetPlayer) {
			ItemStack main = targetPlayer.getMainHandItem();
			ItemStack off = targetPlayer.getOffhandItem();
			if (main.is(DifModItems.GOD_TOTEM.get())) {
				targetPlayer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
				targetPlayer.level().broadcastEntityEvent(targetPlayer, (byte) 35);
			}
			if (off.is(DifModItems.GOD_TOTEM.get())) {
				targetPlayer.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
				targetPlayer.level().broadcastEntityEvent(targetPlayer, (byte) 35);
			}

			MinecraftServer server = targetPlayer.getServer();
			if (server != null) {
				String name = targetPlayer.getGameProfile().getName();
				server.getCommands().performPrefixedCommand(
						server.createCommandSourceStack(),
						"ban " + name
				);
			}

			targetPlayer.getAbilities().invulnerable = false;
			targetPlayer.onUpdateAbilities();
		}

		// 2. Zrušení jakékoliv imunity entitě
		target.setInvulnerable(false);

		// 3. Extrémní Void poškození
		DamageSource divineSource = attacker.level().damageSources().source(DamageTypes.FELL_OUT_OF_WORLD, attacker);
		target.hurt(divineSource, Float.MAX_VALUE);

		if (target instanceof LivingEntity living) {
			living.setHealth(0.0f);
			living.die(divineSource);
		}

		// 4. Absolutní odstranění/zničení ze světa i pro nehitable entitu
		if (target.isAlive() || !target.isRemoved()) {
			target.remove(Entity.RemovalReason.KILLED);
			target.discard();
		}
	}

	private static Entity getTargetEntity(Player player) {
		Vec3 eyePos = player.getEyePosition();
		Vec3 viewVec = player.getViewVector(1.0F);
		Vec3 reachVec = eyePos.add(viewVec.scale(6.0));
		AABB box = player.getBoundingBox().expandTowards(viewVec.scale(6.0)).inflate(1.0);

		Entity closest = null;
		double closestDistance = 6.0 * 6.0;

		for (Entity entity : player.level().getEntities(player, box, e -> e != player)) {
			AABB entityBox = entity.getBoundingBox().inflate(entity.getPickRadius());
			Optional<Vec3> hit = entityBox.clip(eyePos, reachVec);
			if (entityBox.contains(eyePos)) {
				return entity;
			} else if (hit.isPresent()) {
				double distSq = eyePos.distanceToSqr(hit.get());
				if (distSq < closestDistance) {
					closest = entity;
					closestDistance = distSq;
				}
			}
		}
		return closest;
	}
}