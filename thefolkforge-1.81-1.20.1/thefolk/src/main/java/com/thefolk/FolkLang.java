package com.thefolk;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

/** Textos del juego en español e inglés. Para agregar un texto nuevo, añade una línea en add(...). */
public class FolkLang {
    public static final String ES = "es";
    public static final String EN = "en";

    private static final Map<String, String[]> TEXTS = new HashMap<>();

    static {
        // Textos en pantalla
        add("behind", "atrás tuyo", "behind you");
        add("run", "CORRE", "RUN");
        add("coming", "Im C0MING", "Im C0MING");
        add("lang.set", "Idioma: Español", "Language: English");
        add("struggle", "¡Pulsa ESPACIO 3 veces en 2 segundos!", "Press SPACE 3 times in 2 seconds!");
        add("struggle.count", "ESPACIO: %s/3", "SPACE: %s/3");

        // Mensajes de los comandos de prueba (%s se reemplaza por un valor)
        add("cmd.no_spot", "No se encontró un lugar para que aparezca.", "No spot was found for it to appear.");
        add("cmd.folk", "The Folk (fase %s) ha aparecido.", "The Folk (phase %s) has appeared.");
        add("cmd.thing", "The Thing ha aparecido (20 segundos, con el evento Atrás).",
                "The Thing has appeared (20 seconds, with the Behind event).");
        add("cmd.stalk", "Evento Stalk iniciado.", "Stalk event started.");
        add("cmd.stalk_no_spot", "No se encontró un lugar para el evento Stalk.",
                "No spot was found for the Stalk event.");
        add("cmd.man", "Man ha aparecido.", "Man has appeared.");
        add("cmd.uknow", "Uknow ha aparecido.", "Uknow has appeared.");
        add("cmd.guide_given", "Guía de supervivencia entregada.", "Survival guide given.");
        add("cmd.guide_updated", "Guía actualizada a tu idioma.", "Guide updated to your language.");
        add("cmd.mode", "Modo: %s", "Mode: %s");
        add("cmd.status", "Modo: %s | día %s del mundo | día %s del ciclo | fase %s",
                "Mode: %s | world day %s | cycle day %s | phase %s");
        add("cmd.boost", " | apariciones aumentadas por Man", " | spawns boosted by Man");
    }

    private static void add(String key, String es, String en) {
        TEXTS.put(key, new String[]{es, en});
    }

    public static String langOf(ServerPlayer player) {
        return FolkLangData.get(player.serverLevel()).getLang(player.getUUID());
    }

    public static boolean isEnglish(ServerPlayer player) {
        return EN.equals(langOf(player));
    }

    public static String get(ServerPlayer player, String key) {
        String[] texts = TEXTS.get(key);
        if (texts == null) return key;
        return isEnglish(player) ? texts[1] : texts[0];
    }

    /** Texto en español, para cuando no hay jugador (por ejemplo, un comando desde la consola). */
    public static String getDefault(String key) {
        String[] texts = TEXTS.get(key);
        return texts == null ? key : texts[0];
    }
}
