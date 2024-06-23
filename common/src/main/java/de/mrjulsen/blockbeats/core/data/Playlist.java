package de.mrjulsen.blockbeats.core.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import de.mrjulsen.blockbeats.core.OrderedArrayList;
import de.mrjulsen.mcdragonlib.data.INBTSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class Playlist implements INBTSerializable {

    private static final String NBT_LOOP = "Loop";
    private static final String NBT_RANDOM = "Random";
    private static final String NBT_INDEX = "Index";
    private static final String NBT_FILES = "Files";

    private final OrderedArrayList<String> files = new OrderedArrayList<>();
    private ELoopMode loop;
    private EShuffleMode random;

    // Playback Progress
    private Deque<String> remainingTracks = new ConcurrentLinkedDeque<>();
    private Deque<String> consumedTracks = new ConcurrentLinkedDeque<>();
    private String currentFile;
    
    public Playlist(OrderedArrayList<String> files, ELoopMode loop, EShuffleMode random, int offsetIndex) {
        this.loop = loop;
        this.random = random;
        this.files.addAll(files);
    }

    public OrderedArrayList<String> getFiles() {
        return files;
    }

    public ELoopMode getLoop() {
        return loop;
    }

    public void setLoop(ELoopMode loop) {
        this.loop = loop;
    }

    public EShuffleMode getRandom() {
        return random;
    }

    public void setRandom(EShuffleMode random) {
        this.random = random;
    }

    public String getCurrentFile() {
        return currentFile;
    }

    public int getCurrentOffsetIndex() {
        return (files.size() - 1) - (remainingTracks.size() - 1);
    }

    public String restart() {
        refillQueue(0);
        return next(false);
    }

    protected void refillQueue(int offset) {
        List<String> list = new ArrayList<>(files);
        if (getRandom() == EShuffleMode.SHUFFLE) {
            Collections.shuffle(list);
        }
        Collections.rotate(list, offset);
        consumedTracks.clear();
        remainingTracks.clear();
        remainingTracks.addAll(list);
    }
    
    public String next(boolean allowSingleLoop) {
        if (allowSingleLoop && getLoop() == ELoopMode.SINGLE_LOOP) {
            return currentFile;
        }

        if (remainingTracks.isEmpty()) {
            if (getLoop() == ELoopMode.LOOP) {
                refillQueue(0);
            } else {
                return null;
            }
        }

        String val = remainingTracks.pollFirst();
        consumedTracks.addLast(val);
        return currentFile = val;
    }

    public String previous() {
        if (consumedTracks.isEmpty()) {
            if (getLoop() == ELoopMode.LOOP) {
                consumedTracks.addLast(remainingTracks.pollLast());
            } else {
                return null;
            }
        }

        String val = consumedTracks.pollLast();
        remainingTracks.addFirst(val);
        return currentFile = val;
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBT_LOOP, getLoop().getIndex());
        nbt.putInt(NBT_RANDOM, getRandom().getIndex());
        nbt.putInt(NBT_INDEX, getCurrentOffsetIndex());
        ListTag filesTag = new ListTag();
        filesTag.addAll(files.stream().map(x -> StringTag.valueOf(x)).toList());
        nbt.put(NBT_FILES, filesTag);
        return nbt;
    }

    @Override
    public void deserializeNbt(CompoundTag nbt) {
        Collection<String> files = nbt.getList(NBT_FILES, Tag.TAG_STRING).stream().map(x -> x.getAsString()).toList();
        this.files.clear();
        this.files.addAll(files);
        setLoop(ELoopMode.getByIndex(nbt.getInt(NBT_LOOP)));
        setRandom(EShuffleMode.getByIndex(nbt.getInt(NBT_RANDOM)));
        refillQueue(nbt.getInt(NBT_INDEX));
    }

    public static Playlist empty() {
        return new Playlist(new OrderedArrayList<>(), ELoopMode.NO_LOOP, EShuffleMode.NO_SHUFFLE, 0);
    }
}

