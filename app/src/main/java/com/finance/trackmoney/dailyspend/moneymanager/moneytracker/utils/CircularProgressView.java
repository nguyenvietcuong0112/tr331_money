package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircularProgressView extends View {
    private Paint paint, textPaint;
    private RectF rectF;
    private int progress = 75;
    private int progressColor = Color.parseColor("#2A2A2A");
    private int progressColorBackground = Color.parseColor("#84AEDD");

    private boolean showRemainingText = true;


    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeWidth(70f);


        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.parseColor("#FFFFFF"));
        textPaint.setTextSize(40f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        rectF = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int padding = (int) paint.getStrokeWidth();
        rectF.set(padding, padding, w - padding, h - padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(progressColorBackground);
        canvas.drawArc(rectF, 0, 360, false, paint);

        paint.setColor(progressColor);
        float sweepAngle = (360f * progress) / 100;
        canvas.drawArc(rectF, -90, sweepAngle, false, paint);
        if (showRemainingText) {
            textPaint.setColor(Color.parseColor("#FFFFFF"));
            canvas.drawText("", getWidth() / 2, getHeight() / 2 - 20, textPaint);
            canvas.drawText(progress + "%", getWidth() / 2, getHeight() / 2 + 40, textPaint);
        } else {
            textPaint.setTextSize(50f);
            paint.setStrokeWidth(20f);
            textPaint.setColor(Color.parseColor("#FFFFFF"));
            canvas.drawText(progress + "%", getWidth() / 2, getHeight() / 2 + 20, textPaint);

        }


    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    public void setProgressColor(int color) {
        this.progressColor = color;
        invalidate();
    }

    public void setShowRemainingText(boolean show) {
        this.showRemainingText = show;
        invalidate();
    }

}