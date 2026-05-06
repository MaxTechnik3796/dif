package cz.maxtechnik.dif.item;

import cz.maxtechnik.dif.block.entity.CameraMonitorBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

public class CameraLink extends Item{
	public CameraLink(Properties properties){
		super(properties);
	}
	@Override
	public @NotNull InteractionResult useOn(UseOnContext context){
		BlockPos pos=context.getClickedPos();
		ItemStack stack=context.getItemInHand();
		Level level=context.getLevel();
		Player player=context.getPlayer();
		if(player==null) return InteractionResult.PASS;
		// 1. Kliknutí na kameru - Uložení pozice
		if(level.getBlockState(pos).is(DifModBlocks.CAMERA.get())){
			stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, data -> data.update(tag -> tag.putLong("LinkedPos", pos.asLong())));
			player.displayClientMessage(Component.literal("Camera successfully selected!").withStyle(ChatFormatting.GREEN),true);
			return InteractionResult.SUCCESS;
		}
		// 2. Kliknutí na monitor - Propojení
		if(level.getBlockState(pos).is(DifModBlocks.CAMERA_MONITOR.get())){
			CustomData data = stack.get(DataComponents.CUSTOM_DATA);
			if(data != null && data.copyTag().contains("LinkedPos")){
				if(level.getBlockEntity(pos) instanceof CameraMonitorBlockEntity monitor){
					monitor.linkCamera(BlockPos.of(data.copyTag().getLong("LinkedPos")));
					player.displayClientMessage(Component.literal("Camera successfully connected!").withStyle(ChatFormatting.AQUA),true);
					return InteractionResult.SUCCESS;
				}
			}else{
				player.displayClientMessage(Component.literal("Please, select Camera first!").withStyle(ChatFormatting.RED),true);
			}
		}
		return InteractionResult.PASS;
	}
	// 3. Shift + Klik do vzduchu - Resetování výběru
	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level,Player player,@NotNull InteractionHand hand){
		ItemStack stack=player.getItemInHand(hand);
		if(player.isShiftKeyDown()){
			CustomData data = stack.get(DataComponents.CUSTOM_DATA);
			if(data != null && data.copyTag().contains("LinkedPos")){
				stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, d -> d.update(tag -> tag.remove("LinkedPos")));
				if(level.isClientSide){
					player.displayClientMessage(Component.literal("Camera successfully unselected!").withStyle(ChatFormatting.YELLOW),true);
				}
				return InteractionResultHolder.success(stack);
			}
		}
		return InteractionResultHolder.pass(stack);
	}
	// 4. Tooltip - Zobrazení souřadnic vybrané kamery
	@Override
	public void appendHoverText(@NotNull ItemStack stack,@Nullable Item.TooltipContext context,@NotNull List<Component> tooltip,@NotNull TooltipFlag flag){
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		if(data != null && data.copyTag().contains("LinkedPos")){
			BlockPos linkedPos=BlockPos.of(data.copyTag().getLong("LinkedPos"));
			tooltip.add(Component.literal("Selected Camera: ")
					.append(Component.literal(linkedPos.getX()+" "+linkedPos.getY()+" "+linkedPos.getZ())
							.withStyle(ChatFormatting.AQUA)));
			tooltip.add(Component.literal("Shift + Right Click to deselect").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC));
		}else{
			tooltip.add(Component.literal("No Camera selected").withStyle(ChatFormatting.RED));
			tooltip.add(Component.literal("Right Click on Camera for connection").withStyle(ChatFormatting.GRAY));
		}
	}
}