package me.nickpierson.livebtc.prices;

public interface PriceChangeObserver {
	public void onPricesChanged(String prices, String currency);
}
