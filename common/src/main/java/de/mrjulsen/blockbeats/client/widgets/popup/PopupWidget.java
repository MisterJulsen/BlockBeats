package de.mrjulsen.blockbeats.client.widgets.popup;

import java.util.function.Consumer;

import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLWidgetContainer;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public abstract class PopupWidget extends DLWidgetContainer {

    private final Consumer<PopupWidget> close;
    private final DLPopupScreen parent;

    public PopupWidget(DLPopupScreen parent, int layer, int width, int height, Consumer<PopupWidget> close) {
        super(0, 0, width, height);
        this.close = close;
        this.parent = parent;
        setWidgetLayerIndex(layer);
    }

    public DLPopupScreen getParent() {
        return parent;
    }

    @Override
    public final void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.poseStack().pushPose();
        graphics.poseStack().translate(0, 0, 200 * getWidgetLayerIndex());
        graphics.graphics().fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        renderMainPopupLayer(graphics, mouseX, mouseY, partialTicks);
        super.renderMainLayer(graphics, mouseX, mouseY, partialTicks);
        graphics.poseStack().popPose();
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.poseStack().pushPose();
        graphics.poseStack().translate(0, 0, 200 * getWidgetLayerIndex());
        super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
        graphics.poseStack().popPose();
    }

    public void renderMainPopupLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput var1) {
    }

    @FunctionalInterface
    public static interface IPopupBuilder {
        PopupWidget create(int width, int height, int layer, Consumer<PopupWidget> close);
    }

    public void close() {
        close.accept(this);
    }

    @Override
    public boolean consumeScrolling(double mouseX, double mouseY) {
        return true;
    }
    
}
