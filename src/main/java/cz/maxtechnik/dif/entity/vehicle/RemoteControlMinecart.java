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
	private boolean initialized = false;
	// Logický "předek" vozíku. Tohle se v zatáčkách nemění skokově.
	private Vec3 logicForward = new Vec3(1, 0, 0);

	public RemoteControlMinecart(EntityType<? extends AbstractMinecart> type, Level level) {
		super(type, level);
	}

	public RemoteControlMinecart(Level level, double x, double y, double z) {
		super(DifModEntities.REMOTE_MINECART.get(), level, x, y, z);
	}

	public void setRemoteMovement(double pushValue) {
		this.push = pushValue;
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.level().isClientSide) {
			Vec3 motion = this.getDeltaMovement();
			double speed = motion.horizontalDistance();

			if (Math.abs(push) > 0.01) {
				// 1. USTÁLENÍ ORIENTACE (Klíč k zatáčkám)
				// Pokud se hýbeš, neustále aktualizuj logicForward, ale NIKDY ho neotoč o víc než 90 stupňů naráz.
				if (speed > 0.01) {
					Vec3 currentDir = motion.normalize();
					// Pokud se nový směr snaží otočit proti našemu zámku, otočíme ho (zachování orientace)
					if (currentDir.dot(logicForward) < 0) {
						logicForward = currentDir.scale(-1);
					} else {
						logicForward = currentDir;
					}
				} else if (!initialized) {
					// Inicializace při prvním rozjezdu ze stoje
					float f = this.getYRot() * ((float) Math.PI / 180F);
					logicForward = new Vec3(-Math.sin(f), 0, Math.cos(f));
					initialized = true;
				}

				// 2. VÝPOČET SÍLY
				double accel = 0.08;
				double maxSpeed = 0.4;

				// Thrust vždy míří ve směru logicForward (při plynu) nebo proti (při zpátečce)
				Vec3 thrust = logicForward.scale(push * accel);

				// 3. INTELIGENTNÍ BRZDA (Zabraňuje cukání v zatáčce)
				// Pokud jedeme proti směru, který chceme, nejdřív "vymažeme" starou rychlost
				if (speed > 0.02 && motion.dot(thrust) < 0) {
					motion = motion.scale(0.5);
				}

				Vec3 newMotion = motion.add(thrust);

				if (newMotion.horizontalDistance() > maxSpeed) {
					newMotion = newMotion.normalize().scale(maxSpeed);
				}

				this.setDeltaMovement(newMotion.x, motion.y, newMotion.z);
			} else {
				// PASIVNÍ BRZDA
				if (speed > 0.001) {
					this.setDeltaMovement(motion.multiply(0.8, 1.0, 0.8));
				} else {
					this.setDeltaMovement(0, motion.y, 0);
				}
			}

			// Důležité: Postupné vyrovnávání rotace entity k naší logické orientaci
			// Tím se zajistí, že se scoreboard nebude točit, ale plynule sledovat logicForward.
			float targetYaw = (float) (Math.atan2(logicForward.z, logicForward.x) * (180 / Math.PI)) - 90;
			this.setYRot(targetYaw);

			push = 0;
		}
	}

	@Override
	public @NotNull InteractionResult interact(Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (stack.is(DifModItems.REMOTE_CONTROLLER.get())) {
			if (!player.level().isClientSide) {
				// Synchronizace UUID pro ovladač
				CompoundTag nbt = stack.getOrCreateTag();
				nbt.putUUID("LinkedCart", this.getUUID());
				player.displayClientMessage(Component.literal("Minecart linked!"), true);

				// BONUS: Kliknutí ovladačem na vozík resetuje orientaci (otočí ho)
				this.logicForward = this.logicForward.scale(-1);
				player.displayClientMessage(Component.literal("Orientation flipped!"), true);
			}
			return InteractionResult.SUCCESS;
		}
		return super.interact(player, hand);
	}

	// NBT Data pro uložení orientace
	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putDouble("LX", logicForward.x);
		tag.putDouble("LZ", logicForward.z);
		tag.putBoolean("Init", initialized);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.logicForward = new Vec3(tag.getDouble("LX"), 0, tag.getDouble("LZ"));
		this.initialized = tag.getBoolean("Init");
	}

	@Override public @NotNull Item getDropItem() { return DifModItems.REMOTE_MINECART_ITEM.get(); }
	@Override public @NotNull Type getMinecartType() { return Type.RIDEABLE; }
	@Override public @NotNull ItemStack getPickResult() { return new ItemStack(DifModItems.REMOTE_MINECART_ITEM.get()); }
	@Override public @NotNull BlockState getDefaultDisplayBlockState() {
		return net.minecraft.world.level.block.Blocks.BLAST_FURNACE.defaultBlockState()
				.setValue(net.minecraft.world.level.block.BlastFurnaceBlock.FACING, net.minecraft.core.Direction.NORTH);
	}
}