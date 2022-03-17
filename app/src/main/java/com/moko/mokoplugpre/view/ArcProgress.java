package com.moko.mokoplugpre.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.utils.Utils;

import androidx.core.content.ContextCompat;

/**
 * The Class ArcProgress.
 */
public class ArcProgress extends View {

    /**
     * The context
     */
    private Context mContext;

    /**
     * The color of the progress ring
     */
    private Paint circleColor;

    /**
     * the color of the inside circle. Acts as background color
     */
    private Paint innerColor;

    /**
     * The progress circle ring background
     */
    private Paint circleRing;

    /**
     * The angle of progress
     */
    private float angle = 0.0f;

    /**
     * The start angle
     */
    private int startAngle = 135;

    /**
     * The width of the progress ring
     */
    private int barWidth = 50;

    /**
     * The width of the view
     */
    private int width;

    /**
     * The height of the view
     */
    private int height;

    /**
     * The maximum progress amount
     */
    private int maxProgress = 360;

    /**
     * The current progress
     */
    private float progress;

    /**
     * The radius of the inner circle
     */
    private float innerRadius;

    /**
     * The radius of the outer circle
     */
    private float outerRadius;

    /**
     * The circle's center X coordinate
     */
    private float cx;

    /**
     * The circle's center Y coordinate
     */
    private float cy;

    /**
     * The left bound for the circle RectF
     */
    private float left;

    /**
     * The right bound for the circle RectF
     */
    private float right;

    /**
     * The top bound for the circle RectF
     */
    private float top;

    /**
     * The bottom bound for the circle RectF
     */
    private float bottom;

    /**
     * The rectangle containing our circles and arcs.
     */
    private RectF rect = new RectF();


    public ArcProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        barWidth = Utils.dip2px(mContext, 12);
        int intervalsWidth = Utils.dip2px(mContext, 2);

        circleColor = new Paint();
        circleColor.setColor(ContextCompat.getColor(mContext, R.color.grey_cbcbcc)); // Set default
        circleColor.setAntiAlias(true);
        circleColor.setStrokeWidth(barWidth);
        circleColor.setStyle(Paint.Style.STROKE);
        circleColor.setPathEffect(new DashPathEffect(new float[]{intervalsWidth, intervalsWidth}, 0));

        innerColor = new Paint();
        innerColor.setColor(ContextCompat.getColor(mContext, R.color.blue_2681ff)); // Set default background color to
        innerColor.setAntiAlias(true);
        innerColor.setStrokeWidth(barWidth);
        innerColor.setStyle(Paint.Style.STROKE);
        innerColor.setPathEffect(new DashPathEffect(new float[]{intervalsWidth, intervalsWidth}, 0));

        circleRing = new Paint();
        circleRing.setColor(Color.WHITE);
        circleRing.setAntiAlias(true);
        circleRing.setStyle(Paint.Style.FILL);
        circleRing.setShadowLayer(barWidth, 0, 0, Color.parseColor("#552681ff"));
    }

    public ArcProgress(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcProgress(Context context) {
        this(context, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onMeasure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();// Get View Width
        height = getMeasuredHeight();// Get View Height

        int size = (width > height) ? height : width; // Choose the smaller
        // between width and
        // height to make a
        // square

        cx = width / 2; // Center X for circle
        cy = height / 2; // Center Y for circle
        outerRadius = size / 2 - barWidth / 2;
        innerRadius = outerRadius - barWidth * 3 / 2; // Radius of the inner circle

        left = cx - outerRadius; // Calculate left bound of our rect
        right = cx + outerRadius;// Calculate right bound of our rect
        top = cy - outerRadius;// Calculate top bound of our rect
        bottom = cy + outerRadius;// Calculate bottom bound of our rect
        rect.set(left, top, right, bottom); // assign size to rect
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(cx, cy, innerRadius, circleRing);
        canvas.drawArc(rect, startAngle, 270, false, circleColor);
        canvas.drawArc(rect, startAngle, angle, false, innerColor);
        super.onDraw(canvas);
    }

    /**
     * Sets the progress.
     *
     * @param progress the new progress
     */
    public void setProgress(float progress) {
        if (this.progress != progress && progress <= maxProgress) {
            this.progress = progress;
            float newPercent = (this.progress * 100) / this.maxProgress;
            float newAngle = (newPercent * 270) / 100;
            this.angle = newAngle;
            invalidate();
        }
    }
}
