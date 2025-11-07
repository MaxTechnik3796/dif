package cz.maxtechnik.dif.init.special;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.LootTableLoadEvent;

@Mod.EventBusSubscriber(modid=DifMod.MODID,bus=Mod.EventBusSubscriber.Bus.FORGE)
public class DifModDataEvents{
	private static final ResourceLocation CHERRY_LEAVES_ID=ResourceLocation.fromNamespaceAndPath("minecraft","blocks/cherry_leaves");

	@SubscribeEvent
	public static void onLootTableLoad(LootTableLoadEvent event){
		if(event.getName().equals(CHERRY_LEAVES_ID)){
			LootItemCondition.Builder withoutSilkTouch=MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new net.minecraft.advancements.critereon.EnchantmentPredicate(Enchantments.SILK_TOUCH,net.minecraft.advancements.critereon.MinMaxBounds.Ints.atLeast(1)))).invert();
			LootItemCondition.Builder withoutShears=MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS)).invert();
			LootItemCondition.Builder fivePercentChance=LootItemRandomChanceCondition.randomChance(0.05F);
			LootPool newPool=LootPool.lootPool().name("dif_cherry_leaves_cherry_pool")
					.setRolls(ConstantValue.exactly(1F))
					.when(withoutSilkTouch)
					.when(withoutShears)
					.when(fivePercentChance)
					.add(LootItem.lootTableItem(DifModItems.CHERRY.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)))).build();
			LootTable originalTable=event.getTable();
			originalTable.addPool(newPool);
		}
	}
}
