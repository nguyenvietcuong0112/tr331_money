package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class RoundedPieChartView extends View {
    private Paint paint;
    private Paint textPaint;
    private RectF rectF;
    private float[] values = {0, 0};
    private int[] colors;
    private float total;
    private float strokeWidth; // Độ dày của ring
    private String centerText = "";
    private boolean isAnimating = false;
    private float animationProgress = 0f;

    // Khoảng cách "thực" muốn thấy giữa các đoạn (đã trừ bù cap)
    private float desiredVisualGap = 10f; // độ

    public RoundedPieChartView(Context context) {
        super(context);
        init();
    }

    public RoundedPieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        strokeWidth = 20f * density;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND); // Tạo border radius
        paint.setStrokeWidth(strokeWidth);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#666666"));
        textPaint.setTextSize(36f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        rectF = new RectF();

        // Màu mặc định
        colors = new int[]{
                Color.parseColor("#4D9B29"), // Green for Income
                Color.parseColor("#F2444C")  // Red for Expense
        };
    }

    public void setData(float income, float expense) {
        this.values[0] = income;
        this.values[1] = expense;
        this.total = income + expense;

        // Bắt đầu animation
        startAnimation();
    }

    public void setCenterText(String text) {
        this.centerText = text;
        invalidate();
    }

    public void setColors(int[] colors) {
        this.colors = colors;
        invalidate();
    }

    public void setDesiredVisualGap(float gapDegrees) {
        this.desiredVisualGap = gapDegrees;
        invalidate();
    }

    private void startAnimation() {
        isAnimating = true;
        animationProgress = 0f;

        // Tạo animation đơn giản
        post(new Runnable() {
            @Override
            public void run() {
                if (animationProgress < 1f) {
                    animationProgress += 0.05f; // Tốc độ animation
                    invalidate();
                    postDelayed(this, 16); // ~60 FPS
                } else {
                    isAnimating = false;
                    animationProgress = 1f;
                    invalidate();
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (total == 0) {
            // Vẽ text "No data" khi không có dữ liệu
            textPaint.setTextSize(32f);
            textPaint.setColor(Color.GRAY);
            canvas.drawText("No data available", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);

        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = (size / 2f) - strokeWidth / 2f - 20; // Margin

        rectF.set(centerX - radius, centerY - radius,
                centerX + radius, centerY + radius);

        float startAngle = -90f; // Bắt đầu từ 12h

        // Tính phần bù do cap tròn (nửa đường kính stroke trên vòng tròn) chuyển thành độ
        float capOffsetDeg = (float) Math.toDegrees(Math.atan(strokeWidth / 2f / radius));
        float visualGapWithCap = desiredVisualGap + capOffsetDeg;

        for (int i = 0; i < values.length; i++) {
            if (values[i] <= 0) continue;

            float rawAngle = (values[i] / total) * 360f; // tổng góc tương ứng
            float sweepAngle = rawAngle - visualGapWithCap; // trừ khoảng gap đã bù cap

            if (sweepAngle <= 0) {
                // Nếu quá nhỏ không đủ chừa gap, bỏ qua vẽ để tránh artefact
                startAngle += rawAngle;
                continue;
            }

            float animatedSweep = sweepAngle * animationProgress;

            paint.setColor(colors[i % colors.length]);

            canvas.drawArc(rectF, startAngle, animatedSweep, false, paint);

            // Dịch startAngle bằng rawAngle (bao gồm gap), để phần tiếp theo đúng tỷ lệ
            startAngle += rawAngle;
        }

        // Vẽ text ở giữa nếu có và animation đủ
        if (!centerText.isEmpty() && animationProgress > 0.5f) {
            textPaint.setTextSize(36f);
            textPaint.setColor(Color.parseColor("#333333"));

            String[] lines = centerText.split("\n");
            float textHeight = textPaint.getTextSize();
            float totalTextHeight = lines.length * textHeight;
            float startY = centerY - totalTextHeight / 2f + textHeight / 2f;

            for (int i = 0; i < lines.length; i++) {
                canvas.drawText(lines[i], centerX, startY + i * textHeight, textPaint);
            }
        }
    }
}
