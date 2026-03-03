package de.mrjulsen.blockbeats.client.screen;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.block.entity.SoundPlayerBlockEntity;
import de.mrjulsen.blockbeats.client.ClientWrapper;
import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer.TaskBuilder;
import de.mrjulsen.blockbeats.client.widgets.popup.FileSelectionPopup;
import de.mrjulsen.blockbeats.client.widgets.popup.PlaybackAreaPopup;
import de.mrjulsen.blockbeats.client.widgets.popup.PlaybackConfigPopup;
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
import de.mrjulsen.blockbeats.registry.ModNetworkManager;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextLabel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.DLAbstractRichTextInputField;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.render.DLTextureSheet;
import de.mrjulsen.mcdragonlib.client.render.GuiIcons;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;

public class PlaylistScreen extends DLWindow {

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
    private DLScrollBar scrollBar;
    private FileBrowserContainer container;


    private final SoundPlayerBlockEntity blockEntity;

    // Collections
    private final DLPanel soundFileCollection;

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

    public PlaylistScreen(DLWindowManager manager, SoundPlayerBlockEntity blockEntity) {
        super(manager);
        fullscreen.set(true);

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



        
        soundFileCollection = new DLPanel(0, 0, 100, 100);

        container = addComponent(new FileBrowserContainer(0, HEADER_HEIGHT, width() - 8, height() - HEADER_HEIGHT - FOOTER_HEIGHT, false, () -> scrollBar,
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

        scrollBar = addComponent(new DLScrollBar(width() - 8, HEADER_HEIGHT, 8, height() - HEADER_HEIGHT - FOOTER_HEIGHT, Orientation.VERTICAL));
        scrollBar.scrollerSize.set(-1);
        scrollBar.screenSize.set(container.height());
        scrollBar.scrollSteps.set(15);
        scrollBar.addEventListener(DLScrollBar.ValueChangedEvent.class, (s, e) -> {
            container.setScrollOffsetY((int)e.value());
            return false;
        });

        // Widgts
        DLRichTextEditBox box = addComponent(new DLRichTextEditBox(width() - RIGHT_MARGIN - 100, HEADER_HEIGHT - 16 - TOOLBAR_MARGIN, 100, 16));
        box.placeholderText.set(TextUtils.TEXT_SEARCH);
        box.acceptAndCancelKeysEnabled.set(true);
        box.text.get().set(searchTerm);
        box.addEventListener(DLAbstractRichTextInputField.TextAcceptKeyPressedEvent.class, (s, e) -> {
            container.setSearchFilter(box.text.get().getPlainText(), true);
            GuiUtils.playButtonSound();
            return false;
        });
        box.addEventListener(DLRichTextLabel.TextChangedEvent.class, (s, e) -> {
            searchTerm = box.text.get().getPlainText();
            return false;
        });        


        DLButton closeBtn = addComponent(new DLButton(width() - RIGHT_MARGIN - 80, height() - FOOTER_HEIGHT + TOOLBAR_MARGIN, 80, 20));
        closeBtn.text.set(TextUtils.TEXT_CLOSE);
        closeBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });
        
        DLButton addBtn = addComponent(new DLButton(LEFT_MARGIN, height() - FOOTER_HEIGHT + TOOLBAR_MARGIN, 80, 20));
        addBtn.icon.set(ModGuiIcons.ADD.getAsSprite(16, 16));
        addBtn.text.set(textAdd);
        addBtn.tooltip.set(new DLTooltip(List.of(descriptionAdd), 200));
        addBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().createModal(mgr -> new FileSelectionPopup(mgr,
                (selectedFiles) -> {
                    Set<String> fileSet = new LinkedHashSet<>(files);
                    fileSet.addAll(selectedFiles.stream().map(a -> a.toString()).toList());
                    files.clear();
                    files.addAll(fileSet);
                    refreshFileView();
                }
            ));
            return false;
        });

        DLButton iconBtn;
        iconBtn = addComponent(new DLButton(LEFT_MARGIN + 90, height() - FOOTER_HEIGHT + TOOLBAR_MARGIN, 20, 20));
        iconBtn.text.set(TextUtils.EMPTY);
        iconBtn.icon.set(this.redstone.getIcon().getAsSprite(16, 16));
        iconBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            DLButton btn = (DLButton)s;
            this.redstone = this.redstone.next();
            btn.icon.set(this.redstone.getIcon().getAsSprite(16, 16));
            updateRedstoneTooltip(btn);
            return false;
        });        
        updateRedstoneTooltip(iconBtn);
        
        iconBtn = addComponent(new DLButton(LEFT_MARGIN + 110, height() - FOOTER_HEIGHT + TOOLBAR_MARGIN, 20, 20));
        iconBtn.text.set(TextUtils.EMPTY);
        iconBtn.icon.set(this.shuffle.getIcon().getAsSprite(16, 16));
        iconBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            DLButton btn = (DLButton)s;
            this.shuffle = this.shuffle.next();
            btn.icon.set(this.shuffle.getIcon().getAsSprite(16, 16));
            updateShuffleTooltip(btn);
            return false;
        });
        updateShuffleTooltip(iconBtn);        
        
        iconBtn = addComponent(new DLButton(LEFT_MARGIN + 130, height() - FOOTER_HEIGHT + TOOLBAR_MARGIN, 20, 20));
        iconBtn.text.set(TextUtils.EMPTY);
        iconBtn.icon.set(this.loop.getIcon().getAsSprite(16, 16));
        iconBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            DLButton btn = (DLButton)s;
            this.loop = this.loop.next();
            btn.icon.set(this.loop.getIcon().getAsSprite(16, 16));
            updateLoopTooltip(btn);
            return false;
        });
        updateLoopTooltip(iconBtn);
        
        iconBtn = addComponent(new DLButton(LEFT_MARGIN + 160, height() - FOOTER_HEIGHT + TOOLBAR_MARGIN, 20, 20));
        iconBtn.text.set(TextUtils.EMPTY);
        iconBtn.icon.set(ModGuiIcons.BOX.getAsSprite(16, 16));
        iconBtn.tooltip.set(new DLTooltip(List.of(descriptionPlaybackArea), 200));
        iconBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().createModal(mgr -> new PlaybackAreaPopup(mgr, blockEntity.getBlockPos(), playbackArea, 
            (areaSettings) -> {
                this.playbackArea = areaSettings;
            }));
            return false;
        });

        iconBtn = addComponent(new DLButton(LEFT_MARGIN + 180, height() - FOOTER_HEIGHT + TOOLBAR_MARGIN, 20, 20));
        iconBtn.text.set(TextUtils.EMPTY);
        iconBtn.icon.set(ModGuiIcons.SOUND.getAsSprite(16, 16));
        iconBtn.tooltip.set(new DLTooltip(List.of(descriptionPlaybackConfig), 200));
        iconBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().createModal(mgr -> new PlaybackConfigPopup(mgr, volume, pitch, attenuationDistance, bgm, showLabel,
            (pop) -> {
                this.volume = pop.getVolume();
                this.pitch = pop.getPitch();
                this.attenuationDistance = pop.getAttenuationDistance();
                this.bgm = pop.isBgm();
                this.showLabel = pop.isShowLabel();
            }));
            return false;
        });

        if (blockEntity.getOwner().equals(Minecraft.getInstance().player.getUUID())) {
            iconBtn = addComponent(new DLButton(LEFT_MARGIN + 210, height() - FOOTER_HEIGHT + TOOLBAR_MARGIN, 20, 20));
            iconBtn.text.set(TextUtils.EMPTY);
            iconBtn.icon.set(locked ? ModGuiIcons.LOCK.getAsSprite(16, 16) : ModGuiIcons.UNLOCK.getAsSprite(16, 16));
            iconBtn.tooltip.set(new DLTooltip(List.of(descriptionLock), 200));
            iconBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
                DLButton btn = (DLButton)s; 
                this.locked = !this.locked;
                btn.icon.set(locked ? ModGuiIcons.LOCK.getAsSprite(16, 16) : ModGuiIcons.UNLOCK.getAsSprite(16, 16));
                return false;
            });
        }
        
    }

    private static MutableComponent trans(String key) {
        return TextUtils.translate("gui." + BlockBeats.MOD_ID + ".playlist_screen." + key);
    }

    private void updateRedstoneTooltip(DLButton btn) {
        List<FormattedText> lines = ClientWrapper.getEnumTooltipData(BlockBeats.MOD_ID, ERedstoneMode.class, this.redstone, 200);
        btn.tooltip.set(new DLTooltip(lines, 200));
    }

    private void updateLoopTooltip(DLButton btn) {
        List<FormattedText> lines = ClientWrapper.getEnumTooltipData(BlockBeats.MOD_ID, ELoopMode.class, this.loop, 200);
        btn.tooltip.set(new DLTooltip(lines, 200));
    }

    private void updateShuffleTooltip(DLButton btn) {
        List<FormattedText> lines = ClientWrapper.getEnumTooltipData(BlockBeats.MOD_ID, EShuffleMode.class, this.shuffle, 200);
        btn.tooltip.set(new DLTooltip(lines, 200));
    }

    protected void init() {
    }

    public void refreshFileView() {
        container.setFilters(List.of(new SoundPlaylistFilter(files)), orderedPlaylistSortFunc, true);
    }

    @Override
    public void close() throws Exception {
        files.retainAll(container.getFiles().stream().map(x -> x.toString()).toList());
        ModNetworkManager.SOUND_PLAYER.send(NetworkDirection.toServer(), new SoundPlayerPacket(
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
        super.close();
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        // WINDOW
        DLTextureSheet.DRAGONLIB_UI.getSprite(DLTextureSheet.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, -5, -5, width() + 10, HEADER_HEIGHT + 5);
        DLTextureSheet.DRAGONLIB_UI.getSprite(DLTextureSheet.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, -5, height() - FOOTER_HEIGHT, width() + 10, FOOTER_HEIGHT + 5);

        // TITLE
        GuiUtils.drawString(graphics, graphics.defaultFont(), LEFT_MARGIN, HEADER_HEIGHT / 2 - graphics.defaultFont().lineHeight / 2, trans("title"), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);

        // SHADOWS
        GuiUtils.fillGradient(graphics, 0, HEADER_HEIGHT, 0, width(), DLColor.fromInt(0x77000000), DLColor.BLACK, EAlign.TOP);
        GuiUtils.fillGradient(graphics, 0, height() - FOOTER_HEIGHT - 10, 0, width(), DLColor.fromInt(0x77000000), DLColor.BLACK, EAlign.BOTTOM);       
    }    
}
