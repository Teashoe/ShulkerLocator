package com.teashoe.shulkerlocator.client;

import com.teashoe.shulkerlocator.ShulkerBoxTracker;
import com.teashoe.shulkerlocator.command.LocateShulkerBoxCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;

public class ShulkerlocatorClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register client-side command
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LocateShulkerBoxCommand.registerClient(dispatcher);
        });

        // Handle world join
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String worldName = getWorldName(client);
            ShulkerBoxTracker.getInstance().onWorldLoad(worldName);
        });

        // Handle world leave
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ShulkerBoxTracker.getInstance().onWorldUnload();
        });
    }

    private String getWorldName(MinecraftClient client) {
        if (client.isIntegratedServerRunning() && client.getServer() != null) {
            // Singleplayer
            return client.getServer().getSaveProperties().getLevelName();
        } else if (client.getCurrentServerEntry() != null) {
            // Multiplayer
            return client.getCurrentServerEntry().address;
        }
        return "unknown";
    }
}
