package cz.maxtechnik.dif.item;

import cz.maxtechnik.dif.block.entity.MonitorBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
public class CameraLinkerItem extends Item {
    public CameraLinkerItem(Properties properties) { super(properties); }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();

        // 1. Kliknutí na kameru
        if (context.getLevel().getBlockState(pos).is(DifModBlocks.CAMERA.get())) {
            tag.putLong("LinkedPos", pos.asLong());
			assert context.getPlayer()!=null;
	        context.getPlayer().displayClientMessage(Component.literal("Camera linked to tool!"), true);
            return InteractionResult.SUCCESS;
        }

        // 2. Kliknutí na monitor
        if (context.getLevel().getBlockState(pos).is(DifModBlocks.CAMERA_MONITOR.get())) {
            if (tag.contains("LinkedPos")) {
                if (context.getLevel().getBlockEntity(pos) instanceof MonitorBlockEntity monitor) {
                    monitor.linkCamera(BlockPos.of(tag.getLong("LinkedPos")));
					assert context.getPlayer()!=null;
	                context.getPlayer().displayClientMessage(Component.literal("Link established!"), true);
                    tag.remove("LinkedPos");
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}