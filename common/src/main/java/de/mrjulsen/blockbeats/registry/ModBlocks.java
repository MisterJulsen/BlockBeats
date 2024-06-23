package de.mrjulsen.blockbeats.registry;

import java.util.function.Supplier;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.block.SoundPlayerBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BlockBeats.MOD_ID, Registry.BLOCK_REGISTRY);

    public static final RegistrySupplier<SoundPlayerBlock> SOUND_PLAYER = register("sound_player", SoundPlayerBlock::new);



    public static final <T extends Block> RegistrySupplier<T> registerWithoutItem(String name, Supplier<T> block) {
        RegistrySupplier<T> result = BLOCKS.register(name, block);
        return result;
    }
    
    public static final <T extends Block> RegistrySupplier<T> register(String name, Supplier<T> block) {
        RegistrySupplier<T> result = registerWithoutItem(name, block);
        ModItems.ITEMS.register(name, () -> new BlockItem(result.get(), new Properties().tab(ModCreativeModeTab.MOD_TAB)));
        return result;
    }

    public static void init() {
        BLOCKS.register();
    }
}
