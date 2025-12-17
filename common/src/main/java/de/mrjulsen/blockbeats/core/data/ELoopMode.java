package de.mrjulsen.blockbeats.core.data;

import java.util.Arrays;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.data.IIterableEnum;
import de.mrjulsen.mcdragonlib.data.ITranslatableEnum;
import de.mrjulsen.mcdragonlib.util.DLColor;

public enum ELoopMode implements ITranslatableEnum, IIterableEnum<ELoopMode> {
    NO_LOOP(0, "no_loop", ModGuiIcons.NO_LOOP, DragonLib.BUTTON_COLOR_DEFAULT_DARK),
    LOOP(1, "loop", ModGuiIcons.LOOP, DragonLib.BUTTON_COLOR_PRIMARY),
    SINGLE_LOOP(2, "single_loop", ModGuiIcons.LOOP_SINGLE, DragonLib.BUTTON_COLOR_PRIMARY);

    private int index;
    private String name;
    private ModGuiIcons icon;
    private DLColor color;

    private ELoopMode(int index, String name, ModGuiIcons icon, DLColor color) {
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

    public static ELoopMode getByIndex(int id) {
        return Arrays.stream(values()).filter(x -> x.getIndex() == id).findFirst().orElse(NO_LOOP);
    }

    @Override
    public ELoopMode[] getValues() {
        return values();
    }

    @Override
    public Data getTranslationData() {
        return new Data(BlockBeats.MOD_ID, "loop_mode", name);
    }
    
}