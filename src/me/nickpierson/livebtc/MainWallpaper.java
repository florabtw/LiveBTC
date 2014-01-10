package me.nickpierson.livebtc;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;

public class MainWallpaper extends WallpaperService {

	@Override
	public Engine onCreateEngine() {
		return new MyEngine();
	}

	class MyEngine extends Engine {

		private Paint graphPaint;
		private final Handler handler = new Handler();
		private boolean isVisible;
		private int myWidth, myHeight, TOP_OFFSET, SIDE_OFFSET, HATCH_LENGTH, Y_HATCH_WIDTH;

		private final int STROKE_WIDTH = 4; // best if even number
		private final int X_HATCHES = 8;
		private final int Y_HATCHES = 10;

		private final Runnable runnable = new Runnable() {
			public void run() {
				draw();
			}
		};

		public MyEngine() {
			graphPaint = new Paint();
			graphPaint.setColor(Color.WHITE);
			graphPaint.setStrokeWidth(STROKE_WIDTH);
			graphPaint.setTextSize(40);
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			TOP_OFFSET = 0;
			int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
			if (resourceId > 0) {
				TOP_OFFSET = getResources().getDimensionPixelSize(resourceId);
			}

			DisplayMetrics metrics = getResources().getDisplayMetrics();
			myWidth = metrics.widthPixels;
			myHeight = metrics.heightPixels;

			SIDE_OFFSET = myWidth / 20;
			HATCH_LENGTH = myHeight / 100;
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			handler.removeCallbacks(runnable);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			isVisible = visible;
			if (visible) {
				draw();
			} else {
				handler.removeCallbacks(runnable);
			}
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			isVisible = false;
			handler.removeCallbacks(runnable);
		}

		void draw() {
			final SurfaceHolder holder = getSurfaceHolder();

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					// draw something
					drawChart(c);
				}
			} finally {
				if (c != null) {
					holder.unlockCanvasAndPost(c);
				}
			}

			handler.removeCallbacks(runnable);
			if (isVisible) {
				handler.postDelayed(runnable, 1000 / 25);
			}
		}

		void drawChart(Canvas c) {
			// draw axes
			int bottomY = myHeight - TOP_OFFSET;
			int halfStroke = STROKE_WIDTH / 2;
			int leftX = SIDE_OFFSET + halfStroke;
			c.drawLine(SIDE_OFFSET, TOP_OFFSET, SIDE_OFFSET, bottomY + halfStroke, graphPaint);
			c.drawLine(leftX, bottomY, myWidth - SIDE_OFFSET, bottomY, graphPaint);

			// draw hatch marks for x axis
			int xAxisWidth = myWidth - SIDE_OFFSET * 2;
			float xAxisHatchInterval = (float) xAxisWidth / X_HATCHES;
			for (int i = myWidth - SIDE_OFFSET - halfStroke; i > SIDE_OFFSET; i -= xAxisHatchInterval) {
				c.drawLine(i, bottomY + halfStroke, i, bottomY + halfStroke - HATCH_LENGTH, graphPaint);
			}

			// draw hatch marks for y axis
			int yAxisHeight = myHeight - TOP_OFFSET * 2;
			float yAxisHatchInterval = (float) yAxisHeight / Y_HATCHES;
			for (int i = TOP_OFFSET + halfStroke; i < myHeight - TOP_OFFSET; i += Math.ceil(yAxisHatchInterval)) {
				c.drawLine(SIDE_OFFSET - halfStroke, i, SIDE_OFFSET - halfStroke + HATCH_LENGTH, i, graphPaint);
			}
		}
	}
}
