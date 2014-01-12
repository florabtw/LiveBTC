package me.nickpierson.livebtc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PriceParser {

	private static final int VAL_START = 20;
	private static final int DATE_END = 19;

	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	private static int lastFound;

	public static ArrayList<Float> parse(String prices, int numPoints, int minuteInterval) {
		String[] pricesArray = prices.split("\n");

		ArrayList<Float> results = new ArrayList<Float>();
		Date now = parseDate(pricesArray[pricesArray.length - 1].substring(0, DATE_END));

		lastFound = pricesArray.length - 1;
		for (int i = 0; i < numPoints; i++) {
			float val = findNearest(pricesArray, now, minuteInterval * i);
			if (val == 0) {
				return null;
			}

			results.add(0, val);
		}

		return results;
	}

	private static float findNearest(String[] pricesArray, Date now, int minutesAgo) {
		long goalMillis = now.getTime() - (minutesAgo * 60 * 1000);
		Date currDate = parseDate(pricesArray[lastFound].substring(0, DATE_END));
		long currDiff = goalMillis - currDate.getTime();
		Date nextDate;
		for (int i = lastFound; i > 2; i--) {
			nextDate = parseDate(pricesArray[i - 1].substring(0, DATE_END));

			long nextDiff = goalMillis - nextDate.getTime();

			if (Math.abs(nextDiff) > Math.abs(currDiff)) {
				lastFound = i;
				return Float.valueOf(pricesArray[i].substring(VAL_START));
			} else {
				currDate = nextDate;
				currDiff = nextDiff;
			}
		}

		return 0;
	}

	private static Date parseDate(String dateAsString) {
		Date result;
		try {
			result = format.parse(dateAsString);
		} catch (ParseException e) {
			result = null;
		}

		return result;
	}
}
