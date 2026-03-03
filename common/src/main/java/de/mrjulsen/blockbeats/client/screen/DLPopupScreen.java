package de.mrjulsen.blockbeats.client.screen;

import de.mrjulsen.blockbeats.client.widgets.popup.IPopupScreen;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindow;
import de.mrjulsen.mcdragonlib.client.gui.widgets.base.DLWindowManager;

public abstract class DLPopupScreen extends DLWindow implements IPopupScreen {
    protected DLPopupScreen(DLWindowManager manager) {
        super(manager);
    }
}
