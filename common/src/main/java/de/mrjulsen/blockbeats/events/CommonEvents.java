package de.mrjulsen.blockbeats.events;

import de.mrjulsen.blockbeats.core.data.FavoritesList;
import de.mrjulsen.blockbeats.core.data.Usercache;
import de.mrjulsen.dragnsounds.events.ServerEvents;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;

public final class CommonEvents {
    public static void init() {
        LifecycleEvent.SERVER_STOPPING.register((server) -> {            
            if (FavoritesList.hasInstance()) {
                FavoritesList.getInstance(server).close();
            }
            if (Usercache.hasInstance()) {
                Usercache.getInstance(server).close();
            }
        });

        PlayerEvent.PLAYER_JOIN.register((player) -> {
            Usercache.getInstance(ServerEvents.getCurrentServer()).set(player);
        });
    }
}
