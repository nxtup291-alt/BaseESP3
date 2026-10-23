package com.baseesp.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BaseEspConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("baseesp.json");

    public boolean storage = true;
    public boolean mobs = true;
    public boolean villagers = true;
    public boolean animals = false;
    public boolean beds = true;
    public boolean utilityBlocks = true;
    public boolean doors = true;
    public boolean signs = false;
    public boolean showHud = true;
    public boolean showBaseRating = true;
    public boolean showContentsWhenKnown = true;
    public int radius = 10; // 10 blocks in each direction = 20x20 blocks

    public static BaseEspConfig load() {
        try {
            if (Files.exists(FILE)) {
                BaseEspConfig loaded = GSON.fromJson(Files.readString(FILE), BaseEspConfig.class);
                if (loaded != null) {
                    loaded.radius = Math.max(5, Math.min(20, loaded.radius));
                    return loaded;
                }
            }
        } catch (Exception ignored) { }
        return new BaseEspConfig();
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(this));
        } catch (IOException e) {
            System.err.println("[BaseESP] Could not save config: " + e.getMessage());
        }
    }
}
