package de.mrjulsen.blockbeats.fabric;

import net.fabricmc.api.ModInitializer;

import de.mrjulsen.blockbeats.BlockBeats;

public final class BlockBeatsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        BlockBeats.init();
    }
}
