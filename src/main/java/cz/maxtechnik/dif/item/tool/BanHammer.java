package cz.maxtechnik.dif.item.tool;

import cz.maxtechnik.dif.DifMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class BanHammer extends Item{
	public BanHammer(){
		super(new Item.Properties().stacksTo(1).fireResistant());
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@NotNull ItemStack itemStack,Item.@NotNull TooltipContext context,@NotNull List<Component> list,@NotNull TooltipFlag flag){
		super.appendHoverText(itemStack,context,list,flag);
		list.add(Component.empty());
		list.add(Component.literal("» SYSTEM OVERRIDE « ").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)
				.append(Component.literal("ERR_BAN").withStyle(ChatFormatting.OBFUSCATED,ChatFormatting.RED)));
		list.add(Component.literal("# ").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)
				.append(Component.literal("###").withStyle(ChatFormatting.OBFUSCATED,ChatFormatting.DARK_RED))
				.append(Component.literal(" CEASE TO EXIST ").withStyle(ChatFormatting.RED,ChatFormatting.BOLD))
				.append(Component.literal("###").withStyle(ChatFormatting.OBFUSCATED,ChatFormatting.DARK_RED))
				.append(Component.literal(" #").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)));
		list.add(Component.empty());
		list.add(Component.literal("\"You were never ").withStyle(ChatFormatting.RED,ChatFormatting.ITALIC)
				.append(Component.literal("here.\"").withStyle(ChatFormatting.DARK_RED,ChatFormatting.ITALIC,ChatFormatting.BOLD)));
		list.add(Component.empty());
		list.add(Component.translatable("item.modifiers.mainhand").withStyle(ChatFormatting.BLUE));
		list.add(Component.literal(" ∞ ").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)
				.append(Component.translatable("attribute.name.generic.attack_damage").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)));
		list.add(Component.literal(" ⚠ PERMANENT BAN: ").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)
				.append(Component.literal("INSTANT").withStyle(ChatFormatting.RED,ChatFormatting.BOLD)));
	}
	@Override
	public boolean onLeftClickEntity(@NotNull ItemStack itemStack,@NotNull Player attacker,@NotNull Entity target){
		if(!(attacker.level() instanceof ServerLevel level)) return true;
		double x=target.getX(), y=target.getY(), z=target.getZ();
		LightningBolt lightning=EntityType.LIGHTNING_BOLT.create(level);
		if(lightning!=null){
			lightning.moveTo(x,y,z);
			lightning.setVisualOnly(true);
			level.addFreshEntity(lightning);
		}
		level.playSound(null,x,y,z,SoundEvents.LIGHTNING_BOLT_THUNDER,SoundSource.PLAYERS,5F,0.8F);
		level.playSound(null,x,y,z,SoundEvents.WITHER_SPAWN,SoundSource.PLAYERS,2F,0.5F);
		level.playSound(null,x,y,z,SoundEvents.WARDEN_SONIC_BOOM,SoundSource.PLAYERS,2.5F,0.7F);
		level.sendParticles(ParticleTypes.SONIC_BOOM,x,y+1,z,1,0,0,0,0);
		level.sendParticles(ParticleTypes.FLASH,x,y+1,z,3,0.2,0.2,0.2,0);
		level.sendParticles(ParticleTypes.REVERSE_PORTAL,x,y+1,z,80,0.6,1.0,0.6,0.2);
		level.sendParticles(ParticleTypes.LARGE_SMOKE,x,y+1,z,40,0.5,0.8,0.5,0.1);
		level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,x,y+1,z,30,0.4,0.8,0.4,0.15);
		DamageSource source=level.damageSources().source(DamageTypes.FELL_OUT_OF_WORLD,attacker);
		target.setInvulnerable(false);
		target.hurt(source,Float.MAX_VALUE);
		if(target instanceof LivingEntity living){
			living.setHealth(0F);
			living.die(source);
		}
		if(target instanceof Player targetPlayer){
			targetPlayer.getAbilities().invulnerable=false;
			targetPlayer.onUpdateAbilities();
			MinecraftServer server=level.getServer();
			String name=targetPlayer.getGameProfile().getName();
			DifMod.queueServerWork(10,()->{
				Component msg=Component.literal("[BAN] ").withStyle(ChatFormatting.DARK_RED,ChatFormatting.BOLD)
						.append(Component.literal("Player "+name+" was erased from existence by the Ban Hammer!").withStyle(ChatFormatting.RED));
				server.getPlayerList().broadcastSystemMessage(msg,false);
				server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),"ban "+name+" Erased from existence by the Ban Hammer");
			});
		}else if(!target.isRemoved()) target.discard();
		return true;
	}
}
