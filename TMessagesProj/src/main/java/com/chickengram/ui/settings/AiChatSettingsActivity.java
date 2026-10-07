package com.chickengram.ui.settings;

import android.content.Context;
import android.text.InputType;
import android.util.TypedValue;
import android.widget.LinearLayout;

import com.chickengram.ai.AiClient;
import com.chickengram.ai.AiStore;
import com.chickengram.ui.ai.AiChatActivity;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.UItem;

import java.util.ArrayList;

import tw.nekomimi.nekogram.settings.NekoLLMSettingsActivity;

public class AiChatSettingsActivity extends BasePreferencesFragment {

    @Override
    protected CharSequence getTitle() {
        return "ИИ-чат";
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    protected void fill(ArrayList<UItem> items) {
        items.add(UItem.asHeader("ИИ-чат"));
        items.add(button(R.drawable.msg_bot, "Открыть ИИ-чат", null, v -> presentFragment(new AiChatActivity())));
        items.add(UItem.asShadow("Отдельный чат с нейросетью прямо в Чикенграме. В меню сообщения появится пункт «Спросить ИИ»."));

        items.add(UItem.asHeader("Сервис"));
        items.add(button(R.drawable.msg_settings, "ИИ-сервис", AiClient.providerName(), v -> presentFragment(new NekoLLMSettingsActivity())));
        items.add(button(0, "Модель", AiClient.model(), v -> presentFragment(new NekoLLMSettingsActivity())));
        items.add(UItem.asShadow(AiClient.hasKey()
            ? "Ключ API указан. Сервис, модель и ключ меняются в «ИИ-сервисе»."
            : "Ключ API не указан. Откройте «ИИ-сервис», выберите сервис и вставьте ключ: бесплатные ключи есть у Google Gemini (aistudio.google.com) и Groq (console.groq.com)."));

        items.add(UItem.asHeader("Роли"));
        final ArrayList<AiStore.Role> roles = AiStore.roles();
        final int selected = AiStore.selectedRoleIndex();
        for (int i = 0; i < roles.size(); i++) {
            final int index = i;
            items.add(radio(roles.get(i).name, i == selected, v -> {
                AiStore.setSelectedRole(index);
                refresh();
            }));
        }
        items.add(button(R.drawable.msg_edit, "Изменить роль «" + roles.get(selected).name + "»", null, v -> editRole(selected)));
        items.add(button(R.drawable.msg_add, "Добавить роль", null, v -> editRole(-1)));
        if (roles.size() > 1) {
            items.add(button(R.drawable.msg_delete, "Удалить роль «" + roles.get(selected).name + "»", null, v -> confirm(
                "Удалить роль",
                "Роль «" + roles.get(selected).name + "» будет удалена.",
                "Удалить",
                () -> {
                    final ArrayList<AiStore.Role> list = AiStore.roles();
                    list.remove(selected);
                    AiStore.saveRoles(list);
                    AiStore.setSelectedRole(0);
                    refresh();
                })).red());
        }
        items.add(UItem.asShadow("Роль — это инструкция, которую нейросеть получает перед разговором: кем быть и как отвечать."));

        items.add(UItem.asHeader("История"));
        items.add(check("Сохранять историю", AiStore::saveHistory, AiStore::setSaveHistory));
        final ArrayList<AiStore.Chat> chats = AiStore.chats();
        for (AiStore.Chat chat : chats) {
            items.add(button(0, chat.title, LocaleController.formatDateChat(chat.updated), v -> presentFragment(new AiChatActivity(chat.id, null))));
        }
        if (!chats.isEmpty()) {
            items.add(button(R.drawable.msg_delete, "Очистить историю", null, v -> confirm(
                "Очистить историю",
                "Все сохранённые разговоры с ИИ будут удалены.",
                "Очистить",
                () -> {
                    AiStore.clearHistory();
                    refresh();
                })).red());
        }
        items.add(UItem.asShadow(chats.isEmpty() ? "Здесь появятся прошлые разговоры." : "Нажмите на разговор, чтобы продолжить его."));
    }

    private void editRole(int index) {
        if (getParentActivity() == null) {
            return;
        }
        final Context context = getParentActivity();
        final ArrayList<AiStore.Role> roles = AiStore.roles();
        final AiStore.Role current = index >= 0 && index < roles.size() ? roles.get(index) : null;

        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(AndroidUtilities.dp(24), AndroidUtilities.dp(4), AndroidUtilities.dp(24), 0);

        final EditTextBoldCursor name = createField(context, "Название", false);
        final EditTextBoldCursor prompt = createField(context, "Инструкция для ИИ", true);
        if (current != null) {
            name.setText(current.name);
            prompt.setText(current.prompt);
        }
        layout.addView(name, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 12));
        layout.addView(prompt, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, getResourceProvider());
        builder.setTitle(current != null ? "Изменить роль" : "Новая роль");
        builder.setView(layout);
        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            final String newName = name.getText() == null ? "" : name.getText().toString().trim();
            final String newPrompt = prompt.getText() == null ? "" : prompt.getText().toString().trim();
            if (newName.isEmpty()) {
                return;
            }
            final ArrayList<AiStore.Role> list = AiStore.roles();
            if (current != null && index < list.size()) {
                list.get(index).name = newName;
                list.get(index).prompt = newPrompt;
            } else {
                list.add(new AiStore.Role(newName, newPrompt));
                AiStore.setSelectedRole(list.size() - 1);
            }
            AiStore.saveRoles(list);
            refresh();
        });
        builder.setNegativeButton("Отмена", null);
        showDialog(builder.create());
    }

    private EditTextBoldCursor createField(Context context, String hint, boolean multiline) {
        final EditTextBoldCursor field = new EditTextBoldCursor(context);
        field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        field.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        field.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        field.setCursorColor(Theme.getColor(Theme.key_dialogTextBlack));
        field.setHint(hint);
        field.setLineColors(Theme.getColor(Theme.key_dialogInputField), Theme.getColor(Theme.key_dialogInputFieldActivated), Theme.getColor(Theme.key_text_RedBold));
        field.setBackground(null);
        field.setPadding(0, AndroidUtilities.dp(6), 0, AndroidUtilities.dp(6));
        if (multiline) {
            field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            field.setMinLines(3);
            field.setMaxLines(8);
        } else {
            field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            field.setSingleLine(true);
        }
        return field;
    }
}
