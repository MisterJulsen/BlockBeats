package de.mrjulsen.blockbeats.client.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ClientWrapper;
import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.blockbeats.core.filters.CaseInsensitiveMetadataFilter;
import de.mrjulsen.blockbeats.net.cts.GetAdditionalFileDataPacket;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.FileInfoFilter;
import de.mrjulsen.dragnsounds.core.data.filter.IFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLAbstractScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.ScrollableWidgetContainer;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.MutableComponent;

public class FileBrowserContainer extends ScrollableWidgetContainer {

    private final DLPopupScreen parent;
    private final Supplier<DLAbstractScrollBar<?>> scrollBar;
    private final Function<SoundFile, Collection<TaskBuilder>> fileTasks;

    private final Set<SoundFile> files = new HashSet<>();
    private final Set<SoundFile> selectedFiles = new LinkedHashSet<>();

    private Collection<IFilter<SoundFile>> filters;
    private Comparator<SoundFile> sortFunc;
    private CaseInsensitiveMetadataFilter searchTerm;
    private Set<String> favPaths;
    private Map<UUID, String> localUsernamecache;

    private boolean multiselect;

    private DLContextMenu menu;

    // Events
    private Consumer<FileBrowserContainer> onSelectionChanged;

    public FileBrowserContainer(DLPopupScreen parent, int x, int y, int width, int height, Supplier<DLAbstractScrollBar<?>> scrollBar, Collection<IFilter<SoundFile>> filters, Comparator<SoundFile> sortFunc, Function<SoundFile, Collection<TaskBuilder>> fileTasks) {
        super(x, y, width, height);
        this.parent = parent;
        this.scrollBar = scrollBar;
        this.fileTasks = fileTasks;
        setFilters(filters, sortFunc, true);
    }

    public DLContextMenu getMenu() {
        return menu;
    }

    @Override
    public void setMenu(DLContextMenu menu) {
        this.menu = menu;
    }

    @Override
    public int getContextMenuOpenButton() {
        return GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    public void refresh() {
        clearWidgets();
        Collection<IFilter<SoundFile>> filters = new ArrayList<>(this.filters);
        if (searchTerm != null) {
            filters.add(searchTerm);
        }
        filters.add(new FileInfoFilter(FileInfoFilter.KEY_LOCATION, ClientWrapper.location(BlockBeats.SOUND_PLAYER_CATEGORY).toString(), ECompareOperation.STARTS_WITH));
        BlockBeats.net().sendToServer(GetAdditionalFileDataPacket.create((favs, usernamecache) -> {
            this.localUsernamecache = usernamecache;
            this.favPaths = favs;
            ClientApi.getAllSoundFiles(filters, (files) -> {
                loadFiles(Arrays.stream(files).sorted(sortFunc).toList());
            });
        }));
    }

    public Set<String> getFavoritePaths() {
        return favPaths;
    }

    public void setOnSelectionChangedListener(Consumer<FileBrowserContainer> callback) {
        this.onSelectionChanged = callback;
    }

    public void setFilters(Collection<IFilter<SoundFile>> filters, Comparator<SoundFile> sortFunc, boolean refresh) {
        this.filters = filters;
        this.sortFunc = sortFunc;
        if (refresh) {
            refresh();
        }
    }


    public void setSearchFilter(String searchTerm, boolean refresh) {
        if (searchTerm == null || searchTerm.isBlank()) {
            this.searchTerm = null;
        } else {
            this.searchTerm = new CaseInsensitiveMetadataFilter(SoundFile.META_DISPLAY_NAME, searchTerm, ECompareOperation.CONTAINS);
        }

        if (refresh) {
            refresh();
        }
    }

    public void loadFiles(List<SoundFile> files) {
        clearWidgets();
        this.files.clear();
        this.files.addAll(files);
        for (int i = 0; i < files.size(); i++) {
            addRenderableWidget(new SoundFileWidget(parent, this, x, y + i * SoundFileWidget.HEIGHT, width, files.get(i), (btn) -> {
                if (btn.isSelected()) {
                    selectedFiles.add(btn.getAttachedSoundFile());
                    if (!canMultiselect()) {
                        children().stream().filter(x -> x != btn && x instanceof SoundFileWidget).map(x -> (SoundFileWidget)x).forEach(x -> x.select(false));
                    }
                } else {
                    selectedFiles.remove(btn.getAttachedSoundFile());
                }

                if (onSelectionChanged != null) {
                    onSelectionChanged.accept(this);
                }
            }, fileTasks.apply(files.get(i))));
        }
        if (scrollBar.get() != null) {
            scrollBar.get().updateMaxScroll(maxRequiredHeight());
            scrollBar.get().scrollTo(0);
        }
    }
    
    public int maxRequiredHeight() {
        return (children().size()) * SoundFileWidget.HEIGHT;
    }    

    public boolean canMultiselect() {
        return multiselect;
    }

    public void setMultiselect(boolean b) {
        multiselect = b;
    }    

    public Set<SoundFile> getSelectedFiles() {
        return selectedFiles;
    }

    public Set<SoundFile> getFiles() {
        return files;
    }

    public String getUsername(UUID playerId) {
        return localUsernamecache.containsKey(playerId) ? localUsernamecache.get(playerId) : playerId.toString();
    }

    @Override
    public void renderMainLayerScrolled(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainLayerScrolled(graphics, mouseX, mouseY, partialTicks);
        if (children.isEmpty()) {
            GuiUtils.drawString(graphics, font, x + width / 2, y + 50, Utils.trans("file_container", "empty"), DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED, EAlignment.CENTER, false);
        }
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
    
    @Override
    public boolean consumeScrolling(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }

    public static record TaskBuilder(Sprite sprite, MutableComponent text, Consumer<SoundFileWidget> action, boolean addButton) {}
    
}
