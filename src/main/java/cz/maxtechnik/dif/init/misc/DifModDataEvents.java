package cz.maxtechnik.dif.init.misc;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid=DifMod.MODID,bus=Mod.EventBusSubscriber.Bus.FORGE)
public class DifModDataEvents{
	public static final ResourceLocation CHERRY_LEAVES_ID=ResourceLocation.fromNamespaceAndPath("minecraft","blocks/cherry_leaves");
	public static final ResourceLocation CHISELED_BOOKSHELF_ID=ResourceLocation.fromNamespaceAndPath("minecraft","blocks/chiseled_bookshelf");
	public static ItemStack emerald(int count){
		return new ItemStack(Items.EMERALD,count);
	}
	@SubscribeEvent
	public static void onLootTableLoad(LootTableLoadEvent event){
		if(event.getName().equals(CHERRY_LEAVES_ID)){
			LootItemCondition.Builder withoutSilkTouch=MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH,MinMaxBounds.Ints.atLeast(1)))).invert();
			LootItemCondition.Builder withoutShears=MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS)).invert();
			LootItemCondition.Builder fivePercentChance=LootItemRandomChanceCondition.randomChance(0.05F);
			LootPool newPool=LootPool.lootPool().name("dif_cherry_leaves_cherry_pool")
					.setRolls(ConstantValue.exactly(1F))
					.when(withoutSilkTouch)
					.when(withoutShears)
					.when(fivePercentChance)
					.add(LootItem.lootTableItem(DifModItems.CHERRY.get()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1F)))).build();
			LootTable originalTable=event.getTable();
			originalTable.addPool(newPool);
		}else if(event.getName().equals(CHISELED_BOOKSHELF_ID)){
			LootTable table=LootTable.EMPTY;
			LootPool pool=LootPool.lootPool().name("dif_chiseled_bookshelf")
					.setRolls(ConstantValue.exactly(1F))
					.add(LootItem.lootTableItem(Blocks.CHISELED_BOOKSHELF.asItem()).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1F)))).build();
			table.addPool(pool);
			event.setTable(table);
		}
	}
	@SubscribeEvent
	public static void registerWanderingTrades(WandererTradesEvent event){
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.CLAIRDELUNE.get(),1),3,0,0F));
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.CREMEKA.get(),1),3,0,0F));
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.FURT_TA_STEJNA_HRA.get(),1),3,0,0F));
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.MATY_CREATE.get(),1),3,0,0F));
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.MATY_PADA_STREAM.get(),1),3,0,0F));
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.MAYONNAISE.get(),1),3,0,0F));
		event.getGenericTrades().add(new BasicItemListing(emerald(5),new ItemStack(DifModItems.REDSTONE.get(),1),3,0,0F));
	}
	@SubscribeEvent
	public static void addComposterItems(FMLCommonSetupEvent event){
		ComposterBlock.COMPOSTABLES.put(Blocks.BAMBOO.asItem(),0.4F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.MATA.get(),0.9F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.MATA_PLANT.get(),0.88F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.MATY_BLOCK.get(),1F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.CANOLA_PLANT.get(),1F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.CANOLA_SEEDS.get(),1F);
		ComposterBlock.COMPOSTABLES.put(DifModItems.CHERRY.get(),0.65F);
	}
	@SubscribeEvent
	public static void registerTrades(VillagerTradesEvent event){
		/*if(event.getType().equals(VillagerProfession.CARTOGRAPHER)){
			//event.getTrades().get(3).add(new TrialsMapTrade(12,12,5));
		}*/
	}
}
