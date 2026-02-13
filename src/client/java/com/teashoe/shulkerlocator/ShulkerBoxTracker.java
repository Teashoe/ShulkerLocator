package com.teashoe.shulkerlocator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Environment(EnvType.CLIENT)
public class ShulkerBoxTracker {
    private static final String SHULKER_BOXES_KEY = "ShulkerBoxes";
    private static ShulkerBoxTracker INSTANCE;

    private final Map<BlockPos, ShulkerBoxData> shulkerBoxes = new HashMap<>();
    private File saveFile;
    private boolean needsSave = false;
    private String currentWorldName;

    public static class ShulkerBoxData {
        public final BlockPos pos;
        public final String dimensionId;
        public final long placedTime;
        public final String color;
        public final String name; // 추가: 셜커박스 이름

        public ShulkerBoxData(BlockPos pos, String dimensionId, long placedTime, String color, String name) {
            this.pos = pos;
            this.dimensionId = dimensionId;
            this.placedTime = placedTime;
            this.color = color;
            this.name = name;
        }
    }

    private ShulkerBoxTracker() {
    }

    public static ShulkerBoxTracker getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ShulkerBoxTracker();
        }
        return INSTANCE;
    }

    public void onWorldLoad(String worldName) {
        if (!worldName.equals(currentWorldName)) {
            currentWorldName = worldName;
            shulkerBoxes.clear();

            MinecraftClient client = MinecraftClient.getInstance();
            File gameDir = client.runDirectory;
            File configDir = new File(gameDir, "config");
            if (!configDir.exists()) {
                configDir.mkdirs();
            }

            saveFile = new File(configDir, "shulkerlocator_" + sanitizeWorldName(worldName) + ".dat");
            load();
        }
    }

    public void onWorldUnload() {
        if (needsSave) {
            save();
        }
        shulkerBoxes.clear();
        currentWorldName = null;
        saveFile = null;
    }

    private String sanitizeWorldName(String worldName) {
        return worldName.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    public void addShulkerBox(BlockPos pos, String dimensionId, String color, String name) {
        shulkerBoxes.put(pos, new ShulkerBoxData(pos, dimensionId, System.currentTimeMillis(), color, name));
        markDirty();
    }

    public void removeShulkerBox(BlockPos pos) {
        if (shulkerBoxes.remove(pos) != null) {
            markDirty();
        }
    }

    public List<ShulkerBoxData> getAllShulkerBoxes() {
        return new ArrayList<>(shulkerBoxes.values());
    }

    public List<ShulkerBoxData> getShulkerBoxesInDimension(String dimensionId) {
        List<ShulkerBoxData> result = new ArrayList<>();
        for (ShulkerBoxData data : shulkerBoxes.values()) {
            if (data.dimensionId.equals(dimensionId)) {
                result.add(data);
            }
        }
        return result;
    }

    public List<ShulkerBoxData> getShulkerBoxesNearby(String dimensionId, BlockPos playerPos, int range) {
        List<ShulkerBoxData> result = new ArrayList<>();
        for (ShulkerBoxData data : shulkerBoxes.values()) {
            if (data.dimensionId.equals(dimensionId)) {
                double distance = Math.sqrt(
                        Math.pow(data.pos.getX() - playerPos.getX(), 2) +
                                Math.pow(data.pos.getY() - playerPos.getY(), 2) +
                                Math.pow(data.pos.getZ() - playerPos.getZ(), 2)
                );
                if (distance <= range) {
                    result.add(data);
                }
            }
        }
        return result;
    }

    private void markDirty() {
        needsSave = true;
        save();
    }

    private void save() {
        if (!needsSave || saveFile == null) return;

        try {
            NbtCompound nbt = new NbtCompound();
            NbtList list = new NbtList();

            for (ShulkerBoxData data : shulkerBoxes.values()) {
                NbtCompound boxNbt = new NbtCompound();
                boxNbt.putInt("x", data.pos.getX());
                boxNbt.putInt("y", data.pos.getY());
                boxNbt.putInt("z", data.pos.getZ());
                boxNbt.putString("dimension", data.dimensionId);
                boxNbt.putLong("time", data.placedTime);
                boxNbt.putString("color", data.color);
                if (data.name != null) {
                    boxNbt.putString("name", data.name);
                }
                list.add(boxNbt);
            }

            nbt.put(SHULKER_BOXES_KEY, list);

            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                NbtIo.writeCompressed(nbt, fos);
            }
            needsSave = false;
        } catch (IOException e) {
            System.err.println("Failed to save shulker box locations: " + e.getMessage());
        }
    }

    private void load() {
        if (!saveFile.exists()) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(saveFile)) {
            NbtCompound nbt = NbtIo.readCompressed(fis, NbtSizeTracker.ofUnlimitedBytes());

            if (!nbt.contains(SHULKER_BOXES_KEY)) {
                return;
            }

            NbtList list = nbt.getList(SHULKER_BOXES_KEY).orElse(new NbtList());
            for (int i = 0; i < list.size(); i++) {
                NbtCompound boxNbt = list.getCompound(i).orElse(null);
                if (boxNbt == null) continue;

                BlockPos pos = new BlockPos(
                        boxNbt.getInt("x").orElse(0),
                        boxNbt.getInt("y").orElse(0),
                        boxNbt.getInt("z").orElse(0)
                );
                String dimension = boxNbt.getString("dimension").orElse("");
                long time = boxNbt.getLong("time").orElse(0L);
                String color = boxNbt.getString("color").orElse("purple");
                String name = boxNbt.getString("name").orElse(null);

                shulkerBoxes.put(pos, new ShulkerBoxData(pos, dimension, time, color, name));
            }
        } catch (IOException e) {
            System.err.println("Failed to load shulker box locations: " + e.getMessage());
        }
    }
}