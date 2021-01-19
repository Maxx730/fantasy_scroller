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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
        private int framerate = 30, fadeValue = 255;
        private final Handler handler = new Handler();
        private int PAN_SPEED, SHADE_AMOUNT, FPS = 30;
        private SharedPreferences prefs;
        private boolean UPDATING = true;
        private ArrayList<ParallaxScroller> resources;

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
                FPS = Integer.parseInt(this.prefs.getString("FPS", "30"));
                UPDATING = true;
                resources = getResources();
                fadeValue = 255;
            } else {
                resources.clear();
                UPDATING = false;
            }
            super.onVisibilityChanged(visible);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            resources = new ArrayList<>();
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

            if(UPDATING) {
                drawFrame();
            }
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

                    for(ParallaxScroller scroller : resources) {
                        scroller.drawScreen(canvas, PAN_SPEED);
                    }

                    drawShade(canvas);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }

            handler.removeCallbacks(WallRunnable);
            handler.postDelayed(WallRunnable, 1000 / FPS);
        }

        private void drawShade(Canvas can) {
            if (can != null) {
                Paint shadeP = new Paint();
                shadeP.setColor(Color.BLACK);
                shadeP.setAlpha(SHADE_AMOUNT);
                can.drawRect(0,0, WallWidth, WallHeight, shadeP);

                if (fadeValue > 0) {
                    drawFade(can);
                }
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

        private ArrayList<ParallaxScroller> getResources() {
            ArrayList<ParallaxScroller> resources = new ArrayList<>();
            //resources.add(new ParallaxScroller(R.drawable.bg, getApplicationContext(), WallHeight, 2));
            //resources.add(new ParallaxScroller(R.drawable.bg_clouds, getApplicationContext(), WallHeight, 5));
            //resources.add(new ParallaxScroller(R.drawable.bg_parallax_far, getApplicationContext(), WallHeight, 8));
            //resources.add(new ParallaxScroller(R.drawable.bg_parallax_near, getApplicationContext(), WallHeight, 10));
            resources.add(new ParallaxScroller(R.drawable.scroller, getApplicationContext(), WallHeight, 10));
            return resources;
        }

        private void drawFade(Canvas canvas) {
            int fadeAmount = fadeValue / FPS;
            fadeValue -= fadeAmount;

            Paint p = new Paint();
            p.setColor(Color.BLACK);
            p.setStyle(Paint.Style.FILL);
            p.setAlpha(fadeValue);
            canvas.drawRect(0, 0, WallWidth, WallHeight, p);
        }
    }
}