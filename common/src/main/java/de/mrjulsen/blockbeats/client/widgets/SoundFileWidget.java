package de.mrjulsen.blockbeats.client.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ClientWrapper;
import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer.TaskBuilder;
import de.mrjulsen.blockbeats.client.widgets.popup.SoundFileInfoPopupWidget;
import de.mrjulsen.blockbeats.core.ESoundVisibility;
import de.mrjulsen.blockbeats.events.ClientEvents;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.events.ServerEvents;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLTooltip;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLContextMenuItem.ContextMenuItemData;
import de.mrjulsen.mcdragonlib.client.render.Sprite;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiAreaDefinition;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import de.mrjulsen.mcdragonlib.util.IOUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.TimeUtils;
import net.minecraft.Util;
import net.minecraft.network.chat.MutableComponent;

public class SoundFileWidget extends DLButton {

    private final MutableComponent txtPlay = Utils.trans("sound_file", "play");
    private final MutableComponent textOpenLocation = Utils.trans("sound_file", "open_location");
    private final MutableComponent textRefresh = Utils.trans("sound_file", "refresh");
    private final MutableComponent textProperties = Utils.trans("sound_file", "properties");

    public static final int HEIGHT = 34;

    private boolean selected;

    private final SoundFile file;
    private final DLPopupScreen parent;
    private final FileBrowserContainer parentContainer;

    // Buttons
    private int taskIndex = 1;
    private Collection<Task> tasks = new ArrayList<>();
    private final GuiAreaDefinition playButton;

    public SoundFileWidget(DLPopupScreen parent, FileBrowserContainer parentContainer, int pX, int pY, int pWidth, SoundFile file, Consumer<SoundFileWidget> pOnPress, Collection<TaskBuilder> fileTasks) {
        super(pX, pY, pWidth, HEIGHT, TextUtils.empty(), pOnPress);
        this.file = file;
        this.parent = parent;
        this.parentContainer = parentContainer;
        
        setMenu(new DLContextMenu(() -> GuiAreaDefinition.of(this), () -> {
            DLContextMenuItem.Builder builder = new DLContextMenuItem.Builder()
            .add(new ContextMenuItemData(txtPlay, ModGuiIcons.PLAY_SMALL.getAsSprite(16, 16), true, (btn) -> {
                playSample();
            }, null))
            .addSeparator()
            .add(new ContextMenuItemData(textOpenLocation, ModGuiIcons.FOLDER.getAsSprite(16, 16), active, (menuItem) -> {
                try {
                    file.getLocation().setLevel(ServerEvents.getCurrentServer().overworld());
                    Util.getPlatform().openFile(file.getLocation().resolve().orElse(SoundLocation.getModDirectory(ServerEvents.getCurrentServer().overworld())).toFile());
                } catch (Exception e) {
                    BlockBeats.LOGGER.error("Unable to open file location.", e);
                }
            }, null))
            .add(new ContextMenuItemData(textRefresh, ModGuiIcons.REFRESH.getAsSprite(16, 16), active, (menuItem) -> {
                parentContainer.refresh();
            }, null));

            for (TaskBuilder task : fileTasks) {
                builder.add(new ContextMenuItemData(task.text(), task.sprite(), true, (btn) -> task.action().accept(this), null));
            }

            builder.addSeparator()
            .add(new ContextMenuItemData(textProperties, ModGuiIcons.INFO.getAsSprite(16, 16), true, (btn) -> {
                getParent().setPopup((x, y, l, close) -> new SoundFileInfoPopupWidget(getParent(), l, file, x, y, close));
            }, null));            

            return builder;
        }));

        for (TaskBuilder task : fileTasks) {
            if (!task.addButton())
                continue;
                
            addTask(task.sprite(), task.text(), task.action());
        }

        playButton = new GuiAreaDefinition(x(), y(), 34, 34);
    }

    public void addTask(Sprite sprite, MutableComponent text, Consumer<SoundFileWidget> action) {
        tasks.add(new Task(getParent(), new GuiAreaDefinition(x + width - 10 - 20 * taskIndex, y + height / 2 - 10, 20, 20), sprite, DLTooltip.of(text).assignedTo(this).withMaxWidth(width / 4), action));
        taskIndex++;
    }

    public void playSample() {
        if (ClientEvents.getCurrentAudioSamplePath().equals(file.toString())) {
            ClientEvents.stopCurrentAudioSample();
        } else {
            ClientEvents.playAudioSample(file, (i) -> {});
        }
    }

    public DLPopupScreen getParent() {
        return parent;
    }

    public SoundFile getAttachedSoundFile() {
        return file;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public void renderMainLayer(Graphics graphics, int mouseX, int mouseY, float partialTick) {
        final float scale = 0.75f;
        int textRight = isMouseSelected() ? x() + getWidth() - 20 * taskIndex : x() + getWidth() - 15;
        String timeString = TimeUtils.formatDurationMs(file.getInfo().getDuration());
        String sizeString = IOUtils.formatBytes(file.getInfo().getSize());
        String nameString = String.format("%s (%s)", parentContainer.getUsername(file.getInfo().getOwnerId()), TextUtils.translate(ESoundVisibility.getByName(file.getMetadataSafe(BlockBeats.META_VISIBILITY)).getValueTranslationKey(BlockBeats.MOD_ID)).getString());

        GuiUtils.drawString(graphics, font, textRight, y() + 5, timeString, DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.RIGHT, false);
        graphics.poseStack().pushPose();
        graphics.poseStack().scale(scale, scale, 1);        
        GuiUtils.drawString(graphics, font, (int)(textRight / scale), (int)((y() + 15) / scale), sizeString, DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED, EAlignment.RIGHT, false);
        GuiUtils.drawString(graphics, font, (int)(textRight / scale), (int)((y() + 23) / scale), nameString, DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED, EAlignment.RIGHT, false);
        graphics.poseStack().popPose();
        
        
        int labelX = x() + 1 + 32 + 5;
        GuiUtils.drawString(graphics, font, labelX, y() + 5, ClientWrapper.textCutOff(TextUtils.text(file.getDisplayName()), textRight - labelX - 10 - font.width(timeString)), DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE, EAlignment.LEFT, false);
        GuiUtils.drawString(graphics, font, labelX, y() + 20, ClientWrapper.textCutOff(TextUtils.text(file.getInfo().getArtist()), textRight - labelX - 10 - font.width(sizeString)), DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED, EAlignment.LEFT, false);

        GuiUtils.drawTexture(ESoundVisibility.getByName(file.getMetadataSafe(BlockBeats.META_VISIBILITY)).getIconLocation(), graphics, x() + 1, y() + 1, 32, 32);
        GuiUtils.fill(graphics, x() + 10, y() + getHeight() - 1, getWidth() - 20, 1, 0x68888888);

        if (selected) {
            GuiUtils.drawBox(graphics, GuiAreaDefinition.of(this), 0x339E9E9E, DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED);
        }

        if (isMouseSelected() && getParent().getAllowedLayer() == parentContainer.getWidgetLayerIndex()) {
            GuiUtils.drawBox(graphics, GuiAreaDefinition.of(this), 0x33FFFFFF, DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE);
            tasks.forEach(x -> x.render(graphics, mouseX, mouseY));
            
            if (ClientEvents.getCurrentAudioSamplePath().equals(file.toString())) {
                ModGuiIcons.PAUSE.render(graphics, playButton.getX() + playButton.getWidth() / 2 - ModGuiIcons.ICON_SIZE / 2, playButton.getY() + playButton.getHeight() / 2 - ModGuiIcons.ICON_SIZE / 2);
            } else {
                ModGuiIcons.PLAY.render(graphics, playButton.getX() + playButton.getWidth() / 2 - ModGuiIcons.ICON_SIZE / 2, playButton.getY() + playButton.getHeight() / 2 - ModGuiIcons.ICON_SIZE / 2);
            }

            if (playButton.isInBounds(mouseX, mouseY)) {
                GuiUtils.drawBox(graphics, playButton, 0x44FFFFFF, DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE);
            }
        }        
    }

    @Override
    public void renderFrontLayer(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderFrontLayer(graphics, mouseX, mouseY, partialTicks);
        if (getParent().getAllowedLayer() == parentContainer.getWidgetLayerIndex()) {
            tasks.forEach(x -> x.renderTooltip(graphics, mouseX, mouseY, (int)parentContainer.getXScrollOffset(), (int)parentContainer.getYScrollOffset()));
        }
    }

    @Override
    public void onClick(double d, double e) {
        selected = !selected;
        super.onClick(d, e);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isValidClickButton(button)) {

            if (playButton.isInBounds(mouseX, mouseY)) {
                playSample();
                GuiUtils.playButtonSound();
                return true;
            }

            Optional<Task> task = tasks.stream().filter(x -> x.isOver((int)mouseX, (int)mouseY)).findFirst();
            if (task.isPresent()) {
                task.get().run(this);
                GuiUtils.playButtonSound();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void select(boolean b) {
        selected = b;
    }

    public static class Task {
        private final GuiAreaDefinition area;
        private final Sprite sprite;
        private final Consumer<SoundFileWidget> action;
        private final DLTooltip tooltip;
        private final DLPopupScreen parent;
        
        public Task(DLPopupScreen parent, GuiAreaDefinition area, Sprite sprite, DLTooltip tooltip, Consumer<SoundFileWidget> action) {
            this.area = area;
            this.sprite = sprite;
            this.action = action;
            this.tooltip = tooltip;
            this.parent = parent;
        }

        public boolean isOver(int mouseX, int mouseY) {
            return area.isInBounds(mouseX, mouseY);
        }

        public void run(SoundFileWidget widget) {
            action.accept(widget);
        }

        public void render(Graphics graphics, int mouseX, int mouseY) {
            sprite.render(graphics, area.getX() + area.getWidth() / 2 - ModGuiIcons.ICON_SIZE / 2, area.getY() + area.getHeight() / 2 - ModGuiIcons.ICON_SIZE / 2);
            if (isOver(mouseX, mouseY)) {                
                GuiUtils.drawBox(graphics, area, 0x44FFFFFF, DragonLib.NATIVE_BUTTON_FONT_COLOR_ACTIVE);
            }
        }

        public void renderTooltip(Graphics graphics, int mouseX, int mouseY, int xOffset, int yOffset) {
            if (isOver(mouseX + xOffset, mouseY + yOffset)) {
                GuiUtils.renderTooltipAt(parent, GuiAreaDefinition.of(tooltip.getAssignedWidget()), tooltip.getLines(), tooltip.getMaxWidth(), graphics, mouseX + 8, mouseY - 16, mouseX + xOffset, mouseY + yOffset, 0, 0);
            }
        }
    }
    
}
