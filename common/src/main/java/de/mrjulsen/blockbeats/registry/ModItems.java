package de.mrjulsen.blockbeats.registry;

import de.mrjulsen.blockbeats.BlockBeats;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BlockBeats.MOD_ID, Registry.ITEM_REGISTRY);

    public static void init() {
        ITEMS.register();
    }
}
