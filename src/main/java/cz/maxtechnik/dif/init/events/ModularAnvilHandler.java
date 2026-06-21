package cz.maxtechnik.dif.init.events;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.modular.v2.ModularTool;
import cz.maxtechnik.dif.item.modular.v2.ModularToolProperties;
import cz.maxtechnik.dif.item.modular.v2.ModularTier;
import cz.maxtechnik.dif.init.other.DifModComponents;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

import java.util.Locale;
@SuppressWarnings("deprecation")
@EventBusSubscriber(modid = DifMod.MODID)
public class ModularAnvilHandler {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (left.getItem() instanceof ModularTool && right.is(Items.ENCHANTED_BOOK)) {
            ModularToolProperties props = left.get(DifModComponents.MODULAR_TOOL_PROPERTIES.get());
            if (props == null) return;
            
            ModularTier toolTier = ModularTier.byName(props.tier());
            boolean hasRare = toolTier.ordinal() >= ModularTier.RARE.ordinal();
            boolean hasEpic = toolTier.ordinal() >= ModularTier.EPIC.ordinal();


            String type = props.toolType().toLowerCase(Locale.ROOT);
            Item mockItem = switch (type) {
                case "pickaxe" -> Items.DIAMOND_PICKAXE;
                case "axe" -> Items.DIAMOND_AXE;
                case "shovel" -> Items.DIAMOND_SHOVEL;
                case "hoe" -> Items.DIAMOND_HOE;
                case "sword" -> Items.DIAMOND_SWORD;
                default -> null;
            };

            if (mockItem == null) return;

            ItemEnchantments leftEnchantments = left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            ItemEnchantments bookEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(right);

            ItemEnchantments.Mutable newEnchantments = new ItemEnchantments.Mutable(leftEnchantments);
            boolean addedAny = false;
            boolean needsEpic = false;
            int cost = 0;

            for (Object2IntMap.Entry<Holder<Enchantment>> entry : bookEnchantments.entrySet()) {
                Holder<Enchantment> enchantment = entry.getKey();
                int level = entry.getIntValue();

                if (enchantment.value().definition().supportedItems().contains(mockItem.builtInRegistryHolder())) {
                    boolean compatible = true;
                    for (Holder<Enchantment> existing : newEnchantments.keySet()) {
                        if (!existing.equals(enchantment) && !Enchantment.areCompatible(existing, enchantment)) {
                            compatible = false;
                            break;
                        }
                    }

                    if (compatible) {
                        // Vlastní seznam Epic enchantů: Fortune, Mending, Looting
                        if (enchantment.unwrapKey().isPresent()) {
                            String path = enchantment.unwrapKey().get().location().getPath();
                            if (path.equals("fortune") || path.equals("mending") || path.equals("looting")) {
                                needsEpic = true;
                            }
                        }

                        int currentLevel = newEnchantments.getLevel(enchantment);
                        int newLevel = currentLevel == level ? level + 1 : Math.max(currentLevel, level);
                        if (newLevel > enchantment.value().definition().maxLevel()) {
                            newLevel = enchantment.value().definition().maxLevel();
                        }

                        if (newLevel > currentLevel) {
                            newEnchantments.set(enchantment, newLevel);
                            addedAny = true;
                            cost += newLevel * 2;
                        }
                    }
                }
            }

            if (addedAny) {
                if (needsEpic && !hasEpic) {
                    ItemStack errorStack = left.copy();
                    errorStack.set(DataComponents.CUSTOM_NAME, Component.literal("Need Tier Epic").withStyle(ChatFormatting.RED));
                    event.setOutput(errorStack);
                    event.setCost(40);
                    return;
                }

                if (!hasRare) {
                    ItemStack errorStack = left.copy();
                    errorStack.set(DataComponents.CUSTOM_NAME, Component.literal("Need Tier Rare").withStyle(ChatFormatting.RED));
                    event.setOutput(errorStack);
                    event.setCost(40);
                    return;
                }
                ItemStack output = left.copy();
                output.set(DataComponents.ENCHANTMENTS, newEnchantments.toImmutable());
                
                event.setOutput(output);
                event.setCost(cost);
                event.setMaterialCost(1);
            }
        }
    }
}
