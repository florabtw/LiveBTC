package me.nickpierson.livebtc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class PrefsHelper {

	public static final String BOTTOM_MARGIN_KEY = "bottom_margin";

	private SharedPreferences prefs;

	public <T extends OnSharedPreferenceChangeListener> PrefsHelper(Context context, T listener) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}

	public int getBottomMargin(int height) {
		float percentMargin = .01f;
		try {
			percentMargin = Integer.parseInt(prefs.getString(BOTTOM_MARGIN_KEY, "1")) * .01f;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}

		// max allowed is 30%
		if (percentMargin > .3) {
			percentMargin = .3f;
		}

		return (int) (height * percentMargin);
	}
}
