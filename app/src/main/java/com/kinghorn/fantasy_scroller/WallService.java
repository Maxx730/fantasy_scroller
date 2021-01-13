package com.kinghorn.fantasy_scroller;

import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

import androidx.preference.PreferenceManager;

public class WallService extends WallpaperService {
    private int WallWidth, WallHeight;

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
        private int framerate = 120;
        private final Handler handler = new Handler();
        private int PAN_SPEED, SHADE_AMOUNT;

        private ParallaxScroller bg, clouds, far, near;

        private final Runnable WallRunnable = new Runnable() {
            @Override
            public void run() {
                drawFrame();
            }
        };

        public WallEngine() {
            //Load preferences and elements here
            SharedPreferences prefs  = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);

            bg = new ParallaxScroller(R.drawable.bg, getApplicationContext(), WallHeight, 2);
            clouds = new ParallaxScroller(R.drawable.bg_clouds, getApplicationContext(), WallHeight, 5);
            far = new ParallaxScroller(R.drawable.bg_parallax_far, getApplicationContext(), WallHeight, 8);
            near = new ParallaxScroller(R.drawable.bg_parallax_near, getApplicationContext(), WallHeight, 10);
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
                    bg.drawScreen(canvas);
                    clouds.drawScreen(canvas);
                    far.drawScreen(canvas);
                    near.drawScreen(canvas);
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
    }
}