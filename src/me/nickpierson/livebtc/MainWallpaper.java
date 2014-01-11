package me.nickpierson.livebtc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
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

		private static final String PRICES_URL = "https://api.bitcoinaverage.com/history/USD/per_minute_24h_sliding_window.csv";

		private Paint graphPaint;
		private final Handler handler = new Handler();
		private int myWidth, myHeight, TOP_OFFSET, SIDE_OFFSET, BOTTOM_OFFSET, TICK_LENGTH, Y_LABEL_SPACE, X_LABEL_SPACE;

		private final int STROKE_WIDTH = 4; // best if even number
		private final int X_TICKS = 4;
		private final int Y_TICKS = 4;
		private final int PADDING = 10;

		private final Runnable runnable = new Runnable() {
			public void run() {
				new GetPricesTask().execute();
			}
		};

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

			int textSize = myWidth / 40;

			graphPaint = new Paint();
			graphPaint.setColor(Color.WHITE);
			graphPaint.setStrokeWidth(STROKE_WIDTH);
			graphPaint.setStyle(Paint.Style.FILL);
			graphPaint.setAntiAlias(true);
			graphPaint.setTextSize(textSize);

			BOTTOM_OFFSET = myHeight / 100;
			SIDE_OFFSET = myWidth / 100;
			TICK_LENGTH = myHeight / 100;
			Y_LABEL_SPACE = (int) graphPaint.measureText("000");

			Rect rect = new Rect();
			graphPaint.getTextBounds("0m", 0, 2, rect);
			X_LABEL_SPACE = rect.height();

			new GetPricesTask().execute();
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			handler.removeCallbacks(runnable);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			handler.removeCallbacks(runnable);
		}

		private class GetPricesTask extends AsyncTask<Void, Void, String> {
			@Override
			protected String doInBackground(Void... params) {
				try {
					HttpClient client = new DefaultHttpClient();
					HttpGet get = new HttpGet(PRICES_URL);
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
				draw(result);
			}
		}

		void draw(String prices) {
			final SurfaceHolder holder = getSurfaceHolder();

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					drawChart(c, prices);
				}
			} finally {
				if (c != null) {
					holder.unlockCanvasAndPost(c);
				}
			}

			handler.removeCallbacks(runnable);
			handler.postDelayed(runnable, 200000);
		}

		void drawChart(Canvas c, String prices) {
			int halfStroke = STROKE_WIDTH / 2;
			int yOffset = myHeight - BOTTOM_OFFSET - X_LABEL_SPACE - PADDING;
			int xOffset = SIDE_OFFSET + Y_LABEL_SPACE + PADDING;
			int yAxisHeight = yOffset - TOP_OFFSET;
			int xAxisWidth = myWidth - SIDE_OFFSET - xOffset;
			int minutesInterval = 15;
			int dataPoints = 8;

			ArrayList<Float> values = PriceParser.parse(prices, dataPoints + 1, minutesInterval);

			// turn values into x, y coordinates
			float maxVal = Collections.max(values);
			float minVal = Collections.min(values);
			float xScale = ((float) xAxisWidth) / (values.size() - 1);
			float yScale = ((float) yAxisHeight) / (maxVal - minVal);
			List<Point> graphPoints = new ArrayList<Point>();
			for (int i = 0; i < values.size(); i++) {
				int x = (int) (i * xScale + xOffset);
				int y = (int) ((maxVal - values.get(i)) * yScale + TOP_OFFSET);
				graphPoints.add(new Point(x, y));
			}

			// draw background
			c.drawColor(Color.BLACK);

			// draw axes
			c.drawLine(xOffset, TOP_OFFSET, xOffset, yOffset + halfStroke, graphPaint);
			c.drawLine(xOffset + halfStroke, yOffset, myWidth - SIDE_OFFSET, yOffset, graphPaint);

			// draw tick marks, labels for x axis
			float xTickInterval = (float) xAxisWidth / X_TICKS;
			String[] xLabels = getResources().getStringArray(R.array.thirty_min_interval);
			DrawHelper.drawXLabel(c, xLabels[xLabels.length - 1], graphPaint, xOffset, myHeight - BOTTOM_OFFSET, DrawHelper.Position.PAST);
			for (int i = 1; i < X_TICKS; i++) {
				float xPos = (myWidth - SIDE_OFFSET) - (i * xTickInterval);
				c.drawLine(xPos, yOffset + halfStroke, xPos, yOffset + halfStroke - TICK_LENGTH, graphPaint);

				DrawHelper.drawXLabel(c, xLabels[i], graphPaint, xPos, myHeight - BOTTOM_OFFSET, DrawHelper.Position.CENTER);
			}
			c.drawLine(myWidth - SIDE_OFFSET, yOffset + halfStroke, myWidth - SIDE_OFFSET, yOffset + halfStroke - TICK_LENGTH, graphPaint);
			DrawHelper.drawXLabel(c, xLabels[0], graphPaint, myWidth - SIDE_OFFSET, myHeight - BOTTOM_OFFSET, DrawHelper.Position.BEFORE);

			// draw tick marks, labels for y axis
			float yTickInterval = (float) yAxisHeight / Y_TICKS;
			float yValueInterval = (maxVal - minVal) / Y_TICKS;
			c.drawLine(xOffset - halfStroke, TOP_OFFSET, xOffset + TICK_LENGTH, TOP_OFFSET, graphPaint);
			DrawHelper.drawYLabel(c, String.valueOf(Math.round(maxVal)), graphPaint, SIDE_OFFSET, TOP_OFFSET, DrawHelper.Position.PAST);
			for (int i = 1; i < Y_TICKS; i++) {
				float yPos = TOP_OFFSET + (i * yTickInterval);
				c.drawLine(xOffset - halfStroke, yPos, xOffset + TICK_LENGTH, yPos, graphPaint);

				String yLabel = String.valueOf((int) Math.round(maxVal - (i * yValueInterval)));
				DrawHelper.drawYLabel(c, yLabel, graphPaint, SIDE_OFFSET, yPos, DrawHelper.Position.CENTER);
			}
			DrawHelper.drawYLabel(c, String.valueOf(Math.round(minVal)), graphPaint, SIDE_OFFSET, yOffset, DrawHelper.Position.BEFORE);

			// draw lines for graph
			c.drawCircle(graphPoints.get(0).x, graphPoints.get(0).y, halfStroke, graphPaint);
			for (int i = 0; i < graphPoints.size() - 1; i++) {
				int xStart = graphPoints.get(i).x;
				int xEnd = graphPoints.get(i + 1).x;
				int yStart = graphPoints.get(i).y;
				int yEnd = graphPoints.get(i + 1).y;

				c.drawLine(xStart, yStart, xEnd, yEnd, graphPaint);
				c.drawCircle(xEnd, yEnd, halfStroke, graphPaint);
			}
		}
	}
}
