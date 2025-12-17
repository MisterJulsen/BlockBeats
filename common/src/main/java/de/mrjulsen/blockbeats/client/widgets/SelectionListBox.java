package de.mrjulsen.blockbeats.client.widgets;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.annotations.SupportsEvents;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLItemSelectionBox;
import de.mrjulsen.mcdragonlib.client.render.DefaultGuiTextures;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.events.IEvent;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import net.minecraft.client.Minecraft;

@SupportsEvents({
    SelectionListBox.SelectEvent.class
})
public class SelectionListBox<T> extends DLItemSelectionBox<T> { 
    
    public record SelectEvent(DLListBoxItem<?> item) implements IEvent {}

    public SelectionListBox(int x, int y, int w, int h) {
        super(x, y, w, h);
        scrollBar.setPosition(width() - scrollBar.width(), 0);
    }

    @Override
    protected DLListBoxItem<T> defaultItemBuilder(T item) {
        SelectionBoxItem<T> itm = new SelectionBoxItem<>(this, item);
        itm.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            invokeEvent(this, new SelectEvent(itm));
            return false;
        });
        return itm;
    }

    @Override
    protected void defaultItemSelection(ItemSelectionChangeEvent event) {
        List<DLListBoxItem<T>> itemComponents = getSelectedComponent();
        DLListBoxItem<?> clickedItem = event.item();
        boolean isSelected = clickedItem.selected.get();

        if (!multiselect.get()) {
            for (DLListBoxItem<T> itm : itemComponents) {
                if (itm != event.item()) {
                    itm.selected.set(false);
                }
            }
        }
        
        event.selected().setValue(!isSelected);
    }

    public void selectIf(Predicate<T> test) {
        List<T> items = new LinkedList<>();
        for (T item : this.items) {
            if (test.test(item)) {
                items.add(item);
            }
        }
        this.selectedItems.set(items);
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        DefaultGuiTextures.DRAGONLIB_UI.getSprite("container").render(graphics, 0, 0, width(), height());
    }

    public static class SelectionBoxItem<T> extends DLItemSelectionBox.DLListBoxItem<T> {

        protected SelectionBoxItem(SelectionListBox<T> collectionComponentRef, T item) {
            super(collectionComponentRef, item, 100, 16);
        }

        @Override
        public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
            if (selected.get()) {
                GuiUtils.drawBox(graphics, getRenderBounds(), DLColor.BLACK, DLColor.WHITE);
            }
            if (isSelected()) {
                GuiUtils.fill(graphics, getRenderBounds(), DLColor.fromInt(0x44FFFFFF));
            }
            GuiUtils.drawString(graphics, Minecraft.getInstance().font, 4, height() / 2 - Minecraft.getInstance().font.lineHeight / 2, ((SelectionListBox<T>)collectionComponentRef).textFormat.get().apply(item), selected.get() ? DragonLib.VANILLA_BUTTON_HIGHLIGHTED_FONT_COLOR : DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);
        }

        public T getItem() {
            return item;
        }

    }
}
