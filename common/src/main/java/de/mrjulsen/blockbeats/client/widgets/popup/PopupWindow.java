package de.mrjulsen.blockbeats.client.widgets.popup;

import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;
import de.mrjulsen.mcdragonlib.client.gui.widgets.util.EAlign;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;

public abstract class PopupWindow extends DLWindow {

    public PopupWindow(DLWindowManager manager, int width, int height) {
        super(manager);
        setSize(width, height);
        windowSpawnPosition.set(WindowPosition.PARENT_CENTER);
    }

    @Override
    public void renderBackLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        graphics.poseStack().pushPose();
        GuiUtils.fillGradient(graphics, -x(), -y(), (int)getWindowManager().getScreenWidth(), (int)getWindowManager().getScreenHeight(), DLColor.fromInt(-1072689136), DLColor.fromInt(-804253680), EAlign.TOP);
        graphics.poseStack().popPose();
    }


    @FunctionalInterface
    public static interface IPopupBuilder {
        PopupWindow create(int width, int height);
    }    
}
