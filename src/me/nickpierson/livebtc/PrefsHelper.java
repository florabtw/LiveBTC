package me.nickpierson.livebtc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class PrefsHelper {

	public static final String BOTTOM_MARGIN_KEY = "bottom_margin";
	public static final String TOP_MARGIN_KEY = "top_margin";

	private SharedPreferences prefs;

	public <T extends OnSharedPreferenceChangeListener> PrefsHelper(Context context, T listener) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}

	public int getBottomMargin(int height) {
		return getMargin(height, BOTTOM_MARGIN_KEY);
	}

	public int getTopMargin(int height) {
		return getMargin(height, TOP_MARGIN_KEY);
	}

	public int getMargin(int height, String key) {
		float percentMargin = .01f;
		try {
			percentMargin = Integer.parseInt(prefs.getString(key, "1")) * .01f;
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
