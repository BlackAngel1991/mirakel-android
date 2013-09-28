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
package de.azapps.mirakel.settings.semantics;

import java.util.List;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import de.azapps.mirakel.helper.Log;
import de.azapps.mirakel.model.list.ListMirakel;
import de.azapps.mirakel.model.semantic.Semantic;
import de.azapps.mirakelandroid.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SemanticsSettingsFragment extends PreferenceFragment implements
		OnPreferenceChangeListener {
	@SuppressWarnings("unused")
	private static final String TAG = "SemanticsSettingsFragment";
	private Semantic semantic;
	Context ctx;
	protected AlertDialog alert;
	private EditTextPreference semanticsCondition;
	private ListPreference semanticsDue, semanticsList, semanticsPriority;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_semantics);
		Bundle b = getArguments();
		if (b != null) {
			semantic = Semantic.get(getArguments().getInt("id"));
			getActivity().getActionBar().setTitle(semantic.getCondition());
			setup();
		} else {
			Log.d(TAG, "bundle null");
		}

	}

	public void setup() {
		semanticsCondition = (EditTextPreference) findPreference("semantics_condition");
		semanticsCondition.setOnPreferenceChangeListener(this);
		semanticsCondition.setText(semantic.getCondition());
		semanticsCondition.setSummary(semantic.getCondition());

		// Priority
		semanticsPriority = (ListPreference) findPreference("semantics_priority");
		semanticsPriority.setOnPreferenceChangeListener(this);
		semanticsPriority.setEntries(R.array.priority_entries);
		semanticsPriority.setEntryValues(R.array.priority_entry_values);
		if (semantic.getPriority() == null) {
			semanticsPriority.setValueIndex(0);
			semanticsPriority.setSummary(getResources().getStringArray(
					R.array.priority_entries)[0]);
		} else {
			semanticsPriority.setValue(semantic.getPriority().toString());
			semanticsPriority.setSummary(semanticsPriority.getValue());
		}

		// Due
		// semanticsDue = (ListPreference) findPreference("semantics_due");
		// semanticsDue.setOnPreferenceChangeListener(this);
		// TODO
		// List
		semanticsList = (ListPreference) findPreference("semantics_list");
		semanticsList.setOnPreferenceChangeListener(this);

		List<ListMirakel> lists = ListMirakel.all(false);
		final CharSequence[] listEntries = new CharSequence[lists.size() + 1];
		final CharSequence[] listValues = new CharSequence[lists.size() + 1];
		listEntries[0] = getString(R.string.semantics_no_list);
		listValues[0] = "null";
		for (int i = 0; i < lists.size(); i++) {
			listValues[i + 1] = String.valueOf(lists.get(i).getId());
			listEntries[i + 1] = lists.get(i).getName();
		}
		semanticsList.setEntries(listEntries);
		semanticsList.setEntryValues(listValues);

		if (semantic.getList() == null) {
			semanticsList.setValueIndex(0);
			semanticsList.setSummary(getString(R.string.semantics_no_list));
		} else {
			semanticsList.setValue(String.valueOf(semantic.getList().getId()));
			semanticsList.setSummary(semantic.getList().getName());
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object nv) {
		String newValue = String.valueOf(nv);
		String key = preference.getKey();
		if (key.equals("semantics_priority")) {
			if (newValue.equals("null")) {
				semantic.setPriority(null);
				semanticsPriority.setValueIndex(0);
				semanticsPriority.setSummary(semanticsPriority.getEntries()[0]);
			} else {
				semantic.setPriority(Integer.parseInt(newValue));
				semanticsPriority.setValue(newValue);
				semanticsPriority.setSummary(newValue);
			}
			semantic.save();
		} else if (key.equals("semantics_due")) {

		} else if (key.equals("semantics_list")) {
			if (newValue.equals("null")) {
				semantic.setList(null);
				semanticsList.setValueIndex(0);
				semanticsList.setSummary(semanticsList.getEntries()[0]);
			} else {
				ListMirakel newList = ListMirakel.getList(Integer
						.parseInt(newValue));
				semantic.setList(newList);
				semanticsList.setValue(newValue);
				semanticsList.setSummary(newList.getName());
			}
			semantic.save();
		} else if (key.equals("semantics_condition")) {
			semantic.setCondition(newValue);
			semantic.save();
			semanticsCondition.setSummary(newValue);
			semanticsCondition.setText(newValue);
		}
		return false;
	}
}