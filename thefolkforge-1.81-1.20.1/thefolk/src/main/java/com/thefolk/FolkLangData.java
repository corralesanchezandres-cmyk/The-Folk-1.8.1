package com.thefolk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Guarda el idioma elegido por cada jugador (se guarda en el mundo). */
public class FolkLangData extends SavedData {
    private final Map<UUID, String> langs = new HashMap<>();

    public static FolkLangData get(ServerLevel anyLevel) {
        ServerLevel overworld = anyLevel.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(FolkLangData::load, FolkLangData::new, "thefolk_lang");
    }

    public static FolkLangData load(CompoundTag tag) {
        FolkLangData data = new FolkLangData();
        for (String key : tag.getAllKeys()) {
            try {
                data.langs.put(UUID.fromString(key), tag.getString(key));
            } catch (IllegalArgumentException ignored) {
                // clave que no es un UUID: se ignora
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        for (Map.Entry<UUID, String> entry : this.langs.entrySet()) {
            tag.putString(entry.getKey().toString(), entry.getValue());
        }
        return tag;
    }

    public String getLang(UUID id) {
        return this.langs.getOrDefault(id, FolkLang.ES);
    }

    public void setLang(UUID id, String lang) {
        this.langs.put(id, lang);
        this.setDirty();
    }
}
