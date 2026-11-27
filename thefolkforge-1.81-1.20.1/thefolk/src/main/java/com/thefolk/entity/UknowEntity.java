package com.thefolk.entity;

import com.thefolk.FolkLang;
import com.thefolk.FolkUtil;
import com.thefolk.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Uknow: bestia cuadrúpeda con cuernos que mata a todo lo que ve.
 *  - Camina a 5.8 bloques por segundo y se abalanza al doble de velocidad.
 *  - Si atrapa a un jugador lo sube a su lomo (queda inmovilizado) y le hace daño cada segundo.
 *  - El jugador se libera pulsando ESPACIO 3 veces en 2 segundos y queda con el efecto Sangrado.
 *  - Las armas (Segador y Paralizador) también liberan a quien sujeta.
 */
public class UknowEntity extends FolkMonster {

    // ================= CONFIGURACIÓN =================
    private static final double PROWL_BPS = 5.8D;             // bloques por segundo caminando
    private static final double POUNCE_MULTIPLIER = 2.0D;     // al abalanzarse va al doble
    private static final int LIFETIME_TICKS = 20 * 90;        // desaparece a los 90 segundos
    private static final double SIGHT_RANGE = 32.0D;
    private static final double POUNCE_RANGE = 12.0D;
    private static final int POUNCE_TICKS = 30;               // el salto dura 1.5 segundos
    private static final int POUNCE_COOLDOWN_TICKS = 80;      // 4 s entre saltos
    private static final int RELEASE_COOLDOWN_TICKS = 30;     // 1.5 s sin atacar ni saltar después de soltar a alguien
    private static final float HOLD_DAMAGE = 6.0F;            // 6 = 3 corazones
    private static final int HOLD_DAMAGE_INTERVAL = 20;       // cada 1 segundo
    private static final int ESCAPE_PRESSES = 3;              // pulsaciones de ESPACIO...
    private static final int ESCAPE_WINDOW_TICKS = 40;        // ...dentro de 2 segundos
    private static final int BLEED_TICKS = 20 * 10;           // el Sangrado dura 10 segundos
    // =================================================

    public static final int STATE_PROWL = 0;
    public static final int STATE_POUNCE = 1;
    public static final int STATE_HOLD = 2;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(UknowEntity.class, EntityDataSerializers.INT);

    private int life;
    private int pounceTicks;
    private int pounceCooldown;
    private int attackCooldown;
    private int holdTicks;
    private int roamCooldown;
    private LivingEntity prey;
    private ServerPlayer held;
    private final List<Long> pressTimes = new ArrayList<>();

    public UknowEntity(EntityType<? extends UknowEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, FolkUtil.speedAttr(PROWL_BPS))
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, STATE_PROWL);
    }

    /** Estado actual (sincronizado con el cliente para las animaciones). */
    public int getUknowState() {
        return this.entityData.get(DATA_STATE);
    }

    private void setUknowState(int state) {
        if (this.getUknowState() != state) {
            this.entityData.set(DATA_STATE, state);
        }
    }

    /** true si está sujetando a esta entidad (y sigue viva). */
    public boolean isHolding(Entity entity) {
        return this.held != null && this.held == entity && this.held.isAlive();
    }

    private void setSpeedBps(double bps) {
        AttributeInstance attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.setBaseValue(FolkUtil.speedAttr(bps));
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.life++;
        if (this.pounceCooldown > 0) {
            this.pounceCooldown--;
        }
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (this.life > LIFETIME_TICKS) {
            this.vanish();
            return;
        }

        // Paralizado o confundido por un arma: suelta a su presa.
        if (this.handleStatus()) {
            if (this.held != null) {
                this.release(false);
            }
            if (this.getUknowState() != STATE_PROWL) {
                this.setUknowState(STATE_PROWL);
                this.setSpeedBps(PROWL_BPS);
            }
            return;
        }

        if (this.held != null) {
            this.tickHold();
            return;
        }
        if (this.getUknowState() == STATE_HOLD) {
            this.setUknowState(STATE_PROWL);
        }

        LivingEntity target = this.findPrey();
        if (target == null) {
            if (this.getUknowState() == STATE_POUNCE) {
                this.endPounce();
            }
            this.roam();
            return;
        }

        this.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Contacto: mata a un animal o atrapa a un jugador.
        if (this.attackCooldown <= 0 && this.getBoundingBox().inflate(0.4D).intersects(target.getBoundingBox())) {
            this.attack(target);
            return;
        }

        if (this.getUknowState() == STATE_POUNCE) {
            if (--this.pounceTicks <= 0) {
                this.endPounce();
            } else {
                this.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);
            }
            return;
        }

        if (this.pounceCooldown <= 0
                && this.distanceToSqr(target) <= POUNCE_RANGE * POUNCE_RANGE) {
            this.startPounce();
            return;
        }

        if (this.life % 10 == 0 || (this.getNavigation().isDone() && this.life % 4 == 0)) {
            this.getNavigation().moveTo(target, 1.0D);
        }
    }

    // ---------------- Presa ----------------
    private LivingEntity findPrey() {
        if (this.prey == null || !this.prey.isAlive() || this.life % 5 == 0) {
            this.prey = this.scanForPrey();
        }
        return this.prey;
    }

    private LivingEntity scanForPrey() {
        AABB area = this.getBoundingBox().inflate(SIGHT_RANGE);
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : this.level().getEntitiesOfClass(LivingEntity.class, area, this::isValidPrey)) {
            double d = this.distanceToSqr(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    /** Todo lo que ve y no es una de las criaturas del mod. */
    private boolean isValidPrey(LivingEntity e) {
        if (e == this || !e.isAlive()) return false;
        if (e instanceof FolkMonster || e instanceof ArmorStand) return false;
        if (e instanceof Player p && (p.isCreative() || p.isSpectator())) return false;
        return this.hasLineOfSight(e);
    }

    private void roam() {
        if (--this.roamCooldown > 0) return;
        this.roamCooldown = 40 + this.random.nextInt(40);
        double x = this.getX() + (this.random.nextDouble() - 0.5D) * 24.0D;
        double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 24.0D;
        this.getNavigation().moveTo(x, this.getY(), z, 0.6D);
    }

    // ---------------- Abalanzarse ----------------
    private void startPounce() {
        this.setUknowState(STATE_POUNCE);
        this.pounceTicks = POUNCE_TICKS;
        this.setSpeedBps(PROWL_BPS * POUNCE_MULTIPLIER);
        this.getNavigation().stop();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, 0.35D, motion.z);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 2.0F, 0.8F);
    }

    private void endPounce() {
        this.setUknowState(STATE_PROWL);
        this.setSpeedBps(PROWL_BPS);
        this.pounceCooldown = POUNCE_COOLDOWN_TICKS;
    }

    // ---------------- Ataque ----------------
    private void attack(LivingEntity target) {
        if (target instanceof ServerPlayer player) {
            this.grab(player);
        } else {
            target.hurt(this.damageSources().mobAttack(this), 1000.0F);
            this.swing(InteractionHand.MAIN_HAND);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 1.5F, 0.8F);
        }
    }

    private void grab(ServerPlayer player) {
        if (this.held != null) return;
        if (!player.startRiding(this, true)) return;

        this.held = player;
        this.holdTicks = 0;
        this.pressTimes.clear();
        this.setUknowState(STATE_HOLD);
        this.setSpeedBps(PROWL_BPS);
        FolkUtil.holdStill(this);

        FolkUtil.sendTitle(player,
                Component.literal(FolkLang.get(player, "struggle")).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                0, 60, 10);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 2.0F, 0.6F);
    }

    private void tickHold() {
        ServerPlayer player = this.held;
        if (player == null || !player.isAlive() || player.getVehicle() != this) {
            this.held = null;                       // murió o se soltó por otro motivo
            this.pounceCooldown = RELEASE_COOLDOWN_TICKS;
            this.attackCooldown = RELEASE_COOLDOWN_TICKS;
            this.setUknowState(STATE_PROWL);
            return;
        }

        FolkUtil.holdStill(this);
        this.holdTicks++;
        if (this.holdTicks % HOLD_DAMAGE_INTERVAL == 0) {
            player.hurt(this.damageSources().mobAttack(this), HOLD_DAMAGE);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 1.0F, 0.9F);
        }
    }

    /** El jugador atrapado pulsó ESPACIO (lo avisa el cliente). */
    public void struggle(ServerPlayer player) {
        if (this.held != player) return;

        long now = this.level().getGameTime();
        this.pressTimes.add(now);
        this.pressTimes.removeIf(t -> now - t > ESCAPE_WINDOW_TICKS);

        player.displayClientMessage(Component.literal(
                String.format(FolkLang.get(player, "struggle.count"), this.pressTimes.size())), true);

        if (this.pressTimes.size() >= ESCAPE_PRESSES) {
            this.release(true);
        }
    }

    /** Suelta al jugador. escaped = true si logró escapar (queda con Sangrado). */
    private void release(boolean escaped) {
        ServerPlayer player = this.held;
        if (player == null) return;

        this.held = null;           // primero, para que no se cancele el bajarse
        player.stopRiding();
        this.pressTimes.clear();
        this.pounceCooldown = RELEASE_COOLDOWN_TICKS;
        this.attackCooldown = RELEASE_COOLDOWN_TICKS;
        this.setUknowState(STATE_PROWL);
        this.setSpeedBps(PROWL_BPS);

        if (escaped) {
            Vec3 away = player.position().subtract(this.position());
            Vec3 push = new Vec3(away.x, 0.0D, away.z);
            push = push.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : push.normalize();
            player.setDeltaMovement(push.x * 0.8D, 0.35D, push.z * 0.8D);
            player.hurtMarked = true;
            player.addEffect(new MobEffectInstance(ModEffects.BLEEDING.get(), BLEED_TICKS, 0));
            this.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 0.8F);
        }
    }

    private void vanish() {
        this.release(false);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8D, this.getZ(),
                    30, 0.5D, 0.5D, 0.5D, 0.02D);
        }
        this.discard();
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        this.held = null;           // al desaparecer, nunca debe retener al jugador
        super.remove(reason);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.3F, 0.8F);
    }
}
