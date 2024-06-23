package de.mrjulsen.blockbeats.core.data.playback;

import java.util.Set;

import de.mrjulsen.dragnsounds.core.data.IPlaybackArea;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record EntityRidingPlaybackArea(Set<ResourceLocation> entityType, Vec3 center, double radius) implements IPlaybackArea {

    @Override
    public boolean canPlayForPlayer(Level level, Player player) {
        return center().distanceTo(player.position()) <= radius() && entityType().stream().anyMatch(x -> x.equals(Registry.ENTITY_TYPE.getKey(player.getVehicle().getType())));
    }
    
}
