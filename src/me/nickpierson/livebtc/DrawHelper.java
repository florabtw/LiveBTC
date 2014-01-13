package me.nickpierson.livebtc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class DrawHelper {

	public enum Position {
		PAST, BEFORE, CENTER
	};

	public static void drawYLabel(Canvas c, String label, Paint paint, float xPos, float yPos, Position pos) {
		float offset = 0;
		Rect rect = new Rect();
		paint.getTextBounds(label, 0, label.length(), rect);

		if (pos == Position.PAST) {
			offset += rect.height();
		} else if (pos == Position.CENTER) {
			offset += rect.height() / 2;
		}

		c.drawText(label, xPos, yPos + offset, paint);
	}

	public static void drawXLabel(Canvas c, String label, Paint paint, float xPos, float yPos, Position pos) {
		float offset = 0;
		Rect rect = new Rect();
		paint.getTextBounds(label, 0, label.length(), rect);

		if (pos == Position.BEFORE) {
			offset -= rect.width();
		} else if (pos == Position.CENTER) {
			offset -= rect.width() / 2;
		}

		c.drawText(label, xPos + offset, yPos, paint);
	}

	public static String[] getXAxisLabels(int minutesInterval, int numPoints, Context c) {
		int arrayId;
		int product = minutesInterval * numPoints;
		if (product == 40) {
			arrayId = R.array.forty_min_span;
		} else if (product == 80) {
			arrayId = R.array.one_hour_twenty_min_span;
		} else if (product == 120) {
			arrayId = R.array.two_hour_span;
		} else if (product == 240) {
			arrayId = R.array.four_hour_span;
		} else if (product == 360) {
			arrayId = R.array.six_hour_span;
		} else if (product == 480) {
			arrayId = R.array.eight_hour_span;
		} else if (product == 720) {
			arrayId = R.array.twelve_hour_span;
		} else if (product == 960) {
			arrayId = R.array.sixteen_hour_span;
		} else {
			arrayId = R.array.twenty_four_hour_span;
		}

		return c.getResources().getStringArray(arrayId);
	}
}
