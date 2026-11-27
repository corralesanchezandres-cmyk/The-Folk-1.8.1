package com.thefolk.entity;

import com.thefolk.FolkUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Base de The Folk, The Thing y Man. Aquí viven los efectos de las armas:
 *  - confundido (Segador): va mucho más lento y deambula sin atacar.
 *  - paralizado (Paralizador): se queda completamente quieto.
 */
public abstract class FolkMonster extends Monster {
    public static final int STATUS_NONE = 0;
    public static final int STATUS_CONFUSED = 1;
    public static final int STATUS_PARALYZED = 2;

    /** Modificador de velocidad al estar confundido (la velocidad real es proporcional a su cuadrado). */
    private static final double CONFUSED_SPEED_MODIFIER = 0.5D;

    private static final EntityDataAccessor<Integer> DATA_STATUS =
            SynchedEntityData.defineId(FolkMonster.class, EntityDataSerializers.INT);

    private int paralyzedTicks;
    private int confusedTicks;
    private int wanderCooldown;

    protected FolkMonster(EntityType<? extends FolkMonster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setInvulnerable(true);
        this.xpReward = 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATUS, STATUS_NONE);
    }

    /** Estado actual (sincronizado con el cliente para las animaciones). */
    public int getStatus() {
        return this.entityData.get(DATA_STATUS);
    }

    /** Paraliza a la criatura. 20 ticks = 1 segundo. */
    public void paralyze(int ticks) {
        this.paralyzedTicks = Math.max(this.paralyzedTicks, ticks);
    }

    /** La ralentiza y la vuelve tonta. 20 ticks = 1 segundo. */
    public void confuse(int ticks) {
        this.confusedTicks = Math.max(this.confusedTicks, ticks);
    }

    /** Devuelve true si este tick hay un efecto activo y la IA normal debe saltarse. */
    protected boolean handleStatus() {
        int status = STATUS_NONE;
        boolean skipAi = false;

        if (this.paralyzedTicks > 0) {
            this.paralyzedTicks--;
            status = STATUS_PARALYZED;
            FolkUtil.holdStill(this);
            if (this.tickCount % 4 == 0) {
                this.sparks();
            }
            skipAi = true;
        } else if (this.confusedTicks > 0) {
            this.confusedTicks--;
            status = STATUS_CONFUSED;
            this.wander();
            if (this.tickCount % 6 == 0) {
                this.soulParticles();
            }
            skipAi = true;
        }

        if (this.getStatus() != status) {
            this.entityData.set(DATA_STATUS, status);
        }
        return skipAi;
    }

    /** Deambula sin rumbo, despacio, ignorando al jugador. */
    private void wander() {
        if (--this.wanderCooldown > 0) return;
        this.wanderCooldown = 20 + this.random.nextInt(20);
        double x = this.getX() + (this.random.nextDouble() - 0.5D) * 12.0D;
        double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 12.0D;
        this.getNavigation().moveTo(x, this.getY(), z, CONFUSED_SPEED_MODIFIER);
        this.getLookControl().setLookAt(x, this.getEyeY(), z);
    }

    private void sparks() {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                    4, 0.3D, this.getBbHeight() * 0.3D, 0.3D, 0.1D);
        }
    }

    private void soulParticles() {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SOUL,
                    this.getX(), this.getY() + this.getBbHeight(), this.getZ(),
                    2, 0.2D, 0.1D, 0.2D, 0.02D);
        }
    }

    @Override
    protected void registerGoals() {
        // Sin goals de vanilla: toda la IA está en customServerAiStep().
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
