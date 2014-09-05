package com.gihasil.lab.meminfochart.activity;

import com.gihasil.lab.meminfochart.R;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingPreferenceFragment extends PreferenceFragment {
	private static final String KEY_VERSION_NAME = "version";

	@Override
	public void onResume() {
		updatePreference();
		super.onResume();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}

	private void updatePreference() {
		Preference pref = findPreference(KEY_VERSION_NAME);

		String versionName = null;
		try {
			versionName = getActivity().getPackageManager().getPackageInfo(
					getActivity().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		pref.setSummary("Version " + versionName);
	}
}
