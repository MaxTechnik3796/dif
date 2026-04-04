package cz.maxtechnik.dif.entity.vehicle;

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

public class RemoteControlMinecart extends AbstractMinecart {
	private double push;
	// Tyto proměnné si pamatují směr "motoru" nezávisle na rotaci entity
	private double xPush;
	private double zPush;

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

	@Override
	public @NotNull BlockState getDefaultDisplayBlockState() {
		// Orientace pece (NORTH = předek minecartu)
		return net.minecraft.world.level.block.Blocks.BLAST_FURNACE.defaultBlockState()
				.setValue(net.minecraft.world.level.block.BlastFurnaceBlock.FACING, net.minecraft.core.Direction.NORTH);
	}

	@Override
	public @NotNull Type getMinecartType() {
		return Type.RIDEABLE;
	}

	public void setRemoteMovement(double pushValue) {
		this.push = pushValue;
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.level().isClientSide) {
			Vec3 motion = this.getDeltaMovement();
			double horizontalSpeed = Math.sqrt(xPush * xPush + zPush * zPush);

			// 1. OVLÁDÁNÍ (Změna směru tlačení)
			if (Math.abs(push) > 0.01) {
				// Získáme směr podle aktuální rotace "nosu"
				float yawRad = this.getYRot() * ((float) Math.PI / 180F);
				double dirX = -Math.sin(yawRad);
				double dirZ = Math.cos(yawRad);

				// "Nabijeme" motor směrem, kterým koukáme (push 1.0 = vpřed, -1.0 = vzad)
				this.xPush = dirX * push;
				this.zPush = dirZ * push;
			}

			// 2. APLIKACE SÍLY (Logika FurnaceMinecart)
			if (horizontalSpeed > 0.01) {
				// Aplikujeme sílu: 80% starého pohybu + impuls motoru
				// 0.1 je síla motoru, 0.8 je tření při zapnutém motoru
				this.setDeltaMovement(motion.x * 0.8 + xPush * 0.1, motion.y, motion.z * 0.8 + zPush * 0.1);
			} else {
				// 3. PASIVNÍ BRŽDĚNÍ (Když motor netlačí)
				if (motion.horizontalDistance() > 0.001) {
					this.setDeltaMovement(motion.multiply(0.7, 1, 0.7));
				} else {
					this.setDeltaMovement(0, motion.y, 0);
				}
			}

			// Plynulé vyhasínání motoru (aby Minecart nejel věčně)
			xPush *= 0.9;
			zPush *= 0.9;

			// Resetování impulsu z ovladače pro tento tick
			push = 0;
		}
	}

	@Override
	public @NotNull ItemStack getPickResult() {
		return new ItemStack(DifModItems.REMOTE_MINECART_ITEM.get());
	}

	@Override
	public @NotNull InteractionResult interact(Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.is(DifModItems.REMOTE_CONTROLLER.get())) {
			if (!player.level().isClientSide) {
				CompoundTag nbt = stack.getOrCreateTag();
				nbt.putUUID("LinkedCart", this.getUUID());
				player.displayClientMessage(Component.literal("Minecart linked!"), true);
			}
			return InteractionResult.SUCCESS;
		}
		return super.interact(player, hand);
	}
}