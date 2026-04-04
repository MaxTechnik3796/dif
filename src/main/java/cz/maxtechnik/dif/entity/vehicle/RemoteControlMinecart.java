package cz.maxtechnik.dif.entity.vehicle;

import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
public class RemoteControlMinecart extends AbstractMinecart{
    // Síla pohonu (kladná = vpřed, záporná = vzad, 0 = stojí)
    private double remotePushX;
    private double remotePushZ;

	public RemoteControlMinecart(EntityType<? extends AbstractMinecart> type, Level level) {
		super(type, level);
	}

	public RemoteControlMinecart(Level level, double x, double y, double z) {
		super(DifModEntities.REMOTE_MINECART.get(), level, x, y, z);
	}
	@Override
	public @NotNull Item getDropItem() {
		return DifModItems.REMOTE_MINECART_ITEM.get();
	}
    // Definujeme, jaký blok se má zobrazovat uvnitř (např. tvůj Chunk Loader)
    @Override
    public @NotNull BlockState getDefaultDisplayBlockState() {
        return DifModBlocks.CHUNK_LOADER_1X1.get().defaultBlockState();
    }

    @Override
    public @NotNull Type getMinecartType() {
        return Type.RIDEABLE; // Nebo si vytvoř vlastní typ
    }

    // Metoda, kterou budeme volat z dálkového ovladače (přes Packet)
    public void setRemoteMovement(double x, double z) {
        this.remotePushX = x;
        this.remotePushZ = z;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            // Aplikujeme sílu, pokud je nastavena
            if (Math.abs(remotePushX) > 0 || Math.abs(remotePushZ) > 0) {
                Vec3 movement = this.getDeltaMovement();
                this.setDeltaMovement(movement.add(remotePushX * 0.05, 0, remotePushZ * 0.05));
            }
            remotePushX *= 0.08;
            remotePushZ *= 0.08;
        }
    }

    @Override
    public @NotNull ItemStack getPickResult() {
        return new ItemStack(DifModItems.REMOTE_MINECART_ITEM.get());
    }
	@Override
	public @NotNull InteractionResult interact(Player player,@NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.is(DifModItems.REMOTE_CONTROLLER.get())) {
			if (!player.level().isClientSide) {
				// Uložíme UUID Minecartu do NBT ovladače
				CompoundTag nbt = stack.getOrCreateTag();
				nbt.putUUID("LinkedCart", this.getUUID());
				player.displayClientMessage(Component.literal("Minecart linked!"), true);
			}
			return InteractionResult.SUCCESS;
		}
		return super.interact(player, hand);
	}

}