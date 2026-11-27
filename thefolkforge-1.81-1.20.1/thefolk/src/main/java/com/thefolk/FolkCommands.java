package com.thefolk;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * /folklang           abre el panel de idiomas (lo puede usar cualquiera, aunque los trucos estén desactivados)
 * /folklang es|en     cambia el idioma directamente
 *
 * Comandos de prueba (requieren permisos de operador):
 * /thefolk spawn <1-4>, /thefolk thing, /thefolk stalk, /thefolk man,
 * /thefolk mode <normal|error|coming>, /thefolk status
 */
@Mod.EventBusSubscriber(modid = TheFolkMod.MOD_ID)
public class FolkCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // Panel de idiomas: sin permisos especiales.
        event.getDispatcher().register(
                Commands.literal("folklang")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                                    new OpenLangScreenPacket());
                            return 1;
                        })
                        .then(Commands.literal("es").executes(ctx -> setLang(ctx, FolkLang.ES)))
                        .then(Commands.literal("en").executes(ctx -> setLang(ctx, FolkLang.EN))));

        // Guía de supervivencia: sin permisos especiales; nunca se duplica.
        event.getDispatcher().register(
                Commands.literal("folkguide")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            boolean updated = FolkGuide.giveOrUpdate(player);
                            msg(ctx, tr(ctx, updated ? "cmd.guide_updated" : "cmd.guide_given"));
                            return 1;
                        }));

        event.getDispatcher().register(
                Commands.literal("thefolk")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("spawn")
                                .then(Commands.argument("phase", IntegerArgumentType.integer(1, 4))
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            int phase = IntegerArgumentType.getInteger(ctx, "phase");
                                            boolean ok = FolkDirector.trySpawn(player, phase, true);
                                            msg(ctx, ok ? tr(ctx, "cmd.folk", phase) : tr(ctx, "cmd.no_spot"));
                                            return ok ? 1 : 0;
                                        })))
                        .then(Commands.literal("thing")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    boolean ok = FolkDirector.trySpawnThing(player, true);
                                    msg(ctx, ok ? tr(ctx, "cmd.thing") : tr(ctx, "cmd.no_spot"));
                                    return ok ? 1 : 0;
                                }))
                        .then(Commands.literal("stalk")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    boolean ok = FolkDirector.trySpawnStalk(player, true);
                                    msg(ctx, ok ? tr(ctx, "cmd.stalk") : tr(ctx, "cmd.stalk_no_spot"));
                                    return ok ? 1 : 0;
                                }))
                        .then(Commands.literal("man")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    boolean ok = FolkDirector.trySpawnMan(player, true);
                                    msg(ctx, ok ? tr(ctx, "cmd.man") : tr(ctx, "cmd.no_spot"));
                                    return ok ? 1 : 0;
                                }))
                        .then(Commands.literal("uknow")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    boolean ok = FolkDirector.trySpawnUknow(player, true);
                                    msg(ctx, ok ? tr(ctx, "cmd.uknow") : tr(ctx, "cmd.no_spot"));
                                    return ok ? 1 : 0;
                                }))
                        .then(Commands.literal("mode")
                                .then(Commands.literal("normal")
                                        .executes(ctx -> setMode(ctx, FolkDirector.MODE_NORMAL)))
                                .then(Commands.literal("error")
                                        .executes(ctx -> setMode(ctx, FolkDirector.MODE_ERROR)))
                                .then(Commands.literal("coming")
                                        .executes(ctx -> setMode(ctx, FolkDirector.MODE_COMING))))
                        .then(Commands.literal("status")
                                .executes(ctx -> {
                                    ServerLevel level = ctx.getSource().getLevel();
                                    int mode = FolkDirector.currentMode(level);
                                    long day = FolkDirector.currentDay(level);
                                    int phase = FolkDirector.phaseOfDay(day, mode);
                                    int dayInCycle = FolkDirector.dayInCycle(day, mode);
                                    String text = tr(ctx, "cmd.status", FolkDirector.modeName(mode), day, dayInCycle, phase);
                                    if (FolkDirector.isBoosted(level)) {
                                        text = text + tr(ctx, "cmd.boost");
                                    }
                                    msg(ctx, text);
                                    return 1;
                                })));
    }

    /** Texto en el idioma de quien ejecuta el comando (español si no es un jugador). */
    private static String tr(CommandContext<CommandSourceStack> ctx, String key, Object... args) {
        String text = ctx.getSource().getEntity() instanceof ServerPlayer player
                ? FolkLang.get(player, key)
                : FolkLang.getDefault(key);
        return args.length == 0 ? text : String.format(text, args);
    }

    private static void msg(CommandContext<CommandSourceStack> ctx, String text) {
        ctx.getSource().sendSuccess(() -> Component.literal(text), false);
    }

    private static int setMode(CommandContext<CommandSourceStack> ctx, int mode) {
        FolkDirector.setMode(ctx.getSource().getLevel(), mode);
        msg(ctx, tr(ctx, "cmd.mode", FolkDirector.modeName(mode)));
        return 1;
    }

    private static int setLang(CommandContext<CommandSourceStack> ctx, String lang) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        SetLangPacket.applyLanguage(player, lang);
        return 1;
    }
}
