package com.chickengram.ui;

import android.content.Context;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chickengram.messages.EditHistory;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

public final class EditHistoryAlert {

    private EditHistoryAlert() {
    }

    public static void show(BaseFragment fragment, MessageObject message) {
        if (fragment == null || message == null || fragment.getParentActivity() == null) {
            return;
        }
        final Context context = fragment.getParentActivity();
        final ArrayList<EditHistory.Version> versions = EditHistory.get(fragment.getCurrentAccount(), message.getDialogId(), message.getId());

        final LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(4), AndroidUtilities.dp(24), AndroidUtilities.dp(8));
        for (EditHistory.Version version : versions) {
            addEntry(context, list, LocaleController.formatDateTime(version.date, true), version.text);
        }
        final int currentDate = message.messageOwner.edit_date != 0 ? message.messageOwner.edit_date : message.messageOwner.date;
        addEntry(context, list, "Сейчас · " + LocaleController.formatDateTime(currentDate, true), message.messageOwner.message == null ? "" : message.messageOwner.message);

        final ScrollView scroll = new ScrollView(context);
        scroll.addView(list, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0));

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, fragment.getResourceProvider());
        builder.setTitle("История изменений");
        builder.setView(scroll);
        builder.setPositiveButton("Закрыть", null);
        fragment.showDialog(builder.create());
    }

    private static void addEntry(Context context, LinearLayout list, String title, String text) {
        final TextView header = new TextView(context);
        header.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        header.setText(title);
        list.addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 12, 0, 2));

        final TextView body = new TextView(context);
        body.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        body.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        body.setTextIsSelectable(true);
        body.setText(text);
        list.addView(body, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }
}
