package com.example.worldtier;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** Menyimpan boss mana saja yang sudah dikalahkan di dunia ini (bitmask). */
public class WorldTierData extends SavedData {
    public static final int GUARDIAN = 1; // Elder Guardian
    public static final int WITHER = 2;   // Wither
    public static final int WARDEN = 4;   // Warden
    public static final int DRAGON = 8;   // Ender Dragon

    private static final Codec<WorldTierData> CODEC = Codec.INT.xmap(
            WorldTierData::new,
            WorldTierData::getMask
    );

    private static final SavedDataType<WorldTierData> TYPE = new SavedDataType<>(
            WorldTierMod.id("world_tier"),
            WorldTierData::new,
            CODEC,
            null
    );

    private int mask = 0;

    public WorldTierData() {
    }

    public WorldTierData(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return this.mask;
    }

    public boolean has(int flag) {
        return (this.mask & flag) != 0;
    }

    /** Tier dunia = jumlah boss yang sudah dikalahkan (0-4). */
    public int getTier() {
        return Integer.bitCount(this.mask);
    }

    /** @return true jika boss ini baru pertama kali dikalahkan. */
    public boolean unlock(int flag) {
        if (has(flag)) {
            return false;
        }
        this.mask |= flag;
        setDirty();
        return true;
    }

    public static WorldTierData get(MinecraftServer server) {
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) {
            return new WorldTierData();
        }
        return level.getDataStorage().computeIfAbsent(TYPE);
    }
}
