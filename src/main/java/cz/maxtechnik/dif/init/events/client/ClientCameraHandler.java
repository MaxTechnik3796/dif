package cz.maxtechnik.dif.init.events.client;

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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
@EventBusSubscriber(value=Dist.CLIENT)
public class ClientCameraHandler{
	private static boolean isViewing=false;
	private static BlockPos cameraPos=null;
	private static BlockPos currentMonitorPos=null;
	private static ArmorStand dummyEntity=null;
	private static int timeOut=0;
	private static int inputDelay=0;
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
	public static void onClientTick(ClientTickEvent.Post event){ // Registrujeme přímo Post verzi (místo Phase.END)
		// Už nepotřebujeme kontrolovat event.phase!
		if(!isViewing||cameraPos==null) return;
		Minecraft mc=Minecraft.getInstance();
		if(mc.player==null) return;
		// Kontrola zničení bloku
		if(mc.level!=null&&!(mc.level.getBlockState(cameraPos).getBlock() instanceof Camera)){
			timeOut++;
		}
		if(timeOut>=3){
			exitCamera();
			mc.player.displayClientMessage(Component.literal("Camera is too far away or has been destroyed!"),true);
			return;
		}
		// Vynucení pozice dummy entity
		if(dummyEntity!=null){
			dummyEntity.setOldPosAndRot();
		}
		if(mc.player.isShiftKeyDown()){
			exitCamera();
			return;
		}
		// Zamezení pohybu hráče
		mc.player.xxa=0;
		mc.player.yya=0;
		mc.player.zza=0;
		if(inputDelay<2){
			inputDelay++;
			mc.options.keyLeft.consumeClick();
			mc.options.keyRight.consumeClick();
			mc.options.keyUp.consumeClick();
			mc.options.keyDown.consumeClick();
			return;
		}
		// Ovládání
		if(mc.options.keyLeft.consumeClick()){
			switchMonitor(mc,Direction.WEST);
		}else if(mc.options.keyRight.consumeClick()){
			switchMonitor(mc,Direction.EAST);
		}else if(mc.options.keyUp.consumeClick()){
			switchMonitor(mc,Direction.UP);
		}else if(mc.options.keyDown.consumeClick()){
			switchMonitor(mc,Direction.DOWN);
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
			mc.levelRenderer.allChanged();
		}
		if(currentMonitorPos!=null){
			// NOVÝ ZPŮSOB ODESÍLÁNÍ V NEOFORGE 1.21.1
			net.neoforged.neoforge.network.PacketDistributor.sendToServer(new CameraExitPacket(currentMonitorPos));
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
	public static void onInput(net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered event){
		if(isViewing){
			// Zrušíme jakýkoliv pokus o útok nebo bourání (levé tlačítko)
			if(event.isAttack()){
				event.setCanceled(true);
				event.setSwingHand(false);
			}
			// Zrušíme pokus o interakci (pravé tlačítko)
			if(event.isUseItem()){
				event.setCanceled(true);
				event.setSwingHand(false);
			}
		}
	}
	@SubscribeEvent
	public static void onPlayerInteractRightClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock event){
		if(event.getLevel().isClientSide()&&isViewing) event.setCanceled(true);
	}
	@SubscribeEvent
	public static void onPlayerInteractRightClickItem(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem event){
		if(event.getLevel().isClientSide()&&isViewing) event.setCanceled(true);
	}
	@SubscribeEvent
	public static void onPlayerInteractLeftClickBlock(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event){
		if(event.getLevel().isClientSide()&&isViewing) event.setCanceled(true);
	}
	@SubscribeEvent
	public static void onRenderHand(RenderHandEvent event){
		if(isViewing){
			event.setCanceled(true);
		}
	}
	@SubscribeEvent
	public static void onRenderGui(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Pre event){
		if(isViewing){
			// Crosshair necháme vykreslit, vše ostatní zrušíme
			if(event.getName().equals(net.neoforged.neoforge.client.gui.VanillaGuiLayers.CROSSHAIR)){
				return;
			}
			event.setCanceled(true);
		}
	}
}
