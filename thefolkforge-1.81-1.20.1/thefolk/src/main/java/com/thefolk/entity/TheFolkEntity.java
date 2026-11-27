package com.thefolk.entity;

import com.thefolk.FolkUtil;
import com.thefolk.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The Folk: un "dweller" con fases.
 *  Fase 1: solo mira.
 *  Fase 2: se acerca caminando.
 *  Fase 3: persigue rapido, rompe bloques y ataca durante 1 minuto.
 *  Fase 4 (modos ERROR / THE FOLK IS COMING): igual que la 3 al doble de velocidad.
 * Evento "Stalk": mira desde una ventana, silba, sale corriendo y rompe la puerta.
 */
public class TheFolkEntity extends FolkMonster {
    public static final int PHASE_WATCH = 1;
    public static final int PHASE_WALK = 2;
    public static final int PHASE_CHASE = 3;
    public static final int PHASE_FRENZY = 4;

    // ================= CONFIGURACIÓN =================
    public static final int CHASE_TICKS = 20 * 60;          // la persecución dura 1 minuto
    // Velocidades en bloques por segundo (el jugador camina a 4.3 y corre a 5.6)
    private static final double SPEED_WATCH = 0.20D;         // valor de atributo; en la fase 1 no se mueve
    private static final double BPS_WALK = 1.0D;             // fase 2
    private static final double BPS_CHASE = 4.5D;            // fase 3: más lento que un jugador corriendo
    private static final double BPS_FRENZY = 8.0D;           // fase 4
    private static final double BPS_STALK_RUN = 10.0D;       // evento Stalk: igual que The Thing
    private static final double ATTACK_DAMAGE = 6.0D;        // 3 corazones por golpe
    private static final double WATCH_VANISH_DISTANCE = 12.0D;

    // Evento "Stalk"
    private static final int STALK_WATCH_TICKS = 80;         // 4 s mirando
    private static final int STALK_WHISTLE_AT = 25;          // el silbido suena a los ~1.25 s
    private static final int STALK_RUN_TICKS = 300;          // 15 s corriendo antes de desaparecer
    // =================================================

    private static final int STALK_NONE = 0;
    private static final int STALK_WATCH = 1;
    private static final int STALK_RUN = 2;

    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(TheFolkEntity.class, EntityDataSerializers.INT);

    private int life;
    private int closeTicks;
    private int attackCooldown;
    private boolean directMove;

    private int stalkState = STALK_NONE;
    private int stalkTicks;
    private BlockPos stalkDoor;

    public TheFolkEntity(EntityType<? extends TheFolkEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, SPEED_WATCH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, 80.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PHASE, PHASE_WATCH);
    }

    public int getPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    public void setPhase(int phase) {
        this.entityData.set(DATA_PHASE, phase);
        double speed;
        if (phase == PHASE_FRENZY) {
            speed = FolkUtil.speedAttr(BPS_FRENZY);
        } else if (phase == PHASE_CHASE) {
            speed = FolkUtil.speedAttr(BPS_CHASE);
        } else if (phase == PHASE_WALK) {
            speed = FolkUtil.speedAttr(BPS_WALK);
        } else {
            speed = SPEED_WATCH;
        }
        AttributeInstance attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            attr.setBaseValue(speed);
        }
    }

    /** Activa el evento "Stalk" (lo llama el director justo después de hacerlo aparecer). */
    public void startStalk() {
        this.stalkState = STALK_WATCH;
        this.stalkTicks = 0;
        this.stalkDoor = null;
        this.setPhase(PHASE_WATCH);
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

        this.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (this.stalkState != STALK_NONE) {
            this.tickStalk(target);
            return;
        }

        double dist = this.distanceTo(target);
        switch (this.getPhase()) {
            case PHASE_WATCH:
                this.tickWatch(dist);
                break;
            case PHASE_WALK:
                this.tickWalk(target, dist);
                break;
            default:
                this.tickChase(target);
                break;
        }
    }

    // ---------------- Fases normales ----------------
    private void tickWatch(double dist) {
        this.getNavigation().stop();
        if (dist < WATCH_VANISH_DISTANCE || this.life > 2400) {
            this.vanish(true);
        }
    }

    private void tickWalk(Player target, double dist) {
        if (dist > 5.0D) {
            this.closeTicks = 0;
            if (this.life % 10 == 0 || this.getNavigation().isDone()) {
                this.getNavigation().moveTo(target, 1.0D);
            }
        } else {
            this.getNavigation().stop();
            if (++this.closeTicks > 60) {
                this.vanish(true);
                return;
            }
        }
        if (this.life > 6000) {
            this.vanish(true);
        }
    }

    private void tickChase(Player target) {
        if (this.life > CHASE_TICKS) {
            this.vanish(true);
            return;
        }
        this.chase(target, false);
    }

    /** Persigue al jugador. doorsOnly = solo rompe puertas (para el evento Stalk), no paredes. */
    private void chase(Player target, boolean doorsOnly) {
        if (this.life % 10 == 0 || (this.getNavigation().isDone() && this.life % 4 == 0)) {
            Path path = this.getNavigation().createPath(target, 0);
            this.directMove = path == null || !path.canReach();
            if (!this.directMove) {
                this.getNavigation().moveTo(path, 1.0D);
            }
        }
        if (this.directMove) {
            // Sin camino posible: va en línea recta.
            this.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);
        }

        if (this.life % (this.getPhase() == PHASE_FRENZY ? 2 : 4) == 0) {
            this.breakBlocks(target, doorsOnly);
        }

        if (this.attackCooldown <= 0
                && this.getBoundingBox().inflate(0.8D).intersects(target.getBoundingBox())) {
            this.swing(InteractionHand.MAIN_HAND);
            this.doHurtTarget(target);
            this.attackCooldown = 20;
        }
    }

    // ---------------- Evento Stalk ----------------
    private void tickStalk(Player target) {
        this.stalkTicks++;

        if (this.stalkState == STALK_WATCH) {
            this.getNavigation().stop();
            if (this.stalkTicks == STALK_WHISTLE_AT) {
                this.level().playSound(null, this.getX(), this.getY() + 1.5D, this.getZ(),
                        ModSounds.WHISTLE.get(), SoundSource.HOSTILE, 3.0F, 1.0F);
            }
            if (this.stalkTicks >= STALK_WATCH_TICKS) {
                this.stalkState = STALK_RUN;
                this.stalkTicks = 0;
                this.setPhase(PHASE_CHASE);          // brazos al frente
                AttributeInstance runSpeed = this.getAttribute(Attributes.MOVEMENT_SPEED);
                if (runSpeed != null) {
                    runSpeed.setBaseValue(FolkUtil.speedAttr(BPS_STALK_RUN));   // corre a la velocidad de The Thing
                }
                this.stalkDoor = this.findDoor(target);
            }
            return;
        }

        // Corriendo
        if (this.stalkTicks > STALK_RUN_TICKS) {
            this.vanish(true);
            return;
        }
        if (this.stalkDoor != null) {
            this.goToDoor();
            return;
        }
        this.chase(target, true);
    }

    /** La puerta más cercana al jugador (la del lugar donde está). */
    private BlockPos findDoor(Player target) {
        Level lvl = this.level();
        BlockPos center = target.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-16, -3, -16), center.offset(16, 3, 16))) {
            BlockState state = lvl.getBlockState(p);
            if (state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                double d = p.distSqr(center);
                if (d < bestDist) {
                    bestDist = d;
                    best = p.immutable();
                }
            }
        }
        return best;
    }

    private void goToDoor() {
        Level lvl = this.level();
        BlockState state = lvl.getBlockState(this.stalkDoor);
        if (!(state.getBlock() instanceof DoorBlock)) {
            this.stalkDoor = null;      // ya no existe
            return;
        }

        Vec3 center = Vec3.atCenterOf(this.stalkDoor);
        if (this.distanceToSqr(center) < 6.5D) {
            lvl.destroyBlock(this.stalkDoor, false, this);
            lvl.playSound(null, center.x, center.y, center.z,
                    SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 1.5F, 0.8F);
            this.stalkDoor = null;
            return;
        }

        if (this.stalkTicks % 10 == 1 || this.getNavigation().isDone()) {
            boolean ok = this.getNavigation().moveTo(center.x, this.stalkDoor.getY(), center.z, 1.0D);
            if (!ok) {
                this.getMoveControl().setWantedPosition(center.x, center.y, center.z, 1.0D);
            }
        }
    }

    // ---------------- Romper bloques ----------------
    private void breakBlocks(Player target, boolean doorsOnly) {
        Level lvl = this.level();
        if (!(lvl instanceof ServerLevel)) return;
        if (!lvl.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;

        Vec3 dir = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
        if (dir.lengthSqr() > 0.0001D) {
            dir = dir.normalize();
        }

        AABB box = this.getBoundingBox().inflate(0.35D, 0.0D, 0.35D).move(dir.x * 0.6D, 0.0D, dir.z * 0.6D);
        if (!doorsOnly && target.getY() < this.getY() - 1.5D) {
            box = box.expandTowards(0.0D, -1.0D, 0.0D); // si el jugador está más abajo, rompe el suelo
        }

        int broken = 0;
        for (BlockPos p : BlockPos.betweenClosed(
                Mth.floor(box.minX), Mth.floor(box.minY + 0.1D), Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            BlockPos pos = p.immutable();
            BlockState state = lvl.getBlockState(pos);
            if (state.isAir()) continue;
            if (state.getDestroySpeed(lvl, pos) < 0.0F) continue;   // bedrock, etc.
            if (!state.getFluidState().isEmpty()) continue;

            if (doorsOnly) {
                Block block = state.getBlock();
                if (!(block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock)) {
                    continue;
                }
                lvl.playSound(null, pos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE, 1.5F, 0.8F);
            }

            lvl.destroyBlock(pos, false, this);
            if (++broken >= 6) break;
        }
    }

    private void vanish(boolean effects) {
        if (effects && this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.4D, this.getZ(),
                    25, 0.3D, 0.8D, 0.3D, 0.02D);
            server.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 0.5F);
        }
        this.discard();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.stalkState == STALK_WATCH) return null;
        int phase = this.getPhase();
        if (phase >= PHASE_CHASE) return SoundEvents.WARDEN_NEARBY_CLOSEST;
        if (phase == PHASE_WALK) return SoundEvents.WARDEN_NEARBY_CLOSE;
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FolkPhase", this.getPhase());
        tag.putInt("FolkLife", this.life);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        int phase = tag.getInt("FolkPhase");
        this.setPhase(phase >= 1 && phase <= 4 ? phase : PHASE_WATCH);
        this.life = tag.getInt("FolkLife");
    }
}
