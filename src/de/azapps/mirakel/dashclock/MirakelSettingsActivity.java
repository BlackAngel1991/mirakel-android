/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists
 *
 * Copyright (c) 2013 Anatolij Zelenin, Georg Semmler.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.azapps.mirakel.dashclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.NumberPicker;

import com.google.common.base.Optional;

import de.azapps.mirakel.adapter.SimpleModelAdapter;
import de.azapps.mirakel.model.MirakelInternalContentProvider;
import de.azapps.mirakel.model.ModelBase;
import de.azapps.mirakel.model.list.ListMirakel;
import de.azapps.mirakel.model.query_builder.MirakelQueryBuilder;

public class MirakelSettingsActivity extends PreferenceActivity {
    private static final String TAG = "MirakelSettingsActivity";
    private NumberPicker numberPicker;

    @SuppressWarnings("deprecation") // because we use the PreferenceActivity
    public void onCreate(Bundle savedInstanceState) {
        // Init stuff
        super.onCreate(savedInstanceState);
        MirakelExtension.init(this);

        // Set ActionBar stuff
        Drawable d = getResources().getDrawable(R.drawable.bw_mirakel);
        getActionBar().setIcon(d);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getListView().setDividerHeight(0);
        addPreferencesFromResource(R.xml.pref_xml);

        // Preferences
        final Preference startupListPreference = findPreference("startupList");
        final Preference showTasksPreference = findPreference("showTasks");

        // List preference
        Optional<ListMirakel> listMirakelOptional = SettingsHelper.getList();
        if (listMirakelOptional.isPresent()) {
            startupListPreference.setSummary(listMirakelOptional.get().getName());
        } else {
            startupListPreference.setSummary(R.string.no_list_selected);
        }
        startupListPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Cursor cursor = new MirakelQueryBuilder(MirakelSettingsActivity.this).query(
                    MirakelInternalContentProvider.LIST_WITH_SPECIAL_URI);
                final SimpleModelAdapter<ListMirakel> adapter = new SimpleModelAdapter<>
                (MirakelSettingsActivity.this, cursor, 0, ListMirakel.class);
                AlertDialog.Builder builder = new AlertDialog.Builder(MirakelSettingsActivity.this);
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ListMirakel list = MirakelQueryBuilder.cursorToObject((Cursor) adapter.getItem(which),
                                           ListMirakel.class);
                        SettingsHelper.setList(list);
                        startupListPreference.setSummary(list.getName());
                    }
                });
                builder.setTitle(R.string.select_list_title);
                builder.show();
                return true;
            }
        });
        // Show tasks
        int maxTasks = SettingsHelper.getMaxTasks();
        showTasksPreference.setSummary(getResources().getQuantityString(R.plurals.how_many,
                                       maxTasks, maxTasks));
        final Context ctx = this;
        showTasksPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                numberPicker = new NumberPicker(ctx);
                numberPicker.setMaxValue(5);
                numberPicker.setMinValue(0);
                numberPicker.setWrapSelectorWheel(false);
                numberPicker.setValue(SettingsHelper.getMaxTasks());
                numberPicker
                .setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
                new AlertDialog.Builder(ctx)
                .setTitle(getString(R.string.number_of))
                .setMessage(getString(R.string.how_many))
                .setView(numberPicker)
                .setPositiveButton(getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        int count = numberPicker.getValue();
                        SettingsHelper.setMaxTasks(count);
                        showTasksPreference.setSummary(getResources()
                                                       .getQuantityString(
                                                           R.plurals.how_many,
                                                           count, count));
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        // Do nothing.
                    }
                }).show();
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
