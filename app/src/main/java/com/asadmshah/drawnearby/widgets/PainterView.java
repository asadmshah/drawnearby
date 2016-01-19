package com.asadmshah.drawnearby.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.asadmshah.drawnearby.models.DrawEvent;

public class PainterView extends View {

    private static final float TOUCH_TOLERANCE = 4;

    private boolean drawingEnabled = false;
    private OnRemoteDrawListener onRemoteDrawListener;

    private Bitmap bitmap;
    private Canvas canvas;

    private final PathHelper pathLocal = new PathHelper();
    private final PathHelper pathRemote = new PathHelper();
    private final Paint paintLocal = new Paint();
    private final Paint paintRemote = new Paint();

    private DrawEvent currentDrawEvent;

    public PainterView(Context context) {
        this(context, null);
    }

    public PainterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PainterView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PainterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setFocusable(true);
        setFocusableInTouchMode(true);

        paintLocal.setAntiAlias(true);
        paintLocal.setDither(true);
        paintLocal.setColor(Color.BLACK);
        paintLocal.setStyle(Paint.Style.STROKE);
        paintLocal.setStrokeJoin(Paint.Join.ROUND);
        paintLocal.setStrokeCap(Paint.Cap.ROUND);
        paintLocal.setStrokeWidth(12);

        paintRemote.setAntiAlias(true);
        paintRemote.setDither(true);
        paintRemote.setColor(Color.BLACK);
        paintRemote.setStyle(Paint.Style.STROKE);
        paintRemote.setStrokeJoin(Paint.Join.ROUND);
        paintRemote.setStrokeCap(Paint.Cap.ROUND);
        paintRemote.setStrokeWidth(12);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (bitmap == null || bitmap.getWidth() != w || bitmap.getHeight() != h) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawPath(pathLocal.getPath(), paintLocal);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!drawingEnabled) return false;

        float x = event.getX();
        float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                currentDrawEvent = new DrawEvent(getColor(), getStrokeWidth());
                currentDrawEvent.positions.add(x);
                currentDrawEvent.positions.add(y);
                pathLocal.start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                currentDrawEvent.positions.add(x);
                currentDrawEvent.positions.add(y);
                pathLocal.move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                currentDrawEvent.positions.add(x);
                currentDrawEvent.positions.add(y);
                if (onRemoteDrawListener != null) {
                    onRemoteDrawListener.onRemoteDraw(currentDrawEvent);
                }
                canvas.drawPath(pathLocal.getPath(), paintLocal);
                pathLocal.reset();
                invalidate();
                break;
        }

        return true;
    }

    public void setDrawingEnabled(boolean enabled) {
        drawingEnabled = enabled;
    }

    public void setStrokeWidth(float strokeWidth) {
        paintLocal.setStrokeWidth(strokeWidth);
    }

    public float getStrokeWidth() {
        return paintLocal.getStrokeWidth();
    }

    public void setColor(int color) {
        paintLocal.setColor(color);
    }

    public int getColor() {
        return paintLocal.getColor();
    }

    public void setOnRemoteDrawListener(OnRemoteDrawListener onRemoteDrawListener) {
        this.onRemoteDrawListener = onRemoteDrawListener;
    }

    public void onRemoteTouchEvent(DrawEvent event) {
        paintRemote.setColor(event.color);
        paintRemote.setStrokeWidth(event.radius);
        pathRemote.start(event.positions.get(0), event.positions.get(1));

        for (int i = 2; i < event.positions.size()-2; i+=2) {
            pathRemote.move(event.positions.get(i), event.positions.get(i+1));
        }

        canvas.drawPath(pathRemote.getPath(), paintRemote);
        pathRemote.reset();
        postInvalidate();
    }

    private static final class PathHelper {

        private final Path path = new Path();
        private float px;
        private float py;

        public void start(float x, float y) {
            path.reset();
            path.moveTo(x, y);
            px = x;
            py = y;
        }

        public void move(float x, float y) {
            float dx = Math.abs(x - px);
            float dy = Math.abs(y - py);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                path.quadTo(px, py, (x + px) / 2, (y + py) / 2);
                px = x;
                py = y;
            }
        }

        public void reset() {
            path.reset();
        }

        public Path getPath() {
            return path;
        }
    }

    public interface OnRemoteDrawListener {
        void onRemoteDraw(DrawEvent event);
    }

}
