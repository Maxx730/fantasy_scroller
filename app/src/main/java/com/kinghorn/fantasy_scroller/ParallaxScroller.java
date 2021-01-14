package com.kinghorn.fantasy_scroller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class ParallaxScroller {
    private int x = 0, y = 0, speed = 10;
    private float scale = 5.0f;
    private Bitmap source;
    private Context con;

    public ParallaxScroller(int resource, Context con, int screenheight, int speed) {
        this.source = BitmapFactory.decodeResource(con.getResources(), resource);
        this.scale = ((float) screenheight / (float) this.source.getHeight());
        this.speed = speed;
        this.con = con;

        Matrix mat = new Matrix();
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        mat.postScale(this.scale, this.scale);
        paint.setDither(false);
        paint.setAntiAlias(false);
        paint.setFilterBitmap(false);

        Bitmap b = Bitmap.createBitmap(this.source,0,0,this.source.getWidth(), this.source.getHeight(), mat, true);
        this.source = b;
    }

    public void drawScreen(Canvas can, int speed) {
        can.drawBitmap(this.source, this.x, this.y, null);
        can.drawBitmap(this.source, this.x + this.source.getWidth(), this.y, null);
        this.x -= this.speed * speed;

        if (this.x <= -this.source.getWidth()) {
            this.x = 0;
        }
    }
}
