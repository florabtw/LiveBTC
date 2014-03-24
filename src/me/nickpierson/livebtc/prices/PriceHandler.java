package me.nickpierson.livebtc.prices;

import java.util.ArrayList;
import java.util.Observable;

import me.nickpierson.livebtc.PrefsHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Handler;
import android.os.Message;

public class PriceHandler extends Observable implements OnSharedPreferenceChangeListener {

	private static final int UPDATE_FREQUENCY_MS = 5 * 60 * 1000;
	private int timeInterval, numPoints;

	private final PrefsHelper prefsHelper;
	private final Runnable runnable;
	private String latestPrices;

	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			String prices = msg.getData().getString(GetPricesTask.BUNDLE_PRICES_KEY);

			if (prices != null) {
				ArrayList<Float> pricesList = PriceParser.parse(prices, numPoints, timeInterval);
				setChanged();
				notifyObservers(pricesList);
				return true;
			} else {
				return false;
			}
		}
	});

	public PriceHandler(Context context) {
		prefsHelper = new PrefsHelper(context);
		timeInterval = prefsHelper.getTimeInterval();
		numPoints = prefsHelper.getNumberOfPoints();

		runnable = new Runnable() {
			@Override
			public void run() {
				attemptPriceUpdate(prefsHelper.getPricesUrl());
			}
		};
	}

	private void attemptPriceUpdate(String pricesUrl) {
		// if internet...
		new GetPricesTask(handler).execute(pricesUrl);

		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, UPDATE_FREQUENCY_MS);
	}

	public void removeCallbacks() {
		handler.removeCallbacks(runnable);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PrefsHelper.CURRENCY_KEY)) {
			// CURRENCY = prefsHelper.getCurrency();
			attemptPriceUpdate(prefsHelper.getPricesUrl());
		} else if (key.equals(PrefsHelper.TIME_INTERVAL_KEY)) {
			timeInterval = prefsHelper.getTimeInterval();
		} else if (key.equals(PrefsHelper.NUM_POINTS_KEY)) {
			numPoints = prefsHelper.getNumberOfPoints();
		}
	}

	// TODO used?
	public String getLatestPrices() {
		return latestPrices;
	}
}