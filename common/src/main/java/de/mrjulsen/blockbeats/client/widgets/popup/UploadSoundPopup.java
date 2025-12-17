package de.mrjulsen.blockbeats.client.widgets.popup;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ClientWrapper;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer;
import de.mrjulsen.blockbeats.core.ESoundVisibility;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.core.ffmpeg.AudioSettings;
import de.mrjulsen.dragnsounds.core.ffmpeg.EChannels;
import de.mrjulsen.dragnsounds.core.ffmpeg.FFmpegUtils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLNumberPicker;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextLabel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLSlider;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.FlatButtonRenderer;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.IOUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.Holder.MutableHolder;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import ws.schild.jave.EncoderException;
import ws.schild.jave.info.MultimediaInfo;

public class UploadSoundPopup extends PopupWindow {

    private final MutableComponent title = Utils.trans("upload_file", "title");
    private final MutableComponent textUpload = Utils.trans("upload_file", "upload");
    private final MutableComponent hintEnterName = Utils.trans("upload_file", "enter_name");
    private final MutableComponent textChannels = Utils.trans("upload_file", "channels");
    private final MutableComponent textVisibility = Utils.trans("upload_file", "visibility");
    private final MutableComponent textAdvancedSettings = Utils.trans("upload_file", "advanced_settings");
    private final MutableComponent textQuality = Utils.trans("upload_file", "quality");
    private final MutableComponent textBitRate = Utils.trans("upload_file", "bit_rate");
    private final MutableComponent textSamplingRate = Utils.trans("upload_file", "sampling_rate");

    private final int DEFAULT_BIT_RATE = 192000;
    private final int DEFAULT_SAMPLING_RATE = 44100;

    private static final int WIN_WIDTH = 200;
    private static int winHeight = 10;

    private final Path path;

    private boolean extendedOptions;

    private final Supplier<FileBrowserContainer> container;

    // Input
    private DLRichTextEditBox nameBox;
    private EChannels channels = EChannels.MONO;
    private ESoundVisibility visibility = ESoundVisibility.PRIVATE;
    private DLNumberPicker bitRateInput = null;
    private DLNumberPicker samplingRateInput = null;
    private DLSlider qualityInput = null;

    public UploadSoundPopup(DLWindowManager manager, Path path, Supplier<FileBrowserContainer> container) {
        super(manager, WIN_WIDTH, 200);
        this.path = path;
        this.container = container;

        init(false);
    }

    private void init(boolean extended) {
        clearComponents();

        AtomicReference<DLButton> doneBtn = new AtomicReference<>();

        MultimediaInfo rawInfo = new MultimediaInfo();
        try {
            rawInfo = FFmpegUtils.getInfo(path.toFile());
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        final MultimediaInfo info = rawInfo;
        int dy = 25;

        nameBox = addComponent(new DLRichTextEditBox(10, dy, WIN_WIDTH - 20, 18));
        nameBox.placeholderText.set(hintEnterName);
        nameBox.maxCharacters.set(BlockBeats.MAX_FILENAME_LENGTH);
        nameBox.text.get().set(IOUtils.getFileNameWithoutExtension(path.toString()));
        nameBox.addEventListener(DLRichTextLabel.TextChangedEvent.class, (s, e) -> {
            if (doneBtn.get() != null) {
                doneBtn.get().enabled.set(!nameBox.text.get().getPlainText().isBlank());
            }
            return false;
        });
        
        dy += 20 + 10;
        DLCycleButton<EChannels> channelsBtn = addComponent(new DLCycleButton<>(10, dy, WIN_WIDTH - 20, 20));
        channelsBtn.text.set(textChannels);
        channelsBtn.items.addAll(EChannels.values());
        channelsBtn.selectedItem.set(Optional.ofNullable(channels));
        channelsBtn.tooltip.set(new DLTooltip(GuiUtils.getEnumTooltipData(EChannels.class, 200), 200));
        channelsBtn.textFormat.set((src) -> TextUtils.text(src.text.get().getString()).append(": ").append(src.selectedItem.get().map(x -> x.getValueTranslation()).orElse(TextUtils.empty())).withStyle(src.text.get().getStyle()));
        channelsBtn.addEventListener(DLCycleButton.SelectedItemChanged.class, (a, e) -> {
            channelsBtn.selectedItem.get().ifPresent(v -> channels = v);
            return false;
        });

        dy += 20 + 2;
        DLCycleButton<ESoundVisibility> visibilityBtn = addComponent(new DLCycleButton<>(10, dy, WIN_WIDTH - 20, 20));
        visibilityBtn.text.set(textVisibility);
        visibilityBtn.items.addAll(ESoundVisibility.values());
        visibilityBtn.selectedItem.set(Optional.ofNullable(visibility));
        visibilityBtn.tooltip.set(new DLTooltip(GuiUtils.getEnumTooltipData(ESoundVisibility.class, 200), 200));
        visibilityBtn.textFormat.set((src) -> TextUtils.text(src.text.get().getString()).append(": ").append(src.selectedItem.get().map(x -> x.getValueTranslation()).orElse(TextUtils.empty())).withStyle(src.text.get().getStyle()));
        visibilityBtn.addEventListener(DLCycleButton.SelectedItemChanged.class, (s, e) -> {
            visibilityBtn.selectedItem.get().ifPresent(v -> visibility = v);
            return false;
        });

        dy += 20 + 2;
        if (!extended) {
            DLButton advancedBtn = addComponent(new DLButton(10, dy, WIN_WIDTH - 20, 20));
            advancedBtn.text.set(textAdvancedSettings);
            advancedBtn.componentRenderer.set(FlatButtonRenderer.INSTANCE);
            advancedBtn.drawFontShadow.set(false);
            advancedBtn.textColor.set(DragonLib.BUTTON_COLOR_PRIMARY);
            advancedBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                init(true);
                return false;
            });
        } else {
            bitRateInput = addComponent(new DLNumberPicker(WIN_WIDTH - 10 - 80, dy, 80, 20));
            bitRateInput.min.set(0D);
            bitRateInput.max.set(384000D);
            bitRateInput.value.set((double)(info.getAudio().getBitRate() < 0 ? DEFAULT_BIT_RATE : info.getAudio().getBitRate()));

            dy += 20 + 2;
            samplingRateInput = addComponent(new DLNumberPicker(WIN_WIDTH - 10 - 80, dy, 80, 20));
            samplingRateInput.min.set(0D);
            samplingRateInput.max.set(48000D);
            samplingRateInput.value.set((double)(info.getAudio().getSamplingRate() < 0 ? DEFAULT_SAMPLING_RATE : info.getAudio().getSamplingRate()));

            dy += 20 + 2;
            qualityInput = addComponent(new DLSlider(10, dy, WIN_WIDTH - 20, 20));
            qualityInput.text.set(textQuality);
            qualityInput.max.set(10D);
            qualityInput.value.set(5D);
        }


        dy += 20 + 10;
        DLButton cancelBtn = addComponent(new DLButton(width() / 2 + 2, dy, 80, 20));
        cancelBtn.text.set(CommonComponents.GUI_CANCEL);
        cancelBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });
        
        DLButton doneButton = addComponent(new DLButton(width() / 2 - 2 - 80, dy, 80, 20));
        doneButton.text.set(textUpload);
        doneButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            MutableHolder<UploadProgressPopup> progressWindow = new MutableHolder<>(null);
            long id = ClientApi.uploadSound(
                path.toString(),
                new SoundFile.Builder(ClientWrapper.myLocation("sound_player"), nameBox.text.get().getPlainText(), Map.of(BlockBeats.META_VISIBILITY, visibility.getName(), BlockBeats.META_SHARED, "")),
                new AudioSettings(channels, bitRateInput == null ? info.getAudio().getBitRate() : bitRateInput.value.get().intValue(), samplingRateInput == null ? info.getAudio().getSamplingRate() : samplingRateInput.value.get().intValue(), (byte)(qualityInput == null ? 5 : qualityInput.value.get().intValue())),
                (result) -> {
                    if (progressWindow.get() != null) {
                        progressWindow.get().getWindowManager().closeWindow(progressWindow.get());
                        if (container.get() != null) {
                            container.get().refresh();
                        }
                    }
                }, (serverProgress, clientProgress) -> {
                    if (progressWindow.get() != null) {
                        progressWindow.get().progressBar.value.set(clientProgress.progress());
                        progressWindow.get().currentState = clientProgress.state();
                    }
                }, (status) -> {
                    if (progressWindow.get() != null) {
                        progressWindow.get().getWindowManager().closeWindow(progressWindow.get());
                        if (container.get() != null) {
                            container.get().refresh();
                        }
                    }
                }
            );

            getWindowManager().createModal(mgr -> store(new UploadProgressPopup(mgr, id), progressWindow));
            getWindowManager().closeWindow(this);
            return false;
        });
        doneBtn.set(doneButton);

        int oldWinHeight = winHeight;
        winHeight = dy + 30;
        if (oldWinHeight != winHeight) {
            init(extended);
        }
        extendedOptions = extended;
    }

    private <S> S store(S s, MutableHolder<S> in) {
        in.set(s);
        return s;
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite(DefaultGuiTextures.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());
        GuiUtils.drawString(graphics, graphics.defaultFont(), 6, 6, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);

        if (extendedOptions) {
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, bitRateInput.y() + bitRateInput.height() / 2 - graphics.defaultFont().lineHeight / 2, textBitRate, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
            GuiUtils.drawString(graphics, graphics.defaultFont(), 10, samplingRateInput.y() + samplingRateInput.height() / 2 - graphics.defaultFont().lineHeight / 2, textSamplingRate, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
        }
    }
}
