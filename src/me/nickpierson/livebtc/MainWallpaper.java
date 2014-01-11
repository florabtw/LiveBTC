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

		private static final String PRICES_URL = "https://coinbase.com/api/v1/prices/historical";

		private Paint graphPaint;
		private final Handler handler = new Handler();
		private int myWidth, myHeight, TOP_OFFSET, SIDE_OFFSET, HATCH_LENGTH, Y_LABEL_SPACE;

		private final int STROKE_WIDTH = 4; // best if even number
		private final int X_HATCHES = 8;
		private final int Y_HATCHES = 5;
		private final int PADDING = 10;

		private final Runnable runnable = new Runnable() {
			public void run() {
				new GetPricesTask().execute();
			}
		};

		public MyEngine() {
			graphPaint = new Paint();
			graphPaint.setColor(Color.WHITE);
			graphPaint.setStrokeWidth(STROKE_WIDTH);
			graphPaint.setTextSize(40);
			graphPaint.setStyle(Paint.Style.FILL);
			graphPaint.setAntiAlias(true);
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

			SIDE_OFFSET = myWidth / 100;
			HATCH_LENGTH = myHeight / 100;
			Y_LABEL_SPACE = (int) graphPaint.measureText("000");

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
			handler.postDelayed(runnable, 100000);
		}

		void drawChart(Canvas c, String prices) {
			int bottomY = myHeight - TOP_OFFSET;
			int halfStroke = STROKE_WIDTH / 2;
			int xOffset = SIDE_OFFSET + Y_LABEL_SPACE + PADDING;

			ArrayList<Float> values = PriceParser.parse(prices, X_HATCHES + 1, 15);

			// turn values into x, y coordinates
			float maxVal = Collections.max(values);
			float minVal = Collections.min(values);
			double xScale = ((double) myWidth - xOffset - SIDE_OFFSET) / (values.size() - 1);
			double yScale = ((double) myHeight - 2 * TOP_OFFSET) / (maxVal - minVal);
			List<Point> graphPoints = new ArrayList<Point>();
			for (int i = 0; i < values.size(); i++) {
				int x = (int) (i * xScale + xOffset);
				int y = (int) ((maxVal - values.get(i)) * yScale + TOP_OFFSET);
				graphPoints.add(new Point(x, y));
			}

			// draw background
			c.drawColor(Color.BLACK);

			// draw axes
			c.drawLine(xOffset, TOP_OFFSET, xOffset, bottomY + halfStroke, graphPaint);
			c.drawLine(xOffset + halfStroke, bottomY, myWidth - SIDE_OFFSET, bottomY, graphPaint);

			// draw hatch marks for x axis
			int xAxisWidth = myWidth - SIDE_OFFSET - xOffset;
			float xAxisHatchInterval = (float) xAxisWidth / X_HATCHES;
			for (float i = myWidth - SIDE_OFFSET - halfStroke; i > xOffset; i -= xAxisHatchInterval) {
				c.drawLine(i, bottomY + halfStroke, i, bottomY + halfStroke - HATCH_LENGTH, graphPaint);
			}

			// draw hatch marks for y axis
			int yAxisHeight = myHeight - TOP_OFFSET * 2;
			float yAxisHatchInterval = (float) yAxisHeight / Y_HATCHES;
			for (float i = TOP_OFFSET + halfStroke, j = 0; i < myHeight - TOP_OFFSET; i += yAxisHatchInterval, j++) {
				c.drawLine(xOffset - halfStroke, i, xOffset - halfStroke + HATCH_LENGTH, i, graphPaint);

				float yInterval = (maxVal - minVal) / Y_HATCHES;
				String yLabel = String.valueOf((int) Math.round(maxVal - (j * yInterval)));
				Rect rect = new Rect();
				graphPaint.getTextBounds(yLabel, 0, yLabel.length(), rect);
				c.drawText(yLabel, SIDE_OFFSET, i + rect.height() / 2, graphPaint);
			}

			// TODO refactor
			String yLabel = String.valueOf((int) Math.round(minVal));
			Rect rect = new Rect();
			graphPaint.getTextBounds(yLabel, 0, yLabel.length(), rect);
			c.drawText(yLabel, SIDE_OFFSET, myHeight - TOP_OFFSET, graphPaint);

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
