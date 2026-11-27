package com.thefolk.entity;

import com.thefolk.FolkLang;
import com.thefolk.FolkUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

/**
 * Man: aparece 1 bloque detrás de ti y se queda parado hasta que lo miras.
 * 3 segundos después de que lo miras, corre muy rápido hacia ti.
 * Mientras existe, el director hace que las demás criaturas aparezcan más seguido.
 */
public class TheManEntity extends FolkMonster {

    // ================= CONFIGURACIÓN =================
    private static final int WAIT_MAX_TICKS = 20 * 45;   // si en 45 s no lo miras, se va
    private static final int COUNTDOWN_TICKS = 20 * 3;   // 3 segundos desde que lo miras hasta que corre
    private static final int RUN_TICKS = 20 * 30;        // corre hasta 30 segundos
    private static final double BPS = 12.0D;             // bloques por segundo (The Thing va a 10)
    private static final double SPEED = FolkUtil.speedAttr(BPS);
    private static final double DAMAGE = 20.0D;          // 20 = 10 corazones
    // =================================================

    public static final int STATE_WAIT = 0;
    public static final int STATE_COUNTDOWN = 1;
    public static final int STATE_RUN = 2;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(TheManEntity.class, EntityDataSerializers.INT);

    private int life;
    private int attackCooldown;
    private boolean directMove;
    private int stateTicks;

    public TheManEntity(EntityType<? extends TheManEntity> type, Level level) {
        super(type, level);
        if (this.getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanOpenDoors(true);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, SPEED)
                .add(Attributes.ATTACK_DAMAGE, DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 80.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, STATE_WAIT);
    }

    /** Estado actual (sincronizado con el cliente para las animaciones). */
    public int getManState() {
        return this.entityData.get(DATA_STATE);
    }

    private void setManState(int state) {
        this.entityData.set(DATA_STATE, state);
        this.stateTicks = 0;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.life++;
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (this.handleStatus()) {
            return;      // paralizado o confundido por un arma
        }

        Player target = this.level().getNearestPlayer(this, 96.0D);
        if (target == null) {
            if (this.life > 200) {
                this.vanish(false);
            }
            return;
        }

        this.getLookControl().setLookAt(target, 90.0F, 90.0F);
        int state = this.getManState();

        // 1) Parado detrás de ti hasta que lo mires.
        if (state == STATE_WAIT) {
            FolkUtil.holdStill(this);
            if (FolkUtil.isPlayerLookingAt(target, this)) {
                this.setManState(STATE_COUNTDOWN);
            } else if (++this.stateTicks > WAIT_MAX_TICKS) {
                this.vanish(true);
            }
            return;
        }

        // 2) Te mira 3 segundos sin moverse.
        if (state == STATE_COUNTDOWN) {
            FolkUtil.holdStill(this);
            if (++this.stateTicks >= COUNTDOWN_TICKS) {
                this.setManState(STATE_RUN);
                this.directMove = false;
                if (target instanceof ServerPlayer sp) {
                    FolkUtil.sendTitle(sp,
                            Component.literal(FolkLang.get(sp, "run")).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                            2, 30, 10);
                }
            }
            return;
        }

        // 3) Corre hacia ti.
        if (++this.stateTicks > RUN_TICKS) {
            this.vanish(true);
            return;
        }

        if (this.life % 5 == 0) {
            FolkUtil.openDoorsNearby(this);
        }

        if (this.life % 10 == 0 || (this.getNavigation().isDone() && this.life % 4 == 0)) {
            Path path = this.getNavigation().createPath(target, 0);
            this.directMove = path == null || !path.canReach();
            if (!this.directMove) {
                this.getNavigation().moveTo(path, 1.0D);
            }
        }
        if (this.directMove) {
            this.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);
        }

        if (this.attackCooldown <= 0
                && this.getBoundingBox().inflate(0.8D).intersects(target.getBoundingBox())) {
            this.swing(InteractionHand.MAIN_HAND);
            this.doHurtTarget(target);
            this.attackCooldown = 8;
        }
    }

    private void vanish(boolean effects) {
        if (effects && this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(),
                    30, 0.3D, 0.8D, 0.3D, 0.02D);
        }
        this.discard();
    }
}
