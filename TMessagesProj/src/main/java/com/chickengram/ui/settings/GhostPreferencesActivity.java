package com.chickengram.ui.settings;

import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Components.UItem;

import java.util.ArrayList;

import tw.nekomimi.nekogram.NekoConfig;
import tw.nekomimi.nekogram.utils.AyuGhostUtils;

public class GhostPreferencesActivity extends BasePreferencesFragment {

    private boolean expanded;

    @Override
    protected CharSequence getTitle() {
        return "Режим призрака";
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        int enabled = 0;
        if (!NekoConfig.sendReadMessagePackets) enabled++;
        if (!NekoConfig.sendReadStoryPackets) enabled++;
        if (!NekoConfig.sendOnlinePackets) enabled++;
        if (!NekoConfig.sendUploadProgress) enabled++;
        if (NekoConfig.sendOfflineAfterOnline) enabled++;

        items.add(UItem.asHeader("Режим призрака"));
        final int groupId = action(v -> {
            expanded = !expanded;
            refresh();
        });
        items.add(UItem.asExpandableSwitch(groupId, "Режим призрака", enabled + "/5")
            .setChecked(NekoConfig.isGhostModeActive())
            .setCollapsed(!expanded)
            .setClickCallback(v -> {
                NekoConfig.toggleGhostMode();
                AyuGhostUtils.setAllowReadPacket(false, -1);
                notifyDrawer();
                refresh();
            }));
        if (expanded) {
            items.add(option("Не читать сообщения", !NekoConfig.sendReadMessagePackets, () -> {
                NekoConfig.putBoolean("sendReadMessagePackets", NekoConfig.sendReadMessagePackets ^= true);
                AyuGhostUtils.setAllowReadPacket(false, -1);
            }));
            items.add(option("Не читать истории", !NekoConfig.sendReadStoryPackets, () ->
                NekoConfig.putBoolean("sendReadStoryPackets", NekoConfig.sendReadStoryPackets ^= true)));
            items.add(option("Не отправлять «онлайн»", !NekoConfig.sendOnlinePackets, () ->
                NekoConfig.putBoolean("sendOnlinePackets", NekoConfig.sendOnlinePackets ^= true)));
            items.add(option("Не отправлять «печатает»", !NekoConfig.sendUploadProgress, () ->
                NekoConfig.putBoolean("sendUploadProgress", NekoConfig.sendUploadProgress ^= true)));
            items.add(option("Автоматический «офлайн»", NekoConfig.sendOfflineAfterOnline, () ->
                NekoConfig.putBoolean("sendOfflineAfterOnline", NekoConfig.sendOfflineAfterOnline ^= true)));
        }
        items.add(UItem.asShadow("Нажмите на строку, чтобы выбрать, что именно скрывать."));

        items.add(check("Читать при действиях", () -> NekoConfig.markReadAfterSend, v -> {
            NekoConfig.putBoolean("markReadAfterSend", NekoConfig.markReadAfterSend = v);
            AyuGhostUtils.setAllowReadPacket(false, -1);
        }));
        items.add(UItem.asShadow("Автоматически читает сообщения, когда вы отправляете новое сообщение или ставите реакцию."));

        items.add(check("Кнопка призрака в боковом меню", () -> NekoConfig.showGhostToggleInDrawer, v -> {
            NekoConfig.putBoolean("showGhostToggleInDrawer", NekoConfig.showGhostToggleInDrawer = v);
            notifyDrawer();
        }));
        items.add(UItem.asShadow(null));
    }

    private UItem option(String text, boolean checked, Runnable toggle) {
        final int id = action(v -> {
            toggle.run();
            notifyDrawer();
            refresh();
        });
        return UItem.asRoundCheckbox(id, text).setChecked(checked).setPad(1);
    }

    private void notifyDrawer() {
        NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.mainUserInfoChanged);
    }
}
