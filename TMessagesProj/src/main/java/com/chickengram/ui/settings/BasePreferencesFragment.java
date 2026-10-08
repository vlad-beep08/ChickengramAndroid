package com.chickengram.ui.settings;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

public abstract class BasePreferencesFragment extends UniversalFragment {

    protected interface Getter {
        boolean get();
    }

    protected interface Setter {
        void set(boolean value);
    }

    protected interface Action {
        void run(View view);
    }

    protected interface Choice {
        void choose(int index);
    }

    private final SparseArray<Getter> getters = new SparseArray<>();
    private final SparseArray<Setter> setters = new SparseArray<>();
    private final SparseArray<Action> actions = new SparseArray<>();
    private final SparseArray<Boolean> restarts = new SparseArray<>();
    private int nextId;

    protected abstract void fill(ArrayList<UItem> items);

    @Override
    public View createView(Context context) {
        final View view = super.createView(context);
        listView.setSections();
        actionBar.setAdaptiveBackground(listView);
        return view;
    }

    @Override
    protected final void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        nextId = 1;
        getters.clear();
        setters.clear();
        actions.clear();
        restarts.clear();
        fill(items);
    }

    protected UItem check(String text, Getter getter, Setter setter) {
        final int id = nextId++;
        getters.put(id, getter);
        setters.put(id, setter);
        return UItem.asCheck(id, text).setChecked(getter.get());
    }

    protected UItem restartCheck(String text, Getter getter, Setter setter) {
        final UItem item = check(text, getter, setter);
        restarts.put(item.id, true);
        return item;
    }

    protected int action(Action action) {
        final int id = nextId++;
        actions.put(id, action);
        return id;
    }

    protected UItem radio(String text, boolean checked, Action action) {
        final int id = nextId++;
        actions.put(id, action);
        return UItem.asRadio(id, text).setChecked(checked);
    }

    protected UItem button(int icon, String text, String value, Action action) {
        final int id = nextId++;
        actions.put(id, action);
        if (icon != 0) {
            return value != null ? UItem.asButton(id, icon, text, value) : UItem.asButton(id, icon, text);
        }
        return value != null ? UItem.asButton(id, text, value) : UItem.asButton(id, text);
    }

    protected void refresh() {
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    protected void showRestartBulletin() {
        BulletinFactory.of(this).createSimpleBulletin(R.raw.info, "Перезапустите приложение, чтобы изменения вступили в силу.").show();
    }

    protected void choose(String title, String[] options, int selected, Choice choice) {
        if (getParentActivity() == null) {
            return;
        }
        showDialog(org.telegram.ui.Components.AlertsCreator.createSingleChoiceDialog(getParentActivity(), options, title, selected, (dialog, which) -> {
            choice.choose(which);
            refresh();
        }));
    }

    protected void confirm(String title, String message, String button, Runnable action) {
        if (getParentActivity() == null) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity(), getResourceProvider());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(button, (dialog, which) -> action.run());
        builder.setNegativeButton("Отмена", null);
        final AlertDialog dialog = builder.create();
        showDialog(dialog);
        final View positive = dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
        if (positive instanceof android.widget.TextView) {
            ((android.widget.TextView) positive).setTextColor(org.telegram.ui.ActionBar.Theme.getColor(org.telegram.ui.ActionBar.Theme.key_text_RedBold));
        }
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        final Setter setter = setters.get(item.id);
        if (setter != null) {
            final Getter getter = getters.get(item.id);
            setter.set(!getter.get());
            refresh();
            if (restarts.get(item.id) != null) {
                showRestartBulletin();
            }
            return;
        }
        final Action action = actions.get(item.id);
        if (action != null) {
            action.run(view);
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}
