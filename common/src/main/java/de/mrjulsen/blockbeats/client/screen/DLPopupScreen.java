package de.mrjulsen.blockbeats.client.screen;

import de.mrjulsen.blockbeats.client.widgets.popup.IPopupScreen;
import de.mrjulsen.mcdragonlib.client.gui.DLScreen;
import net.minecraft.network.chat.Component;

public abstract class DLPopupScreen extends DLScreen implements IPopupScreen {
    protected DLPopupScreen(Component title) {
        super(title);
    }
}
