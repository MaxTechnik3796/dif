package cz.maxtechnik.dif.entity.vehicle;

import cz.maxtechnik.dif.init.basic.DifModItems;
import cz.maxtechnik.dif.init.other.DifModEntities;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import static javax.swing.SwingConstants.SOUTH_WEST;
import static net.minecraft.world.level.block.state.properties.RailShape.*;
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
				// 1. CHYTRÁ INICIALIZACE V ZATÁČCE
				if (speed < 0.01) {
					// Pokud stojíme, zkusíme najít směr kolejí, abychom se nerozjeli blbě
					Vec3 railDir = getRailDirection();

					// Pokud jsme už inicializovaní, srovnáme logicForward tak, aby odpovídal kolejím
					if (initialized) {
						if (railDir.dot(logicForward) < 0) {
							logicForward = railDir.scale(-1);
						} else {
							logicForward = railDir;
						}
					} else {
						// Úplně první rozjezd - srovnáme logicForward s rotací, ale zarovnáme na koleje
						float f = this.getYRot() * ((float) Math.PI / 180F);
						Vec3 lookDir = new Vec3(-Math.sin(f), 0, Math.cos(f));
						logicForward = railDir.dot(lookDir) < 0 ? railDir.scale(-1) : railDir;
						initialized = true;
					}
				} else {
					// Plynulá aktualizace směru za jízdy (zatáčení)
					Vec3 currentDir = motion.normalize();
					if (currentDir.dot(logicForward) < 0) {
						logicForward = currentDir.scale(-1);
					} else {
						logicForward = currentDir;
					}
				}

				// 2. VÝPOČET SÍLY
				double accel = 0.08;
				double maxSpeed = 0.4;
				Vec3 thrust = logicForward.scale(push * accel);

				// Aktivní brzda
				if (speed > 0.02 && motion.dot(thrust) < 0) {
					motion = motion.scale(0.5);
				}

				Vec3 newMotion = motion.add(thrust);
				if (newMotion.horizontalDistance() > maxSpeed) {
					newMotion = newMotion.normalize().scale(maxSpeed);
				}
				this.setDeltaMovement(newMotion.x, motion.y, newMotion.z);
			} else {
				// Pasivní brzda
				if (speed > 0.001) {
					this.setDeltaMovement(motion.multiply(0.8, 1.0, 0.8));
				} else {
					this.setDeltaMovement(0, motion.y, 0);
				}
			}

			// Vizuální srovnání (scoreboard a model)
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
				CompoundTag nbt = stack.getOrCreateTag();

				// Kontrola, jestli už je tento vozík s ovladačem propojený
				if (nbt.hasUUID("LinkedCart") && nbt.getUUID("LinkedCart").equals(this.getUUID())) {
					// POKUD JIŽ JE PROPOJEN: Otočíme orientaci (předek/zadek)
					this.logicForward = this.logicForward.scale(-1);
					player.displayClientMessage(Component.literal("Orientation flipped!"), true);
				} else {
					// POKUD NENÍ PROPOJEN: Propojíme ho
					nbt.putUUID("LinkedCart", this.getUUID());
					player.displayClientMessage(Component.literal("Minecart linked to controller!"), true);
				}
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
	/**
	 * Pomocná metoda, která zjistí směr kolejí pod Minecartem.
	 * Tím zabráníme prohození směru při rozjezdu v zatáčce.
	 */
	private Vec3 getRailDirection() {
		BlockPos pos = this.blockPosition();
		BlockState state = this.level().getBlockState(pos);
		if (state.getBlock() instanceof BaseRailBlock rail) {
			RailShape shape = state.getValue(rail.getShapeProperty());
			return switch (shape) {
				case NORTH_SOUTH -> new Vec3(0, 0, 1);
				case EAST_WEST -> new Vec3(1, 0, 0);
				// V zatáčkách je lepší brát směr jako průměr obou směrů, kam koleje vedou
				case NORTH_EAST -> new Vec3(0.7, 0, 0.7);
				case NORTH_WEST -> new Vec3(-0.7, 0, 0.7);
				case SOUTH_EAST -> new Vec3(0.7, 0, -0.7);
				case SOUTH_WEST -> new Vec3(-0.7, 0, -0.7);
				default -> new Vec3(1, 0, 0);
			};
		}
		return new Vec3(this.getDeltaMovement().x, 0, this.getDeltaMovement().z).normalize();
	}
	@Override public @NotNull Item getDropItem() { return DifModItems.REMOTE_MINECART_ITEM.get(); }
	@Override public @NotNull Type getMinecartType() { return Type.RIDEABLE; }
	@Override public @NotNull ItemStack getPickResult() { return new ItemStack(DifModItems.REMOTE_MINECART_ITEM.get()); }
	@Override public @NotNull BlockState getDefaultDisplayBlockState() {
		return net.minecraft.world.level.block.Blocks.BLAST_FURNACE.defaultBlockState()
				.setValue(net.minecraft.world.level.block.BlastFurnaceBlock.FACING, net.minecraft.core.Direction.NORTH);
	}
}