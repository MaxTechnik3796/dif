package cz.maxtechnik.dif.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PhantomRing extends Item {
    public PhantomRing() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }
    @Override
    public void appendHoverText(@NotNull ItemStack itemStack,@Nullable Level level,List<Component> list,@NotNull TooltipFlag flag){
        list.add(Component.literal("Prevents phantoms from spawning").withStyle(ChatFormatting.GRAY));
    }
}