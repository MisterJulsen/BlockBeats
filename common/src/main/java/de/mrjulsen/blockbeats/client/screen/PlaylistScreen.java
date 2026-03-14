package de.mrjulsen.blockbeats.client.screen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mojang.blaze3d.platform.InputConstants;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.block.entity.SoundPlayerBlockEntity;
import de.mrjulsen.blockbeats.client.ClientWrapper;
import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer.TaskBuilder;
import de.mrjulsen.blockbeats.client.widgets.popup.FileSelectionPopup;
import de.mrjulsen.blockbeats.client.widgets.popup.PlaybackAreaPopup;
import de.mrjulsen.blockbeats.client.widgets.popup.PlaybackConfigPopup;
import de.mrjulsen.blockbeats.client.widgets.popup.PopupWidget.IPopupBuilder;
import de.mrjulsen.blockbeats.core.OrderedArrayList;
import de.mrjulsen.blockbeats.core.data.ELoopMode;
import de.mrjulsen.blockbeats.core.data.ERedstoneMode;
import de.mrjulsen.blockbeats.core.data.EShuffleMode;
import de.mrjulsen.blockbeats.core.data.Playlist;
import de.mrjulsen.blockbeats.core.data.playback.IPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.core.data.playback.RadiusPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.core.filters.SoundPlaylistFilter;
import de.mrjulsen.blockbeats.events.ClientEvents;
import de.mrjulsen.blockbeats.net.cts.SoundPlayerPacket;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLIconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLVerticalScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IDragonLibWidget;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLAbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.WidgetsCollection;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class PlaylistScreen extends DLPopupScreen {

    private static final int HEADER_HEIGHT = 40;
    private static final int FOOTER_HEIGHT = 40;
    private static final int LEFT_MARGIN = 20;
    private static final int RIGHT_MARGIN = 20;
    private static final int TOOLBAR_MARGIN = 8;

    // Text
    private final MutableComponent textAdd = Utils.trans("playlist_screen", "add");
    private final MutableComponent textRemove = Utils.trans("playlist_screen", "remove");
    private final MutableComponent textMoveUp = Utils.trans("playlist_screen", "move_up");
    private final MutableComponent textMoveDown = Utils.trans("playlist_screen", "move_down");

    private final MutableComponent descriptionAdd = Utils.trans("playlist_screen", "description.add");
    private final MutableComponent descriptionPlaybackArea = Utils.trans("playlist_screen", "description.playback_area");
    private final MutableComponent descriptionPlaybackConfig = Utils.trans("playlist_screen", "description.playback_config");
    private final MutableComponent descriptionLock = Utils.trans("playlist_screen", "description.lock");

    // Settings
    private String searchTerm = "";
    private DLVerticalScrollBar scrollBar;
    private FileBrowserContainer container;


    private final SoundPlayerBlockEntity blockEntity;

    // Collections
    private static final WidgetsCollection soundFileCollection = new WidgetsCollection();
    private final Collection<DLTooltip> tooltips = new ArrayList<>();

    private DLTooltip redstoneTooltip;
    private DLTooltip loopTooltip;
    private DLTooltip shuffleTooltip;

    // Settings
    private final OrderedArrayList<String> files = new OrderedArrayList<>();
    private final Comparator<SoundFile> orderedPlaylistSortFunc = (a, b) -> files.indexOf(a.toString()) - files.indexOf(b.toString());

    private ELoopMode loop = ELoopMode.NO_LOOP;
    private ERedstoneMode redstone = ERedstoneMode.NO_REDSTONE;
    private EShuffleMode shuffle = EShuffleMode.NO_SHUFFLE;
    private IPlaybackAreaBuilder playbackArea = new RadiusPlaybackAreaBuilder();
    private boolean locked;
    private float volume;
    private float pitch;
    private int attenuationDistance;
    private boolean bgm;
    private boolean showLabel;

    public PlaylistScreen(SoundPlayerBlockEntity blockEntity) {
        super(trans("title"));
        this.blockEntity = blockEntity;
        this.files.addAll(blockEntity.getPlaylist().getFiles());
        this.loop = blockEntity.getPlaylist().getLoop();
        this.shuffle = blockEntity.getPlaylist().getRandom();
        this.redstone = blockEntity.getRedstone();
        this.locked = blockEntity.isLocked();
        this.playbackArea = blockEntity.getPlaybackAreaBuilder();
        this.volume = blockEntity.getVolume();
        this.pitch = blockEntity.getPitch();
        this.attenuationDistance = blockEntity.getAttenuationDistance();
        this.bgm = blockEntity.isBgm();
        this.showLabel = blockEntity.isShowLabel();
    }

    private static MutableComponent trans(String key) {
        return TextUtils.translate("gui." + BlockBeats.MOD_ID + ".playlist_screen." + key);
    }

    private void updateRedstoneTooltip(DLIconButton btn) {
        List<FormattedText> lines = ClientWrapper.getEnumTooltipData(BlockBeats.MOD_ID, ERedstoneMode.class, this.redstone, width / 2);
        redstoneTooltip = DLTooltip.of(lines).assignedTo(btn);
    }

    private void updateLoopTooltip(DLIconButton btn) {
        List<FormattedText> lines = ClientWrapper.getEnumTooltipData(BlockBeats.MOD_ID, ELoopMode.class, this.loop, width / 2);
        loopTooltip = DLTooltip.of(lines).assignedTo(btn);
    }

    private void updateShuffleTooltip(DLIconButton btn) {
        List<FormattedText> lines = ClientWrapper.getEnumTooltipData(BlockBeats.MOD_ID, EShuffleMode.class, this.shuffle, width / 2);
        shuffleTooltip = DLTooltip.of(lines).assignedTo(btn);
    }

    @Override
    protected void init() {
        super.init();
        soundFileCollection.clear();

        container = addRenderableWidget(new FileBrowserContainer(this, 0, HEADER_HEIGHT, width - 8, height - HEADER_HEIGHT - FOOTER_HEIGHT, this::getScrollBar,
        List.of(new SoundPlaylistFilter(files)),
        orderedPlaylistSortFunc,
        (file) -> List.of(
                new TaskBuilder(ModGuiIcons.REMOVE.getAsSprite(16, 16), textRemove,
                    (btn) -> {
                        files.removeIf(x -> x.equals(btn.getAttachedSoundFile().toString()));
                        refreshFileView();
                    }, true),
                new TaskBuilder(GuiIcons.ARROW_UP.getAsSprite(16, 16), textMoveUp,
                    (btn) -> {
                        int index = files.indexOf(btn.getAttachedSoundFile().toString());
                        if (index <= 0) {
                            return;
                        }
                        files.moveBack(index, 1);
                        refreshFileView();
                    }, true),
                new TaskBuilder(GuiIcons.ARROW_DOWN.getAsSprite(16, 16), textMoveDown,
                    (btn) -> {
                        int index = files.indexOf(btn.getAttachedSoundFile().toString());
                        if (index >= files.size() - 1) {
                            return;
                        }
                        files.moveForth(index, 1);
                        refreshFileView();
                    }, true)
            )
        ));
        scrollBar = addRenderableWidget(new DLVerticalScrollBar(width - 8, HEADER_HEIGHT, 8, height - HEADER_HEIGHT - FOOTER_HEIGHT, new GuiAreaDefinition(0, HEADER_HEIGHT, width, height - HEADER_HEIGHT - FOOTER_HEIGHT)))
            .setAutoScrollerSize(true)
            .setScreenSize(container.getHeight())
            .setStepSize(15)
            .setMaxScroll(container.maxRequiredHeight())
            .withOnValueChanged((scrollbar) -> container.setYScrollOffset(scrollbar.getScrollValue()))
        ;

        // Widgts
        DLEditBox box = addRenderableWidget(new DLEditBox(font, width - RIGHT_MARGIN - 100, HEADER_HEIGHT - 16 - TOOLBAR_MARGIN, 100, 16, TextUtils.empty()) {
            @Override
            public boolean keyPressed(int code, int scan, int mod) {
                if (container != null && (code == InputConstants.KEY_RETURN || code == InputConstants.KEY_NUMPADENTER)) {
                    container.setSearchFilter(getValue(), true);
                    GuiUtils.playButtonSound();
                }
                return super.keyPressed(code, scan, mod);
            }
        }
            .withHint(DragonLib.TEXT_SEARCH)
            .withOnFocusChanged((b, focus) -> {

            }
        ));
        box.setMaxLength(BlockBeats.MAX_FILENAME_LENGTH);
        box.setResponder((value) -> {
            searchTerm = value;
        });
        box.setValue(searchTerm);
        box.setBordered(true);


        addButton(width - RIGHT_MARGIN - 80, height - FOOTER_HEIGHT + TOOLBAR_MARGIN, 80, 20, DragonLib.TEXT_CLOSE, (btn) -> onClose(), null).setRenderStyle(AreaStyle.DRAGONLIB);
        
        DLIconButton addBtn = addRenderableWidget(new DLIconButton(ButtonType.DEFAULT, AreaStyle.DRAGONLIB, ModGuiIcons.ADD.getAsSprite(16, 16), LEFT_MARGIN, height - FOOTER_HEIGHT + TOOLBAR_MARGIN, 80, 20, textAdd,
        (btn) -> {
            setPopup((x, y, l, close) -> new FileSelectionPopup(this, l, width, height, close,
                (selectedFiles) -> {
                    Set<String> fileSet = new LinkedHashSet<>(files);
                    fileSet.addAll(selectedFiles.stream().map(a -> a.toString()).toList());
                    files.clear();
                    files.addAll(fileSet);
                    refreshFileView();
                }
            ));
        }));
        addBtn.withAlignment(EAlignment.LEFT);
        addBtn.setBackColor(DragonLib.DEFAULT_BUTTON_COLOR);
        addBtn.setFontColor(DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE);
        tooltips.add(DLTooltip.of(ClientWrapper.split(descriptionAdd, width / 2, Style.EMPTY)).assignedTo(addBtn));

        DLIconButton iconBtn;
        iconBtn = addRenderableWidget(new DLIconButton(ButtonType.DEFAULT, AreaStyle.DRAGONLIB, this.redstone.getIcon().getAsSprite(16, 16), LEFT_MARGIN + 90, height - FOOTER_HEIGHT + TOOLBAR_MARGIN, 20, 20, null,
        (btn) -> {
            this.redstone = this.redstone.next();
            btn.setBackColor(this.redstone.getButtonColor());
            btn.setSprite(this.redstone.getIcon().getAsSprite(16, 16));
            updateRedstoneTooltip(btn);
        }));
        iconBtn.setBackColor(this.redstone.getButtonColor());
        updateRedstoneTooltip(iconBtn);

        iconBtn = addRenderableWidget(new DLIconButton(ButtonType.DEFAULT, AreaStyle.DRAGONLIB, this.shuffle.getIcon().getAsSprite(16, 16), LEFT_MARGIN + 110, height - FOOTER_HEIGHT + TOOLBAR_MARGIN, 20, 20, null,
        (btn) -> {
            this.shuffle = this.shuffle.next();
            btn.setBackColor(this.shuffle.getButtonColor());
            btn.setSprite(this.shuffle.getIcon().getAsSprite(16, 16));
            updateShuffleTooltip(btn);
        }));
        iconBtn.setBackColor(this.shuffle.getButtonColor());
        updateShuffleTooltip(iconBtn);

        iconBtn = addRenderableWidget(new DLIconButton(ButtonType.DEFAULT, AreaStyle.DRAGONLIB, this.loop.getIcon().getAsSprite(16, 16), LEFT_MARGIN + 130, height - FOOTER_HEIGHT + TOOLBAR_MARGIN, 20, 20, null,
        (btn) -> {
            this.loop = this.loop.next();
            btn.setBackColor(this.loop.getButtonColor());
            btn.setSprite(this.loop.getIcon().getAsSprite(16, 16));
            updateLoopTooltip(btn);
        }));
        iconBtn.setBackColor(this.loop.getButtonColor());
        updateLoopTooltip(iconBtn);

        iconBtn = addRenderableWidget(new DLIconButton(ButtonType.DEFAULT, AreaStyle.DRAGONLIB, ModGuiIcons.BOX.getAsSprite(16, 16), LEFT_MARGIN + 160, height - FOOTER_HEIGHT + TOOLBAR_MARGIN, 20, 20, null,
        (btn) -> {
            setPopup((x, y, l, close) -> new PlaybackAreaPopup(this, blockEntity.getBlockPos(), l, width, height, playbackArea, 
            (areaSettings) -> {
                this.playbackArea = areaSettings;
            }, close));
        }));
        iconBtn.setBackColor(DragonLib.DEFAULT_BUTTON_COLOR);
        tooltips.add(DLTooltip.of(ClientWrapper.split(descriptionPlaybackArea, width / 2, Style.EMPTY)).assignedTo(iconBtn));

        iconBtn = addRenderableWidget(new DLIconButton(ButtonType.DEFAULT, AreaStyle.DRAGONLIB, ModGuiIcons.SOUND.getAsSprite(16, 16), LEFT_MARGIN + 180, height - FOOTER_HEIGHT + TOOLBAR_MARGIN, 20, 20, null,
        (btn) -> {
            setPopup((x, y, l, close) -> new PlaybackConfigPopup(this, l, width, height, volume, pitch, attenuationDistance, bgm, showLabel, close,
            (pop) -> {
                this.volume = pop.getVolume();
                this.pitch = pop.getPitch();
                this.attenuationDistance = pop.getAttenuationDistance();
                this.bgm = pop.isBgm();
                this.showLabel = pop.isShowLabel();
            }));
        }));
        iconBtn.setBackColor(DragonLib.DEFAULT_BUTTON_COLOR);
        tooltips.add(DLTooltip.of(ClientWrapper.split(descriptionPlaybackConfig, width / 2, Style.EMPTY)).assignedTo(iconBtn));

        if (blockEntity.getOwner().equals(Minecraft.getInstance().player.getUUID())) {
            iconBtn = addRenderableWidget(new DLIconButton(ButtonType.DEFAULT, AreaStyle.DRAGONLIB, locked ? ModGuiIcons.LOCK.getAsSprite(16, 16) : ModGuiIcons.UNLOCK.getAsSprite(16, 16), LEFT_MARGIN + 210, height - FOOTER_HEIGHT + TOOLBAR_MARGIN, 20, 20, null,
            (btn) -> {
                this.locked = !this.locked;
                btn.setBackColor(locked ? DragonLib.WARN_BUTTON_COLOR : DragonLib.DEFAULT_BUTTON_COLOR);
                btn.setSprite(locked ? ModGuiIcons.LOCK.getAsSprite(16, 16) : ModGuiIcons.UNLOCK.getAsSprite(16, 16));
            }));
            iconBtn.setBackColor(locked ? DragonLib.WARN_BUTTON_COLOR : DragonLib.DEFAULT_BUTTON_COLOR);
            tooltips.add(DLTooltip.of(ClientWrapper.split(descriptionLock, width / 2, Style.EMPTY)).assignedTo(iconBtn));
        }
    }

    public void refreshFileView() {
        container.setFilters(List.of(new SoundPlaylistFilter(files)), orderedPlaylistSortFunc, true);
    }

    public DLVerticalScrollBar getScrollBar() {
        return scrollBar;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        setDragging(true);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        return this.getFocused() != null && this.isDragging() && this.getFocused().mouseDragged(d, e, i, f, g);
    }

    @Override
    public void setPopup(IPopupBuilder builder) {
        int allowedLayer = getAllowedLayer() + 1;
        setAllowedLayer(allowedLayer);
        addRenderableWidget(builder.create(width, height, allowedLayer, p -> {
            removeWidget(p);
            setAllowedLayer(getAllowedLayer() - 1);
        }));
    }

    @Override
    public void onClose() {
        files.retainAll(container.getFiles().stream().map(x -> x.toString()).toList());
        BlockBeats.net().sendToServer(new SoundPlayerPacket(
            blockEntity.getBlockPos(),
            new Playlist(files, loop, shuffle, 0),
            playbackArea,
            redstone,
            volume,
            pitch,
            attenuationDistance,
            bgm,
            showLabel,
            locked
        ));
        ClientEvents.stopCurrentAudioSample();
        super.onClose();
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        renderScreenBackground(graphics);

        // WINDOW
        DynamicGuiRenderer.renderWindow(graphics, new GuiAreaDefinition(-5, -5, width + 10, HEADER_HEIGHT + 5));
        DynamicGuiRenderer.renderWindow(graphics, new GuiAreaDefinition(-5, height - FOOTER_HEIGHT, width + 10, FOOTER_HEIGHT + 5));

        // TITLE
        GuiUtils.drawString(graphics, font, LEFT_MARGIN, HEADER_HEIGHT / 2 - font.lineHeight / 2, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);

        // SHADOWS
        GuiUtils.fillGradient(graphics, 0, HEADER_HEIGHT, 0, width, 10, 0x77000000, 0x00000000);
        GuiUtils.fillGradient(graphics, 0, height - FOOTER_HEIGHT - 10, 0, width, 10, 0x00000000, 0x77000000);       

        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderFrontLayer(graphics, mouseX, mouseY, partialTick);
        if (getAllowedLayer() != 0) {
            return;
        }

        tooltips.forEach(x -> renderToolip(graphics, mouseX, mouseY, x));
        renderToolip(graphics, mouseX, mouseY, redstoneTooltip);
        renderToolip(graphics, mouseX, mouseY, loopTooltip);
        renderToolip(graphics, mouseX, mouseY, shuffleTooltip);
    }

    private void renderToolip(Graphics graphics, int mouseX, int mouseY, DLTooltip tooltip) {
        if (tooltip.getAssignedWidget() instanceof IDragonLibWidget wgt && !wgt.isMouseSelected()) {
            return;
        }
        int i = tooltip.getLines().size() * (font.lineHeight + 1) + 8;
        GuiUtils.renderTooltipAt(this, GuiAreaDefinition.of(tooltip.getAssignedWidget()), tooltip.getLines(), tooltip.getMaxWidth() > 0 ? tooltip.getMaxWidth() : this.width(), graphics, tooltip.getAssignedWidget().x, height - FOOTER_HEIGHT - i, mouseX, mouseY, 0, 0);  
    }
    
}
