package com.kinghorn.fantasy_scroller;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.preference.PreferenceManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WallService extends WallpaperService {
    private int WallWidth, WallHeight, time = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        //Get Wallpaper Width and Height
        WallpaperManager manager = WallpaperManager.getInstance(getApplicationContext());
        WallWidth = manager.getDesiredMinimumWidth();
        WallHeight = manager.getDesiredMinimumHeight();
    }

    @Override
    public Engine onCreateEngine() {
        return new WallEngine();
    }

    class WallEngine extends Engine {
        private int framerate = 60;
        private final Handler handler = new Handler();
        private int PAN_SPEED, SHADE_AMOUNT;
        private SharedPreferences prefs;

        private ParallaxScroller bg, clouds, far, near, scroller;

        private final Runnable WallRunnable = new Runnable() {
            @Override
            public void run() {
                drawFrame();
            }
        };

        public WallEngine() {

            //Load preferences and elements here
            this.prefs  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if(visible) {
                SHADE_AMOUNT = Integer.parseInt(this.prefs.getString("SHADE_AMOUNT", "0"));
                PAN_SPEED = Integer.parseInt(this.prefs.getString("PAN_SPEED", "0"));
            }
            super.onVisibilityChanged(visible);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            bg = new ParallaxScroller(R.drawable.bg, getApplicationContext(), WallHeight, 2);
            clouds = new ParallaxScroller(R.drawable.bg_clouds, getApplicationContext(), WallHeight, 5);
            far = new ParallaxScroller(R.drawable.bg_parallax_far, getApplicationContext(), WallHeight, 8);
            near = new ParallaxScroller(R.drawable.bg_parallax_near, getApplicationContext(), WallHeight, 10);
            scroller = new ParallaxScroller(R.drawable.scroller, getApplicationContext(), WallHeight, 10);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            handler.removeCallbacks(WallRunnable);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder,
                                     int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            drawFrame();
        }

        void drawFrame() {
            final SurfaceHolder holder = getSurfaceHolder();
            Matrix mat = new Matrix();
            Canvas canvas = null;
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setStyle(Paint.Style.FILL);

            try {
                canvas = holder.lockCanvas();

                if(canvas != null) {
                    //Erase what was on the screen
                    canvas.drawRect(0, 0, WallWidth, WallHeight, p);
                    bg.drawScreen(canvas, PAN_SPEED);
                    clouds.drawScreen(canvas, PAN_SPEED);
                    far.drawScreen(canvas, PAN_SPEED);
                    near.drawScreen(canvas, PAN_SPEED);
                    scroller.drawScreen(canvas, PAN_SPEED);
                    drawShade(canvas);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            handler.removeCallbacks(WallRunnable);
            handler.postDelayed(WallRunnable, 1000 / framerate);
        }

        private void drawShade(Canvas can) {
            if (can != null) {
                Paint shadeP = new Paint();
                shadeP.setColor(Color.BLACK);
                shadeP.setAlpha(SHADE_AMOUNT);
                can.drawRect(0,0, WallWidth, WallHeight, shadeP);
            }
        }

        private int getShadeAmount() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
            String hour = sdf.format(new Date());
            int value = Integer.parseInt(hour);

            if (value >= 0 && value <= 12) {
                return Math.round(200 * (1.0f - (float) value / 24.0f));
            } else {
                return Math.round(200 * ((float) value / 24.0f));
            }
        }
    }
}