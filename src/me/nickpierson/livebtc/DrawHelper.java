package me.nickpierson.livebtc;

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
}
