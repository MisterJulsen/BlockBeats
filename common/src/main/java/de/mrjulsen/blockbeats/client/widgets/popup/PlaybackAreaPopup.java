package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Consumer;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ClientWrapper;
import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.blockbeats.client.widgets.MapAreaSelectionWidget;
import de.mrjulsen.blockbeats.core.data.EPlaybackAreaType;
import de.mrjulsen.blockbeats.core.data.playback.BoxPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.core.data.playback.EntityRidingPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.core.data.playback.IPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.core.data.playback.RadiusPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLIconButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLListBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLNumberSelector;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.IDragonLibWidget;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLAbstractImageButton.ButtonType;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLListBox.DLListBoxItemBuilder;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.render.DynamicGuiRenderer.AreaStyle;
import de.mrjulsen.mcdragonlib.client.util.DLWidgetsCollection;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.WidgetsCollection;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class PlaybackAreaPopup extends PopupWidget {

    private final MutableComponent title = Utils.trans("playback_area", "title");
    private final MutableComponent textRadius = Utils.trans("playback_area", "radius");
    private final MutableComponent textEntity = Utils.trans("playback_area", "entity");

    private final GuiAreaDefinition workingArea;
    private final GuiAreaDefinition mapArea;

    private static final int WIN_WIDTH = 350;
    private static final int WIN_HEIGHT = 250;

    private static final int MAX_RADIUS = 128;

    private EPlaybackAreaType type;
    private IPlaybackAreaBuilder areaSettings; 

    private final Collection<DLTooltip> tooltips = new ArrayList<>();
    private final WidgetsCollection tabsCollection = new WidgetsCollection();
    private final DLWidgetsCollection radiusCollection = new DLWidgetsCollection();
    private final DLWidgetsCollection boxCollection = new DLWidgetsCollection();
    private final DLWidgetsCollection entityCollection = new DLWidgetsCollection();

    private DLNumberSelector selectorX1;
    private DLNumberSelector selectorY1;
    private DLNumberSelector selectorZ1;
    private DLNumberSelector selectorX2;
    private DLNumberSelector selectorY2;
    private DLNumberSelector selectorZ2;
    private MapAreaSelectionWidget map;
    private DLNumberSelector radiusBox;
    private DLListBox<ResourceLocation> entityListBox;
    private DLEditBox entitySearchBox;
    private DLButton doneButton;


    private int selectedEntitiesCount = 0;
    private boolean initialized = false;
    private int guiLeft, guiTop;
    private int currentTabX = 0;

    public <I extends IPlaybackAreaBuilder> PlaybackAreaPopup(DLPopupScreen parent, BlockPos center, int layer, int width, int height, I initialValue, Consumer<IPlaybackAreaBuilder> onAccept, Consumer<PopupWidget> close) {
        super(parent, layer, width, height, close);
        guiLeft = width / 2 - WIN_WIDTH / 2;
        guiTop = height / 2 - WIN_HEIGHT / 2;
        workingArea = new GuiAreaDefinition(guiLeft + 6, guiTop + 38, WIN_WIDTH - 12, WIN_HEIGHT - 35 - 38);
        mapArea = new GuiAreaDefinition(guiLeft + 6, guiTop + 38, WIN_WIDTH - 12, WIN_HEIGHT - 35 - 38 - 16);

        final EPlaybackAreaType initialType = EPlaybackAreaType.getByType(initialValue.getClass());
        for (EPlaybackAreaType t : EPlaybackAreaType.values()) {
            final EPlaybackAreaType type = t;
            DLIconButton tab = addTab(TextUtils.translate(t.getValueTranslationKey(BlockBeats.MOD_ID)), t.getIcon(), (btn) -> {
                setType(t, type == initialType ? initialValue : null);
            });
            tooltips.add(DLTooltip.of(TextUtils.translate(t.getValueInfoTranslationKey(BlockBeats.MOD_ID))).assignedTo(tab).withMaxWidth(width / 4));

            if (initialType == t) {
                tab.select();
            }
        }

        addRenderableWidget(new DLButton(guiLeft + WIN_WIDTH - 6 - 80, guiTop + WIN_HEIGHT - 10 - 20, 80, 20, CommonComponents.GUI_CANCEL, b -> close())).setRenderStyle(AreaStyle.DRAGONLIB);
        doneButton = addRenderableWidget(new DLButton(guiLeft + WIN_WIDTH - 10 - 160, guiTop + WIN_HEIGHT - 10 - 20, 80, 20, CommonComponents.GUI_DONE, b -> {
            onAccept.accept(areaSettings);
            close();
        }));
        doneButton.setRenderStyle(AreaStyle.DRAGONLIB);
        doneButton.setBackColor(DragonLib.PRIMARY_BUTTON_COLOR);

        // Box Settings
        selectorX1 = addRenderableWidget(new DLNumberSelector(guiLeft + 6, mapArea.getBottom(), 40, 16, 0, false, (btn, val) -> updateSelectedBoxArea()));
        selectorX1.setNumberBounds(-MAX_RADIUS, MAX_RADIUS);
        selectorY1 = addRenderableWidget(new DLNumberSelector(guiLeft + 6 + 40, mapArea.getBottom(), 40, 16, 0, false, (btn, val) -> updateSelectedBoxArea()));
        selectorY1.setNumberBounds(-MAX_RADIUS, MAX_RADIUS);
        selectorZ1 = addRenderableWidget(new DLNumberSelector(guiLeft + 6 + 80, mapArea.getBottom(), 40, 16, 0, false, (btn, val) -> updateSelectedBoxArea()));
        selectorZ1.setNumberBounds(-MAX_RADIUS, MAX_RADIUS);

        selectorX2 = addRenderableWidget(new DLNumberSelector(guiLeft + WIN_WIDTH - 6 - 120, mapArea.getBottom(), 40, 16, 0, false, (btn, val) -> updateSelectedBoxArea()));
        selectorX2.setNumberBounds(-MAX_RADIUS, MAX_RADIUS);
        selectorY2 = addRenderableWidget(new DLNumberSelector(guiLeft + WIN_WIDTH - 6 - 80, mapArea.getBottom(), 40, 16, 0, false, (btn, val) -> updateSelectedBoxArea()));
        selectorY2.setNumberBounds(-MAX_RADIUS, MAX_RADIUS);
        selectorZ2 = addRenderableWidget(new DLNumberSelector(guiLeft + WIN_WIDTH - 6 - 40, mapArea.getBottom(), 40, 16, 0, false, (btn, val) -> updateSelectedBoxArea()));
        selectorZ2.setNumberBounds(-MAX_RADIUS, MAX_RADIUS);

        map = addRenderableWidget(new MapAreaSelectionWidget(parent, mapArea.getX() + 1, mapArea.getY() + 1, mapArea.getWidth() - 2, mapArea.getHeight() - 2, center, MAX_RADIUS * 2, MAX_RADIUS * 2,
        (pos1, pos2) -> {
            selectorX1.setValue(pos1.x, true);
            selectorZ1.setValue(pos1.y, true);
            selectorX2.setValue(pos2.x, true);
            selectorZ2.setValue(pos2.y, true);
        }));

        boxCollection.add(selectorX1);
        boxCollection.add(selectorY1);
        boxCollection.add(selectorZ1);
        boxCollection.add(selectorX2);
        boxCollection.add(selectorY2);
        boxCollection.add(selectorZ2);
        boxCollection.add(map);

        // Radius Settings
        radiusBox = addRenderableWidget(new DLNumberSelector(workingArea.getX() + font.width(textRadius) + 15, workingArea.getY() + 10, 40, 16, 0, false, (selector, value) -> updateRadius()));
        radiusBox.setNumberBounds(0, MAX_RADIUS);
        radiusCollection.add(radiusBox);
        entityCollection.add(radiusBox);

        // Entity Settings
        int x = workingArea.getX() + font.width(textEntity) + 15;
        int y = workingArea.getY() + 15 + 18 + radiusBox.getHeight();
        entityListBox = new DLListBox<>(x, y, Math.min(workingArea.getRight() - x - 10, 200), workingArea.getBottom() - y - 16, true);
        
        Registry.ENTITY_TYPE.keySet().forEach(a -> entityListBox.add(new DLListBoxItemBuilder<ResourceLocation>(Registry.ENTITY_TYPE.get(a).getDescription(), Sprite.empty(), a, (b) -> {})));
        entityListBox.withOnSelectionChangedEvent((box) -> {
            selectedEntitiesCount = box.getSelectedCount();
            updateEntities();
        });
        entityCollection.add(addRenderableWidget(entityListBox));
        
        entitySearchBox = addRenderableWidget(new DLEditBox(font, x + 1, y - 17, entityListBox.getWidth() - 2, 16, TextUtils.empty())
            .withHint(DragonLib.TEXT_SEARCH)
        );
        entitySearchBox.setMaxLength(BlockBeats.MAX_FILENAME_LENGTH);
        entitySearchBox.setResponder((value) -> {
            entityListBox.getScrollBar().scrollTo(0);
            ClientWrapper.filterListBox(entityListBox, (item) -> value.isBlank() ? true : item.getMessage().getString().toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT)));           
        });
        entitySearchBox.setValue("");
        entitySearchBox.setBordered(true);
        entityCollection.add(entitySearchBox);

        setType(initialType, initialValue);
        initialized = true;
    }

    @Override
    public void close() {
        map.close();
        super.close();
    }

    public void setType(EPlaybackAreaType type, IPlaybackAreaBuilder initial) {
        initialized = false;
        this.type = type;

        radiusCollection.setVisible(type == EPlaybackAreaType.RADIUS);
        boxCollection.setVisible(type == EPlaybackAreaType.BOX);
        entityCollection.setVisible(type == EPlaybackAreaType.ENTITY);
        radiusBox.set_visible(type == EPlaybackAreaType.RADIUS || type == EPlaybackAreaType.ENTITY);

        switch (type) {
            case RADIUS -> {
                boolean createNew = (initial == null || !(initial instanceof RadiusPlaybackAreaBuilder));
                RadiusPlaybackAreaBuilder settings = createNew ? new RadiusPlaybackAreaBuilder() : (RadiusPlaybackAreaBuilder)initial;
                areaSettings = settings;
                radiusBox.setValue(settings.getRadius(), true);
            }
            case BOX -> {
                boolean createNew = (initial == null || !(initial instanceof BoxPlaybackAreaBuilder));
                BoxPlaybackAreaBuilder settings = createNew ? new BoxPlaybackAreaBuilder() : (BoxPlaybackAreaBuilder)initial;
                areaSettings = settings;
                selectorX1.setValue(settings.getX1(), true);
                selectorY1.setValue(settings.getY1(), true);
                selectorZ1.setValue(settings.getZ1(), true);
                selectorX2.setValue(settings.getX2(), true);
                selectorY2.setValue(settings.getY2(), true);
                selectorZ2.setValue(settings.getZ2(), true);
                map.setArea((int)settings.getX1(), (int)settings.getZ1(), (int)settings.getX2(), (int)settings.getZ2());
            }
            case ENTITY -> {
                boolean createNew = (initial == null || !(initial instanceof EntityRidingPlaybackAreaBuilder));
                EntityRidingPlaybackAreaBuilder settings = createNew ? new EntityRidingPlaybackAreaBuilder() : (EntityRidingPlaybackAreaBuilder)initial;
                areaSettings = settings;
                radiusBox.setValue(settings.getRadius(), true);
                entitySearchBox.setValue("");
                entityListBox.getItems().forEach(x -> x.setSelected(settings.contains(x.getData())));
                selectedEntitiesCount = entityListBox.getSelectedCount();
            }
        }
        initialized = true;
    }

    private void updateSelectedBoxArea() {
        if (!initialized || type != EPlaybackAreaType.BOX) {
            return;
        }
        BoxPlaybackAreaBuilder builder = (BoxPlaybackAreaBuilder)areaSettings;
        builder.setX1(selectorX1.getAsInt());
        builder.setY1(selectorY1.getAsInt());
        builder.setZ1(selectorZ1.getAsInt());
        builder.setX2(selectorX2.getAsInt());
        builder.setY2(selectorY2.getAsInt());
        builder.setZ2(selectorZ2.getAsInt());
        map.setArea(selectorX1.getAsInt(), selectorZ1.getAsInt(), selectorX2.getAsInt(), selectorZ2.getAsInt());
    }

    private void updateRadius() {
        if (!initialized && type != EPlaybackAreaType.RADIUS && type != EPlaybackAreaType.ENTITY) {
            return;
        }
        
        switch (type) {
            case RADIUS -> {
                RadiusPlaybackAreaBuilder builder = (RadiusPlaybackAreaBuilder)areaSettings;
                builder.setRadius(radiusBox.getAsInt());
            }
            case ENTITY -> {
                EntityRidingPlaybackAreaBuilder builder = (EntityRidingPlaybackAreaBuilder)areaSettings;
                builder.setRadius(radiusBox.getAsInt());
            }
            default -> {}
        }
    }

    private void updateEntities() {
        if (!initialized && type != EPlaybackAreaType.ENTITY) {
            return;
        }
        EntityRidingPlaybackAreaBuilder builder = (EntityRidingPlaybackAreaBuilder)areaSettings;
        builder.setEntityIds(entityListBox.getSelectedItems().stream().map(x -> x.getData()).toList());
        selectedEntitiesCount = entityListBox.getSelectedCount();
    }

    private DLIconButton addTab(MutableComponent text, Sprite icon, Consumer<DLIconButton> onClick) {
        int w = font.width(text) + 30;
        DLIconButton btn = addRenderableWidget(new DLIconButton(
            ButtonType.RADIO_BUTTON,
            AreaStyle.DRAGONLIB,
            icon,
            tabsCollection,
            guiLeft + 6 + currentTabX,
            guiTop + 20,
            w,
            18,
            text,
            onClick
        ));
        btn.withAlignment(EAlignment.LEFT);
        currentTabX += w;
        return btn;
    }
    
    @Override
    public void renderMainPopupLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainPopupLayer(graphics, mouseX, mouseY, partialTicks);
        DynamicGuiRenderer.renderWindow(graphics, guiLeft, guiTop, WIN_WIDTH, WIN_HEIGHT);
        GuiUtils.drawString(graphics, font, guiLeft + 6, guiTop + 6, title, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);

        switch (type) {
            case RADIUS -> {
                GuiUtils.drawString(graphics, font, workingArea.getX() + 10, radiusBox.getY() + radiusBox.getHeight() / 2 - font.lineHeight / 2, textRadius, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
            }
            case BOX -> {
                DynamicGuiRenderer.renderContainerBackground(graphics, mapArea);
            }
            case ENTITY -> {
                GuiUtils.drawString(graphics, font, workingArea.getX() + 10, radiusBox.getY() + radiusBox.getHeight() / 2 - font.lineHeight / 2, textRadius, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
                GuiUtils.drawString(graphics, font, workingArea.getX() + 10, entitySearchBox.y() + entitySearchBox.getHeight() / 2 - font.lineHeight / 2, textEntity, DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
                GuiUtils.drawString(graphics, font, entityListBox.getX(), entityListBox.getY() + entityListBox.getHeight() + 2, Utils.trans("playback_area", "entities_selected", selectedEntitiesCount), DragonLib.NATIVE_UI_FONT_COLOR, EAlignment.LEFT, false);
            }
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
