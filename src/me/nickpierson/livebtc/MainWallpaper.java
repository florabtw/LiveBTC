package me.nickpierson.livebtc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import me.nickpierson.livebtc.prices.PriceHandler;
import me.nickpierson.livebtc.utils.PrefsHelper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;

public class MainWallpaper extends WallpaperService {

	@Override
	public Engine onCreateEngine() {
		return new MyEngine();
	}

	class MyEngine extends Engine implements OnSharedPreferenceChangeListener, Observer {

		private PrefsHelper prefsHelper;
		private PriceHandler priceHandler;
		private Paint graphPaint, currPricePaint;

		private int myWidth, myHeight, STATUS_BAR_HEIGHT, TOP_MARGIN, SIDE_MARGIN, BOTTOM_MARGIN, TICK_LENGTH, X_LABEL_HEIGHT, CURR_PRICE_SPACE,
				CURR_PRICE_PADDING, TIME_INTERVAL_M, NUM_POINTS, STROKE_WIDTH, BACKGROUND_COLOR;

		private final int X_TICKS = 4;
		private final int Y_TICKS = 4;
		private final int LBL_PADDING = 10;

		public MyEngine() {
			graphPaint = new Paint();
			graphPaint.setStyle(Paint.Style.FILL);
			graphPaint.setAntiAlias(true);

			currPricePaint = new Paint();
			currPricePaint.setAntiAlias(true);

			DisplayMetrics metrics = getResources().getDisplayMetrics();
			myWidth = metrics.widthPixels;
			myHeight = metrics.heightPixels;

			int combinedWeight = myWidth + myHeight;

			SIDE_MARGIN = combinedWeight / 250;
			TICK_LENGTH = combinedWeight / 150;
			CURR_PRICE_PADDING = combinedWeight / 140;
			STROKE_WIDTH = combinedWeight / 750;

			// best if stroke width is even
			if (STROKE_WIDTH > 1 && STROKE_WIDTH % 2 == 1) {
				STROKE_WIDTH++;
			}

			graphPaint.setStrokeWidth(STROKE_WIDTH);

			int graphTextSize = combinedWeight / 100;
			int currPriceTextSize = combinedWeight / 30;

			graphPaint.setTextSize(graphTextSize);
			currPricePaint.setTextSize(currPriceTextSize);

			prefsHelper = new PrefsHelper(MainWallpaper.this);
			prefsHelper.registerOnSharedPreferenceChangeListener(this);

			TIME_INTERVAL_M = prefsHelper.getTimeInterval();
			NUM_POINTS = prefsHelper.getNumberOfPoints();
			BACKGROUND_COLOR = prefsHelper.getBackgroundColor();
			int lineColor = prefsHelper.getLineColor();

			graphPaint.setColor(lineColor);
			currPricePaint.setColor(lineColor);
		}

		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);

			updateMeasurements();

			priceHandler = new PriceHandler(MainWallpaper.this);
			priceHandler.addObserver(this);
			priceHandler.beginUpdating();
		};

		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);

			updateMeasurements();

			draw(priceHandler.getLatestPrices());
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(PrefsHelper.BOTTOM_MARGIN_KEY)) {
				BOTTOM_MARGIN = prefsHelper.getBottomMargin(myHeight);
			} else if (key.equals(PrefsHelper.TOP_MARGIN_KEY)) {
				TOP_MARGIN = prefsHelper.getTopMargin(myHeight);
			} else if (key.equals(PrefsHelper.TIME_INTERVAL_KEY)) {
				TIME_INTERVAL_M = prefsHelper.getTimeInterval();
			} else if (key.equals(PrefsHelper.NUM_POINTS_KEY)) {
				NUM_POINTS = prefsHelper.getNumberOfPoints();
			} else if (key.equals(PrefsHelper.BASIC_BACKGROUND_KEY)) {
				// TODO refactor
				BACKGROUND_COLOR = prefsHelper.getBackgroundColor();
			} else if (key.equals(PrefsHelper.BASIC_LINE_KEY)) {
				int lineColor = prefsHelper.getLineColor();
				graphPaint.setColor(lineColor);
				currPricePaint.setColor(lineColor);
			} else if (key.equals(PrefsHelper.USE_ADVANCED_COLORS_KEY)) {
				BACKGROUND_COLOR = prefsHelper.getBackgroundColor();
				int lineColor = prefsHelper.getLineColor();
				graphPaint.setColor(lineColor);
				currPricePaint.setColor(lineColor);
			} else if (key.equals(PrefsHelper.ADVANCED_LINE_KEY)) {
				int lineColor = prefsHelper.getLineColor();
				graphPaint.setColor(lineColor);
				currPricePaint.setColor(lineColor);
			} else if (key.equals(PrefsHelper.ADVANCED_BACKGROUND_KEY)) {
				BACKGROUND_COLOR = prefsHelper.getBackgroundColor();
			} else {
				// don't draw for other preference changes
				return;
			}

			draw(priceHandler.getLatestPrices());
		}

		private void updateMeasurements() {
			DisplayMetrics metrics = getResources().getDisplayMetrics();
			myWidth = metrics.widthPixels;
			myHeight = metrics.heightPixels;

			STATUS_BAR_HEIGHT = 0;
			int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
			if (resourceId > 0) {
				STATUS_BAR_HEIGHT = getResources().getDimensionPixelSize(resourceId);
			}

			TOP_MARGIN = prefsHelper.getTopMargin(myHeight);
			BOTTOM_MARGIN = prefsHelper.getBottomMargin(myHeight);

			Rect rect = new Rect();
			graphPaint.getTextBounds("0m", 0, 2, rect);
			X_LABEL_HEIGHT = rect.height();

			currPricePaint.getTextBounds("000.00", 0, 6, rect);
			CURR_PRICE_SPACE = rect.height();
		};

		@Override
		public void update(Observable observable, Object o) {
			if (observable == priceHandler) {
				draw(priceHandler.getLatestPrices());
			}
		}

		void draw(ArrayList<Float> prices) {
			final SurfaceHolder holder = getSurfaceHolder();

			if (prices != null) {
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
			}
		}

		void drawChart(Canvas c, ArrayList<Float> prices) {
			float maxVal = Collections.max(prices);
			float minVal = Collections.min(prices);

			int halfStroke = STROKE_WIDTH / 2;
			int yAxisLabelWidth = (int) graphPaint.measureText(String.valueOf(Math.round(maxVal)));
			int yStartGap = STATUS_BAR_HEIGHT + TOP_MARGIN;
			int yChartStart = yStartGap + CURR_PRICE_SPACE + CURR_PRICE_PADDING;
			int yChartEnd = myHeight - BOTTOM_MARGIN - X_LABEL_HEIGHT - LBL_PADDING;
			int xChartStart = SIDE_MARGIN + yAxisLabelWidth + LBL_PADDING;
			int yAxisHeight = yChartEnd - yChartStart;
			int xAxisWidth = myWidth - SIDE_MARGIN - xChartStart;

			// turn values into x, y coordinates
			float xScale = ((float) xAxisWidth) / (prices.size() - 1);
			float yScale = ((float) yAxisHeight) / (maxVal - minVal);
			List<Point> graphPoints = new ArrayList<Point>();
			for (int i = 0; i < prices.size(); i++) {
				int x = (int) (i * xScale + xChartStart);
				int y = (int) ((maxVal - prices.get(i)) * yScale + yChartStart);
				graphPoints.add(new Point(x, y));
			}

			// draw background
			c.drawColor(BACKGROUND_COLOR);

			// draw axes
			c.drawLine(xChartStart, yChartStart, xChartStart, yChartEnd + halfStroke, graphPaint);
			c.drawLine(xChartStart + halfStroke, yChartEnd, myWidth - SIDE_MARGIN, yChartEnd, graphPaint);

			// draw tick marks, labels for x axis
			float xTickInterval = (float) xAxisWidth / X_TICKS;
			String[] xLabels = DrawHelper.getXAxisLabels(TIME_INTERVAL_M, NUM_POINTS, MainWallpaper.this);
			DrawHelper.drawXLabel(c, xLabels[xLabels.length - 1], graphPaint, xChartStart, myHeight - BOTTOM_MARGIN, DrawHelper.Position.PAST);
			for (int i = 1; i < X_TICKS; i++) {
				float xPos = (myWidth - SIDE_MARGIN) - (i * xTickInterval);
				c.drawLine(xPos, yChartEnd + halfStroke, xPos, yChartEnd + halfStroke - TICK_LENGTH, graphPaint);

				DrawHelper.drawXLabel(c, xLabels[i], graphPaint, xPos, myHeight - BOTTOM_MARGIN, DrawHelper.Position.CENTER);
			}
			c.drawLine(myWidth - SIDE_MARGIN, yChartEnd + halfStroke, myWidth - SIDE_MARGIN, yChartEnd + halfStroke - TICK_LENGTH, graphPaint);
			DrawHelper.drawXLabel(c, xLabels[0], graphPaint, myWidth - SIDE_MARGIN, myHeight - BOTTOM_MARGIN, DrawHelper.Position.BEFORE);

			// draw tick marks, labels for y axis
			float yTickInterval = (float) yAxisHeight / Y_TICKS;
			float yValueInterval = (maxVal - minVal) / Y_TICKS;
			c.drawLine(xChartStart - halfStroke, yChartStart, xChartStart + TICK_LENGTH, yChartStart, graphPaint);
			DrawHelper.drawYLabel(c, String.valueOf(Math.round(maxVal)), graphPaint, SIDE_MARGIN, yChartStart, DrawHelper.Position.PAST);
			for (int i = 1; i < Y_TICKS; i++) {
				float yPos = yChartStart + (i * yTickInterval);
				c.drawLine(xChartStart - halfStroke, yPos, xChartStart + TICK_LENGTH, yPos, graphPaint);

				String yLabel = String.valueOf((int) Math.round(maxVal - (i * yValueInterval)));
				DrawHelper.drawYLabel(c, yLabel, graphPaint, SIDE_MARGIN, yPos, DrawHelper.Position.CENTER);
			}
			DrawHelper.drawYLabel(c, String.valueOf(Math.round(minVal)), graphPaint, SIDE_MARGIN, yChartEnd, DrawHelper.Position.BEFORE);

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

			// draw current price units
			String units = priceHandler.getCurrency() + "/BTC";
			Rect unitsBounds = new Rect();
			graphPaint.getTextBounds(units, 0, units.length(), unitsBounds);
			int unitsX = myWidth - SIDE_MARGIN - unitsBounds.width();
			c.drawText(units, unitsX, yStartGap + CURR_PRICE_SPACE, graphPaint);

			// draw current price
			String currPrice = String.format(Locale.US, "%.02f", prices.get(prices.size() - 1));
			int currPriceX = unitsX - (int) currPricePaint.measureText(currPrice);
			c.drawText(currPrice, currPriceX, yStartGap + CURR_PRICE_SPACE, currPricePaint);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			priceHandler.stopUpdating();
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			priceHandler.stopUpdating();
		}
	}
}