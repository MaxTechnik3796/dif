package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.entity.vehicle.BaseCarEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
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