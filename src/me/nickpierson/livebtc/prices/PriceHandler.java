package me.nickpierson.livebtc.prices;

import java.util.ArrayList;
import java.util.Observable;

import me.nickpierson.livebtc.network.NetworkObserver;
import me.nickpierson.livebtc.network.NetworkReceiver;
import me.nickpierson.livebtc.timing.TimeObserver;
import me.nickpierson.livebtc.timing.Timer;
import me.nickpierson.livebtc.utils.PrefsHelper;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;

public class PriceHandler extends Observable implements OnSharedPreferenceChangeListener, NetworkObserver, TimeObserver, PriceChangeObserver {

	private static final int UPDATE_FREQUENCY_MS = 5 * 60 * 1000;
	private int timeInterval, numPoints;
	private boolean awaitingNetwork = false;

	private final PrefsHelper prefsHelper;
	private Timer timer;
	private Context context;

	private NetworkReceiver receiver;
	private IntentFilter networkFilter;

	private String currency;
	private String prices;
	private ArrayList<Float> pricesList;

	public PriceHandler(Context context) {
		this.context = context;

		prefsHelper = new PrefsHelper(context);
		prefsHelper.registerOnSharedPreferenceChangeListener(this);

		timeInterval = prefsHelper.getTimeInterval();
		numPoints = prefsHelper.getNumberOfPoints();

		networkFilter = new IntentFilter();
		networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		receiver = new NetworkReceiver(this, context);

		timer = new Timer(this, UPDATE_FREQUENCY_MS);
	}

	public void beginUpdating() {
		timer.startTiming();
		attemptPriceUpdate();
	}

	private void attemptPriceUpdate() {
		if (receiver.hasConnection()) {
			if (awaitingNetwork) {
				timer.startTiming();
				unregisterReceiver();
				awaitingNetwork = false;
			}

			new GetPricesTask(this, prefsHelper.getCurrency()).execute(prefsHelper.getPricesUrl());
		} else if (!awaitingNetwork) {
			timer.stopTiming();
			context.registerReceiver(receiver, networkFilter);
			awaitingNetwork = true;
		}
	}

	@Override
	public void onPricesChanged(String prices, String currency) {
		if (prices != null) {
			this.prices = prices;
			this.currency = currency;

			parsePrices();
			setChanged();
			notifyObservers();
		}
	}

	private void parsePrices() {
		if (prices != null) {
			pricesList = PriceParser.parse(prices, numPoints, timeInterval);
		}
	}

	@Override
	public void onConnected() {
		attemptPriceUpdate();
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
			parsePrices();
		} else if (key.equals(PrefsHelper.NUM_POINTS_KEY)) {
			numPoints = prefsHelper.getNumberOfPoints();
			parsePrices();
		}

		if (!awaitingNetwork) {
			setChanged();
			notifyObservers();
		}
	}

	public void stopUpdating() {
		timer.stopTiming();
		unregisterReceiver();
	}

	private void unregisterReceiver() {
		try {
			context.unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {

		}
	}

	public String getCurrency() {
		return currency;
	}

	public int getTimeInterval() {
		return timeInterval;
	}

	public int getNumberOfPoints() {
		return numPoints;
	}

	public boolean hasPrices() {
		return pricesList != null;
	}

	public ArrayList<Float> getLatestPrices() {
		return pricesList;
	}
}