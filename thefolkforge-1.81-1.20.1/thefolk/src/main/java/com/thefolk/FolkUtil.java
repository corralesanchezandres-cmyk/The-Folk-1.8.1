package com.thefolk;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Funciones compartidas por las criaturas. */
public class FolkUtil {

    /**
     * Convierte bloques por segundo en el valor de atributo de velocidad que hay que ponerle a una criatura.
     * En los monstruos, bloques/s = 43,17 × atributo² (estimación, no medida en el juego).
     */
    public static double speedAttr(double blocksPerSecond) {
        return Math.sqrt(blocksPerSecond / 43.17D);
    }

    /** Muestra un texto grande en pantalla al jugador. */
    public static void sendTitle(ServerPlayer player, Component text, int fadeIn, int stay, int fadeOut) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
        player.connection.send(new ClientboundSetTitleTextPacket(text));
    }

    /** Deja a la criatura completamente quieta. */
    public static void holdStill(Mob mob) {
        mob.getNavigation().stop();
        mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 1.0D);
        mob.setDeltaMovement(0.0D, mob.getDeltaMovement().y, 0.0D);
    }

    /** true si el jugador tiene a la entidad delante (dentro de su campo de visión) y sin paredes de por medio. */
    public static boolean isPlayerLookingAt(Player player, Entity entity) {
        Vec3 look = player.getLookAngle();
        Vec3 toEntity = entity.getEyePosition().subtract(player.getEyePosition());
        if (toEntity.lengthSqr() < 1.0E-6D) {
            return true;
        }
        double dot = look.dot(toEntity.normalize());
        return dot > 0.7D && player.hasLineOfSight(entity);
    }

    /**
     * Busca un lugar libre detrás del jugador (según hacia dónde mira), a las distancias indicadas
     * y en ese orden. Devuelve null si no hay sitio.
     */
    public static Vec3 findSpotBehind(Player player, Mob mob, double[] distances) {
        Vec3 look = player.getLookAngle();
        Vec3 back = new Vec3(-look.x, 0.0D, -look.z);
        if (back.lengthSqr() < 1.0E-4D) {
            back = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            back = back.normalize();
        }

        for (double d : distances) {
            double x = player.getX() + back.x * d;
            double z = player.getZ() + back.z * d;
            double y = player.getY();
            AABB box = mob.getDimensions(mob.getPose()).makeBoundingBox(x, y, z);
            if (mob.level().noCollision(mob, box)) {
                return new Vec3(x, y, z);
            }
        }
        return null;
    }

    /** Abre puertas, trampillas y portones que la criatura tenga cerca. */
    public static void openDoorsNearby(Mob mob) {
        Level lvl = mob.level();
        AABB box = mob.getBoundingBox().inflate(1.0D, 0.0D, 1.0D);
        for (BlockPos p : BlockPos.betweenClosed(
                Mth.floor(box.minX), Mth.floor(box.minY + 0.1D), Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.minY + 1.1D), Mth.floor(box.maxZ))) {
            BlockPos pos = p.immutable();
            BlockState state = lvl.getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock door) {
                if (!door.isOpen(state)) {
                    door.setOpen(mob, lvl, state, pos, true);
                }
            } else if (state.getBlock() instanceof FenceGateBlock) {
                if (!state.getValue(FenceGateBlock.OPEN)) {
                    lvl.setBlock(pos, state.setValue(FenceGateBlock.OPEN, true), 10);
                }
            } else if (state.getBlock() instanceof TrapDoorBlock) {
                if (!state.getValue(TrapDoorBlock.OPEN)) {
                    lvl.setBlock(pos, state.setValue(TrapDoorBlock.OPEN, true), 10);
                }
            }
        }
    }
}
