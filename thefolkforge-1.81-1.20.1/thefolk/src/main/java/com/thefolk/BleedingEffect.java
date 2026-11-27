package com.thefolk;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.joml.Vector3f;

/**
 * Sangrado: como un veneno suave (no mata: se detiene en medio corazón), ralentiza un poco
 * y deja partículas rojas.
 */
public class BleedingEffect extends MobEffect {

    // ================= CONFIGURACIÓN =================
    private static final double SLOWDOWN = -0.15D;     // -15% de velocidad
    private static final float DAMAGE = 1.0F;          // medio corazón cada vez
    // =================================================

    public BleedingEffect() {
        super(MobEffectCategory.HARMFUL, 0xB00000);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "7b5f5a55-3d1e-4c0b-9f0e-5b1d9c2a8e41",
                SLOWDOWN, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Daño una de cada tres veces (cada 1.5 segundos), como el veneno no baja de medio corazón.
        if ((entity.tickCount / 10) % 3 == 0 && entity.getHealth() > 1.0F) {
            entity.hurt(entity.damageSources().generic(), DAMAGE);
        }
        if (entity.level() instanceof ServerLevel server) {
            server.sendParticles(new DustParticleOptions(new Vector3f(0.75F, 0.0F, 0.0F), 1.2F),
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.6D, entity.getZ(),
                    12, 0.3D, 0.5D, 0.3D, 0.0D);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
