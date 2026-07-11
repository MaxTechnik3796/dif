package cz.maxtechnik.dif.network;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.item.armor.Jetpack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
public record JetpackSyncMessage(int thrust) implements CustomPacketPayload{
	public static final Type<JetpackSyncMessage> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(DifMod.MODID,"jetpack_sync"));
	public static final StreamCodec<FriendlyByteBuf,JetpackSyncMessage> STREAM_CODEC=StreamCodec.composite(
			ByteBufCodecs.INT,JetpackSyncMessage::thrust,
			JetpackSyncMessage::new
	);
	@Override
	public @NotNull Type<? extends CustomPacketPayload> type(){
		return TYPE;
	}
	public void handle(IPayloadContext context){
		context.enqueueWork(()->{
			ItemStack chest=context.player().getItemBySlot(EquipmentSlot.CHEST);
			if(!(chest.getItem() instanceof Jetpack)) return;
			Jetpack.Chestplate.setThrust(chest,thrust);
		});
	}
}