package me.nickpierson.livebtc.timing;

import android.os.Handler;

public class Timer {

	private TimeObserver observer;
	private int millisDelay;
	private boolean isTiming;

	private Thread timerThread;
	private Handler handler;

	public Timer(TimeObserver observer, int millisDelay) {
		this.observer = observer;
		this.millisDelay = millisDelay;

		isTiming = false;

		handler = new Handler();
		timerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				if (isTiming) {
					timeEvent();
				}
			}
		});
	}

	public void startTiming() {
		if (!isTiming) {
			postThreadWithDelay();
			isTiming = true;
		}
	}

	public void stopTiming() {
		handler.removeCallbacks(timerThread);
		isTiming = false;
	}

	private void timeEvent() {
		notifyObserver();

		postThreadWithDelay();
	}

	private void postThreadWithDelay() {
		handler.removeCallbacks(timerThread);
		handler.postDelayed(timerThread, millisDelay);
	}

	private void notifyObserver() {
		observer.onTimeEvent();
	}
}