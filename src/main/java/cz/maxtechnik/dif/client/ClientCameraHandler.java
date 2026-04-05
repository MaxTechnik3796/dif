package cz.maxtechnik.dif.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.CameraBlock;
import cz.maxtechnik.dif.network.CameraExitPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientCameraHandler {
	private static boolean isViewing = false;
	private static BlockPos cameraPos = null;
	private static BlockPos currentMonitorPos = null;
	private static ArmorStand dummyEntity = null;

	public static void enterCamera(BlockPos pos,BlockPos monPos) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null) return;
		currentMonitorPos = monPos;
		cameraPos = pos;
		BlockState state = mc.level.getBlockState(pos);

		// VÝPOČET POZICE: Posuneme entitu mírně PŘED blok, aby nebyl vidět vnitřek (X-ray)
		double x = pos.getX() + 0.5;
		double y = pos.getY()-1;// + 0.70; // Výška očí v kameře
		double z = pos.getZ() + 0.5;

		float yRot = 0;
		if (state.hasProperty(CameraBlock.FACING)) {
			Direction dir = state.getValue(CameraBlock.FACING);
			yRot = dir.toYRot();
			// Posun o 0.2 bloku směrem, kam kamera kouká
			x += dir.getStepX() * 0.2;
			z += dir.getStepZ() * 0.2;
		}

		// Vytvoření dummy entity
		dummyEntity = new ArmorStand(mc.level, x, y, z);
		dummyEntity.setInvisible(true);
		dummyEntity.setNoGravity(true);
		dummyEntity.setYRot(yRot);
		dummyEntity.setYHeadRot(yRot);
		dummyEntity.setXRot(10.0F); // Mírný pohled dolů

		// Klíčové pro stabilitu:
		dummyEntity.noPhysics = true;

		mc.setCameraEntity(dummyEntity);
		isViewing = true;
		mc.player.displayClientMessage(Component.literal("Kamera aktivní (SHIFT pro ukončení)"), true);
	}

	@SubscribeEvent
	public static void onComputeAngles(ViewportEvent.ComputeCameraAngles event) {
		if (isViewing && dummyEntity != null) {
			event.setYaw(dummyEntity.getYRot());
			event.setPitch(dummyEntity.getXRot());

			// ZÁKAZ OTÁČENÍ MODELU HRÁČE:
			/*Minecraft mc = Minecraft.getInstance();
			if (mc.player != null) {
				mc.player.setYRot(dummyEntity.getYRot());
				mc.player.setXRot(dummyEntity.getXRot());
				mc.player.yRotO = dummyEntity.getYRot();
				mc.player.xRotO = dummyEntity.getXRot();
			}*/
		}
	}
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END || !isViewing || cameraPos == null) return;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		// Kontrola zničení bloku
		if (mc.level != null && !(mc.level.getBlockState(cameraPos).getBlock() instanceof CameraBlock)) {
			exitCamera();
			mc.player.displayClientMessage(Component.literal("Spojení ztraceno!"), false);
			return;
		}

		// Vynucení pozice dummy entity v každém ticku (zabrání plavání)
		if (dummyEntity != null) {
			dummyEntity.setOldPosAndRot(); // Resetuje interpolace pohybu
		}

		if (mc.player.isShiftKeyDown()) {
			exitCamera();
		}
	}


	public static void exitCamera() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			mc.setCameraEntity(mc.player);
		}
		if (currentMonitorPos != null) {
			DifMod.PACKET_HANDLER.sendToServer(new CameraExitPacket(currentMonitorPos));
			currentMonitorPos = null;
		}
		isViewing = false;
		cameraPos = null;
		if (dummyEntity != null) {
			dummyEntity.discard();
			dummyEntity = null;
		}
	}
	@SubscribeEvent
	public static void onInput(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
		if (isViewing) {
			// Zrušíme jakýkoliv pokus o útok nebo bourání (levé tlačítko)
			if (event.isAttack()) {
				event.setCanceled(true);
				event.setSwingHand(false);
			}
			// Zrušíme pokus o interakci (pravé tlačítko - stavění, otevírání beden)
			if (event.isUseItem()) {
				event.setCanceled(true);
				event.setSwingHand(false);
			}
		}
	}
	@SubscribeEvent
	public static void onPlayerInteract(net.minecraftforge.event.entity.player.PlayerInteractEvent event) {
		// Toto běží na obou stranách, ale my to řešíme hlavně pro klienta v kameře
		if (event.getSide().isClient() && isViewing) {
			if (event.isCancelable()) {
				event.setCanceled(true);
			}
		}
	}
	@SubscribeEvent
	public static void onRenderHand(net.minecraftforge.client.event.RenderHandEvent event) {
		if (isViewing) {
			event.setCanceled(true); // Toto skryje ruku i item, který držíš
		}
	}

}