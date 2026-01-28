package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.block.PortalBlock;
import cz.maxtechnik.dif.block.entity.PortalBlockEntity;
import cz.maxtechnik.dif.init.basic.DifModBlocks;
import cz.maxtechnik.dif.init.PortalStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
public class PortalGun extends Item {
	public PortalGun() {
		// Durability nastavíme na 24, aby odpovídalo max počtu střel
		super(new Properties().stacksTo(1).durability(24));
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level world,Player player,@NotNull InteractionHand hand) {
		ItemStack gun = player.getItemInHand(hand);
		CompoundTag nbt = gun.getOrCreateTag();

		// Inicializace NBT, pokud neexistuje
		if (!nbt.contains("ammo")) {
			nbt.putInt("ammo", 24);
			nbt.putInt("CustomModelData", 1);
			nbt.putBoolean("mode", true);
		}

		// 1. NABÍJENÍ (Ender perla v Offhand)
		ItemStack offhandItem = player.getOffhandItem();
		if (offhandItem.is(Items.ENDER_PEARL)) {
			int currentAmmo = nbt.getInt("ammo");
			if (currentAmmo < 24) {
				if (!world.isClientSide) {
					int newAmmo = Math.min(currentAmmo + 4, 24);
					nbt.putInt("ammo", newAmmo);
					offhandItem.shrink(1);

					// AKTUALIZACE DMG BARU: 0 poškození je plný bar
					gun.setDamageValue(24 - newAmmo);

					player.displayClientMessage(Component.literal("§a[+] Energie doplněna (+4)"), true);
				}
				return InteractionResultHolder.sidedSuccess(gun, world.isClientSide());
			} else {
				if (!world.isClientSide) player.displayClientMessage(Component.literal("§e[!] Energie je již plná"), true);
				return InteractionResultHolder.fail(gun);
			}
		}

		// 2. PŘEPÍNÁNÍ (Shift + Klik)
		if (player.isShiftKeyDown()) {
			if (!world.isClientSide) {
				boolean isBlue = !nbt.getBoolean("mode");
				nbt.putBoolean("mode", isBlue);
				nbt.putInt("CustomModelData", isBlue ? 1 : 2);
				player.displayClientMessage(Component.literal(isBlue ? "§b[!] Režim: MODRÝ" : "§6[!] Režim: ORANŽOVÝ"), true);
			}
			return InteractionResultHolder.sidedSuccess(gun, world.isClientSide());
		}

		// 3. STŘELBA
		if (!world.isClientSide) {
			int ammo = nbt.getInt("ammo");
			if (ammo > 0) {
				firePortal((ServerLevel) world, player,nbt.getBoolean("mode"));

				// Po výstřelu snížíme munici a aktualizujeme bar
				int newAmmo = ammo - 1;
				nbt.putInt("ammo", newAmmo);
				gun.setDamageValue(24 - newAmmo);

				player.getCooldowns().addCooldown(this, 10);
			} else {
				player.displayClientMessage(Component.literal("§c[!] Vybité! Nabij pomocí Ender perly v levé ruce."), true);
			}
		}

		return InteractionResultHolder.success(gun);
	}

	private void firePortal(ServerLevel world,Player player,boolean isBlue) {
		Vec3 start = player.getEyePosition();
		Vec3 end = start.add(player.getLookAngle().scale(128.0));
		BlockHitResult hit = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

		if (hit.getType() == HitResult.Type.BLOCK) {
			Direction face = hit.getDirection();
			BlockPos pos = hit.getBlockPos().relative(face);

			// Druhý blok se pokládá směrem OD hráče (před něj)
			Direction extDir = (face.getAxis() == Direction.Axis.Y) ? player.getDirection() : Direction.UP;
			BlockPos extPos = pos.relative(extDir);

			if (world.isEmptyBlock(pos) && world.isEmptyBlock(extPos)) {
				PortalStorage.removeOldPortal(world, player.getUUID(), isBlue);
				PortalStorage.savePortal(player.getUUID(), isBlue, pos);

				world.setBlock(pos, DifModBlocks.PORTAL_BLOCK.get().defaultBlockState()
						.setValue(PortalBlock.HALF, DoubleBlockHalf.LOWER)
						.setValue(PortalBlock.FACING, face)
						.setValue(PortalBlock.EXTENSION_DIR, extDir)
						.setValue(PortalBlock.IS_BLUE, isBlue), 3);

				world.setBlock(extPos, DifModBlocks.PORTAL_BLOCK.get().defaultBlockState()
						.setValue(PortalBlock.HALF, DoubleBlockHalf.UPPER)
						.setValue(PortalBlock.FACING, face)
						.setValue(PortalBlock.EXTENSION_DIR, extDir)
						.setValue(PortalBlock.IS_BLUE, isBlue), 3);

				if (world.getBlockEntity(pos) instanceof PortalBlockEntity be) {
					be.setup(player.getUUID(), isBlue, face);
				}
			}
		}
	}
}