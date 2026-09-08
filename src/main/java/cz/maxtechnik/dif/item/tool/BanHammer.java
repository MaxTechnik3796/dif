package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.DifMod;
import cz.maxtechnik.dif.init.basic.DifModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
@SuppressWarnings("unused")
@EventBusSubscriber(modid=DifMod.MODID)
public class BanHammer extends Item{
	public BanHammer(){
		super(new Item.Properties().stacksTo(1).fireResistant());
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@NotNull ItemStack stack,Item.@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		super.appendHoverText(stack,context,list,flag);
		list.add(Component.empty());
		// Header - System Override Glitch
		list.add(Component.literal("» SYSTEM OVERRIDE « ").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)
				.append(Component.literal("ERR_BAN").withStyle(ChatFormatting.OBFUSCATED,ChatFormatting.RED)));
		// Obfuskovaný blikající řádek
		list.add(Component.literal("# ").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)
				.append(Component.literal("###").withStyle(ChatFormatting.OBFUSCATED,ChatFormatting.DARK_RED))
				.append(Component.literal(" CEASE TO EXIST ").withStyle(ChatFormatting.RED,ChatFormatting.BOLD))
				.append(Component.literal("###").withStyle(ChatFormatting.OBFUSCATED,ChatFormatting.DARK_RED))
				.append(Component.literal(" #").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)));
		list.add(Component.empty());
		// English Lore Text
		list.add(Component.literal("\"You were never ").withStyle(ChatFormatting.RED,ChatFormatting.ITALIC)
				.append(Component.literal("here.\"").withStyle(ChatFormatting.DARK_RED,ChatFormatting.ITALIC,ChatFormatting.BOLD)));
		list.add(Component.empty());
		// Modifier Header & Attack Damage
		list.add(Component.translatable("item.modifiers.mainhand").withStyle(ChatFormatting.BLUE));
		list.add(Component.literal(" ")
				.append(Component.literal("∞ ").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD))
				.append(Component.translatable("attribute.name.generic.attack_damage").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)));
		// Extra Ban Status
		list.add(Component.literal(" ")
				.append(Component.literal("⚠ ").withStyle(ChatFormatting.DARK_RED))
				.append(Component.literal("PERMANENT BAN: ").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD))
				.append(Component.literal("INSTANT").withStyle(ChatFormatting.RED,ChatFormatting.BOLD)));
	}
	@Override
	public boolean onLeftClickEntity(@NotNull ItemStack stack,@NotNull Player attacker,@NotNull Entity entity){
		executeBanHammerEffect(attacker,entity);
		return true;
	}
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public static void onAttackEntity(AttackEntityEvent event){
		Player player=event.getEntity();
		if(!player.level().isClientSide&&player.getMainHandItem().is(DifModItems.BAN_HAMMER.get())){
			executeBanHammerEffect(player,event.getTarget());
			event.setCanceled(true);
		}
	}
	private static final Set<UUID> PENDING_BAN_UUIDS=ConcurrentHashMap.newKeySet();
	public static void executeBanHammerEffect(Player attacker,Entity target){
		if(attacker==null||attacker.level().isClientSide||target==null) return;
		if(!(attacker.level() instanceof ServerLevel serverLevel)) return;
		LightningBolt lightning=EntityType.LIGHTNING_BOLT.create(serverLevel);
		if(lightning!=null){
			lightning.moveTo(target.position());
			lightning.setVisualOnly(true);
			serverLevel.addFreshEntity(lightning);
		}
		serverLevel.playSound(null,target.getX(),target.getY(),target.getZ(),
				SoundEvents.LIGHTNING_BOLT_THUNDER,SoundSource.PLAYERS,5.0F,0.8F);
		serverLevel.playSound(null,target.getX(),target.getY(),target.getZ(),
				SoundEvents.WITHER_SPAWN,SoundSource.PLAYERS,2.0F,0.5F);
		serverLevel.playSound(null,target.getX(),target.getY(),target.getZ(),
				SoundEvents.WARDEN_SONIC_BOOM,SoundSource.PLAYERS,2.5F,0.7F);
		serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,target.getX(),target.getY()+1.0,target.getZ(),1,0,0,0,0);
		serverLevel.sendParticles(ParticleTypes.FLASH,target.getX(),target.getY()+1.0,target.getZ(),3,0.2,0.2,0.2,0);
		serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,target.getX(),target.getY()+1.0,target.getZ(),80,0.6,1.0,0.6,0.2);
		serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,target.getX(),target.getY()+1.0,target.getZ(),40,0.5,0.8,0.5,0.1);
		serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,target.getX(),target.getY()+1.0,target.getZ(),30,0.4,0.8,0.4,0.15);
		DamageSource divineSource=serverLevel.damageSources().source(DamageTypes.FELL_OUT_OF_WORLD,attacker);
		target.setInvulnerable(false);
		target.hurt(divineSource,Float.MAX_VALUE);
		if(target instanceof LivingEntity living){
			living.setHealth(0.0F);
			living.die(divineSource);
		}
		if(target instanceof Player targetPlayer){
			UUID targetUuid=targetPlayer.getUUID();
			if(!PENDING_BAN_UUIDS.add(targetUuid)){
				return;
			}
			targetPlayer.getAbilities().invulnerable=false;
			targetPlayer.onUpdateAbilities();
			MinecraftServer server=serverLevel.getServer();
			String playerName=targetPlayer.getGameProfile().getName();
			long executeTick=server.getTickCount()+10;
			server.tell(new TickTask((int)executeTick,()->{
				try{
					Component banMessage=Component.literal("[BAN] ")
							.withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)
							.append(Component.literal("Player "+playerName+" was erased from existence by the Ban Hammer!")
									.withStyle(ChatFormatting.RED));
					server.getPlayerList().broadcastSystemMessage(banMessage,false);
					server.getCommands().performPrefixedCommand(
							server.createCommandSourceStack(),
							"ban "+playerName+" Erased from existence by the Ban Hammer"
					);
				}finally{
					PENDING_BAN_UUIDS.remove(targetUuid);
				}
			}));
		}else if(target.isAlive()||!target.isRemoved()){
			target.remove(Entity.RemovalReason.KILLED);
			target.discard();
		}
	}
}