package de.mrjulsen.blockbeats.client.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ClientWrapper;
import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.blockbeats.client.widgets.FileBrowserContainer.TaskBuilder;
import de.mrjulsen.blockbeats.client.widgets.popup.SoundFileInfoPopupWidget;
import de.mrjulsen.blockbeats.core.ESoundVisibility;
import de.mrjulsen.blockbeats.events.ClientEvents;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.dragnsounds.core.filesystem.SoundLocation;
import de.mrjulsen.dragnsounds.events.ServerEvents;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.events.DLGuiStandardEvents;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLButton;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLContextMenu;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLPanel;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.BorderLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout;
import de.mrjulsen.mcdragonlib.client.gui.widgets.layout.FlowLayout.Direction;
import de.mrjulsen.mcdragonlib.client.gui.widgets.render.FlatButtonRenderer;
import de.mrjulsen.mcdragonlib.client.gui.widgets.richtext.Padding;
import de.mrjulsen.mcdragonlib.client.gui.widgets.components.DLTooltip;
import de.mrjulsen.mcdragonlib.client.util.DLGuiGraphics;
import de.mrjulsen.mcdragonlib.client.util.DLSprite;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils.TextureFillMode;
import de.mrjulsen.mcdragonlib.data.ETextAlignment;
import de.mrjulsen.mcdragonlib.util.DLColor;
import de.mrjulsen.mcdragonlib.util.IOUtils;
import de.mrjulsen.mcdragonlib.util.TextUtils;
import de.mrjulsen.mcdragonlib.util.math.Rectangle;
import de.mrjulsen.mcdragonlib.util.time.DLTime;
import de.mrjulsen.mcdragonlib.util.time.TimeContext;
import de.mrjulsen.mcdragonlib.util.time.VanillaTimeSystem;
import de.mrjulsen.mcdragonlib.util.time.format.TimeFormatDigitalDuration;
import net.minecraft.Util;
import net.minecraft.network.chat.MutableComponent;

public class SoundFileWidget extends DLButton {
    
    private final MutableComponent txtPlay = Utils.trans("sound_file", "play");
    private final MutableComponent textOpenLocation = Utils.trans("sound_file", "open_location");
    private final MutableComponent textRefresh = Utils.trans("sound_file", "refresh");
    private final MutableComponent textProperties = Utils.trans("sound_file", "properties");

    public static final int HEIGHT = 34;

    private boolean picked;

    private final SoundFile file;
    private final FileBrowserContainer parentContainer;
    private final DLPanel buttonsPanel;


    public SoundFileWidget(FileBrowserContainer parentContainer, int pX, int pY, int pWidth, SoundFile file, Consumer<SoundFileWidget> pOnPress, Collection<TaskBuilder> fileTasks) {
        super(pX, pY, pWidth, HEIGHT);
        text.set(TextUtils.empty());
        addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {            
            picked = !picked;
            pOnPress.accept(this);
            return false;
        });

        addEventListener(DLGuiStandardEvents.MouseEnterEvent.class, (s, e) -> {
            getComponents().forEach(c -> c.visible.set(true));
            return false;
        });

        addEventListener(DLGuiStandardEvents.MouseLeaveEvent.class, (s, e) -> {
            getComponents().forEach(c -> c.visible.set(false));
            return false;
        });

        this.file = file;
        this.parentContainer = parentContainer;

        DLContextMenu contextMenu = new DLContextMenu((x, y) -> {
            List<DLContextMenu.ItemEntry> entries = new ArrayList<>();
            entries.add(new DLContextMenu.ItemEntry(txtPlay, ModGuiIcons.PLAY_SMALL.getAsSprite(16, 16), true, () -> {
                playSample();
            }, null));
            entries.add(DLContextMenu.ItemEntry.SEPARATOR);
            entries.add(new DLContextMenu.ItemEntry(textOpenLocation, ModGuiIcons.FOLDER.getAsSprite(16, 16), true, () -> {
                try {
                    file.getLocation().setLevel(ServerEvents.getCurrentServer().overworld());
                    Util.getPlatform().openFile(file.getLocation().resolve().orElse(SoundLocation.getModDirectory(ServerEvents.getCurrentServer().overworld())).toFile());
                } catch (Exception e) {
                    BlockBeats.LOGGER.error("Unable to open file location.", e);
                }
            }, null));
            entries.add(new DLContextMenu.ItemEntry(textRefresh, ModGuiIcons.REFRESH.getAsSprite(16, 16), true, () -> {
                parentContainer.refresh();
            }, null));

            for (TaskBuilder task : fileTasks) {
                final TaskBuilder fTask = task;
                entries.add(new DLContextMenu.ItemEntry(fTask.text(), fTask.sprite(), true, () -> {
                    fTask.action().accept(this);
                }, null));
            }
            entries.add(DLContextMenu.ItemEntry.SEPARATOR);            
            entries.add(new DLContextMenu.ItemEntry(textProperties, ModGuiIcons.INFO.getAsSprite(16, 16), true, () -> {
                getWindowManager().createModal(mgr -> new SoundFileInfoPopupWidget(mgr, file));
            }, null));
            return entries;
        });
        addEventListener(DLGuiStandardEvents.RightClickEvent.class, (s, e) -> {
            contextMenu.open(getWindowManager());
            return false;
        });
        /*
        setMenu(new DLContextMenu(() -> GuiAreaDefinition.of(this), () -> {
            DLContextMenuItem.Builder builder = new DLContextMenuItem.Builder()
            .add(new ContextMenuItemData(txtPlay, ModGuiIcons.PLAY_SMALL.getAsSprite(16, 16), true, (btn) -> {
                playSample();
            }, null))
            .addSeparator()
            .add(new ContextMenuItemData(textOpenLocation, ModGuiIcons.FOLDER.getAsSprite(16, 16), active, (menuItem) -> {
                
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
        */

        buttonsPanel = addComponent(new DLPanel(0, 0, 1, 1));
        buttonsPanel.inputConsumptionPolicy.set(c -> false);
        buttonsPanel.layoutContraint.set(BorderLayout.BorderPosition.CENTER);
        buttonsPanel.visible.set(false);
        FlowLayout layout = new FlowLayout();
        layout.flowDirection.set(Direction.HORIZONTAL);
        layout.wrap.set(false);
        layout.padding.set(new Padding(8, 8, 8, 8));
        buttonsPanel.layout.set(layout);

        DLButton playBtn = addComponent(new DLButton(0, 0, HEIGHT, HEIGHT));
        playBtn.layoutContraint.set(BorderLayout.BorderPosition.WEST);
        playBtn.inputConsumptionPolicy.set(c -> c != ConsumptionType.MOUSE_MOVE);
        playBtn.text.set(TextUtils.EMPTY);
        playBtn.icon.set(ModGuiIcons.PLAY.getAsSprite(16, 16));
        playBtn.componentRenderer.set(FlatButtonRenderer.INSTANCE);
        playBtn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            playSample();
            return false;
        });


        BorderLayout borderLayout = new BorderLayout(0, 0);
        this.layout.set(borderLayout);

        for (TaskBuilder task : fileTasks) {
            if (!task.addButton())
                continue;
                
            addTask(task.sprite(), task.text(), task.action());
        }
    }

    public void addTask(DLSprite sprite, MutableComponent text, Consumer<SoundFileWidget> action) {
        DLButton btn = buttonsPanel.addComponent(new DLButton(0, 0, 18, 18));
        btn.componentRenderer.set(FlatButtonRenderer.INSTANCE);
        btn.text.set(TextUtils.empty());
        btn.layoutContraint.set(FlowLayout.FlowConstraint.END);
        btn.inputConsumptionPolicy.set(c -> c != ConsumptionType.MOUSE_MOVE);
        btn.tooltip.set(new DLTooltip(List.of(text), 200));
        btn.icon.set(sprite);
        btn.addEventListener(DLGuiStandardEvents.ClickEvent.class, (s, e) -> {
            action.accept(this);
            return false;
        });
    }

    public void playSample() {
        if (ClientEvents.getCurrentAudioSamplePath().equals(file.toString())) {
            ClientEvents.stopCurrentAudioSample();
        } else {
            ClientEvents.playAudioSample(file, (i) -> {});
        }
    }

    public SoundFile getAttachedSoundFile() {
        return file;
    }

    public boolean isPicked() {
        return picked;
    }

    @Override
    public void renderMainLayer(DLGuiGraphics graphics, double mouseX, double mouseY, Rectangle renderBounds) {
        final float scale = 0.75f;
        int textRight = isSelected() ? width() - 20 * buttonsPanel.componentsCount() - 10 : width() - 10;
        String timeString = DLTime.fromReal(0, 0, 0, 0, (int)file.getInfo().getDuration(), VanillaTimeSystem.INSTANCE).format(new TimeFormatDigitalDuration(), TimeContext.REAL);
        String sizeString = IOUtils.formatBytes(file.getInfo().getSize());
        String nameString = String.format("%s (%s)", parentContainer.getUsername(file.getInfo().getOwnerId()), ESoundVisibility.getByName(file.getMetadataSafe(BlockBeats.META_VISIBILITY)).getValueTranslation().getString());

        GuiUtils.drawString(graphics, graphics.defaultFont(), textRight,5, timeString, DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.RIGHT, false);
        graphics.poseStack().pushPose();
        graphics.poseStack().scale(scale, scale, 1);        
        GuiUtils.drawString(graphics, graphics.defaultFont(), (int)(textRight / scale), (int)((15) / scale), sizeString, DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.RIGHT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), (int)(textRight / scale), (int)((23) / scale), nameString, DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.RIGHT, false);
        graphics.poseStack().popPose();
        
        
        int labelX = x() + 1 + 32 + 5;
        GuiUtils.drawString(graphics, graphics.defaultFont(), labelX, 5, ClientWrapper.textCutOff(TextUtils.text(file.getDisplayName()), textRight - labelX - 10 - graphics.defaultFont().width(timeString)), DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);
        GuiUtils.drawString(graphics, graphics.defaultFont(), labelX, 20, ClientWrapper.textCutOff(TextUtils.text(file.getInfo().getArtist()), textRight - labelX - 10 - graphics.defaultFont().width(sizeString)), DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR, ETextAlignment.LEFT, false);

        GuiUtils.drawTexture(ESoundVisibility.getByName(file.getMetadataSafe(BlockBeats.META_VISIBILITY)).getIconLocation(), graphics, 1, 1, 32, 32, 0, 0, 16, 16, TextureFillMode.STRETCH, 16, 16);
        GuiUtils.fill(graphics, 10, height() - 1, width() - 20, 1, DLColor.fromInt(0x68888888));

        if (picked) {
            GuiUtils.drawBox(graphics, 0, 0, width(), height(), DLColor.fromInt(0x339E9E9E), DragonLib.VANILLA_BUTTON_DISABLED_FONT_COLOR);
        }

        if (isSelected()) {
            GuiUtils.drawBox(graphics, 0, 0, width(), height(), DLColor.fromInt(0x33FFFFFF), DragonLib.VANILLA_BUTTON_ACTIVE_FONT_COLOR);
        }        
    }

    public void select(boolean b) {
        picked = b;
    }    
}
