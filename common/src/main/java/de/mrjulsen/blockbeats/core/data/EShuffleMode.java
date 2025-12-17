package de.mrjulsen.blockbeats.core.data;

import java.util.Arrays;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.IIterableEnum;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.util.DLColor;

public enum EShuffleMode implements ITranslatableEnum, IIterableEnum<EShuffleMode> {
    NO_SHUFFLE(0, "no_shuffle", ModGuiIcons.NO_SHUFFLE, DragonLib.BUTTON_COLOR_DEFAULT_DARK),
    SHUFFLE(1, "shuffle", ModGuiIcons.SHUFFLE, DragonLib.BUTTON_COLOR_PRIMARY);

    private int index;
    private String name;
    private ModGuiIcons icon;
    private DLColor color;

    private EShuffleMode(int index, String name, ModGuiIcons icon, DLColor color) {
        this.index = index;
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public ModGuiIcons getIcon() {
        return icon;
    }

    public DLColor getButtonColor() {
        return color;
    }

    public static EShuffleMode getByIndex(int id) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == id).findFirst().orElse(NO_SHUFFLE);
    }

    @Override
    public EShuffleMode[] getValues() {
        return values();
    }

    @Override
    public Data getTranslationData() {
        return new Data(BlockBeats.MOD_ID, "shuffle_mode", name);
    }
    
}
