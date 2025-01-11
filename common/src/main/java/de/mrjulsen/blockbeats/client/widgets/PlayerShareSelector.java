package de.mrjulsen.blockbeats.client.widgets;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.mrjulsen.blockbeats.BlockBeats;
import de.mrjulsen.blockbeats.client.ModGuiIcons;
import de.mrjulsen.blockbeats.client.screen.DLPopupScreen;
import de.mrjulsen.blockbeats.client.widgets.PlayerWidget.TaskBuilder;
import de.mrjulsen.blockbeats.core.data.SharingUtils;
import de.mrjulsen.blockbeats.core.data.SharingUtils.ShareData;
import de.mrjulsen.blockbeats.net.cts.GetUsernameCachePacket;
import de.mrjulsen.blockbeats.util.Utils;
import de.mrjulsen.dragnsounds.api.ClientApi;
import de.mrjulsen.dragnsounds.core.filesystem.SoundFile;
import de.mrjulsen.mcdragonlib.DragonLib;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLAbstractScrollBar;
import de.mrjulsen.mcdragonlib.client.gui.widgets.DLScrollableWidgetContainer;
import de.mrjulsen.mcdragonlib.client.util.Graphics;
import de.mrjulsen.mcdragonlib.client.util.GuiUtils;
import de.mrjulsen.mcdragonlib.core.EAlignment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class PlayerShareSelector extends DLScrollableWidgetContainer {

    private final MutableComponent textSharedWith = Utils.trans("share", "shared_with");
    private final MutableComponent textOnlinePlayers = Utils.trans("share", "online_players");
    private final MutableComponent textClickToToggle = Utils.trans("share", "click_to_toggle").withStyle(ChatFormatting.GRAY);
    private final MutableComponent textShare = Utils.trans("share", "add");
    private final MutableComponent textStopSharing = Utils.trans("share", "remove");
    private final MutableComponent textCanEdit = Utils.trans("share", "can_edit").append("\n").append(textClickToToggle);
    private final MutableComponent textCannotEdit = Utils.trans("share", "cannot_edit").append("\n").append(textClickToToggle);

    private final DLPopupScreen parent;
    @SuppressWarnings("unused") private final Supplier<DLAbstractScrollBar<?>> scrollBar;
    private SoundFile file;
    private int separatorY;
    private boolean isShared;
    private String filter = "";

    public PlayerShareSelector(DLPopupScreen parent, int x, int y, int width, int height, SoundFile file, Supplier<DLAbstractScrollBar<?>> scrollBar) {
        super(x, y, width, height);
        this.parent = parent;
        this.scrollBar = scrollBar;        
        this.file = file;
        refresh("");
    }

    public void refresh(String filter) {
        this.filter = filter;
        clearWidgets();

        BlockBeats.net().sendToServer(GetUsernameCachePacket.create((usernamecache) -> {            
            ClientPacketListener clientPacketListener = Minecraft.getInstance().player.connection;
            Map<UUID, PlayerInfo> infos = clientPacketListener.getOnlinePlayers().stream().collect(Collectors.toMap(x -> x.getProfile().getId(), x -> x));
            Set<UUID> shareEntries = SharingUtils.deserialize(file).keySet();

            isShared = !shareEntries.isEmpty();
            final int minY = getY() + (isShared ? PlayerWidget.HEIGHT : 0);
            int dY = minY;
            for (UUID id : shareEntries) {
                final UUID currentId = id;
                final String name = infos.containsKey(currentId) ? infos.get(currentId).getProfile().getName() : usernamecache.getOrDefault(currentId, currentId.toString());
                final ResourceLocation skinLocation = infos.containsKey(currentId) ? infos.get(currentId).getSkin().texture() : null;

                if ((name != null && !name.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))) && !currentId.toString().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT))) {
                    continue;
                }

                List<TaskBuilder> tasks = new LinkedList<>();
                
                Map<UUID, ShareData> tmpShareData = SharingUtils.deserialize(file);
                tasks.add(new TaskBuilder(ModGuiIcons.REMOVE.getAsSprite(16, 16), textStopSharing, (btn) -> {
                    Map<UUID, ShareData> shareData = SharingUtils.deserialize(file);
                    SharingUtils.stopSharingWith(shareData, currentId);
                    reload(SharingUtils.serialize(shareData));
                }));
                boolean canEdit = SharingUtils.getMetaSafe(tmpShareData, id, BlockBeats.META_SHARE_CAN_EDIT).isBlank() ? false : Boolean.parseBoolean(SharingUtils.getMetaSafe(tmpShareData, id, BlockBeats.META_SHARE_CAN_EDIT));
                tasks.add(new TaskBuilder(canEdit ? ModGuiIcons.EDIT.getAsSprite(16, 16) : ModGuiIcons.VISIBLE.getAsSprite(16, 16), (canEdit ? textCanEdit : textCannotEdit), (btn) -> {
                    Map<UUID, ShareData> shareData = SharingUtils.deserialize(file);
                    boolean b = !canEdit;
                    SharingUtils.addOrUpdateMeta(shareData, currentId, Map.of(BlockBeats.META_SHARE_CAN_EDIT, String.valueOf(b)));
                    reload(SharingUtils.serialize(shareData));
                }));

                addRenderableWidget(new PlayerWidget(parent, this, getX(), dY, getWidth(), id, name, skinLocation,
                (b) -> {

                }, tasks));
                dY += PlayerWidget.HEIGHT;
            }

            separatorY = dY;
            dY += PlayerWidget.HEIGHT;
            for (Map.Entry<UUID, PlayerInfo> info : infos.entrySet()) {

                if (shareEntries.contains(info.getKey()) || !(info.getValue().getProfile().getName().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT)) || info.getKey().toString().toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT)))) {
                    continue;
                }

                final UUID currentId = info.getKey();
                addRenderableWidget(new PlayerWidget(parent, this, getX(), dY, getWidth(), info.getValue().getProfile().getId(), info.getValue().getProfile().getName(), info.getValue().getSkin().texture(),
                (b) -> {

                }, List.of(
                    new TaskBuilder(ModGuiIcons.ADD.getAsSprite(16, 16), textShare, (btn) -> {
                        Map<UUID, ShareData> shareData = SharingUtils.deserialize(file);
                        SharingUtils.shareWith(shareData, currentId, Map.of(BlockBeats.META_SHARE_CAN_EDIT, String.valueOf(false)));
                        reload(SharingUtils.serialize(shareData));
                    })
                )));
                dY += PlayerWidget.HEIGHT;
            }
        }));
    }

    public void reload(String shareString) {
        file.updateMetadata(Map.of(BlockBeats.META_SHARED, shareString));
        ClientApi.getSoundFile(file.getLocation(), file.getId(), (fle) -> {
            if (fle.isPresent()) {
                file = fle.get();
                refresh(filter);
            }
        });
    }
    
    public int maxRequiredHeight() {
        return (children().size()) * SoundFileWidget.HEIGHT;
    }    

    @Override
    public void renderMainLayerScrolled(Graphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderMainLayerScrolled(graphics, mouseX, mouseY, partialTicks);
        if (isShared) {
            GuiUtils.drawString(graphics, font, x + width / 2, getY() + PlayerWidget.HEIGHT / 2 - font.lineHeight / 2, textSharedWith, DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED, EAlignment.CENTER, false);
        }
        GuiUtils.drawString(graphics, font, x + width / 2, separatorY + PlayerWidget.HEIGHT / 2 - font.lineHeight / 2, textOnlinePlayers, DragonLib.NATIVE_BUTTON_FONT_COLOR_DISABLED, EAlignment.CENTER, false);
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.HOVERED;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
    }
    
    @Override
    public boolean consumeScrolling(double mouseX, double mouseY) {
        return isMouseOver(mouseX, mouseY);
    }
}
