package me.nickpierson.livebtc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.preference.PreferenceManager;

public class PrefsHelper {

	public static final String BOTTOM_MARGIN_KEY = "bottom_margin";
	public static final String TOP_MARGIN_KEY = "top_margin";
	public static final String TIME_INTERVAL_KEY = "time_interval";
	public static final String NUM_POINTS_KEY = "num_points";
	public static final String CURRENCY_KEY = "currency";
	public static final String BASIC_BACKGROUND_KEY = "basic_background_color";
	public static final String BASIC_LINE_KEY = "basic_line_color";

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

	public int getTimeInterval() {
		return Integer.valueOf(prefs.getString(TIME_INTERVAL_KEY, "30"));
	};

	public int getNumberOfPoints() {
		return Integer.valueOf(prefs.getString(NUM_POINTS_KEY, "16"));
	}

	public String getPricesUrl() {
		return "https://api.bitcoinaverage.com/history/" + getCurrency() + "/per_minute_24h_sliding_window.csv";
	}

	public String getCurrency() {
		return prefs.getString(CURRENCY_KEY, "USD");
	}

	public int getBackgroundColor() {
		String prefsColor = prefs.getString(BASIC_BACKGROUND_KEY, "Black");
		return getColor(prefsColor);
	}

	public int getLineColor() {
		String prefsColor = prefs.getString(BASIC_LINE_KEY, "White");
		return getColor(prefsColor);
	}

	public int getColor(String prefsColor) {
		if (prefsColor.equals("Blue")) {
			return Color.BLUE;
		} else if (prefsColor.equals("Cyan")) {
			return Color.CYAN;
		} else if (prefsColor.equals("Gray")) {
			return Color.GRAY;
		} else if (prefsColor.equals("Green")) {
			return Color.GREEN;
		} else if (prefsColor.equals("Magenta")) {
			return Color.MAGENTA;
		} else if (prefsColor.equals("Red")) {
			return Color.RED;
		} else if (prefsColor.equals("White")) {
			return Color.WHITE;
		} else if (prefsColor.equals("Yellow")) {
			return Color.YELLOW;
		} else {
			return Color.BLACK;
		}
	}
}
