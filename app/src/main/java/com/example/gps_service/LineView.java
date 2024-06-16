package com.example.gps_service;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

// LineView.java
public class LineView extends View {
    private Paint paint;
    private List<LatLng> locations;

    public LineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        locations = new ArrayList<>();
    }

    public void setLocations(List<LatLng> locations) {
        this.locations = locations;
        invalidate(); // Wywołanie metody odświeżającej widok
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (locations.size() > 1) {
            Path path = new Path();
            LatLng first = locations.get(0);
            path.moveTo((float) first.latitude, (float) first.longitude);

            for (int i = 1; i < locations.size(); i++) {
                LatLng point = locations.get(i);
                path.lineTo((float) point.latitude, (float) point.longitude);
            }

            canvas.drawPath(path, paint);
        }
    }
}

