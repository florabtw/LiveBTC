package me.nickpierson.livebtc;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class MainWallpaper extends WallpaperService {

	@Override
	public Engine onCreateEngine() {
		return new MyEngine();
	}

	class MyEngine extends Engine {

		private Paint paint;
		private final Handler handler = new Handler();
		private boolean isVisible;

		private final Runnable runnable = new Runnable() {
			public void run() {
				draw();
			}
		};

		public MyEngine() {
			paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setTextSize(40);
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
			final Rect frame = holder.getSurfaceFrame();
			final int width = frame.width();
			final int height = frame.height();

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				if (c != null) {
					// draw something
					c.drawText("Is this working?", 0, 10, 10, 10, paint);
					c.drawCircle(width / 2, height / 2, 20, paint);
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);
			}

			handler.removeCallbacks(runnable);
			if (isVisible) {
				handler.postDelayed(runnable, 1000 / 25);
			}
		}
	}
}
