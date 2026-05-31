package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.events.JetpackHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record JetpackFlyMessage(int actionType,int pressedms) implements CustomPacketPayload{
	public static final Type<JetpackFlyMessage> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"jetpack_fly"));
	public static final StreamCodec<FriendlyByteBuf,JetpackFlyMessage> STREAM_CODEC=StreamCodec.composite(
			ByteBufCodecs.INT,JetpackFlyMessage::actionType,
			ByteBufCodecs.INT,JetpackFlyMessage::pressedms,
			JetpackFlyMessage::new
	);
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type(){
		return TYPE;
	}
	public void handle(IPayloadContext context){
		context.enqueueWork(()->pressAction(context.player(),actionType));
	}
	public static void pressAction(Player player,int actionType){
		Level world=player.level();
		if(!world.isLoaded(player.blockPosition())) return;
		if(actionType==0){
			JetpackHandler.fly(player);
		}else if(actionType==1){
			JetpackHandler.decelerate(player);
		}else if(actionType==2){
			JetpackHandler.toggleHover(player);
		}
	}
}