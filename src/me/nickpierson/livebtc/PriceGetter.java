package me.nickpierson.livebtc;

import java.util.ArrayList;

public class PriceGetter {

	public static ArrayList<Float> get(int numPoints, int minuteInterval) {
		ArrayList<Float> points = new ArrayList<Float>();
		points.add(43.6f);
		points.add(40.3f);
		points.add(46.5f);
		points.add(46.3f);
		points.add(42.6f);
		points.add(40.3f);
		points.add(51.2f);
		points.add(40.3f);
		points.add(45.0f);

		return points;
	}

}
