package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.core.ext.CustomSoundInstance;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLCheckBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLNumberSelector;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLSlider;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IDragonLibWidget;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;

public class PlaybackConfigPopup extends PopupWidget {

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
    private int guiLeft, guiTop;
    private final Collection<DLTooltip> tooltips = new ArrayList<>();

    private DLButton cancelButton;
    private DLButton doneButton;
    private DLSlider volumeSlider;
    private DLSlider pitchSlider;
    private DLCheckBox bgmCheck;
    private DLCheckBox labelCheck;
    private DLNumberSelector attenuationDistanceSelector;

    public PlaybackConfigPopup(DLPopupScreen parent, int layer, int width, int height, float volume, float pitch, int attenuationDistance, boolean bgm, boolean showLabel, Consumer<PopupWidget> close, Consumer<PlaybackConfigPopup> onDone) {
        super(parent, layer, width, height, close);
        guiLeft = width / 2 - WIN_WIDTH / 2;
        guiTop = height / 2 - WIN_HEIGHT / 2;
        cancelButton = addRenderableWidget(new DLButton(guiLeft + WIN_WIDTH - 80 - 6, guiTop + WIN_HEIGHT - 30, 80, 20, CommonComponents.GUI_CANCEL, (btn) -> close()));
        cancelButton.setRenderStyle(AreaStyle.DRAGONLIB);
        doneButton = addRenderableWidget(new DLButton(guiLeft + WIN_WIDTH - 160 - 10, guiTop + WIN_HEIGHT - 30, 80, 20, CommonComponents.GUI_DONE, (btn) ->  {            
            onDone.accept(this);
            close();
        }));
        doneButton.setRenderStyle(AreaStyle.DRAGONLIB);
        doneButton.setBackColor(DragonLib.PRIMARY_BUTTON_COLOR);

        volumeSlider = addRenderableWidget(new DLSlider(guiLeft + 6, guiTop + 20, WIN_WIDTH - 12, 20, TextUtils.empty(), TextUtils.empty(), CustomSoundInstance.VOLUME_MIN * 100, CustomSoundInstance.VOLUME_MAX * 100, volume * 100, 1, 1, true));
        volumeSlider.setOnUpdateMessage((slider) -> {
            slider.setMessage(slider.getValue() <= 0 ? TextUtils.text(String.format("%s: %s", textVolume.getString(), CommonComponents.OPTION_OFF.getString())) : TextUtils.text(String.format("%s: %s%%", textVolume.getString(), slider.getValueInt())));
        });
        volumeSlider.setRenderStyle(AreaStyle.DRAGONLIB);
        volumeSlider.setValue(volumeSlider.getValue());
        pitchSlider = addRenderableWidget(new DLSlider(guiLeft + 6, guiTop + 20 + 25, WIN_WIDTH - 12, 20, TextUtils.empty(), TextUtils.empty(), CustomSoundInstance.PITCH_MIN, CustomSoundInstance.PITCH_MAX, pitch, 0.05f, 1, true));
        pitchSlider.setOnUpdateMessage((slider) -> {
            slider.setMessage(TextUtils.text(String.format("%s: %.2f", textPitch.getString(), slider.getValue())));
        });
        pitchSlider.setRenderStyle(AreaStyle.DRAGONLIB);
        pitchSlider.setValue(pitchSlider.getValue());
        attenuationDistanceSelector = addRenderableWidget(new DLNumberSelector(guiLeft + WIN_WIDTH - 6 - 50, guiTop + 20 + 50, 50, 20, attenuationDistance, true, (box, value) -> {

        }));
        attenuationDistanceSelector.setNumberBounds(1, 1024);
        tooltips.add(DLTooltip.of(descriptionAttenuationDistance).assignedTo(GuiAreaDefinition.of(attenuationDistanceSelector)).withMaxWidth(width / 4));

        bgmCheck = addRenderableWidget(new DLCheckBox(guiLeft + 6, guiTop + 20 + 75, WIN_WIDTH - 12, textBgm.getString(), bgm, (box) -> {}));
        bgmCheck.setRenderStyle(AreaStyle.DRAGONLIB);
        bgmCheck.setFontColor(DragonLib.NATIVE_UI_FONT_COLOR);
        bgmCheck.setTextShadow(false);
        tooltips.add(DLTooltip.of(descriptionBgm).assignedTo(bgmCheck).withMaxWidth(width / 4));
        labelCheck = addRenderableWidget(new DLCheckBox(guiLeft + 6, guiTop + 20 + 91, WIN_WIDTH - 12, textShowLabel.getString(), showLabel, (box) -> {}));
        labelCheck.setRenderStyle(AreaStyle.DRAGONLIB);
        labelCheck.setFontColor(DragonLib.NATIVE_UI_FONT_COLOR);
        labelCheck.setTextShadow(false);
        tooltips.add(DLTooltip.of(descriptionShowLabel).assignedTo(labelCheck).withMaxWidth(width / 4));
    }

    @Override
    public void close() {
        super.close();
    }
    
    @Override
    public void renderMainPopupLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainPopupLayer(graphics, mouseX, mouseY, partialTicks);
        DynamicGuiRenderer.renderWindow(graphics, guiLeft, guiTop, WIN_WIDTH, WIN_HEIGHT);
        GuiUtils.drawString(graphics, font, guiLeft + 6, attenuationDistanceSelector.getY() + attenuationDistanceSelector.getHeight() / 2 - font.lineHeight / 2, textAttenuationDistance, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
        GuiUtils.drawString(graphics, font, guiLeft + 6, guiTop + 6, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
    }

    public float getVolume() {
        return (float)(volumeSlider.getValue() / 100f);
    }

    public float getPitch() {
        return (float)pitchSlider.getValue();
    }

    public int getAttenuationDistance() {
        return attenuationDistanceSelector.getAsInt();
    }

    public boolean isBgm() {
        return bgmCheck.isChecked();
    }

    public boolean isShowLabel() {
        return labelCheck.isChecked();
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
        tooltips.forEach(x -> {
            if (!(x.getAssignedWidget() != null && x.getAssignedWidget() instanceof IDragonLibWidget wgt && wgt.isMouseSelected()) && !(x.getAssignedArea() != null && x.getAssignedArea().isInBounds(mouseX, mouseY) && getParent().getAllowedLayer() == this.getWidgetLayerIndex())) {
                return;
            }
            x.render(getParent(), graphics, mouseX, mouseY);
        });
    }
}
