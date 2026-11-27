package com.thefolk;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Guarda el modo elegido con el FolkConfig dentro del mundo. */
public class FolkModeData extends SavedData {
    private int mode = FolkDirector.MODE_NORMAL;

    public static FolkModeData get(ServerLevel anyLevel) {
        ServerLevel overworld = anyLevel.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(FolkModeData::load, FolkModeData::new, "thefolk_mode");
    }

    public static FolkModeData load(CompoundTag tag) {
        FolkModeData data = new FolkModeData();
        data.mode = tag.getInt("Mode");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Mode", this.mode);
        return tag;
    }

    public int getMode() {
        return this.mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
        this.setDirty();
    }
}
