package me.nickpierson.livebtc.prices;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

class GetPricesTask extends AsyncTask<String, Void, String> {

	public static final String BUNDLE_PRICES_KEY = "bundle_prices_key";

	private Handler handler;

	public GetPricesTask(Handler handler) {
		this.handler = handler;
	}

	@Override
	protected String doInBackground(String... pricesUrl) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(pricesUrl[0]);
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
		// latestPrices = result;
		Message message = handler.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putString(BUNDLE_PRICES_KEY, result);
		message.setData(bundle);
		handler.sendMessage(message);
		// draw(result);
	}
}