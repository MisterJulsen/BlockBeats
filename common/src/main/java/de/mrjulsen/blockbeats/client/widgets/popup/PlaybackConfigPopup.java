package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.List;
import java.util.function.Consumer;

import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.core.ext.CustomSoundInstance;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLCheckBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLNumberPicker;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLSlider;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.ITextFormatter;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;

public class PlaybackConfigPopup extends PopupWindow {

    private final MutableComponent title = Utils.trans("playback_config", "title");
    private final MutableComponent textVolume = Utils.trans("playback_config", "volume");
    private final MutableComponent textPitch = Utils.trans("playback_config", "pitch");
    private final MutableComponent textAttenuationDistance = Utils.trans("playback_config", "attenuation_distance");
    private final MutableComponent textBgm = Utils.trans("playback_config", "bgm");
    private final MutableComponent textShowLabel = Utils.trans("playback_config", "show_label");
    private final MutableComponent descriptionAttenuationDistance = Utils.trans("playback_config", "description.attenuation_distance");
    private final MutableComponent descriptionBgm = Utils.trans("playback_config", "description.bgm");
    private final MutableComponent descriptionShowLabel = Utils.trans("playback_config", "description.show_label");

    private static final int WIN_WIDTH = 176;
    private static final int WIN_HEIGHT = 160;

    private DLButton cancelButton;
    private DLButton doneButton;
    private DLSlider volumeSlider;
    private DLSlider pitchSlider;
    private DLCheckBox bgmCheck;
    private DLCheckBox labelCheck;
    private DLNumberPicker attenuationDistanceSelector;

    public PlaybackConfigPopup(DLWindowManager manager, float volume, float pitch, int attenuationDistance, boolean bgm, boolean showLabel, Consumer<PlaybackConfigPopup> onDone) {
        super(manager, WIN_WIDTH, WIN_HEIGHT);

        cancelButton = addComponent(new DLButton(WIN_WIDTH - 80 - 6, WIN_HEIGHT - 30, 80, 20));
        cancelButton.text.set(CommonComponents.GUI_CANCEL);
        cancelButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });
        
        doneButton = addComponent(new DLButton(WIN_WIDTH - 160 - 10, WIN_HEIGHT - 30, 80, 20));
        doneButton.text.set(CommonComponents.GUI_DONE);
        doneButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            onDone.accept(this);
            getWindowManager().closeWindow(this);
            return false;
        });        

        volumeSlider = addComponent(new DLSlider(6, 20, WIN_WIDTH - 12, 20));
        volumeSlider.text.set(textVolume);
        volumeSlider.min.set((double)CustomSoundInstance.VOLUME_MIN);
        volumeSlider.max.set((double)CustomSoundInstance.VOLUME_MAX);
        volumeSlider.step.set(0.01);
        volumeSlider.textFormat.set(DLSlider.DEFAULT_TEXT_DOUBLE_PERCENTAGE_FORMAT);
        volumeSlider.value.set((double)volume);

        pitchSlider = addComponent(new DLSlider(6, 20 + 25, WIN_WIDTH - 12, 20));
        pitchSlider.text.set(textPitch);
        pitchSlider.min.set((double)CustomSoundInstance.PITCH_MIN);
        pitchSlider.max.set((double)CustomSoundInstance.PITCH_MAX);
        pitchSlider.step.set(0.05);
        pitchSlider.textFormat.set((src) -> TextUtils.text(src.text.get().getString()).append(": ").append(String.valueOf(src.value.get().floatValue())).withStyle(src.text.get().getStyle()));
        pitchSlider.value.set((double)pitch);
        
        attenuationDistanceSelector = addComponent(new DLNumberPicker(WIN_WIDTH - 6 - 50, 20 + 50, 50, 20));
        attenuationDistanceSelector.min.set(1D);
        attenuationDistanceSelector.max.set(1024D);
        attenuationDistanceSelector.value.set((double)attenuationDistance);
        attenuationDistanceSelector.tooltip.set(new DLTooltip(List.of(descriptionAttenuationDistance), 200));
        
        bgmCheck = addComponent(new DLCheckBox(6, 20 + 75, WIN_WIDTH - 12, 20));
        bgmCheck.checked.set(bgm);
        bgmCheck.text.set(textBgm);
        //bgmCheck.drawFontShadow.set(false);
        //bgmCheck.textColor.set(DragonLib.VANILLA_UI_FONT_COLOR);
        bgmCheck.tooltip.set(new DLTooltip(List.of(descriptionBgm), 200));
        
        labelCheck = addComponent(new DLCheckBox(6, 20 + 91, WIN_WIDTH - 12, 20));
        labelCheck.checked.set(showLabel);
        labelCheck.text.set(textShowLabel);
        //labelCheck.drawFontShadow.set(false);
        //labelCheck.textColor.set(DragonLib.VANILLA_UI_FONT_COLOR);
        labelCheck.tooltip.set(new DLTooltip(List.of(descriptionShowLabel), 200));
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DLTextureSheet.DRAGONLIB_UI.getSprite(DLTextureSheet.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());
        GuiUtils.drawString(graphics, graphics.defaultFont(), 6, attenuationDistanceSelector.y() + attenuationDistanceSelector.height() / 2 - graphics.defaultFont().lineHeight / 2, textAttenuationDistance, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), 6, 6, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
    }

    public float getVolume() {
        return volumeSlider.value.get().floatValue();
    }

    public float getPitch() {
        return pitchSlider.value.get().floatValue();
    }

    public int getAttenuationDistance() {
        return attenuationDistanceSelector.value.get().intValue();
    }

    public boolean isBgm() {
        return bgmCheck.checked.get();
    }

    public boolean isShowLabel() {
        return labelCheck.checked.get();
    }
}
