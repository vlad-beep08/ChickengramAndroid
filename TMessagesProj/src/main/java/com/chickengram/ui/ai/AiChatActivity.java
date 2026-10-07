package com.chickengram.ui.ai;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chickengram.ai.AiClient;
import com.chickengram.ai.AiStore;
import com.chickengram.ui.settings.AiChatSettingsActivity;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiChatActivity extends BaseFragment {

    private static final int MENU_NEW = 1;
    private static final int MENU_ROLE = 2;
    private static final int MENU_HISTORY = 3;
    private static final int MENU_SETTINGS = 4;

    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*", Pattern.DOTALL);
    private static final Pattern CODE = Pattern.compile("`([^`\\n]+)`");

    private final ArrayList<AiClient.Message> history = new ArrayList<>();
    private long chatId;
    private final String prefill;
    private AiStore.Role role;
    private boolean loading;

    private ScrollView scrollView;
    private LinearLayout messagesLayout;
    private TextView emptyView;
    private LinearLayout inputLayout;
    private EditTextBoldCursor input;
    private ImageView sendButton;
    private TextView pendingView;

    public AiChatActivity() {
        this(-1, null);
    }

    public AiChatActivity(long chatId, String prefill) {
        this.chatId = chatId;
        this.prefill = prefill;
    }

    @Override
    public boolean onFragmentCreate() {
        role = AiStore.selectedRole();
        if (chatId >= 0) {
            history.addAll(AiStore.messages(chatId));
            final AiStore.Chat chat = AiStore.chat(chatId);
            if (chat != null && chat.role != null) {
                for (AiStore.Role item : AiStore.roles()) {
                    if (chat.role.equals(item.name)) {
                        role = item;
                    }
                }
            }
        }
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle("ИИ-чат");
        updateSubtitle();
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == MENU_NEW) {
                    startNewChat();
                } else if (id == MENU_ROLE) {
                    chooseRole();
                } else if (id == MENU_HISTORY) {
                    presentFragment(new AiChatSettingsActivity());
                } else if (id == MENU_SETTINGS) {
                    presentFragment(new tw.nekomimi.nekogram.settings.NekoLLMSettingsActivity());
                }
            }
        });
        final ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(MENU_NEW, R.drawable.msg_add);
        final ActionBarMenuItem other = menu.addItem(10, R.drawable.ic_ab_other);
        other.addSubItem(MENU_ROLE, R.drawable.msg_customize, "Роль");
        other.addSubItem(MENU_HISTORY, R.drawable.msg_recent, "История и роли");
        other.addSubItem(MENU_SETTINGS, R.drawable.msg_settings, "ИИ-сервис");

        final FrameLayout root = new FrameLayout(context);
        root.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        final LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        root.addView(column, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        final FrameLayout content = new FrameLayout(context);
        column.addView(content, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f));

        scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        messagesLayout = new LinearLayout(context);
        messagesLayout.setOrientation(LinearLayout.VERTICAL);
        messagesLayout.setPadding(AndroidUtilities.dp(10), AndroidUtilities.dp(8), AndroidUtilities.dp(10), AndroidUtilities.dp(8));
        scrollView.addView(messagesLayout, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        content.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        emptyView = new TextView(context);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        emptyView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setPadding(AndroidUtilities.dp(32), 0, AndroidUtilities.dp(32), 0);
        content.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        inputLayout = new LinearLayout(context);
        inputLayout.setOrientation(LinearLayout.HORIZONTAL);
        inputLayout.setGravity(Gravity.CENTER_VERTICAL);
        inputLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        inputLayout.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(6), AndroidUtilities.dp(4), AndroidUtilities.dp(6));
        column.addView(inputLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        input = new EditTextBoldCursor(context);
        input.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        input.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        input.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        input.setCursorColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        input.setHint("Сообщение для ИИ");
        input.setBackground(null);
        input.setMaxLines(6);
        input.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        input.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setPadding(0, AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8));
        inputLayout.addView(input, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

        sendButton = new ImageView(context);
        sendButton.setScaleType(ImageView.ScaleType.CENTER);
        sendButton.setImageResource(R.drawable.ic_send);
        sendButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_messagePanelSend), PorterDuff.Mode.SRC_IN));
        sendButton.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), Theme.RIPPLE_MASK_CIRCLE_20DP));
        sendButton.setOnClickListener(v -> send());
        inputLayout.addView(sendButton, LayoutHelper.createLinear(48, 48));

        for (AiClient.Message message : history) {
            addBubble(message.role, message.text, false);
        }
        if (!TextUtils.isEmpty(prefill)) {
            input.setText(prefill);
            input.setSelection(input.length());
        }
        updateEmpty();
        scrollToBottom();

        fragmentView = root;
        return fragmentView;
    }

    @Override
    public void onInsets(int left, int top, int right, int bottom) {
        if (inputLayout != null) {
            inputLayout.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(6), AndroidUtilities.dp(4), AndroidUtilities.dp(6) + bottom);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidUtilities.requestAdjustResize(getParentActivity(), classGuid);
        final AiStore.Role current = findRole(role != null ? role.name : null);
        if (current != null) {
            role = current;
        }
        updateSubtitle();
        updateEmpty();
    }

    @Override
    public void onPause() {
        super.onPause();
        AndroidUtilities.removeAdjustResize(getParentActivity(), classGuid);
    }

    private AiStore.Role findRole(String name) {
        if (name == null) {
            return null;
        }
        for (AiStore.Role item : AiStore.roles()) {
            if (name.equals(item.name)) {
                return item;
            }
        }
        return null;
    }

    private void updateSubtitle() {
        if (actionBar == null) {
            return;
        }
        actionBar.setSubtitle((role != null ? role.name : "Помощник") + " · " + AiClient.providerName() + " · " + AiClient.model());
    }

    private void updateEmpty() {
        if (emptyView == null) {
            return;
        }
        if (!history.isEmpty() || loading) {
            emptyView.setVisibility(View.GONE);
            return;
        }
        emptyView.setVisibility(View.VISIBLE);
        if (!AiClient.hasKey()) {
            emptyView.setText("Чтобы начать, откройте ⋮ → «ИИ-сервис», выберите сервис (например, Gemini, Groq или OpenAI) и вставьте свой ключ API.");
        } else {
            emptyView.setText("Спросите что-нибудь.\nРоль: " + (role != null ? role.name : "Помощник") + ". Сменить роль можно в меню ⋮.");
        }
    }

    private void startNewChat() {
        if (loading) {
            return;
        }
        history.clear();
        chatId = -1;
        messagesLayout.removeAllViews();
        updateEmpty();
    }

    private void chooseRole() {
        if (getParentActivity() == null) {
            return;
        }
        final ArrayList<AiStore.Role> roles = AiStore.roles();
        final CharSequence[] names = new CharSequence[roles.size()];
        for (int i = 0; i < roles.size(); i++) {
            names[i] = role != null && roles.get(i).name.equals(role.name) ? roles.get(i).name + "  ✓" : roles.get(i).name;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
        builder.setTitle("Роль ИИ");
        builder.setItems(names, (dialog, which) -> {
            role = roles.get(which);
            AiStore.setSelectedRole(which);
            updateSubtitle();
            updateEmpty();
        });
        builder.setNegativeButton("Отмена", null);
        showDialog(builder.create());
    }

    private void send() {
        if (loading || input == null) {
            return;
        }
        final String text = input.getText() == null ? "" : input.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }
        input.setText("");
        final AiClient.Message message = new AiClient.Message(AiClient.ROLE_USER, text);
        history.add(message);
        addBubble(AiClient.ROLE_USER, text, false);
        if (AiStore.saveHistory()) {
            if (chatId < 0) {
                chatId = AiStore.createChat(text.length() > 48 ? text.substring(0, 48) + "…" : text, role != null ? role.name : null);
            }
            AiStore.addMessage(chatId, AiClient.ROLE_USER, text);
        }
        loading = true;
        updateEmpty();
        pendingView = addBubble(AiClient.ROLE_ASSISTANT, "Думаю…", false);
        sendButton.setAlpha(0.4f);
        final ArrayList<AiClient.Message> request = new ArrayList<>(history);
        final String prompt = role != null ? role.prompt : null;
        final long requestChat = chatId;
        Utilities.globalQueue.postRunnable(() -> {
            String answer = null;
            String error = null;
            try {
                answer = AiClient.complete(prompt, request);
            } catch (Exception e) {
                error = e.getMessage() != null ? e.getMessage() : e.toString();
            }
            final String finalAnswer = answer;
            final String finalError = error;
            if (finalAnswer != null && requestChat >= 0 && AiStore.saveHistory()) {
                AiStore.addMessage(requestChat, AiClient.ROLE_ASSISTANT, finalAnswer);
            }
            AndroidUtilities.runOnUIThread(() -> onAnswer(requestChat, finalAnswer, finalError));
        });
    }

    private void onAnswer(long requestChat, String answer, String error) {
        loading = false;
        if (sendButton != null) {
            sendButton.setAlpha(1f);
        }
        if (pendingView != null && pendingView.getParent() != null) {
            ((ViewGroup) pendingView.getParent()).removeView(pendingView);
        }
        pendingView = null;
        if (requestChat != chatId || messagesLayout == null) {
            return;
        }
        if (answer != null) {
            history.add(new AiClient.Message(AiClient.ROLE_ASSISTANT, answer));
            addBubble(AiClient.ROLE_ASSISTANT, answer, false);
        } else {
            addBubble(AiClient.ROLE_ASSISTANT, error, true);
        }
        updateEmpty();
    }

    private TextView addBubble(String sender, String text, boolean error) {
        final Context context = getContext();
        final boolean mine = AiClient.ROLE_USER.equals(sender);
        final TextView bubble = new TextView(context);
        bubble.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        bubble.setLineSpacing(AndroidUtilities.dp(2), 1f);
        bubble.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(8), AndroidUtilities.dp(12), AndroidUtilities.dp(8));
        bubble.setMaxWidth((int) (AndroidUtilities.displaySize.x * 0.82f));
        bubble.setTextIsSelectable(true);
        final GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(AndroidUtilities.dp(16));
        if (mine) {
            background.setColor(Theme.getColor(Theme.key_chat_outBubble));
            bubble.setTextColor(Theme.getColor(Theme.key_chat_messageTextOut));
        } else {
            background.setColor(Theme.getColor(Theme.key_chat_inBubble));
            bubble.setTextColor(error ? Theme.getColor(Theme.key_text_RedBold) : Theme.getColor(Theme.key_chat_messageTextIn));
        }
        bubble.setBackground(background);
        bubble.setText(mine || error ? text : format(text));
        final LinearLayout.LayoutParams params = LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, mine ? Gravity.RIGHT : Gravity.LEFT, mine ? 48 : 0, 4, mine ? 0 : 48, 4);
        messagesLayout.addView(bubble, params);
        scrollToBottom();
        return bubble;
    }

    private static CharSequence format(String text) {
        final SpannableStringBuilder builder = new SpannableStringBuilder(text.replace("```", ""));
        applyPattern(builder, BOLD, true);
        applyPattern(builder, CODE, false);
        return builder;
    }

    private static void applyPattern(SpannableStringBuilder builder, Pattern pattern, boolean bold) {
        final Matcher matcher = pattern.matcher(builder);
        int offset = 0;
        final ArrayList<int[]> ranges = new ArrayList<>();
        while (matcher.find()) {
            ranges.add(new int[] { matcher.start(), matcher.end(), matcher.start(1), matcher.end(1) });
        }
        for (int[] range : ranges) {
            final int start = range[0] - offset;
            final int end = range[1] - offset;
            final int inner = range[3] - range[2];
            final int markerLength = range[2] - range[0];
            builder.delete(end - markerLength, end);
            builder.delete(start, start + markerLength);
            builder.setSpan(bold ? new StyleSpan(Typeface.BOLD) : new TypefaceSpan("monospace"), start, start + inner, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            offset += markerLength * 2;
        }
    }

    private void scrollToBottom() {
        if (scrollView != null) {
            scrollView.post(() -> scrollView.scrollTo(0, messagesLayout.getHeight()));
        }
    }
}
