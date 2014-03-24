package me.nickpierson.livebtc.prices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

class GetPricesTask extends AsyncTask<String, Void, String> {

	static final String BUNDLE_PRICES_KEY = "bundle_prices_key";
	static final String BUNDLE_CURR_KEY = "bundle_currency_key";

	PriceChangeObserver observer;
	private String currency;

	public GetPricesTask(PriceChangeObserver observer, String currency) {
		this.observer = observer;
		this.currency = currency;
	}

	@Override
	protected String doInBackground(String... pricesUrl) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(pricesUrl[0]);

			get.addHeader("User-Agent", "LiveBTC Android Live Wallpaper");
			get.addHeader("From", "piersync@gmail.com");

			HttpResponse responseGet = client.execute(get);
			HttpEntity resEntityGet = responseGet.getEntity();
			if (resEntityGet != null) {
				return EntityUtils.toString(resEntityGet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		observer.onPricesChanged(result, currency);
	}
}