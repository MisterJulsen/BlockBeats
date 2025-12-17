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

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ClientWrapper;
import de.mrjulsen.blockbeats.core.filters.CaseInsensitiveMetadataFilter;
import de.mrjulsen.blockbeats.net.cts.GetAdditionalFileDataPacket;
import de.mrjulsen.blockbeats.registry.ModNetworkManager;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.core.data.ECompareOperation;
import de.mrjulsen.dragnsounds.core.data.filter.FileInfoFilter;
import de.mrjulsen.dragnsounds.core.data.filter.IFilter;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLGuiComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.network.NetworkDirection;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.network.chat.MutableComponent;

public class FileBrowserContainer extends DLGuiComponent {

    private final Supplier<DLScrollBar> scrollBar;
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
    private final boolean renderBg;

    public FileBrowserContainer(int x, int y, int width, int height, boolean renderBg, Supplier<DLScrollBar> scrollBar, Collection<IFilter<SoundFile>> filters, Comparator<SoundFile> sortFunc, Function<SoundFile, Collection<TaskBuilder>> fileTasks) {
        super(x, y, width, height);
        this.scrollBar = scrollBar;
        this.fileTasks = fileTasks;
        this.renderBg = renderBg;
        setFilters(filters, sortFunc, true);

        FlowLayout layout = new FlowLayout();
        layout.fillCrossAxis.set(true);
        layout.flowDirection.set(Direction.VERTICAL);
        layout.wrap.set(false);
        layout.padding.set(new Padding(1));
        this.layout.set(layout);

        refresh();
    }

    public void refresh() {
        clearComponents();
        Collection<IFilter<SoundFile>> filters = new ArrayList<>(this.filters);
        if (searchTerm != null) {
            filters.add(searchTerm);
        }
        filters.add(new FileInfoFilter(FileInfoFilter.KEY_LOCATION, ClientWrapper.location(BlockBeats.SOUND_PLAYER_CATEGORY).toString(), ECompareOperation.STARTS_WITH));
        
        ModNetworkManager.GET_ADDITIONAL_FILE_DATA.send(NetworkDirection.toServer(), GetAdditionalFileDataPacket.create((favs, usernamecache) -> {
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
        clearComponents();
        this.files.clear();
        this.files.addAll(files);
        for (int i = 0; i < files.size(); i++) {
            final int k = i;
            addComponent(new SoundFileWidget(this, 0, 0, width(), files.get(k), (btn) -> {
                if (btn.isPicked()) {
                    selectedFiles.add(btn.getAttachedSoundFile());
                    if (!canMultiselect()) {
                        getComponentsOfType(SoundFileWidget.class, true).forEach(x -> x.select(false));
                    }
                } else {
                    selectedFiles.remove(btn.getAttachedSoundFile());
                }

                if (onSelectionChanged != null) {
                    onSelectionChanged.accept(this);
                }
            }, fileTasks.apply(files.get(k))));
        }
        if (scrollBar.get() != null) {
            scrollBar.get().max.set(maxRequiredHeight());
            scrollBar.get().scrollTo(0);
        }
    }
    
    public int maxRequiredHeight() {
        return (componentsCount()) * SoundFileWidget.HEIGHT;
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
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        if (renderBg) {
            DefaultGuiTextures.DRAGONLIB_UI.getSprite("container").render(graphics, 0, 0, width(), height());
        }
        if (getComponents().isEmpty()) {
            GuiUtils.drawString(graphics, graphics.defaultFont(), width() / 2, 50, Utils.trans("file_container", "empty"), DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR, ETextAlignment.CENTER, false);
        }
    }

    public static record TaskBuilder(DLSprite sprite, MutableComponent text, Consumer<SoundFileWidget> action, boolean addButton) {}
    
}
