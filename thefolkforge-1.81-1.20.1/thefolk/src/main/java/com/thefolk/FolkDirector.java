package com.thefolk;

import com.thefolk.entity.TheFolkEntity;
import com.thefolk.entity.TheManEntity;
import com.thefolk.entity.TheThingEntity;
import com.thefolk.entity.UknowEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controla el ciclo de fases, el modo del FolkConfig y cuándo aparece cada criatura y evento.
 * Todo lo editable está en el bloque CONFIGURACIÓN.
 */
@Mod.EventBusSubscriber(modid = TheFolkMod.MOD_ID)
public class FolkDirector {

    public static final int MODE_NORMAL = 0;
    public static final int MODE_ERROR = 1;
    public static final int MODE_COMING = 2;

    // ================= CONFIGURACIÓN =================
    /**
     * Multiplica TODAS las esperas entre apariciones (The Folk, The Thing, Man y Uknow).
     * 1.0 = como en la versión 1.80; 2.5 = apariciones 2,5 veces más espaciadas; 4.0 = todavía más raras.
     */
    private static final float SPAWN_DELAY_FACTOR = 2.5F;

    /** Días que dura cada fase [fase1, fase2, fase3, fase4] en modo Normal. */
    private static final int[] NORMAL_DAYS = {5, 8, 5, 0};
    /** Días por fase en modo ERROR y THE FOLK IS COMING (más rápido + fase 4). */
    private static final int[] ERROR_DAYS = {2, 4, 2, 2};

    /** Espera entre apariciones de The Folk, en ticks (20 ticks = 1 segundo). Índice = fase. */
    private static final int[] COOLDOWN_MIN = {0, 3000, 2400, 6000, 4800};
    private static final int[] COOLDOWN_MAX = {0, 6000, 6000, 12000, 9600};

    /** Distancia al jugador a la que aparece The Folk, en bloques. Índice = fase. */
    private static final int[] DIST_MIN = {0, 28, 22, 14, 14};
    private static final int[] DIST_MAX = {0, 45, 38, 24, 24};

    /** The Thing (solo THE FOLK IS COMING): espera entre apariciones y distancia. */
    private static final int THING_COOLDOWN_MIN = 3000;
    private static final int THING_COOLDOWN_MAX = 9000;
    private static final int THING_DIST_MIN = 12;
    private static final int THING_DIST_MAX = 22;

    /** Evento "Atrás": probabilidad de que ocurra en cada aparición de The Thing (fases 2 a 4). */
    private static final float BEHIND_CHANCE = 0.7F;

    /** Evento "Stalk": probabilidad de reemplazar una aparición de The Folk de fase 1-2 (modos ERROR y COMING). */
    private static final float STALK_CHANCE = 0.4F;

    /** Man (solo THE FOLK IS COMING): espera entre apariciones y distancia detrás del jugador (en bloques). */
    private static final int MAN_COOLDOWN_MIN = 12000;
    private static final int MAN_COOLDOWN_MAX = 24000;
    private static final double[] MAN_DISTANCES = {1.0D, 1.5D, 2.0D};

    /** Uknow (solo THE FOLK IS COMING): espera entre apariciones y distancia (en bloques). Aparece fuera de tu vista. */
    private static final int UKNOW_COOLDOWN_MIN = 6000;
    private static final int UKNOW_COOLDOWN_MAX = 12000;
    private static final int UKNOW_DIST_MIN = 26;
    private static final int UKNOW_DIST_MAX = 38;

    /** Cuando aparece Man, The Folk y The Thing aparecen BOOST_FACTOR veces más seguido durante BOOST_TICKS. */
    private static final int BOOST_TICKS = 3600;
    private static final int BOOST_FACTOR = 3;
    // =================================================

    private static final long DAY_TICKS = 24000L;
    private static final Map<UUID, Long> NEXT_SPAWN = new HashMap<>();
    private static final Map<UUID, Long> THING_NEXT = new HashMap<>();
    private static final Map<UUID, Long> MAN_NEXT = new HashMap<>();
    private static final Map<UUID, Long> UKNOW_NEXT = new HashMap<>();
    private static long boostUntil = 0L;

    // ---------- Modo ----------
    public static int currentMode(ServerLevel level) {
        return FolkModeData.get(level).getMode();
    }

    public static void setMode(ServerLevel level, int mode) {
        FolkModeData.get(level).setMode(mode);
        clearSchedules();
    }

    public static String modeName(int mode) {
        if (mode == MODE_ERROR) return "ERROR";
        if (mode == MODE_COMING) return "THE FOLK IS COMING";
        return "Normal";
    }

    private static void clearSchedules() {
        NEXT_SPAWN.clear();
        THING_NEXT.clear();
        MAN_NEXT.clear();
        UKNOW_NEXT.clear();
        boostUntil = 0L;
    }

    // ---------- Fases ----------
    private static int[] durations(int mode) {
        return mode == MODE_NORMAL ? NORMAL_DAYS : ERROR_DAYS;
    }

    private static int cycleLength(int mode) {
        int sum = 0;
        for (int d : durations(mode)) sum += d;
        return sum;
    }

    public static long currentDay(ServerLevel level) {
        return level.getDayTime() / DAY_TICKS;
    }

    public static int phaseOfDay(long day, int mode) {
        int[] d = durations(mode);
        int pos = (int) (day % cycleLength(mode));
        for (int i = 0; i < d.length; i++) {
            if (pos < d[i]) return i + 1;
            pos -= d[i];
        }
        return TheFolkEntity.PHASE_WATCH;
    }

    public static int dayInCycle(long day, int mode) {
        return (int) (day % cycleLength(mode)) + 1;
    }

    // ---------- Aumento de apariciones (lo activa Man) ----------
    public static boolean isBoosted(ServerLevel level) {
        return level.getGameTime() < boostUntil;
    }

    public static void startSpawnBoost(ServerLevel level) {
        long now = level.getGameTime();
        boostUntil = now + BOOST_TICKS;
        // Acorta las esperas que ya estaban programadas.
        NEXT_SPAWN.replaceAll((id, t) -> now + Math.max(0L, t - now) / BOOST_FACTOR);
        THING_NEXT.replaceAll((id, t) -> now + Math.max(0L, t - now) / BOOST_FACTOR);
        UKNOW_NEXT.replaceAll((id, t) -> now + Math.max(0L, t - now) / BOOST_FACTOR);
    }

    private static int scaled(ServerLevel level, int cooldown) {
        int slowed = Math.round(cooldown * SPAWN_DELAY_FACTOR);
        return isBoosted(level) ? Math.max(100, slowed / BOOST_FACTOR) : slowed;
    }

    // ---------- Tick ----------
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % 40 != 0) return;
        if (player.isCreative() || player.isSpectator()) return;

        ServerLevel level = player.serverLevel();
        if (level.dimension() != Level.OVERWORLD) return;
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;

        int mode = currentMode(level);
        tickFolk(player, level, mode);
        if (mode == MODE_COMING) {
            tickThing(player, level);
            tickMan(player, level);
            tickUknow(player, level);
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        clearSchedules();
    }

    private static void tickFolk(ServerPlayer player, ServerLevel level, int mode) {
        long now = level.getGameTime();
        Long next = NEXT_SPAWN.get(player.getUUID());
        if (next == null) {
            NEXT_SPAWN.put(player.getUUID(), now + Math.round((1200 + level.random.nextInt(2400)) * SPAWN_DELAY_FACTOR));
            return;
        }
        if (now < next) return;

        int phase = phaseOfDay(currentDay(level), mode);
        if (phase < TheFolkEntity.PHASE_CHASE && !isDark(level, player)) return;

        boolean spawned = false;
        if (mode != MODE_NORMAL && phase <= TheFolkEntity.PHASE_WALK && level.random.nextFloat() < STALK_CHANCE) {
            spawned = trySpawnStalk(player, false);
        }
        if (!spawned) {
            spawned = trySpawn(player, phase, false);
        }

        if (spawned) {
            int span = COOLDOWN_MAX[phase] - COOLDOWN_MIN[phase] + 1;
            int cd = COOLDOWN_MIN[phase] + level.random.nextInt(span);
            NEXT_SPAWN.put(player.getUUID(), now + scaled(level, cd));
        } else {
            NEXT_SPAWN.put(player.getUUID(), now + 200);
        }
    }

    private static void tickThing(ServerPlayer player, ServerLevel level) {
        long now = level.getGameTime();
        Long next = THING_NEXT.get(player.getUUID());
        if (next == null) {
            THING_NEXT.put(player.getUUID(), now + Math.round((1200 + level.random.nextInt(2400)) * SPAWN_DELAY_FACTOR));
            return;
        }
        if (now < next) return;

        if (trySpawnThing(player, false)) {
            int span = THING_COOLDOWN_MAX - THING_COOLDOWN_MIN + 1;
            int cd = THING_COOLDOWN_MIN + level.random.nextInt(span);
            THING_NEXT.put(player.getUUID(), now + scaled(level, cd));
        } else {
            THING_NEXT.put(player.getUUID(), now + 200);
        }
    }

    private static void tickMan(ServerPlayer player, ServerLevel level) {
        long now = level.getGameTime();
        Long next = MAN_NEXT.get(player.getUUID());
        if (next == null) {
            MAN_NEXT.put(player.getUUID(), now + Math.round((3600 + level.random.nextInt(3600)) * SPAWN_DELAY_FACTOR));
            return;
        }
        if (now < next) return;

        if (trySpawnMan(player, false)) {
            int span = MAN_COOLDOWN_MAX - MAN_COOLDOWN_MIN + 1;
            MAN_NEXT.put(player.getUUID(), now + Math.round((MAN_COOLDOWN_MIN + level.random.nextInt(span)) * SPAWN_DELAY_FACTOR));
        } else {
            MAN_NEXT.put(player.getUUID(), now + 200);
        }
    }

    private static void tickUknow(ServerPlayer player, ServerLevel level) {
        long now = level.getGameTime();
        Long next = UKNOW_NEXT.get(player.getUUID());
        if (next == null) {
            UKNOW_NEXT.put(player.getUUID(), now + Math.round((2400 + level.random.nextInt(2400)) * SPAWN_DELAY_FACTOR));
            return;
        }
        if (now < next) return;

        if (trySpawnUknow(player, false)) {
            int span = UKNOW_COOLDOWN_MAX - UKNOW_COOLDOWN_MIN + 1;
            int cd = UKNOW_COOLDOWN_MIN + level.random.nextInt(span);
            UKNOW_NEXT.put(player.getUUID(), now + scaled(level, cd));
        } else {
            UKNOW_NEXT.put(player.getUUID(), now + 200);
        }
    }

    private static boolean isDark(ServerLevel level, ServerPlayer player) {
        long t = level.getDayTime() % DAY_TICKS;
        boolean night = t >= 13000L && t <= 23000L;
        return night || level.getMaxLocalRawBrightness(player.blockPosition()) <= 4;
    }

    // ---------- Aparición de The Folk ----------
    public static boolean trySpawn(ServerPlayer player, int phase, boolean force) {
        return spawnFolk(player, phase, force) != null;
    }

    private static boolean folkNearby(ServerLevel level, ServerPlayer player) {
        return !level.getEntitiesOfClass(TheFolkEntity.class, player.getBoundingBox().inflate(128.0D)).isEmpty();
    }

    /** Hace aparecer a The Folk cerca del jugador. force = ignora límites (para el comando). */
    private static TheFolkEntity spawnFolk(ServerPlayer player, int phase, boolean force) {
        ServerLevel level = player.serverLevel();
        if (!force && folkNearby(level, player)) {
            return null;
        }

        RandomSource rnd = level.random;
        Vec3 eye = player.getEyePosition();

        for (int attempt = 0; attempt < 40; attempt++) {
            double angle = rnd.nextDouble() * Math.PI * 2.0D;
            double dist = DIST_MIN[phase] + rnd.nextDouble() * (DIST_MAX[phase] - DIST_MIN[phase]);
            int x = Mth.floor(player.getX() + Math.cos(angle) * dist);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * dist);

            BlockPos spot = findStandingSpot(level, x, z, player.blockPosition().getY());
            if (spot == null) continue;

            boolean relax = force && attempt >= 25;
            if (!relax) {
                boolean visible = isVisible(level, player, eye, headOf(spot));
                // Fases 3 y 4 aparecen escondido; las otras aparecen a la vista.
                if (phase >= TheFolkEntity.PHASE_CHASE && visible) continue;
                if (phase < TheFolkEntity.PHASE_CHASE && !visible) continue;
            }

            TheFolkEntity folk = ModEntities.THE_FOLK.get().create(level);
            if (folk == null) return null;
            folk.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, rnd.nextFloat() * 360.0F, 0.0F);
            if (!level.noCollision(folk)) continue;

            folk.setPhase(phase);
            level.addFreshEntity(folk);
            return folk;
        }
        return null;
    }

    // ---------- Evento "Stalk" ----------
    /** The Folk aparece detrás de una ventana (o asomándose por una esquina) y luego sale a por ti. */
    public static boolean trySpawnStalk(ServerPlayer player, boolean force) {
        ServerLevel level = player.serverLevel();
        if (!force && folkNearby(level, player)) {
            return false;
        }

        BlockPos spot = findWindowSpot(level, player);
        if (spot == null) {
            spot = findPeekSpot(level, player);
        }
        if (spot != null) {
            TheFolkEntity folk = ModEntities.THE_FOLK.get().create(level);
            if (folk != null) {
                folk.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
                if (level.noCollision(folk)) {
                    folk.setPhase(TheFolkEntity.PHASE_WATCH);
                    folk.startStalk();
                    level.addFreshEntity(folk);
                    return true;
                }
            }
        }

        if (force) {
            // Para pruebas con el comando: si no hay ventana, aparece a la vista y hace el evento igual.
            TheFolkEntity folk = spawnFolk(player, TheFolkEntity.PHASE_WATCH, true);
            if (folk != null) {
                folk.startStalk();
                return true;
            }
        }
        return false;
    }

    private static boolean isWindow(BlockState state) {
        Block block = state.getBlock();
        return block instanceof AbstractGlassBlock
                || (block instanceof IronBarsBlock && !state.is(Blocks.IRON_BARS));
    }

    private static BlockPos findWindowSpot(ServerLevel level, ServerPlayer player) {
        BlockPos center = player.blockPosition();
        Vec3 eye = player.getEyePosition();

        List<BlockPos> windows = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-9, -2, -9), center.offset(9, 3, 9))) {
            if (isWindow(level.getBlockState(p))) {
                windows.add(p.immutable());
            }
        }
        Collections.shuffle(windows);

        int tries = 0;
        for (BlockPos w : windows) {
            if (tries++ > 30) break;
            double dist = Math.sqrt(w.distSqr(center));
            if (dist < 3.0D || dist > 10.0D) continue;

            Vec3 to = Vec3.atCenterOf(w);
            BlockHitResult hit = level.clip(new ClipContext(eye, to, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, player));
            // El jugador tiene que ver la ventana directamente.
            if (hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(w)) continue;

            Vec3 d = to.subtract(eye);
            Direction dir = Direction.getNearest(d.x, 0.0D, d.z);
            BlockPos out = w.relative(dir, 1);   // el bloque justo al otro lado de la ventana
            BlockPos spot = findStandingSpotRange(level, out.getX(), out.getZ(), w.getY(), w.getY() - 3);
            if (spot != null) return spot;
        }
        return null;
    }

    /** Lugar escondido tras una pared del que solo se ve un borde, como si se asomara por una esquina. */
    private static BlockPos findPeekSpot(ServerLevel level, ServerPlayer player) {
        RandomSource rnd = level.random;
        Vec3 eye = player.getEyePosition();

        for (int attempt = 0; attempt < 60; attempt++) {
            double angle = rnd.nextDouble() * Math.PI * 2.0D;
            double dist = 6.0D + rnd.nextDouble() * 8.0D;
            int x = Mth.floor(player.getX() + Math.cos(angle) * dist);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * dist);

            BlockPos spot = findStandingSpot(level, x, z, player.blockPosition().getY());
            if (spot == null) continue;

            Vec3 head = headOf(spot);
            if (isVisible(level, player, eye, head)) continue;   // si se ve entero no es una esquina

            Vec3 dir = head.subtract(eye);
            Vec3 side = new Vec3(-dir.z, 0.0D, dir.x);
            if (side.lengthSqr() < 1.0E-4D) continue;
            side = side.normalize().scale(0.25D);
            if (isVisible(level, player, eye, head.add(side)) || isVisible(level, player, eye, head.subtract(side))) {
                return spot;
            }
        }
        return null;
    }

    // ---------- Aparición de The Thing ----------
    public static boolean trySpawnThing(ServerPlayer player, boolean force) {
        ServerLevel level = player.serverLevel();
        if (!force && !level.getEntitiesOfClass(TheThingEntity.class, player.getBoundingBox().inflate(128.0D)).isEmpty()) {
            return false;
        }

        RandomSource rnd = level.random;
        Vec3 eye = player.getEyePosition();

        for (int attempt = 0; attempt < 40; attempt++) {
            double angle = rnd.nextDouble() * Math.PI * 2.0D;
            double dist = THING_DIST_MIN + rnd.nextDouble() * (THING_DIST_MAX - THING_DIST_MIN);
            int x = Mth.floor(player.getX() + Math.cos(angle) * dist);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * dist);

            BlockPos spot = findStandingSpot(level, x, z, player.blockPosition().getY());
            if (spot == null) continue;

            boolean relax = force && attempt >= 25;
            if (!relax && isVisible(level, player, eye, headOf(spot))) continue;   // aparece escondido

            TheThingEntity thing = ModEntities.THE_THING.get().create(level);
            if (thing == null) return false;
            thing.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, rnd.nextFloat() * 360.0F, 0.0F);
            if (!level.noCollision(thing)) continue;

            thing.setCustomName(Component.literal("Im C0MING").withStyle(ChatFormatting.RED));
            thing.setCustomNameVisible(true);

            // Evento "Atrás": solo si el ciclo está en las fases 2 a 4 (o si se invoca con el comando).
            int cyclePhase = phaseOfDay(currentDay(level), currentMode(level));
            boolean phaseOk = cyclePhase >= TheFolkEntity.PHASE_WALK && cyclePhase <= TheFolkEntity.PHASE_FRENZY;
            if (force || (phaseOk && rnd.nextFloat() < BEHIND_CHANCE)) {
                thing.enableBehindEvent();
            }

            level.addFreshEntity(thing);

            // Texto rojo en pantalla
            FolkUtil.sendTitle(player,
                    Component.literal(FolkLang.get(player, "coming")).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                    5, 50, 15);
            return true;
        }
        return false;
    }

    // ---------- Aparición de Man ----------
    /** Man aparece 1 bloque detrás del jugador y se queda parado hasta que lo mire. */
    public static boolean trySpawnMan(ServerPlayer player, boolean force) {
        ServerLevel level = player.serverLevel();
        if (!force && !level.getEntitiesOfClass(TheManEntity.class, player.getBoundingBox().inflate(128.0D)).isEmpty()) {
            return false;
        }

        TheManEntity man = ModEntities.THE_MAN.get().create(level);
        if (man == null) return false;

        Vec3 spot = FolkUtil.findSpotBehind(player, man, MAN_DISTANCES);
        if (spot == null) return false;

        man.moveTo(spot.x, spot.y, spot.z, player.getYRot(), 0.0F);
        level.addFreshEntity(man);
        startSpawnBoost(level);
        return true;
    }

    // ---------- Aparición de Uknow ----------
    public static boolean trySpawnUknow(ServerPlayer player, boolean force) {
        ServerLevel level = player.serverLevel();
        if (!force && !level.getEntitiesOfClass(UknowEntity.class, player.getBoundingBox().inflate(128.0D)).isEmpty()) {
            return false;
        }

        RandomSource rnd = level.random;
        Vec3 eye = player.getEyePosition();

        for (int attempt = 0; attempt < 40; attempt++) {
            double angle = rnd.nextDouble() * Math.PI * 2.0D;
            double dist = UKNOW_DIST_MIN + rnd.nextDouble() * (UKNOW_DIST_MAX - UKNOW_DIST_MIN);
            int x = Mth.floor(player.getX() + Math.cos(angle) * dist);
            int z = Mth.floor(player.getZ() + Math.sin(angle) * dist);

            BlockPos spot = findStandingSpot(level, x, z, player.blockPosition().getY());
            if (spot == null) continue;

            boolean relax = force && attempt >= 25;
            if (!relax && isVisible(level, player, eye, headOf(spot))) continue;   // aparece fuera de tu vista

            UknowEntity uknow = ModEntities.UKNOW.get().create(level);
            if (uknow == null) return false;
            uknow.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D, rnd.nextFloat() * 360.0F, 0.0F);
            if (!level.noCollision(uknow)) continue;

            level.addFreshEntity(uknow);
            // Un rugido que se oye de lejos avisa de que anda cerca.
            level.playSound(null, uknow.getX(), uknow.getY(), uknow.getZ(),
                    SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 3.0F, 0.7F);
            return true;
        }
        return false;
    }

    // ---------- Ayudas ----------
    private static Vec3 headOf(BlockPos spot) {
        return new Vec3(spot.getX() + 0.5D, spot.getY() + 2.0D, spot.getZ() + 0.5D);
    }

    private static boolean isVisible(ServerLevel level, ServerPlayer player, Vec3 eye, Vec3 target) {
        return level.clip(new ClipContext(eye, target, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;
    }

    private static BlockPos findStandingSpot(ServerLevel level, int x, int z, int aroundY) {
        int minY = Math.max(level.getMinBuildHeight() + 1, aroundY - 10);
        int maxY = Math.min(level.getMaxBuildHeight() - 4, aroundY + 10);
        return findStandingSpotRange(level, x, z, maxY, minY);
    }

    /** Busca de arriba hacia abajo un lugar con suelo firme y 3 bloques de aire. */
    private static BlockPos findStandingSpotRange(ServerLevel level, int x, int z, int fromY, int toY) {
        if (!level.hasChunkAt(new BlockPos(x, fromY, z))) return null;
        int top = Math.min(fromY, level.getMaxBuildHeight() - 4);
        int bottom = Math.max(toY, level.getMinBuildHeight() + 1);
        for (int y = top; y >= bottom; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos below = pos.below();
            if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) continue;
            if (level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.above()).isAir()
                    && level.getBlockState(pos.above(2)).isAir()) {
                return pos;
            }
        }
        return null;
    }
}
