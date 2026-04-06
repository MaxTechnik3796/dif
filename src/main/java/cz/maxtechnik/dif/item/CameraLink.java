package cz.maxtechnik.dif.item;

import cz.maxtechnik.dif.block.entity.CameraMonitorBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CameraLink extends Item {
	public CameraLink(Properties properties) {
		super(properties);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context) {
		BlockPos pos = context.getClickedPos();
		ItemStack stack = context.getItemInHand();
		CompoundTag tag = stack.getOrCreateTag();
		Level level = context.getLevel();
		Player player = context.getPlayer();

		if (player == null) return InteractionResult.PASS;

		// 1. Kliknutí na kameru - Uložení pozice
		if (level.getBlockState(pos).is(DifModBlocks.CAMERA.get())) {
			tag.putLong("LinkedPos", pos.asLong());
			player.displayClientMessage(Component.literal("Camera successfully selected!").withStyle(ChatFormatting.GREEN), true);
			return InteractionResult.SUCCESS;
		}

		// 2. Kliknutí na monitor - Propojení
		if (level.getBlockState(pos).is(DifModBlocks.CAMERA_MONITOR.get())) {
			if (tag.contains("LinkedPos")) {
				if (level.getBlockEntity(pos) instanceof CameraMonitorBlockEntity monitor) {
					monitor.linkCamera(BlockPos.of(tag.getLong("LinkedPos")));
					player.displayClientMessage(Component.literal("Camera successfully connected!").withStyle(ChatFormatting.AQUA), true);
					return InteractionResult.SUCCESS;
				}
			} else {
				player.displayClientMessage(Component.literal("Please, select Camera first!").withStyle(ChatFormatting.RED), true);
			}
		}
		return InteractionResult.PASS;
	}

	// 3. Shift + Klik do vzduchu - Resetování výběru
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level,Player player,@NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (player.isShiftKeyDown()) {
			CompoundTag tag = stack.getOrCreateTag();
			if (tag.contains("LinkedPos")) {
				tag.remove("LinkedPos");
				if (level.isClientSide) {
					player.displayClientMessage(Component.literal("Camera successfully unselected!").withStyle(ChatFormatting.YELLOW), true);
				}
				return InteractionResultHolder.success(stack);
			}
		}
		return InteractionResultHolder.pass(stack);
	}

	// 4. Tooltip - Zobrazení souřadnic vybrané kamery
	@Override
	public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains("LinkedPos")) {
			BlockPos linkedPos = BlockPos.of(tag.getLong("LinkedPos"));
			tooltip.add(Component.literal("Selected Camera: ")
					.append(Component.literal(linkedPos.getX() + " " + linkedPos.getY() + " " + linkedPos.getZ())
							.withStyle(ChatFormatting.AQUA)));
			tooltip.add(Component.literal("Shift + Right Click to deselect").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		} else {
			tooltip.add(Component.literal("No Camera selected").withStyle(ChatFormatting.RED));
			tooltip.add(Component.literal("Right Click on Camera for connection").withStyle(ChatFormatting.GRAY));
		}
		super.appendHoverText(stack, level, tooltip, flag);
	}
}