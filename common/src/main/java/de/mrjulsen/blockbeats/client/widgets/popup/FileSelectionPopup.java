package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer.TaskBuilder;
import de.mrjulsen.blockbeats.core.ESoundVisibility;
import de.mrjulsen.blockbeats.core.data.SharingUtils;
import de.mrjulsen.blockbeats.core.data.SharingUtils.ShareData;
import de.mrjulsen.blockbeats.core.filters.FavoritesFilter;
import de.mrjulsen.blockbeats.core.filters.PlayerFileAccessFilter;
import de.mrjulsen.blockbeats.net.cts.GetAdditionalFileDataPacket;
import de.mrjulsen.blockbeats.net.cts.ManageFavoritesPacket;
import de.mrjulsen.blockbeats.registry.ModNetworkManager;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.FileInfoFilter;
import de.mrjulsen.dragnsounds.core.data.filter.FileMetadataFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLToggleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar.Orientation;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.DLAbstractRichTextInputField;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.Holder.MutableHolder;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public class FileSelectionPopup extends PopupWindow {

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
    private final Rectangle definition;

    private final DLPanel tabsCollection;
    
    private final Comparator<SoundFile> alphabeticalOrder = (a, b) -> a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
    private final PlayerFileAccessFilter defaultAccessFilter = new PlayerFileAccessFilter(Minecraft.getInstance().player);

    private static final int WIN_WIDTH = 400;
    private static final int WIN_HEIGHT = 250;

    private DLScrollBar scrollBar;
    private DLButton doneButton;
    private int currentTabX = 0;

    @SuppressWarnings("resource")
    public FileSelectionPopup(DLWindowManager manager, Consumer<Set<SoundFile>> onSelect) {
        super(manager, WIN_WIDTH, WIN_HEIGHT);
        
        definition = Rectangle.withSize(6, 38, WIN_WIDTH - 12 - 8, WIN_HEIGHT - 35 - 38);

        MutableHolder<FileBrowserContainer> container = new MutableHolder<>(null);

        tabsCollection = addComponent(new DLPanel(6, 20, WIN_WIDTH - 12 - 8, 18));
        FlowLayout tabsLayout = new FlowLayout();
        tabsLayout.flowDirection.set(Direction.HORIZONTAL);
        tabsCollection.layout.set(tabsLayout);
        
        // TABS
        addTab(tabAll, descriptionAll, Items.MUSIC_DISC_CAT, () -> {
            if (container.get() != null) {
                container.get().setFilters(List.of(defaultAccessFilter), alphabeticalOrder, true);
            }
        });
        addTab(tabMyCollection, descriptionMyCollection, Blocks.BOOKSHELF, () -> {
            container.get().setFilters(List.of(defaultAccessFilter, new FileInfoFilter(FileInfoFilter.KEY_OWNER_UUID, Minecraft.getInstance().player.getUUID().toString(), ECompareOperation.EQUALS)), alphabeticalOrder, true);
        }).checked.set(true);
        addTab(tabShared, descriptionShared, Items.DIAMOND, () -> {
            container.get().setFilters(List.of(defaultAccessFilter, new FileMetadataFilter(BlockBeats.META_SHARED, Minecraft.getInstance().player.getUUID().toString(), ECompareOperation.CONTAINS)), alphabeticalOrder, true); 
        });
        addTab(tabFavorites, descriptionFavorites, Items.NETHER_STAR, () -> {
            container.get().setFilters(List.of(defaultAccessFilter, FavoritesFilter.of(Minecraft.getInstance().player, ECompareOperation.EQUALS)), alphabeticalOrder, true); 
        });

        // Search bar
        int maxSearchBarWidth = Math.min(WIN_WIDTH - 18 - currentTabX, 100);

        // Browser
        container.set(addComponent(new FileBrowserContainer((int)definition.x(), (int)definition.y(), (int)definition.width(), (int)definition.height(), true, this::getScrollBar,
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
                    getWindowManager().createModal(mgr -> new DeleteFilePopup(mgr, widget.getAttachedSoundFile(), container::get, 200, 100));
                }, true));
            }
            
            if (canEdit) {                
                tasks.add(new TaskBuilder(ModGuiIcons.RENAME.getAsSprite(16, 16), textRename, (widget) -> {
                    getWindowManager().createModal(mgr -> new RenameSoundFilePopup(mgr, widget.getAttachedSoundFile(), container::get));
                }, true));
            }
            
            if (isOwner) {
                tasks.add(new TaskBuilder(ModGuiIcons.SHARE2.getAsSprite(16, 16), textShare, (widget) -> {

                    getWindowManager().createModal(mgr -> {
                        SharePopup win = new SharePopup(mgr, widget.getAttachedSoundFile());
                        win.addEventListener(DLGuiStandardEvents.CloseEvent.class, (s, e) -> {
                            container.get().refresh();
                            return false;
                        });
                        return win;
                    });
                }, true));
            }

            boolean isFav = container.get() != null && container.get().getFavoritePaths().contains(file.toString());
            tasks.add(new TaskBuilder(isFav ? ModGuiIcons.STAR_FILLED.getAsSprite(16, 16) : ModGuiIcons.STAR.getAsSprite(16, 16), isFav ? textRemFavorite : textFavorite, (widget) -> {
                
                ModNetworkManager.GET_ADDITIONAL_FILE_DATA.send(NetworkDirection.toServer(), GetAdditionalFileDataPacket.create((favs, usernamecache) -> {
                    final String path = widget.getAttachedSoundFile().toString();
                    final boolean remove = favs.contains(path);
                    ModNetworkManager.MANAGE_FAVORITES.send(NetworkDirection.toServer(), ManageFavoritesPacket.create(Set.of(path), remove, () -> {
                        if (remove) {
                            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, textFavoritesRemoved, TextUtils.text(widget.getAttachedSoundFile().getDisplayName())));
                        } else {                                
                        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, textFavoritesAdded, TextUtils.text(widget.getAttachedSoundFile().getDisplayName())));
                        }
                        container.get().refresh();
                    }));
                }));
            }, true));

            return tasks;
        })));
        scrollBar = addComponent(new DLScrollBar((int)definition.right(), (int)definition.y(), 8, (int)definition.height(), Orientation.VERTICAL));
        scrollBar.scrollerSize.set(-1);
        scrollBar.screenSize.set(container.get().height());
        scrollBar.scrollSteps.set(15);
        scrollBar.addEventListener(DLScrollBar.ValueChangedEvent.class, (s, e) -> {
            container.get().setScrollOffsetY((int)e.value());
            return false;
        });

        DLButton cancelBtn = addComponent(new DLButton(WIN_WIDTH - 6 - 80, WIN_HEIGHT - 10 - 20, 80, 20));
        cancelBtn.text.set(CommonComponents.GUI_CANCEL);
        cancelBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });
        
        doneButton = addComponent(new DLButton(WIN_WIDTH - 10 - 160, WIN_HEIGHT - 10 - 20, 80, 20));
        doneButton.text.set(textSelect);
        doneButton.enabled.set(false);
        doneButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            onSelect.accept(container.get() == null ? Set.of() : container.get().getSelectedFiles());
            getWindowManager().closeWindow(this);
            return false;
        });

        DLButton uploadBtn = addComponent(new DLButton(6, WIN_HEIGHT - 10 - 20, 80, 20));
        uploadBtn.text.set(textUpload);
        uploadBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            ClientApi.showFileDialog(false, (paths) -> {
                if (paths.isPresent()) {
                    getWindowManager().createModal(mgr -> new UploadSoundPopup(mgr, paths.get()[0], container::get));
                }
            });
            return false;
        });

        
        DLRichTextEditBox box = addComponent(new DLRichTextEditBox(WIN_WIDTH - 6 - maxSearchBarWidth + 1, 21, maxSearchBarWidth - 2, 16));
        box.placeholderText.set(TextUtils.TEXT_SEARCH);
        box.acceptAndCancelKeysEnabled.set(true);
        box.addEventListener(DLAbstractRichTextInputField.TextAcceptKeyPressedEvent.class, (s, e) -> {
            container.get().setSearchFilter(box.text.get().getPlainText(), true);
            GuiUtils.playButtonSound();
            return false;
        });

        container.get().setMultiselect(true);
        container.get().setOnSelectionChangedListener((cont) -> {
            doneButton.text.set(TextUtils.empty().append(textSelect).append(cont.getSelectedFiles().size() > 0 ? " (" + cont.getSelectedFiles().size() + ")" : ""));
            doneButton.enabled.set(cont.getSelectedFiles().size() > 0);
        });
    }

    public DLScrollBar getScrollBar() {
        return scrollBar;
    }

    private DLToggleButton addTab(MutableComponent text, MutableComponent description, ItemLike item, Runnable onClick) {
        int w = Minecraft.getInstance().font.width(text) + 30;
        DLToggleButton btn = new DLToggleButton(0, 0, w, 18);
        btn.radioButtonMode.set(true);
        btn.text.set(text);
        btn.icon.set(new DLSprite(new ItemStack(item), 16, false));
        btn.tooltip.set(new DLTooltip(List.of(description), 200));
        btn.iconAlignment.set(ETextAlignment.LEFT);
        btn.textAlignment.set(ETextAlignment.LEFT);
        btn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            onClick.run();
            return false;
        });
        tabsCollection.addComponent(btn);
        return btn;
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite(DefaultGuiTextures.SPRITE_NAME_WINDOW_ROUNDED).render(graphics, 0, 0, width(), height());
        GuiUtils.drawString(graphics, graphics.defaultFont(), 6, 6, title, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
    }
}
