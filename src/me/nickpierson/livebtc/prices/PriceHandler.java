package me.nickpierson.livebtc.prices;

import java.util.ArrayList;
import java.util.Observable;

import me.nickpierson.livebtc.utils.PrefsHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class PriceHandler extends Observable implements OnSharedPreferenceChangeListener {

	private static final int UPDATE_FREQUENCY_MS = 5 * 60 * 1000;
	private int timeInterval, numPoints;

	private final PrefsHelper prefsHelper;
	private final Runnable runnable;

	private String currency;
	private String prices;
	private ArrayList<Float> pricesList;

	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			Bundle data = msg.getData();
			prices = data.getString(GetPricesTask.BUNDLE_PRICES_KEY);
			currency = data.getString(GetPricesTask.BUNDLE_CURR_KEY);

			if (prices != null) {
				pricesList = PriceParser.parse(prices, numPoints, timeInterval);
				setChanged();
				notifyObservers();
				return true;
			} else {
				return false;
			}
		}
	});

	public PriceHandler(Context context) {
		prefsHelper = new PrefsHelper(context);
		prefsHelper.registerOnSharedPreferenceChangeListener(this);

		timeInterval = prefsHelper.getTimeInterval();
		numPoints = prefsHelper.getNumberOfPoints();

		runnable = new Runnable() {
			@Override
			public void run() {
				attemptPriceUpdate();
			}
		};
	}

	public void attemptPriceUpdate() {
		new GetPricesTask(handler, prefsHelper.getCurrency()).execute(prefsHelper.getPricesUrl());

		handler.removeCallbacks(runnable);
		handler.postDelayed(runnable, UPDATE_FREQUENCY_MS);
	}

	public void removeCallbacks() {
		handler.removeCallbacks(runnable);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(PrefsHelper.CURRENCY_KEY)) {
			attemptPriceUpdate();
		} else if (key.equals(PrefsHelper.TIME_INTERVAL_KEY)) {
			timeInterval = prefsHelper.getTimeInterval();
			pricesList = PriceParser.parse(prices, numPoints, timeInterval);
		} else if (key.equals(PrefsHelper.NUM_POINTS_KEY)) {
			numPoints = prefsHelper.getNumberOfPoints();
			pricesList = PriceParser.parse(prices, numPoints, timeInterval);
		}

		notifyObservers();
	}

	public String getCurrency() {
		return currency;
	}

	public ArrayList<Float> getLatestPrices() {
		return pricesList;
	}
}