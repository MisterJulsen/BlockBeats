package de.mrjulsen.blockbeats.registry;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.block.entity.SoundPlayerBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BlockBeats.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<?>> SOUND_PLAYER_BLOCK_ENTITY = BLOCK_ENTITIES.register("sound_player_block_entity", () -> BlockEntityType.Builder.of(SoundPlayerBlockEntity::new, ModBlocks.SOUND_PLAYER.get()).build(null));

    public static void init() {
        BLOCK_ENTITIES.register();
    }
}
