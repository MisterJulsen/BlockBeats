package de.mrjulsen.blockbeats.events;

import java.util.function.Consumer;

import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.core.data.PlaybackConfig;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;

public final class ClientEvents {

    private static long playingAudioSampleId;
    private static String playingAudioSamplePath = "";

    public static void playAudioSample(SoundFile file, Consumer<Long> idCallback) {
        stopCurrentAudioSample();

        ClientApi.playSound(file, PlaybackConfig.defaultUI(1, 1, 0), (i) -> {
            playingAudioSampleId = i;
            playingAudioSamplePath = file.toString();
            idCallback.accept(i);
        });
    }

    public static void stopCurrentAudioSample() {
        if (ClientApi.isPlaying(playingAudioSampleId)) {
            ClientApi.stopSound(playingAudioSampleId);
        }
        playingAudioSampleId = 0;
        playingAudioSamplePath = "";
    }

    public static long getCurrentAudioSampleId() {
        return playingAudioSampleId;
    }

    public static String getCurrentAudioSamplePath() {
        return playingAudioSamplePath;
    }

    public static void init() {
    }
}
