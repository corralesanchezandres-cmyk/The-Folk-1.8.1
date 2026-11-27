package com.thefolk.item;

import com.thefolk.entity.FolkMonster;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Segador: al golpear a The Folk, The Thing o Man los ralentiza y los vuelve tontos durante 3 segundos.
 * (Las criaturas son invulnerables, así que el efecto se aplica con el golpe, no con el daño.)
 */
public class ReaperItem extends SwordItem {

    // ================= CONFIGURACIÓN =================
    private static final int EFFECT_TICKS = 20 * 3;      // 3 segundos
    private static final int COOLDOWN_TICKS = 20 * 5;    // 5 segundos hasta poder volver a usarlo
    // =================================================

    public ReaperItem(Properties properties) {
        super(Tiers.IRON, 3, -2.4F, properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide()
                && entity instanceof FolkMonster monster
                && !player.getCooldowns().isOnCooldown(this)) {
            monster.confuse(EFFECT_TICKS);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));

            if (player.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SOUL,
                        entity.getX(), entity.getY() + entity.getBbHeight() * 0.6D, entity.getZ(),
                        20, 0.3D, 0.5D, 0.3D, 0.05D);
            }
            player.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 0.6F);
        }
        return false;   // el golpe sigue su curso normal
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thefolk.reaper.desc").withStyle(ChatFormatting.GRAY));
    }
}
