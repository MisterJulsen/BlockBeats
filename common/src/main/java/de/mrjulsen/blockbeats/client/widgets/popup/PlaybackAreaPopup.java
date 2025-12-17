package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.mrjulsen.blockbeats.client.widgets.MapAreaSelectionWidget;
import de.mrjulsen.blockbeats.client.widgets.SelectionListBox;
import de.mrjulsen.blockbeats.core.data.EPlaybackAreaType;
import de.mrjulsen.blockbeats.core.data.playback.BoxPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.core.data.playback.EntityRidingPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.core.data.playback.IPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.core.data.playback.RadiusPlaybackAreaBuilder;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLAbstractCollectionComponent;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLNumberPicker;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLRichTextEditBox;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLToggleButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.DLAbstractRichTextInputField;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.RenderLayer;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class PlaybackAreaPopup extends PopupWindow {

    private final MutableComponent title = Utils.trans("playback_area", "title");
    private final MutableComponent textRadius = Utils.trans("playback_area", "radius");
    private final MutableComponent textEntity = Utils.trans("playback_area", "entity");

    private final Rectangle workingArea;
    private final Rectangle mapArea;

    private static final int WIN_WIDTH = 350;
    private static final int WIN_HEIGHT = 250;

    private static final int MAX_RADIUS = 128;

    private EPlaybackAreaType type;
    private IPlaybackAreaBuilder areaSettings; 

    private final DLPanel tabsCollection;
    private final DLPanel radiusCollection;
    private final DLPanel boxCollection;
    private final DLPanel entityCollection;

    private DLNumberPicker selectorX1;
    private DLNumberPicker selectorY1;
    private DLNumberPicker selectorZ1;
    private DLNumberPicker selectorX2;
    private DLNumberPicker selectorY2;
    private DLNumberPicker selectorZ2;
    private MapAreaSelectionWidget map;
    private DLNumberPicker radiusBox;
    private DLNumberPicker entityRadiusBox;
    private SelectionListBox<ResourceLocation> entityListBox;
    private DLRichTextEditBox entitySearchBox;
    private DLButton doneButton;


    private int selectedEntitiesCount = 0;
    private boolean initialized = false;
    private int currentTabX = 0;

    public <I extends IPlaybackAreaBuilder> PlaybackAreaPopup(DLWindowManager manager, BlockPos center, I initialValue, Consumer<IPlaybackAreaBuilder> onAccept) {
        super(manager, WIN_WIDTH, WIN_HEIGHT);
        
        workingArea = Rectangle.withSize(6, 38, WIN_WIDTH - 12, WIN_HEIGHT - 35 - 38);
        mapArea = Rectangle.withSize(6, 38, WIN_WIDTH - 12, WIN_HEIGHT - 35 - 38 - 16);

        radiusCollection = addComponent(new DLPanel((int)workingArea.x(), (int)workingArea.y(), (int)workingArea.width(), (int)workingArea.height()));
        radiusCollection.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                GuiUtils.drawString(e.graphics(), e.graphics().defaultFont(), (int)workingArea.x() + 5, radiusBox.y() + radiusBox.height() / 2 - e.graphics().defaultFont().lineHeight / 2, textRadius, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
            }
            return false;
        });

        boxCollection = addComponent(new DLPanel((int)workingArea.x(), (int)workingArea.y(), (int)workingArea.width(), (int)workingArea.height()));
        boxCollection.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
            }
            return false;
        });

        entityCollection = addComponent(new DLPanel((int)workingArea.x(), (int)workingArea.y(), (int)workingArea.width(), (int)workingArea.height()));
        entityCollection.addEventListener(DLGuiStandardEvents.RenderEvent.class, (s, e) -> {
            if (e.layer() == RenderLayer.MAIN) {
                GuiUtils.drawString(e.graphics(), e.graphics().defaultFont(), (int)workingArea.x() + 5, radiusBox.y() + radiusBox.height() / 2 - e.graphics().defaultFont().lineHeight / 2, textRadius, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
                GuiUtils.drawString(e.graphics(), e.graphics().defaultFont(), (int)workingArea.x() + 5, entitySearchBox.y() + entitySearchBox.height() / 2 - e.graphics().defaultFont().lineHeight / 2, textEntity, DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);
                GuiUtils.drawString(e.graphics(), e.graphics().defaultFont(), (int)entityListBox.x(), (int)entityListBox.y() + entityListBox.height() + 2, Utils.trans("playback_area", "entities_selected", selectedEntitiesCount), DragonLib.VANILLA_UI_FONT_COLOR, ETextAlignment.LEFT, false);

            }
            return false;
        });
        

        tabsCollection = addComponent(new DLPanel(6, 20, WIN_WIDTH - 12 - 8, 18));
        FlowLayout tabsLayout = new FlowLayout();
        tabsLayout.flowDirection.set(Direction.HORIZONTAL);
        tabsCollection.layout.set(tabsLayout);

        final EPlaybackAreaType initialType = EPlaybackAreaType.getByType(initialValue.getClass());
        for (EPlaybackAreaType t : EPlaybackAreaType.values()) {
            final EPlaybackAreaType type = t;
            DLToggleButton tab = addTab(t.getValueTranslation(), t.getIcon(), () -> {
                setType(t, type == initialType ? initialValue : null);
            });
            tab.tooltip.set(new DLTooltip(List.of(t.getValueDescriptionTranslation()), 200));

            if (initialType == t) {
                tab.checked.set(true);
            }
        }

        DLButton cancelBtn = addComponent(new DLButton(WIN_WIDTH - 6 - 80, WIN_HEIGHT - 10 - 20, 80, 20));
        cancelBtn.text.set(CommonComponents.GUI_CANCEL);
        cancelBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            getWindowManager().closeWindow(this);
            return false;
        });
        
        doneButton = addComponent(new DLButton(WIN_WIDTH- 10 - 160, WIN_HEIGHT - 10 - 20, 80, 20));
        doneButton.text.set(CommonComponents.GUI_DONE);
        doneButton.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            onAccept.accept(areaSettings);
            getWindowManager().closeWindow(this);
            return false;
        });

        // Box Settings
        selectorX1 = boxCollection.addComponent(new DLNumberPicker(0, boxCollection.height() - 16, 40, 16));
        selectorX1.min.set((double)-MAX_RADIUS);
        selectorX1.max.set((double)MAX_RADIUS);
        selectorX1.showButtons.set(false);
        selectorX1.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            updateSelectedBoxArea();
            return false;
        });

        selectorY1 = boxCollection.addComponent(new DLNumberPicker(40, boxCollection.height() - 16, 40, 16));
        selectorY1.min.set((double)-MAX_RADIUS);
        selectorY1.max.set((double)MAX_RADIUS);
        selectorY1.showButtons.set(false);
        selectorY1.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            updateSelectedBoxArea();
            return false;
        });

        selectorZ1 = boxCollection.addComponent(new DLNumberPicker(80, boxCollection.height() - 16, 40, 16));
        selectorZ1.min.set((double)-MAX_RADIUS);
        selectorZ1.max.set((double)MAX_RADIUS);
        selectorZ1.showButtons.set(false);
        selectorZ1.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            updateSelectedBoxArea();
            return false;
        });
        

        selectorX2 = boxCollection.addComponent(new DLNumberPicker(boxCollection.width() - 120, boxCollection.height() - 16, 40, 16));
        selectorX2.min.set((double)-MAX_RADIUS);
        selectorX2.max.set((double)MAX_RADIUS);
        selectorX2.showButtons.set(false);
        selectorX2.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            updateSelectedBoxArea();
            return false;
        });        

        selectorY2 = boxCollection.addComponent(new DLNumberPicker(boxCollection.width() - 80, boxCollection.height() - 16, 40, 16));
        selectorY2.min.set((double)-MAX_RADIUS);
        selectorY2.max.set((double)MAX_RADIUS);
        selectorY2.showButtons.set(false);
        selectorY2.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            updateSelectedBoxArea();
            return false;
        });        

        selectorZ2 = boxCollection.addComponent(new DLNumberPicker(boxCollection.width() - 40, boxCollection.height() - 16, 40, 16));
        selectorZ2.min.set((double)-MAX_RADIUS);
        selectorZ2.max.set((double)MAX_RADIUS);
        selectorZ2.showButtons.set(false);
        selectorZ2.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            updateSelectedBoxArea();
            return false;
        });

        map = boxCollection.addComponent(new MapAreaSelectionWidget(0, 0, boxCollection.width(), (int)mapArea.height(), center, MAX_RADIUS * 2, MAX_RADIUS * 2,
        (pos1, pos2) -> {
            selectorX1.value.set((double)pos1.x);
            selectorZ1.value.set((double)pos1.y);
            selectorX2.value.set((double)pos2.x);
            selectorZ2.value.set((double)pos2.y);
        }));
        

        // Radius Settings
        radiusBox = radiusCollection.addComponent(new DLNumberPicker(Minecraft.getInstance().font.width(textRadius) + 15, 10, 40, 16));
        radiusBox.max.set((double)MAX_RADIUS);
        radiusBox.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            updateRadius();
            return false;
        });
        
        entityRadiusBox = entityCollection.addComponent(new DLNumberPicker(Minecraft.getInstance().font.width(textRadius) + 15, 10, 40, 16));
        entityRadiusBox.max.set((double)MAX_RADIUS);
        entityRadiusBox.addEventListener(DLNumberPicker.ValueChangedEvent.class, (s, e) -> {
            updateRadius();
            return false;
        });

        // Entity Settings
        int x = (int)workingArea.x() + Minecraft.getInstance().font.width(textEntity) + 15;
        int y = 15 + 18 + entityRadiusBox.height();
        entityListBox = entityCollection.addComponent(new SelectionListBox<>(x, y, (int)Math.min(workingArea.right() - x - 10, 200), (int)entityCollection.height() - y - 16));
        entityListBox.multiselect.set(true);
        entityListBox.items.addAll(BuiltInRegistries.ENTITY_TYPE.stream().map(a -> EntityType.getKey(a)).toList());
        entityListBox.textFormat.set(a -> BuiltInRegistries.ENTITY_TYPE.get(a).getDescription());
        entityListBox.addEventListener(SelectionListBox.SelectEvent.class, (s, e) -> {
            selectedEntitiesCount = entityListBox.selectedItems.get().size();
            updateEntities();
            return false;
        });
        entityListBox.addEventListener(DLAbstractCollectionComponent.FilterChangedEvent.class, (s, e) -> {
            return false;
        });
                
        entitySearchBox = entityCollection.addComponent(new DLRichTextEditBox(x, y - 16, entityListBox.width(), 16));
        entitySearchBox.placeholderText.set(TextUtils.TEXT_SEARCH);
        entitySearchBox.acceptAndCancelKeysEnabled.set(true);
        entitySearchBox.addEventListener(DLAbstractRichTextInputField.TextAcceptKeyPressedEvent.class, (s, e) -> {
            entityListBox.filter.set(res -> res.toString().toLowerCase().contains(entitySearchBox.text.get().getPlainText().toLowerCase()));
            return false;
        });

        setType(initialType, initialValue);
        initialized = true;
    }

    public void setType(EPlaybackAreaType type, IPlaybackAreaBuilder initial) {
        initialized = false;
        this.type = type;

        radiusCollection.visible.set(type == EPlaybackAreaType.RADIUS);
        boxCollection.visible.set(type == EPlaybackAreaType.BOX);
        entityCollection.visible.set(type == EPlaybackAreaType.ENTITY);

        switch (type) {
            case RADIUS -> {
                boolean createNew = (initial == null || !(initial instanceof RadiusPlaybackAreaBuilder));
                RadiusPlaybackAreaBuilder settings = createNew ? new RadiusPlaybackAreaBuilder() : (RadiusPlaybackAreaBuilder)initial;
                areaSettings = settings;
                radiusBox.value.set((double)settings.getRadius());
            }
            case BOX -> {
                boolean createNew = (initial == null || !(initial instanceof BoxPlaybackAreaBuilder));
                BoxPlaybackAreaBuilder settings = createNew ? new BoxPlaybackAreaBuilder() : (BoxPlaybackAreaBuilder)initial;
                areaSettings = settings;
                selectorX1.value.set((double)settings.getX1());
                selectorY1.value.set((double)settings.getY1());
                selectorZ1.value.set((double)settings.getZ1());
                selectorX2.value.set((double)settings.getX2());
                selectorY2.value.set((double)settings.getY2());
                selectorZ2.value.set((double)settings.getZ2());
                map.setArea((int)settings.getX1(), (int)settings.getZ1(), (int)settings.getX2(), (int)settings.getZ2());
            }
            case ENTITY -> {
                boolean createNew = (initial == null || !(initial instanceof EntityRidingPlaybackAreaBuilder));
                EntityRidingPlaybackAreaBuilder settings = createNew ? new EntityRidingPlaybackAreaBuilder() : (EntityRidingPlaybackAreaBuilder)initial;
                areaSettings = settings;
                entityRadiusBox.value.set((double)settings.getRadius());
                entityListBox.selectedItems.set(new ArrayList<>(settings.getEntityIds()));
            }
        }
        initialized = true;
    }

    private void updateSelectedBoxArea() {
        if (!initialized || type != EPlaybackAreaType.BOX) {
            return;
        }
        BoxPlaybackAreaBuilder builder = (BoxPlaybackAreaBuilder)areaSettings;
        builder.setX1(selectorX1.value.get().intValue());
        builder.setY1(selectorY1.value.get().intValue());
        builder.setZ1(selectorZ1.value.get().intValue());
        builder.setX2(selectorX2.value.get().intValue());
        builder.setY2(selectorY2.value.get().intValue());
        builder.setZ2(selectorZ2.value.get().intValue());
        map.setArea(selectorX1.value.get().intValue(), selectorZ1.value.get().intValue(), selectorX2.value.get().intValue(), selectorZ2.value.get().intValue());
    }

    private void updateRadius() {
        if (!initialized && type != EPlaybackAreaType.RADIUS && type != EPlaybackAreaType.ENTITY) {
            return;
        }
        
        switch (type) {
            case RADIUS -> {
                RadiusPlaybackAreaBuilder builder = (RadiusPlaybackAreaBuilder)areaSettings;
                builder.setRadius(radiusBox.value.get().intValue());
            }
            case ENTITY -> {
                EntityRidingPlaybackAreaBuilder builder = (EntityRidingPlaybackAreaBuilder)areaSettings;
                builder.setRadius(radiusBox.value.get().intValue());
            }
            default -> {}
        }
    }

    private void updateEntities() {
        if (!initialized && type != EPlaybackAreaType.ENTITY) {
            return;
        }
        EntityRidingPlaybackAreaBuilder builder = (EntityRidingPlaybackAreaBuilder)areaSettings;
        builder.setEntityIds(entityListBox.selectedItems.get());
        selectedEntitiesCount = entityListBox.selectedItems.get().size();
    }

    private DLToggleButton addTab(Component text, DLSprite icon, Runnable onClick) {
        int w = Minecraft.getInstance().font.width(text) + 30;
        DLToggleButton btn = new DLToggleButton(0, 0, w, 18);
        btn.radioButtonMode.set(true);
        btn.text.set(text);
        btn.icon.set(icon);
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
