package com.thefolk.item;

import com.thefolk.entity.FolkMonster;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Paralizador: clic derecho apuntando a The Folk, The Thing o Man los paraliza 2 segundos.
 */
public class ParalyzerItem extends Item {

    // ================= CONFIGURACIÓN =================
    private static final int PARALYZE_TICKS = 20 * 2;    // 2 segundos
    private static final int COOLDOWN_TICKS = 20 * 8;    // 8 segundos hasta poder volver a usarlo
    private static final double RANGE = 16.0D;           // alcance en bloques
    // =================================================

    public ParalyzerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide() && level instanceof ServerLevel server) {
            Vec3 start = player.getEyePosition();
            FolkMonster target = this.findTarget(level, player, start);

            if (target != null) {
                target.paralyze(PARALYZE_TICKS);
                player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));

                // Rayo de chispas hasta el objetivo
                Vec3 end = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.6D, target.getZ());
                Vec3 beam = end.subtract(start);
                int steps = (int) (beam.length() * 2.0D);
                for (int i = 1; i <= steps; i++) {
                    Vec3 point = start.add(beam.scale((double) i / (double) steps));
                    server.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y - 0.2D, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.6F, 2.0F);
            } else {
                // Sin objetivo: solo un chasquido, sin gastar el arma.
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8F, 0.6F);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** La criatura a la que apunta el jugador, si no hay un bloque en medio. */
    private FolkMonster findTarget(Level level, Player player, Vec3 start) {
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(RANGE));

        BlockHitResult blockHit = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxDistSqr = blockHit.getType() == HitResult.Type.MISS
                ? RANGE * RANGE
                : start.distanceToSqr(blockHit.getLocation());

        AABB box = player.getBoundingBox().expandTowards(look.scale(RANGE)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(level, player, start, end, box,
                e -> e instanceof FolkMonster);

        if (hit != null
                && hit.getEntity() instanceof FolkMonster monster
                && start.distanceToSqr(hit.getLocation()) <= maxDistSqr) {
            return monster;
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thefolk.paralyzer.desc").withStyle(ChatFormatting.GRAY));
    }
}
