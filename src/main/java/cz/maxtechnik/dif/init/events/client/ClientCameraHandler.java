package cz.maxtechnik.dif.init.events.client;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.block.Camera;
import cz.maxtechnik.dif.block.CameraMonitor;
import cz.maxtechnik.dif.network.CameraExitPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(value=Dist.CLIENT)
public class ClientCameraHandler{
	private static boolean isViewing=false;
	private static BlockPos cameraPos=null;
	private static BlockPos currentMonitorPos=null;
	private static ArmorStand dummyEntity=null;
	private static int timeOut=0;
	private static int inputDelay = 0;
	public static void enterCamera(BlockPos pos,BlockPos monPos){
		Minecraft mc=Minecraft.getInstance();
		if(mc.level==null||mc.player==null) return;
		timeOut=0;
		inputDelay=0;
		currentMonitorPos=monPos;
		cameraPos=pos;
		BlockState blockState=mc.level.getBlockState(pos);
		// VÝPOČET POZICE: Posuneme entitu mírně PŘED blok, aby nebyl vidět vnitřek (X-ray)
		double x=pos.getX()+0.5;
		double y=pos.getY()-1;
		double z=pos.getZ()+0.5;
		float yRot=0;
		if(blockState.hasProperty(Camera.FACING)){
			Direction dir=blockState.getValue(Camera.FACING);
			yRot=dir.toYRot();
			x+=dir.getStepX()*0.2;
			z+=dir.getStepZ()*0.2;
		}
		// Vytvoření dummy entity
		dummyEntity=new ArmorStand(mc.level,x,y,z);
		dummyEntity.setInvisible(true);
		dummyEntity.setNoGravity(true);
		dummyEntity.setYRot(yRot);
		dummyEntity.setInvulnerable(true);
		dummyEntity.setYHeadRot(yRot);
		dummyEntity.setXRot(10F); // Mírný pohled dolů
		dummyEntity.noPhysics=true;
		mc.setCameraEntity(dummyEntity);
		isViewing=true;
		mc.levelRenderer.allChanged();
		// Toto řekne Minecraftu, aby bral pozici kamery jako prioritu pro renderování
		mc.level.setSectionDirtyWithNeighbors(pos.getX()>>4,pos.getY()>>4,pos.getZ()>>4);
		assert mc.player!=null;
		mc.player.setJumping(false);
		mc.player.setDeltaMovement(0, 0, 0); // Zastavíme fyzický pohyb
		mc.options.keyUp.setDown(false);      // Resetujeme stisknuté klávesy v engine
		mc.options.keyDown.setDown(false);
		mc.options.keyLeft.setDown(false);
		mc.options.keyRight.setDown(false);
		mc.options.keyJump.setDown(false);
		mc.player.displayClientMessage(Component.translatable("mount.onboard",mc.options.keyShift.getTranslatedKeyMessage()),true);
	}
	@SubscribeEvent
	public static void onComputeAngles(ViewportEvent.ComputeCameraAngles event){
		if(isViewing&&dummyEntity!=null){
			event.setYaw(dummyEntity.getYRot());
			event.setPitch(dummyEntity.getXRot());
		}
	}
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event){
		if(event.phase!=TickEvent.Phase.END||!isViewing||cameraPos==null) return;
		Minecraft mc=Minecraft.getInstance();
		if(mc.player==null) return;
		// Kontrola zničení bloku
		if(mc.level!=null&&!(mc.level.getBlockState(cameraPos).getBlock() instanceof Camera)) timeOut+=1;
		if(timeOut>=5){
			exitCamera();
			mc.player.displayClientMessage(Component.literal("Camera is too far away or has been destroyed!"),true);
			return;
		}
		// Vynucení pozice dummy entity v každém ticku (zabrání plavání)
		if(dummyEntity!=null){
			dummyEntity.setOldPosAndRot(); // Resetuje interpolace pohybu
		}
		if(mc.player.isShiftKeyDown()){
			exitCamera();
			return;
		}
		// --- OPRAVA: Input Cooldown ---
		if (inputDelay < 5) { // Počkáme 5 ticků (čtvrt sekundy)
			inputDelay++;
			// Vyčistíme kliknutí, která se stala během animace otevírání
			mc.options.keyLeft.consumeClick();
			mc.options.keyRight.consumeClick();
			mc.options.keyUp.consumeClick();
			mc.options.keyDown.consumeClick();
			return;
		}
		if(mc.options.keyLeft.consumeClick()){
			switchMonitor(mc,Direction.WEST); // Relativní vlevo
		}else if(mc.options.keyRight.consumeClick()){
			switchMonitor(mc,Direction.EAST); // Relativní vpravo
		}else if(mc.options.keyUp.consumeClick()){
			switchMonitor(mc,Direction.UP);    // Nahoru
		}else if(mc.options.keyDown.consumeClick()){
			switchMonitor(mc,Direction.DOWN);  // Dolů
		}

	}
	private static void switchMonitor(Minecraft mc,Direction relativeDir){
		if(currentMonitorPos==null||mc.level==null) return;
		BlockState state=mc.level.getBlockState(currentMonitorPos);
		if(!(state.getBlock() instanceof CameraMonitor)) return;
		Direction monitorFacing=state.getValue(CameraMonitor.FACING);
		BlockPos neighborPos;
		// Přepočet relativního směru na absolutní souřadnice světa
		if(relativeDir==Direction.UP||relativeDir==Direction.DOWN)
			neighborPos=currentMonitorPos.relative(relativeDir);
		else{
			// Logika pro strany (vlevo/vpravo) podle rotace monitoru
			Direction absoluteSide=(relativeDir==Direction.WEST)?monitorFacing.getClockWise():monitorFacing.getCounterClockWise();
			neighborPos=currentMonitorPos.relative(absoluteSide);
		}
		BlockState neighborState=mc.level.getBlockState(neighborPos);
		// Pokud je na cílové pozici další monitor, přepneme
		if(neighborState.getBlock() instanceof CameraMonitor){
			// Ukončíme starou kameru
			exitCamera();
			// Nasimulujeme kliknutí (v Minecraftu "Use") na nový monitor
			assert mc.player!=null;
			assert mc.gameMode!=null;
			mc.gameMode.useItemOn(mc.player,InteractionHand.MAIN_HAND,new BlockHitResult(new Vec3(neighborPos.getX()+0.5,neighborPos.getY()+0.5,neighborPos.getZ()+0.5),Direction.UP,neighborPos,false));
		}
	}
	public static void exitCamera(){
		Minecraft mc=Minecraft.getInstance();
		if(mc.player!=null){
			mc.setCameraEntity(mc.player);
			// --- KLÍČOVÁ OPRAVA ---
			// Vynutíme, aby si klient znovu načetl okolí hráče
			mc.levelRenderer.allChanged();
			// ----------------------
		}
		if(currentMonitorPos!=null){
			DifMod.PACKET_HANDLER.sendToServer(new CameraExitPacket(currentMonitorPos));
			currentMonitorPos=null;
		}
		isViewing=false;
		cameraPos=null;
		if(dummyEntity!=null){
			dummyEntity.discard();
			dummyEntity=null;
		}
	}
	@SubscribeEvent
	public static void onInput(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered event){
		if(isViewing){
			// Zrušíme jakýkoliv pokus o útok nebo bourání (levé tlačítko)
			if(event.isAttack()){
				event.setCanceled(true);
				event.setSwingHand(false);
			}
			// Zrušíme pokus o interakci (pravé tlačítko - stavění, otevírání beden)
			if(event.isUseItem()){
				event.setCanceled(true);
				event.setSwingHand(false);
			}
		}
	}
	@SubscribeEvent
	public static void onPlayerInteract(PlayerInteractEvent event){
		// Toto běží na obou stranách, ale my to řešíme hlavně pro klienta v kameře
		if(event.getSide().isClient()&&isViewing){
			if(event.isCancelable()){
				event.setCanceled(true);
			}
		}
	}
	@SubscribeEvent
	public static void onRenderHand(RenderHandEvent event){
		if(isViewing){
			event.setCanceled(true);
		}
	}
}