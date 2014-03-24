package me.nickpierson.livebtc.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkReceiver extends BroadcastReceiver {

	private NetworkObserver observer;
	private Context context;

	public NetworkReceiver(NetworkObserver observer, Context context) {
		this.observer = observer;
		this.context = context;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (hasConnection()) {
			notifyObserver();
		}
	}

	public boolean hasConnection() {
		ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conn.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	private void notifyObserver() {
		observer.onConnected();
	}
}