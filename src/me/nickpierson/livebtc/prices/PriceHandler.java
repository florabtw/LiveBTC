package me.nickpierson.livebtc.prices;

import java.util.ArrayList;
import java.util.Observable;

import me.nickpierson.livebtc.network.NetworkObserver;
import me.nickpierson.livebtc.timing.Timer;
import me.nickpierson.livebtc.timing.TimeObserver;
import me.nickpierson.livebtc.utils.PrefsHelper;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.util.Log;

public class PriceHandler extends Observable implements OnSharedPreferenceChangeListener, NetworkObserver, TimeObserver, PriceChangeObserver {

	private static final int UPDATE_FREQUENCY_MS = 5 * 60 * 1000;
	private int timeInterval, numPoints;

	private final PrefsHelper prefsHelper;

	private String currency;
	private String prices;
	private ArrayList<Float> pricesList;
	private Timer timer;

	public PriceHandler(Context context) {
		prefsHelper = new PrefsHelper(context);
		prefsHelper.registerOnSharedPreferenceChangeListener(this);

		timeInterval = prefsHelper.getTimeInterval();
		numPoints = prefsHelper.getNumberOfPoints();

		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		// NetworkReceiver receiver = new NetworkReceiver(this, context);
		// context.registerReceiver(receiver, filter);

		timer = new Timer(this, UPDATE_FREQUENCY_MS);
		timer.startTiming();
	}

	public void attemptPriceUpdate() {
		Log.e("TAG", "Price updating!!");
		new GetPricesTask(this, prefsHelper.getCurrency()).execute(prefsHelper.getPricesUrl());
	}

	@Override
	public void onPricesChanged(String prices, String currency) {
		this.prices = prices;
		this.currency = currency;

		if (prices != null) {
			pricesList = PriceParser.parse(prices, numPoints, timeInterval);
			setChanged();
			notifyObservers();
		}
	}

	@Override
	public void onConnected() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTimeEvent() {
		attemptPriceUpdate();
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

	public void stopUpdating() {
		timer.stopTiming();
		// TODO unregister networkreceiver?
	}

	public String getCurrency() {
		return currency;
	}

	public ArrayList<Float> getLatestPrices() {
		return pricesList;
	}
}