package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;
@SuppressWarnings("removal")
@EventBusSubscriber(modid=DifMod.MODID, bus=EventBusSubscriber.Bus.MOD)
public class ModNetworking{
	@SubscribeEvent
	public static void register(final RegisterPayloadHandlersEvent event){
		final PayloadRegistrar registrar=event.registrar(DifMod.MODID).versioned("1");
		// Server-bound packets (Client -> Server)
		registrar.playToServer(JetpackFlyMessage.TYPE,JetpackFlyMessage.STREAM_CODEC,JetpackFlyMessage::handle);
		registrar.playToServer(EnderOpenMessage.TYPE,EnderOpenMessage.STREAM_CODEC,EnderOpenMessage::handle);
		registrar.playToServer(RemoteControlPacket.TYPE,RemoteControlPacket.STREAM_CODEC,RemoteControlPacket::handle);
		registrar.playToServer(MegaBackpackOpenPacket.TYPE,MegaBackpackOpenPacket.STREAM_CODEC,MegaBackpackOpenPacket::handle);
		registrar.playToServer(MegaBackpackPagePacket.TYPE,MegaBackpackPagePacket.STREAM_CODEC,MegaBackpackPagePacket::handle);
		registrar.playToServer(SpaceshipScreenButtonMessage.TYPE,SpaceshipScreenButtonMessage.STREAM_CODEC,SpaceshipScreenButtonMessage::handle);
		registrar.playToServer(ShiftGearPacket.TYPE,ShiftGearPacket.STREAM_CODEC,ShiftGearPacket::handle);
		registrar.playToServer(CameraExitPacket.TYPE,CameraExitPacket.STREAM_CODEC,CameraExitPacket::handle);
		registrar.playToClient(JetpackSyncMessage.TYPE,JetpackSyncMessage.STREAM_CODEC,JetpackSyncMessage::handle); // ← přidej toto
		// Client-bound packets (Server -> Client)
		registrar.playToClient(SyncCarPositionPacket.TYPE,SyncCarPositionPacket.STREAM_CODEC,SyncCarPositionPacket::handle);
		registrar.playToServer(cz.maxtechnik.dif.network.ForgeSelectFluidPacket.TYPE, cz.maxtechnik.dif.network.ForgeSelectFluidPacket.STREAM_CODEC, cz.maxtechnik.dif.network.ForgeSelectFluidPacket::handle);
	}
	public record ShiftGearPacket(int direction) implements CustomPacketPayload{
		public static final Type<ShiftGearPacket> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"shift_gear"));
		public static final StreamCodec<FriendlyByteBuf,ShiftGearPacket> STREAM_CODEC=StreamCodec.composite(
				ByteBufCodecs.INT,ShiftGearPacket::direction,
				ShiftGearPacket::new
		);
		@Override
		public @NotNull Type<? extends CustomPacketPayload> type(){
			return TYPE;
		}
		public void handle(IPayloadContext context){
			context.enqueueWork(()->{
				if(context.player().getVehicle() instanceof BaseCarEntity car){
					int current=car.getCurrentGear();
					int maxGear=car.getGearRatios().length;
					int newGear=current+direction;
					if(newGear<-1) newGear=-1;
					if(newGear>maxGear) newGear=maxGear;
					if(newGear!=current){
						car.setCurrentGear(newGear);
						car.applyShiftCooldown();
					}
				}
			});
		}
	}
	public record SyncCarPositionPacket(int entityId,double x,double y,double z,float yRot,
	                                    float velocity) implements CustomPacketPayload{
		public static final Type<SyncCarPositionPacket> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"sync_car_pos"));
		public static final StreamCodec<FriendlyByteBuf,SyncCarPositionPacket> STREAM_CODEC=StreamCodec.composite(
				ByteBufCodecs.VAR_INT,SyncCarPositionPacket::entityId,
				ByteBufCodecs.DOUBLE,SyncCarPositionPacket::x,
				ByteBufCodecs.DOUBLE,SyncCarPositionPacket::y,
				ByteBufCodecs.DOUBLE,SyncCarPositionPacket::z,
				ByteBufCodecs.FLOAT,SyncCarPositionPacket::yRot,
				ByteBufCodecs.FLOAT,SyncCarPositionPacket::velocity,
				SyncCarPositionPacket::new
		);
		@Override
		public @NotNull Type<? extends CustomPacketPayload> type(){
			return TYPE;
		}
		public void handle(IPayloadContext context){
			context.enqueueWork(()->ClientPacketHandler.handleSyncCarPosition(this));
		}
	}
}