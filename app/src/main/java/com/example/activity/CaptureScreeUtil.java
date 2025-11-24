package com.example.activity;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

class CaptureScreeUtil{

    private static final String TAG = "TestCaptureScreen";

    public static Bitmap capture(int width,int height,View view) {
        // TODO Auto-generated method stub
        Log.i(TAG,"capture");

        Bitmap bitmap = Bitmap.createBitmap(width, height,Bitmap.Config.RGB_565);
        bitmap.eraseColor(Color.WHITE);

        Canvas canvas = new Canvas(bitmap);
        final int left = view.getScrollX();
        final int top = view.getScrollY();
        int state = canvas.save();
        canvas.translate(-left, -top);
        float scale = width / (float) view.getWidth();
        canvas.scale(scale, scale, left, top);
        view.draw(canvas);
        canvas.restoreToCount(state);

        // manually anti-alias the edges for the tilt
        Paint sAlphaPaint = new Paint();
        //sAlphaPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        sAlphaPaint.setColor(Color.TRANSPARENT);

        canvas.drawRect(0, 0, 1, height, sAlphaPaint);
        canvas.drawRect(width - 1, 0, width,
                height, sAlphaPaint);
        canvas.drawRect(0, 0, width, 1, sAlphaPaint);
        canvas.drawRect(0, height - 1, width,
                height, sAlphaPaint);
        canvas.setBitmap(null);

        return bitmap;
    }
}
