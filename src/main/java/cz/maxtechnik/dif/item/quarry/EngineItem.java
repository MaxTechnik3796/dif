package cz.maxtechnik.dif.item.quarry;

import net.minecraft.world.item.Item;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class EngineItem extends Item {
    public final int dpGen;
    public final int feCost;

    public EngineItem(int dpGen, int feCost) {
        super(new Item.Properties().stacksTo(1));
        this.dpGen = dpGen;
        this.feCost = feCost;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag) {
        tooltips.add(Component.literal("§7Drill Power Generated: §a+" + dpGen + " DP"));
        tooltips.add(Component.literal("§7Energy Consumption: §e-" + feCost + " FE/t"));
        super.appendHoverText(stack, level, tooltips, flag);
    }
}
