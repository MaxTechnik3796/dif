package cz.maxtechnik.dif.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries; // POTŘEBNÝ IMPORT
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments; // POTŘEBNÝ IMPORT
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class AddMeatLootModifier extends LootModifier {
	public static final MapCodec<AddMeatLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(
			inst.group(
					BuiltInRegistries.ITEM.byNameCodec().fieldOf("rawItem").forGetter(m -> m.rawItem),
					BuiltInRegistries.ITEM.byNameCodec().fieldOf("cookedItem").forGetter(m -> m.cookedItem)
			)
	).apply(inst, AddMeatLootModifier::new));

	private final Item rawItem;
	private final Item cookedItem;

	public AddMeatLootModifier(LootItemCondition[] conditionsIn, Item rawItem, Item cookedItem) {
		super(conditionsIn);
		this.rawItem = rawItem;
		this.cookedItem = cookedItem;
	}

	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);

		// 1. POJISTKA: Hříbata (miminka) zvířat v Minecraftu maso nedropují
		if (entity instanceof AgeableMob ageable && ageable.isBaby()) {
			return generatedLoot;
		}

		// 2. KONTROLA OHNĚ: Pokud entita uhořela, dropneme pečené maso
		Item itemToDrop = this.rawItem;
		if (entity != null && entity.isOnFire()) {
			itemToDrop = this.cookedItem;
		}

		// 3. POČET: Vygenerujeme standardní množství 1 až 3 kusy (jako u krav/ovcí)
		int count = context.getRandom().nextInt(3) + 1; // 1-3 ks

		// 4. LOOTING BONUS (FIX PRO 1.21.1): Bezpečné načtení Lootingu z hlavní ruky útočníka
		int lootingLevel = 0;
		Entity attacker = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
		if (attacker instanceof LivingEntity livingAttacker) {
			// Vytáhneme si lookup registr pro enchantmenty
			var enchantmentRegistry = context.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

			// Tady je ta změna: místo getUsedItemHand se ptáme rovnou na hlavní ruku
			lootingLevel = livingAttacker.getMainHandItem().getEnchantmentLevel(
					enchantmentRegistry.getOrThrow(Enchantments.LOOTING)
			);
		}

		if (lootingLevel > 0) {
			count += context.getRandom().nextInt(lootingLevel + 1);
		}

		generatedLoot.add(new ItemStack(itemToDrop, count));
		return generatedLoot;
	}

	@Override
	public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}