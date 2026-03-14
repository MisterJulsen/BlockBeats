package de.mrjulsen.blockbeats.client.widgets.popup;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ClientWrapper;
import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer;
import de.mrjulsen.blockbeats.core.ESoundVisibility;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.DragNSounds;
import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.core.ffmpeg.AudioSettings;
import de.mrjulsen.dragnsounds.core.ffmpeg.EChannels;
import de.mrjulsen.dragnsounds.core.ffmpeg.FFmpegUtils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLCycleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLEditBox;
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
import de.mrjulsen.mcdragonlib.data.Single.MutableSingle;
import de.mrjulsen.mcdragonlib.util.IOUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import ws.schild.jave.EncoderException;
import ws.schild.jave.info.MultimediaInfo;

public class UploadSoundPopup extends PopupWidget {

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

    private int guiLeft, guiTop;
    private final Path path;

    private boolean extendedOptions;

    private final Supplier<FileBrowserContainer> container;
    private final Collection<DLTooltip> tooltips = new ArrayList<>();

    // Input
    private DLEditBox nameBox;
    private EChannels channels = EChannels.MONO;
    private ESoundVisibility visibility = ESoundVisibility.PRIVATE;
    private DLNumberSelector bitRateInput = null;
    private DLNumberSelector samplingRateInput = null;
    private DLSlider qualityInput = null;

    public UploadSoundPopup(DLPopupScreen parent, int layer, Path path, Supplier<FileBrowserContainer> container, int width, int height, Consumer<PopupWidget> close) {
        super(parent, layer, width, height, close);
        this.path = path;
        this.container = container;

        init(false);
    }

    private void init(boolean extended) {
        clearWidgets();
        guiLeft = width / 2 - WIN_WIDTH / 2;
        guiTop = height / 2 - winHeight / 2;
        AtomicReference<DLButton> doneBtn = new AtomicReference<>();

        MultimediaInfo rawInfo = new MultimediaInfo();
        try {
            rawInfo = FFmpegUtils.getInfo(path.toFile());
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        final MultimediaInfo info = rawInfo;
        int dy = 25;

        nameBox = addRenderableWidget(new DLEditBox(font, guiLeft + 10, guiTop + dy, WIN_WIDTH - 20, 18, hintEnterName));
        nameBox.setMaxLength(BlockBeats.MAX_FILENAME_LENGTH);
        nameBox.setValue(IOUtils.getFileNameWithoutExtension(path.toString()));
        nameBox.withHint(hintEnterName);
        nameBox.setResponder((text) -> {
            if (doneBtn.get() != null) {
                doneBtn.get().active = !text.isBlank();
            }
        });
        dy += 20 + 10;
        DLCycleButton<EChannels> channelsBtn = addRenderableWidget(GuiUtils.createCycleButton(DragNSounds.MOD_ID, EChannels.class, guiLeft + 10, guiTop + dy, WIN_WIDTH - 20, 20, textChannels, channels,
        (btn, value) -> {
            channels = value;
        }));
        channelsBtn.setRenderStyle(AreaStyle.DRAGONLIB);
        tooltips.add(DLTooltip.of(DragNSounds.MOD_ID, EChannels.class).assignedTo(channelsBtn).withMaxWidth(width / 4));

        dy += 20 + 2;
        DLCycleButton<ESoundVisibility> visibilityBtn = addRenderableWidget(GuiUtils.createCycleButton(BlockBeats.MOD_ID, ESoundVisibility.class, guiLeft + 10, guiTop + dy, WIN_WIDTH - 20, 20, textVisibility, visibility,
        (btn, value) -> {
            visibility = value;
        }));
        visibilityBtn.setRenderStyle(AreaStyle.DRAGONLIB);
        tooltips.add(DLTooltip.of(BlockBeats.MOD_ID, ESoundVisibility.class).assignedTo(visibilityBtn).withMaxWidth(width / 4));

        dy += 20 + 2;
        if (!extended) {
            DLButton advancedBtn = addRenderableWidget(GuiUtils.createButton(guiLeft + 10, guiTop + dy, WIN_WIDTH - 20, 20, textAdvancedSettings,
            (btn) -> {
                init(true);
            }));
            advancedBtn.setRenderStyle(AreaStyle.FLAT);
            advancedBtn.setBackColor(0x00FFFFFF);
            advancedBtn.setFontColor(DragonLib.PRIMARY_BUTTON_COLOR);
            advancedBtn.setTextShadow(false);
        } else {
            bitRateInput = addRenderableWidget(new DLNumberSelector(guiLeft + WIN_WIDTH - 10 - 80, guiTop + dy, 80, 20, info.getAudio().getBitRate(), false,
            (b, value) -> {

            }));
            bitRateInput.setNumberBounds(0, 384000);
            bitRateInput.setValue(info.getAudio().getBitRate() < 0 ? DEFAULT_BIT_RATE : info.getAudio().getBitRate(), true);
            dy += 20 + 2;
            samplingRateInput = addRenderableWidget(new DLNumberSelector(guiLeft + WIN_WIDTH - 10 - 80, guiTop + dy, 80, 20, info.getAudio().getSamplingRate(), false,
            (b, value) -> {

            }));
            samplingRateInput.setNumberBounds(0, 48000);
            samplingRateInput.setValue(info.getAudio().getSamplingRate() < 0 ? DEFAULT_SAMPLING_RATE : info.getAudio().getSamplingRate(), true);
            dy += 20 + 2;
            qualityInput = addRenderableWidget(GuiUtils.createSlider(guiLeft + 10, guiTop + dy, WIN_WIDTH - 20, 20, textQuality, TextUtils.empty(), 0, 10, 1, 5, true,
            (b, val) -> {

            }, (b) -> {
                b.setMessage(TextUtils.text(String.format("%s: %s", textQuality.getString(), b.getValueInt())));
            }));
            qualityInput.setRenderStyle(AreaStyle.DRAGONLIB);
            qualityInput.setValue(qualityInput.getValue());
        }


        dy += 20 + 10;
        addRenderableWidget(new DLButton(width / 2 + 2, guiTop + dy, 80, 20, CommonComponents.GUI_CANCEL, b -> close())).setRenderStyle(AreaStyle.DRAGONLIB);
        
        doneBtn.set(addRenderableWidget(new DLButton(width / 2 - 2 - 80, guiTop + dy, 80, 20, textUpload, b -> {
            MutableSingle<UploadProgressPopup> progressWindow = new MutableSingle<>(null);
            long id = ClientApi.uploadSound(
                path.toString(),
                new SoundFile.Builder(ClientWrapper.myLocation("sound_player"), nameBox.getValue(), Map.of(BlockBeats.META_VISIBILITY, visibility.getName(), BlockBeats.META_SHARED, "")),
                new AudioSettings(channels, bitRateInput == null ? info.getAudio().getBitRate() : (int)bitRateInput.getValue(), samplingRateInput == null ? info.getAudio().getSamplingRate() : (int)samplingRateInput.getValue(), (byte)(qualityInput == null ? 5 : qualityInput.getValueInt())),
                (result) -> {
                    if (progressWindow.getFirst() != null) {
                        progressWindow.getFirst().close();
                        if (container.get() != null) {
                            container.get().refresh();
                        }
                    }
                }, (serverProgress, clientProgress) -> {
                    if (progressWindow.getFirst() != null) {
                        progressWindow.getFirst().progressBar.setValue(clientProgress.progress());
                        progressWindow.getFirst().progressBar.setBufferValue(serverProgress.progress());
                        progressWindow.getFirst().currentState = clientProgress.state();
                    }
                }, (status) -> {
                    if (progressWindow.getFirst() != null) {
                        progressWindow.getFirst().close();
                        if (container.get() != null) {
                            container.get().refresh();
                        }
                    }
                }
            );
            getParent().setPopup((w, h, l, cl) -> {
                return store(new UploadProgressPopup(getParent(), l, id, w, h, cl), progressWindow);
            });
            close();
        })));
        doneBtn.get().setRenderStyle(AreaStyle.DRAGONLIB);
        doneBtn.get().setBackColor(DragonLib.PRIMARY_BUTTON_COLOR);

        int oldWinHeight = winHeight;
        winHeight = dy + 30;
        if (oldWinHeight != winHeight) {
            init(extended);
        }
        extendedOptions = extended;
    }

    private <S> S store(S s, MutableSingle<S> in) {
        in.setFirst(s);
        return s;
    }
    
    @Override
    public void renderMainPopupLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainPopupLayer(graphics, mouseX, mouseY, partialTicks);
        DynamicGuiRenderer.renderWindow(graphics, guiLeft, guiTop, WIN_WIDTH, winHeight);
        GuiUtils.drawString(graphics, font, guiLeft + 6, guiTop + 6, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);

        if (extendedOptions) {
            GuiUtils.drawString(graphics, font, guiLeft + 10, bitRateInput.getY() + bitRateInput.getHeight() / 2 - font.lineHeight / 2, textBitRate, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
            GuiUtils.drawString(graphics, font, guiLeft + 10, samplingRateInput.getY() + samplingRateInput.getHeight() / 2 - font.lineHeight / 2, textSamplingRate, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
        }
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
        tooltips.forEach(x -> {
            if (x.getAssignedWidget() instanceof IDragonLibWidget wgt && !wgt.isMouseSelected()) {
                return;
            }
            GuiUtils.renderTooltipAt(getParent(), GuiAreaDefinition.of(x.getAssignedWidget()), x.getLines(), x.getMaxWidth() > 0 ? x.getMaxWidth() : getParent().width(), graphics, x.getAssignedWidget().x, x.getAssignedWidget().y + x.getAssignedWidget().getHeight(), mouseX, mouseY, 0, 0);  
        });
    }
}
