package com.thefolk.entity;

import com.thefolk.FolkLang;
import com.thefolk.FolkUtil;
import com.thefolk.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * The Thing: aparece solo 20 segundos, es más rápida que The Folk, abre puertas,
 * hace ruidos saturados y hace 12 corazones de daño.
 *
 * Evento "Atrás": aparece detrás de ti sin moverse (con texto y voz). Si lo miras, se queda
 * parada 2 segundos y luego te persigue más rápido. Si no lo miras, se va sin moverse.
 */
public class TheThingEntity extends FolkMonster {

    // ================= CONFIGURACIÓN =================
    public static final int LIFETIME_TICKS = 20 * 20;   // 20 segundos
    private static final double BPS = 10.0D;             // bloques por segundo (The Folk en la fase 4 va a 8)
    private static final double SPEED = FolkUtil.speedAttr(BPS);
    private static final double DAMAGE = 24.0D;          // 24 = 12 corazones
    private static final float NOISE_VOLUME = 5.0F;      // más volumen = se oye desde más lejos

    // Evento "Atrás"
    private static final int BEHIND_MIN_TICK = 60;       // ocurre entre los 3 y los 7 segundos
    private static final int BEHIND_MAX_TICK = 140;
    private static final int BEHIND_WAIT_TICKS = 120;    // 6 s esperando a que la mires; si no, se va
    private static final int BEHIND_FREEZE_TICKS = 40;   // 2 s parada después de que la miras
    private static final double BEHIND_SPEED_MULTIPLIER = 1.5D;
    private static final double[] BEHIND_DISTANCES = {2.0D, 1.5D, 2.5D, 3.0D};
    // =================================================

    private static final int BEHIND_NONE = 0;
    private static final int BEHIND_WAIT = 1;
    private static final int BEHIND_FREEZE = 2;

    private static final SoundEvent[] NOISES = {
            SoundEvents.WARDEN_ROAR,
            SoundEvents.ENDERMAN_SCREAM,
            SoundEvents.GHAST_SCREAM,
            SoundEvents.RAVAGER_ROAR,
            SoundEvents.ELDER_GUARDIAN_CURSE,
            SoundEvents.ENDER_DRAGON_GROWL
    };

    private int life;
    private int attackCooldown;
    private boolean directMove;

    private int behindTick = -1;     // -1 = este Thing no hará el evento
    private boolean behindDone;
    private int behindState = BEHIND_NONE;
    private int behindTicks;

    public TheThingEntity(EntityType<? extends TheThingEntity> type, Level level) {
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

    /** Activa el evento "Atrás" para esta aparición (lo llama el director). */
    public void enableBehindEvent() {
        this.behindTick = BEHIND_MIN_TICK + this.random.nextInt(BEHIND_MAX_TICK - BEHIND_MIN_TICK + 1);
    }

    @Override
    protected void registerGoals() {
        // Sin goals de vanilla: toda la IA está en customServerAiStep().
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.life++;
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (this.life > LIFETIME_TICKS) {
            this.vanish();
            return;
        }

        if (this.handleStatus()) {
            return;      // paralizada o confundida por un arma
        }

        // Mientras espera a que la mires se queda en silencio.
        if (this.life % 3 == 0 && this.behindState != BEHIND_WAIT) {
            this.playNoise();
        }

        Player target = this.level().getNearestPlayer(this, 96.0D);
        if (target == null) return;

        this.getLookControl().setLookAt(target, 60.0F, 60.0F);

        if (this.behindState != BEHIND_NONE) {
            this.tickBehind(target);
            return;
        }
        if (this.behindTick >= 0 && !this.behindDone && this.life >= this.behindTick) {
            this.startBehind(target);
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
            this.attackCooldown = 10;
        }
    }

    /** Evento "Atrás": aparece detrás del jugador, con texto y voz, y espera sin moverse. */
    private void startBehind(Player target) {
        this.behindDone = true;

        Vec3 spot = FolkUtil.findSpotBehind(target, this, BEHIND_DISTANCES);
        if (spot == null) {
            return;     // no hay sitio detrás del jugador: se salta el evento y persigue normal
        }
        this.moveTo(spot.x, spot.y, spot.z, target.getYRot(), 0.0F);
        FolkUtil.holdStill(this);

        this.behindState = BEHIND_WAIT;
        this.behindTicks = 0;

        if (target instanceof ServerPlayer sp) {
            FolkUtil.sendTitle(sp,
                    Component.literal(FolkLang.get(sp, "behind")).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                    3, 40, 10);
            SoundEvent voice = FolkLang.isEnglish(sp)
                    ? ModSounds.VOICE_BEHIND_EN.get()
                    : ModSounds.VOICE_BEHIND_ES.get();
            this.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), voice, SoundSource.HOSTILE, 4.0F, 0.8F);
        }
    }

    private void tickBehind(Player target) {
        FolkUtil.holdStill(this);
        this.behindTicks++;

        if (this.behindState == BEHIND_WAIT) {
            if (FolkUtil.isPlayerLookingAt(target, this)) {
                this.behindState = BEHIND_FREEZE;      // la miraste: empieza la cuenta de 2 segundos
                this.behindTicks = 0;
            } else if (this.behindTicks > BEHIND_WAIT_TICKS) {
                this.vanish();                         // no la miraste: se va sin moverse
            }
            return;
        }

        if (this.behindTicks >= BEHIND_FREEZE_TICKS) {
            this.behindState = BEHIND_NONE;
            AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) {
                speed.setBaseValue(FolkUtil.speedAttr(BPS * BEHIND_SPEED_MULTIPLIER));   // ahora corre más rápido
            }
            this.directMove = false;
        }
    }

    /** Ruido saturado: varios gritos a todo volumen y tono bajo, superpuestos. */
    private void playNoise() {
        for (int i = 0; i < 2; i++) {
            SoundEvent sound = NOISES[this.random.nextInt(NOISES.length)];
            float pitch = 0.5F + this.random.nextFloat() * 0.6F;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    sound, SoundSource.HOSTILE, NOISE_VOLUME, pitch);
        }
    }

    private void vanish() {
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(),
                    30, 0.4D, 0.8D, 0.4D, 0.02D);
        }
        this.discard();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }
}
