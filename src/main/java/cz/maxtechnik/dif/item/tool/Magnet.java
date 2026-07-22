package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.init.other.DifModComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Magnet extends Item {

    public Magnet() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    public static boolean isEnabled(ItemStack stack) {
        return stack.getOrDefault(DifModComponents.MAGNET_ENABLED.get(), false);
    }

    private static void setEnabled(ItemStack stack, boolean value) {
        stack.set(DifModComponents.MAGNET_ENABLED.get(), value);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            boolean newState = !isEnabled(stack);
            setEnabled(stack, newState);

            player.displayClientMessage(
                    Component.literal(newState ? "Magnet: ON" : "Magnet: OFF"),
                    true
            );

            level.playSound(null, player.blockPosition(),
                    newState ? SoundEvents.NOTE_BLOCK_CHIME.value() : SoundEvents.NOTE_BLOCK_BASS.value(),
                    SoundSource.PLAYERS, 0.6f, newState ? 1.4f : 1.0f);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return isEnabled(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack itemStack, Item.@NotNull TooltipContext context, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
        super.appendHoverText(itemStack, context, list, flag);

        boolean enabled = isEnabled(itemStack);
        list.add(Component.literal("Magnet: ")
                .append(Component.literal(enabled ? "ZAPNUTO" : "VYPNUTO")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED))
                .withStyle(ChatFormatting.GRAY));
        list.add(Component.literal("Přitahuje itemy do 5 bloků").withStyle(ChatFormatting.GRAY));
        list.add(Component.literal("Pravým klikem přepneš magnet").withStyle(ChatFormatting.DARK_GRAY));
    }
}