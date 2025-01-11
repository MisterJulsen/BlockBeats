package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer.TaskBuilder;
import de.mrjulsen.blockbeats.core.ESoundVisibility;
import de.mrjulsen.blockbeats.core.data.SharingUtils;
import de.mrjulsen.blockbeats.core.data.SharingUtils.ShareData;
import de.mrjulsen.blockbeats.core.filters.FavoritesFilter;
import de.mrjulsen.blockbeats.core.filters.PlayerFileAccessFilter;
import de.mrjulsen.blockbeats.net.cts.GetAdditionalFileDataPacket;
import de.mrjulsen.blockbeats.net.cts.ManageFavoritesPacket;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.FileInfoFilter;
import de.mrjulsen.dragnsounds.core.data.filter.FileMetadataFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLItemButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLVerticalScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IDragonLibWidget;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLAbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.WidgetsCollection;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.data.Single.MutableSingle;
import de.mrjulsen.mcdragonlib.net.DLNetworkManager;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class FileSelectionPopup extends PopupWidget {

    private final MutableComponent title = Utils.trans("browser", "title");
    private final MutableComponent textUpload = Utils.trans("browser", "upload");
    private final MutableComponent textSelect = Utils.trans("browser", "select");
    private final MutableComponent textRename = Utils.trans("browser", "rename");
    private final MutableComponent textDelete = Utils.trans("browser", "delete");
    private final MutableComponent textShare = Utils.trans("browser", "share");
    private final MutableComponent textFavorite = Utils.trans("browser", "add_favorite");
    private final MutableComponent textRemFavorite = Utils.trans("browser", "rem_favorite");
    
    private final MutableComponent tabAll = Utils.trans("browser", "tab_all");
    private final MutableComponent tabMyCollection = Utils.trans("browser", "tab_my_collection");
    private final MutableComponent tabShared = Utils.trans("browser", "tab_shared");
    private final MutableComponent tabFavorites = Utils.trans("browser", "tab_favorites");
    private final MutableComponent descriptionAll = Utils.trans("browser", "description_all");
    private final MutableComponent descriptionMyCollection = Utils.trans("browser", "description_my_collection");
    private final MutableComponent descriptionShared = Utils.trans("browser", "description_shared");
    private final MutableComponent descriptionFavorites = Utils.trans("browser", "description_favorites");

    private final MutableComponent textFavoritesAdded = Utils.trans("browser", "favorites_added");
    private final MutableComponent textFavoritesRemoved = Utils.trans("browser", "favorites_removed");
    private final GuiAreaDefinition definition;

    private final WidgetsCollection tabsCollection = new WidgetsCollection();
    private final Collection<DLTooltip> tooltips = new ArrayList<>();
    private final Comparator<SoundFile> alphabeticalOrder = (a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
    private final PlayerFileAccessFilter defaultAccessFilter = new PlayerFileAccessFilter(Minecraft.getInstance().player);

    private static final int WIN_WIDTH = 400;
    private static final int WIN_HEIGHT = 250;

    private int guiLeft, guiTop;

    private DLVerticalScrollBar scrollBar;
    private DLButton doneButton;
    private int currentTabX = 0;

    @SuppressWarnings("resource")
    public FileSelectionPopup(DLPopupScreen parent, int layer, int width, int height, Consumer<PopupWidget> close, Consumer<Set<SoundFile>> onSelect) {
        super(parent, layer, width, height, close);
        guiLeft = width / 2 - WIN_WIDTH / 2;
        guiTop = height / 2 - WIN_HEIGHT / 2;
        definition = new GuiAreaDefinition(guiLeft + 6, guiTop + 38, WIN_WIDTH - 12 - 8, WIN_HEIGHT - 35 - 38);

        MutableSingle<FileBrowserContainer> container = new MutableSingle<>(null);

        // TABS
        addTab(tabAll, descriptionAll, Items.MUSIC_DISC_CAT, (btn) -> {
            if (container.getFirst() != null) {
                container.getFirst().setFilters(List.of(defaultAccessFilter), alphabeticalOrder, true);
            }
        });
        addTab(tabMyCollection, descriptionMyCollection, Blocks.BOOKSHELF, (btn) -> {
            container.getFirst().setFilters(List.of(defaultAccessFilter, new FileInfoFilter(FileInfoFilter.KEY_OWNER_UUID, Minecraft.getInstance().player.getUUID().toString(), ECompareOperation.EQUALS)), alphabeticalOrder, true);
        }).select();
        addTab(tabShared, descriptionShared, Items.DIAMOND, (btn) -> {
            container.getFirst().setFilters(List.of(defaultAccessFilter, new FileMetadataFilter(BlockBeats.META_SHARED, Minecraft.getInstance().player.getUUID().toString(), ECompareOperation.CONTAINS)), alphabeticalOrder, true); 
        });
        addTab(tabFavorites, descriptionFavorites, Items.NETHER_STAR, (btn) -> {
            container.getFirst().setFilters(List.of(defaultAccessFilter, FavoritesFilter.of(Minecraft.getInstance().player, ECompareOperation.EQUALS)), alphabeticalOrder, true); 
        });

        // Search bar
        int maxSearchBarWidth = Math.min(WIN_WIDTH - 18 - currentTabX, 100);

        // Browser
        container.setFirst(addRenderableWidget(new FileBrowserContainer(parent, definition.getX() + 1, definition.getY() + 1, definition.getWidth() - 2, definition.getHeight() - 2, this::getScrollBar,
        List.of(defaultAccessFilter, new FileInfoFilter(FileInfoFilter.KEY_OWNER_UUID, Minecraft.getInstance().player.getUUID().toString(), ECompareOperation.EQUALS)),
        alphabeticalOrder,
        (file) -> {
            List<TaskBuilder> tasks = new LinkedList<>();
            Map<UUID, ShareData> sharingData = SharingUtils.deserialize(file);
            boolean canEditProp = false;
            try{
                canEditProp = Boolean.parseBoolean(SharingUtils.getMetaSafe(sharingData, Minecraft.getInstance().player.getUUID(), BlockBeats.META_SHARE_CAN_EDIT));
            } catch (Exception e) {}
            boolean isOwner = file.getInfo().getOwnerId().equals(Minecraft.getInstance().player.getUUID());
            boolean canDelete = isOwner || ESoundVisibility.getByName(file.getMetadataSafe(BlockBeats.META_VISIBILITY)) == ESoundVisibility.PUBLIC;
            boolean canEdit = canDelete || canEditProp;

            if (canDelete) {                
                tasks.add(new TaskBuilder(ModGuiIcons.DELETE.getAsSprite(16, 16), textDelete, (widget) -> {
                    getParent().setPopup((w, h, l, c) -> new DeleteFilePopup(getParent(), l, widget.getAttachedSoundFile(), container::getFirst, w, h, c));
                }, true));
            }
            
            if (canEdit) {                
                tasks.add(new TaskBuilder(ModGuiIcons.RENAME.getAsSprite(16, 16), textRename, (widget) -> {
                    getParent().setPopup((w, h, l, c) -> new RenameSoundFilePopup(getParent(), l, widget.getAttachedSoundFile(), container::getFirst, w, h, c));
                }, true));
            }
            
            if (isOwner) {
                tasks.add(new TaskBuilder(ModGuiIcons.SHARE2.getAsSprite(16, 16), textShare, (widget) -> {
                    getParent().setPopup((w, h, l, c) -> new SharePopup(getParent(), l, w, h, widget.getAttachedSoundFile(), (pop) -> {
                        c.accept(pop);
                        container.getFirst().refresh();
                    }));
                }, true));
            }

            boolean isFav = container.getFirst() != null && container.getFirst().getFavoritePaths().contains(file.toString());
            tasks.add(new TaskBuilder(isFav ? ModGuiIcons.STAR_FILLED.getAsSprite(16, 16) : ModGuiIcons.STAR.getAsSprite(16, 16), isFav ? textRemFavorite : textFavorite, (widget) -> {
                DLNetworkManager.sendToServer(GetAdditionalFileDataPacket.create((favs, usernamecache) -> {
                    final String path = widget.getAttachedSoundFile().toString();
                    final boolean remove = favs.contains(path);
                    DLNetworkManager.sendToServer(ManageFavoritesPacket.create(Set.of(path), remove, () -> {
                        if (remove) {
                            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastId.PERIODIC_NOTIFICATION, textFavoritesRemoved, TextUtils.text(widget.getAttachedSoundFile().getDisplayName())));
                        } else {                                
                        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastId.PERIODIC_NOTIFICATION, textFavoritesAdded, TextUtils.text(widget.getAttachedSoundFile().getDisplayName())));
                        }
                        container.getFirst().refresh();
                    }));
                }));
            }, true));

            return tasks;
        })));
        scrollBar = addRenderableWidget(new DLVerticalScrollBar(definition.getRight(), definition.getY(), 8, definition.getHeight(), definition))
            .setAutoScrollerSize(true)
            .setScreenSize(container.getFirst().getHeight())
            .setStepSize(15)
            .setMaxScroll(container.getFirst().maxRequiredHeight())
            .withOnValueChanged((scrollbar) -> container.getFirst().setYScrollOffset(scrollbar.getScrollValue()))
        ;

        addRenderableWidget(new DLButton(guiLeft + WIN_WIDTH - 6 - 80, guiTop + WIN_HEIGHT - 10 - 20, 80, 20, CommonComponents.GUI_CANCEL, b -> close())).setRenderStyle(AreaStyle.DRAGONLIB);
        doneButton = addRenderableWidget(new DLButton(guiLeft + WIN_WIDTH - 10 - 160, guiTop + WIN_HEIGHT - 10 - 20, 80, 20, textSelect, b -> {
            onSelect.accept(container.getFirst() == null ? Set.of() : container.getFirst().getSelectedFiles());
            close();
        }));
        doneButton.set_active(false);
        doneButton.setRenderStyle(AreaStyle.DRAGONLIB);
        doneButton.setBackColor(DragonLib.PRIMARY_BUTTON_COLOR);

        addRenderableWidget(new DLButton(guiLeft + 6, guiTop + WIN_HEIGHT - 10 - 20, 80, 20, textUpload, b -> {
            ClientApi.showFileDialog(false, (paths) -> {
                if (paths.isPresent()) {
                    getParent().setPopup((x, y, l, cl) -> new UploadSoundPopup(getParent(), l, paths.get()[0], container::getFirst, width, height, cl));
                }
            });
        })).setRenderStyle(AreaStyle.DRAGONLIB);

        
        DLEditBox box = addRenderableWidget(new DLEditBox(font, guiLeft + WIN_WIDTH - 6 - maxSearchBarWidth + 1, guiTop + 21, maxSearchBarWidth - 2, 16, TextUtils.empty()) {
            @Override
            public boolean keyPressed(int code, int scan, int mod) {
                if (container.getFirst() != null && (code == InputConstants.KEY_RETURN || code == InputConstants.KEY_NUMPADENTER)) {
                    container.getFirst().setSearchFilter(getValue(), true);
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
        });
        box.setValue("");
        box.setBordered(true);

        container.getFirst().setMultiselect(true);
        container.getFirst().setWidgetLayerIndex(this.getWidgetLayerIndex());
        container.getFirst().setOnSelectionChangedListener((cont) -> {
            doneButton.setMessage(TextUtils.empty().append(textSelect).append(cont.getSelectedFiles().size() > 0 ? " (" + cont.getSelectedFiles().size() + ")" : ""));
            doneButton.set_active(cont.getSelectedFiles().size() > 0);
        });
    }

    public DLVerticalScrollBar getScrollBar() {
        return scrollBar;
    }

    private DLItemButton addTab(MutableComponent text, MutableComponent description, ItemLike item, Consumer<DLItemButton> onClick) {
        int w = font.width(text) + 30;
        DLItemButton btn = addRenderableWidget(new DLItemButton(
            ButtonType.RADIO_BUTTON,
            AreaStyle.DRAGONLIB,
            new ItemStack(item),
            tabsCollection,
            guiLeft + 6 + currentTabX,
            guiTop + 20,
            w,
            18,
            text,
            onClick
        ));
        tooltips.add(DLTooltip.of(description).assignedTo(btn).withMaxWidth(WIN_WIDTH / 2));
        btn.withAlignment(EAlignment.LEFT);
        currentTabX += w;
        return btn;
    }
    
    @Override
    public void renderMainPopupLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainPopupLayer(graphics, mouseX, mouseY, partialTicks);
        DynamicGuiRenderer.renderWindow(graphics, guiLeft, guiTop, WIN_WIDTH, WIN_HEIGHT);
        DynamicGuiRenderer.renderContainerBackground(graphics, definition);
        GuiUtils.drawString(graphics, font, guiLeft + 6, guiTop + 6, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
        tooltips.forEach(x -> {
            if (x.getAssignedWidget() instanceof IDragonLibWidget wgt && !wgt.isMouseSelected()) {
                return;
            }
            GuiUtils.renderTooltipAt(getParent(), GuiAreaDefinition.of(x.getAssignedWidget()), x.getLines(), x.getMaxWidth() > 0 ? x.getMaxWidth() : getParent().width(), graphics, x.getAssignedWidget().getX(), x.getAssignedWidget().getY() + x.getAssignedWidget().getHeight(), mouseX, mouseY, 0, 0);  
        });
    }
}
