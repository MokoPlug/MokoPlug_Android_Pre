package com.moko.mokoplugpre.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.moko.mokoplugpre.R;
import com.moko.mokoplugpre.entity.EnergyInfoData;
import com.moko.mokoplugpre.utils.Utils;

import java.util.List;

import androidx.annotation.Nullable;

public class ScrollChartView extends View {
    private int width;
    private int height;
    /**
     * 坐标轴线宽度
     */
    private int coordinateAxisWidth;
    /**
     * x轴下方文字大小
     */
    private int itemNameTextSize;
    /**
     * 柱状图之间的间距
     */
    private int itemInterval;
    /**
     * 柱状图上方文字大小
     */
    private int histogramValueTextSize;
    /**
     * 柱状图和顶部的距离
     */
    private int chartPaddingTop;
    /**
     * x轴下方文字距离x轴的间距
     */
    private int distanceFormItemNameToAxis;
    /**
     * 柱状图上方文字距离柱状图的距离
     */
    private int distanceFromValueToHistogram;
    /**
     * 柱状图最大高度
     */
    private int maxHistogramHeight;
    /**
     * 轴线画笔
     */
    private Paint coordinateAxisPaint;
    /**
     * 柱子画笔
     */
    private Paint histogramPaint;
    /**
     * 柱子上方文字画笔
     */
    private Paint histogramValuePaint;
    /**
     * x轴下方文字画笔
     */
    private Paint itemNamePaint;
    /**
     * 直方图绘制区域
     */
    private Rect histogramPaintRect;
    private int coordinateAxisColor;
    private int histogramItemColor;

    private List<EnergyInfoData> dataList;
    /**
     * 一组分为两个柱子，两个柱子分别为百分比和分数，这个是用来记录最大百分比和最大分数的
     */
//    private SparseArray<Float> childMaxValueArray;//SparseArray就是HashMap
    private float maxValue;

    /**
     * 存储组内直方图shader color，例如，每组有3个直方图，该SparseArray就存储3个相对应的shader color
     */
//    private SparseArray<int[]> histogramShaderColorArray;

    /**
     * 直方图表视图总宽度（第一个柱状图到最后一个柱状图的距离）
     */
    private int histogramContentWidth;
    /**
     * 一根柱子的宽度
     */
    private int histogramItemWidth;
    /**
     * 顶部半圆半径
     */
    private int histogramItemHeaderRadius;
    /**
     * 小数点位数
     */
//    private int histogramValueDecimalCount;
    private int histogramValueTextColor;
    private Paint.FontMetrics histogramValueFontMetrics;
    private int itemNameTextColor;
    private Paint.FontMetrics itemNameFontMetrics;

    private VelocityTracker velocityTracker;
    private Scroller scroller;
    private int minimumVelocity;
    private int maximumVelocity;
    private float lastX;

    public void setDataList(List<EnergyInfoData> dataList) {
        this.dataList = dataList;
//        if (childMaxValueArray == null) {
//            childMaxValueArray = new SparseArray<>();
//        } else {
//            childMaxValueArray.clear();
//        }
        histogramContentWidth = 0;
        for (EnergyInfoData infoData : dataList) {
//            List<MultiGroupHistogramChildData> childDataList = infoData.getChildDataList();
//            if (childDataList != null && childDataList.size() > 0) {
//                for (int i = 0; i < childDataList.size(); i++) {
            histogramContentWidth += histogramItemWidth;
//                    Float childMaxValue = childMaxValueArray.get(i);
//                    if (childMaxValue == null || childMaxValue < childDataList.get(i).getValue()) {
//                        childMaxValueArray.put(i, childDataList.get(i).getValue());
//                    }
//                }
            if (infoData.value > maxValue) {
                maxValue = infoData.value;
            }
            histogramContentWidth += itemInterval;
//            }
        }
        histogramContentWidth -= itemInterval;
    }

    /**
     * 设置组内直方图颜色
     */
//    public void setHistogramColor(int[]... colors) {
//        if (colors != null && colors.length > 0) {
//            if (histogramShaderColorArray == null) {
//                histogramShaderColorArray = new SparseArray<>();
//            } else {
//                histogramShaderColorArray.clear();
//            }
//            for (int i = 0; i < colors.length; i++) {
//                histogramShaderColorArray.put(i, colors[i]);
//            }
//        }
//    }
    public ScrollChartView(Context context) {
        this(context, null);
    }

    public ScrollChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScrollChartView);
        coordinateAxisWidth = typedArray.getDimensionPixelSize(R.styleable.ScrollChartView_coordinateAxisWidth, Utils.dip2px(getContext(), 1));
        // 坐标轴线颜色
        coordinateAxisColor = typedArray.getColor(R.styleable.ScrollChartView_coordinateAxisColor, Color.parseColor("#2681ff"));
        // x轴下方文字颜色
        itemNameTextColor = typedArray.getColor(R.styleable.ScrollChartView_itemNameTextColor, Color.parseColor("#808080"));
        itemNameTextSize = typedArray.getDimensionPixelSize(R.styleable.ScrollChartView_itemNameTextSize, Utils.dip2px(getContext(), 14));
//        groupInterval = typedArray.getDimensionPixelSize(R.styleable.ScrollChartView_groupInterval, Utils.dip2px(getContext(), 30));
        // 直方图数值文本颜色
        histogramValueTextSize = typedArray.getDimensionPixelSize(R.styleable.ScrollChartView_histogramValueTextSize, Utils.dip2px(getContext(), 14));
        chartPaddingTop = typedArray.getDimensionPixelSize(R.styleable.ScrollChartView_chartPaddingTop, Utils.dip2px(getContext(), 10));
        distanceFormItemNameToAxis = typedArray.getDimensionPixelSize(R.styleable.ScrollChartView_distanceFormItemNameToAxis, Utils.dip2px(getContext(), 15));
        distanceFromValueToHistogram = typedArray.getDimensionPixelSize(R.styleable.ScrollChartView_distanceFromValueToHistogram, Utils.dip2px(getContext(), 10));
        histogramItemWidth = typedArray.getDimensionPixelSize(R.styleable.ScrollChartView_histogramItemWidth, Utils.dip2px(getContext(), 12));
        histogramItemColor = typedArray.getColor(R.styleable.ScrollChartView_histogramItemColor, Color.parseColor("#2681ff"));
        histogramItemHeaderRadius = histogramItemWidth / 2;
//        histogramValueDecimalCount = typedArray.getInt(R.styleable.ScrollChartView_histogramValueDecimalCount, 0);
        // 直方图数值文本颜色
        histogramValueTextColor = typedArray.getColor(R.styleable.ScrollChartView_histogramValueTextColor, Color.parseColor("#2681ff"));
        // 底部小组名称字体颜色
        typedArray.recycle();
        initPaint();
        init();

    }

    private void init() {
        histogramPaintRect = new Rect();
        scroller = new Scroller(getContext(), new LinearInterpolator());
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        minimumVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    private void initPaint() {
        coordinateAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        coordinateAxisPaint.setStyle(Paint.Style.FILL);
        coordinateAxisPaint.setStrokeWidth(coordinateAxisWidth);
        coordinateAxisPaint.setColor(coordinateAxisColor);

        histogramPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        histogramPaint.setStyle(Paint.Style.FILL);
        histogramPaint.setColor(histogramItemColor);

        histogramValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        histogramValuePaint.setTextSize(histogramValueTextSize);
        histogramValuePaint.setColor(histogramValueTextColor);
        histogramValueFontMetrics = histogramValuePaint.getFontMetrics();

        itemNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        itemNamePaint.setTextSize(itemNameTextSize);
        itemNamePaint.setColor(itemNameTextColor);
        itemNameFontMetrics = itemNamePaint.getFontMetrics();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        itemInterval = (width - histogramItemWidth * 7) / 7;
        maxHistogramHeight = height - coordinateAxisWidth - itemNameTextSize
                - histogramValueTextSize - chartPaddingTop - distanceFormItemNameToAxis
                - distanceFromValueToHistogram;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (width == 0 || height == 0) {
            return;
        }
        /** 注意这里要加上getScrollX是因为在滑动过程中使坐标轴看起来不变，其实原先的坐标轴已经被滑走了*/
        int scrollX = getScrollX();
        int axisBottom = height - itemNameTextSize - distanceFormItemNameToAxis - coordinateAxisWidth / 2;
        /** 这里不直接从0开始画，是因为从0开始画轴线画笔宽度的一半会看不见，所以从coordinateAxisWidth/2开始画全部能看见 */
        //画y轴
//        canvas.drawLine(coordinateAxisWidth / 2 + scrollX, 0, coordinateAxisWidth / 2 + scrollX, axisBottom, coordinateAxisPaint);
        //画x轴
        canvas.drawLine(scrollX, axisBottom, scrollX + width, axisBottom, coordinateAxisPaint);
        //画柱子
        if (dataList != null && dataList.size() > 0) {
            int xAxisOffset = itemInterval / 2;
            for (int i = 0; i < dataList.size(); i++) {
                EnergyInfoData infoData = dataList.get(i);
//                List<MultiGroupHistogramChildData> childDataList = groupData.getChildDataList();
//                if (childDataList != null && childDataList.size() > 0) {
                int groupWidth = 0;
//                    for (int i = 0; i < childDataList.size(); i++) {
//                        MultiGroupHistogramChildData childData = childDataList.get(i);
                histogramPaintRect.left = xAxisOffset;
                histogramPaintRect.right = histogramPaintRect.left + histogramItemWidth;
                int childHistogramHeight;
                if (infoData.value <= 0) {
                    childHistogramHeight = 0;
                } else {
                    childHistogramHeight = (int) (infoData.value / maxValue * maxHistogramHeight);
                }
                histogramPaintRect.top = height - childHistogramHeight - coordinateAxisWidth - itemNameTextSize - distanceFormItemNameToAxis;
                histogramPaintRect.bottom = histogramPaintRect.top + childHistogramHeight;
                //设置颜色（渐变色）
//                        int[] histogramShaderColor = histogramShaderColorArray.get(i);
//                        LinearGradient shader = null;
//                        if (histogramShaderColor != null && histogramShaderColor.length > 0) {
//                            shader = getHistogramShader(histogramPaintRect.left, histogramPaintRect.right, histogramPaintRect.top, histogramPaintRect.bottom, histogramShaderColor);
//                        }
//                        histogramPaint.setShader(shader);
                canvas.drawRect(histogramPaintRect, histogramPaint);
                //画柱状图上方文字
//                        String childHistogramHeightValue = StringUtil.NumericScaleByFloor(String.valueOf(childData.getValue()), histogramValueDecimalCount) + childData.getSuffix();
                float valueTextX = xAxisOffset + (histogramItemWidth - histogramValuePaint.measureText(infoData.valueStr)) / 2;
                float valueTextY = histogramPaintRect.top - distanceFromValueToHistogram + (histogramValueFontMetrics.bottom) / 2;
                canvas.drawText(infoData.valueStr, valueTextX, valueTextY, histogramValuePaint);
                int deltaX = i < dataList.size() - 1 ? histogramItemWidth + itemInterval : histogramItemWidth;
                groupWidth += deltaX;
                xAxisOffset += i == dataList.size() - 1 ? deltaX + itemInterval : deltaX;
//                    }

                //画x轴下方文字
                String groupName = infoData.index;
                float groupNameTextWidth = itemNamePaint.measureText(groupName);
                float groupNameTextX = xAxisOffset - groupWidth - itemInterval + (groupWidth - groupNameTextWidth) / 2;
                float groupNameTextY = (height - itemNameFontMetrics.bottom / 2);
                canvas.drawText(groupName, groupNameTextX, groupNameTextY, itemNamePaint);
//                }
            }
        }

    }

    private LinearGradient getHistogramShader(float x0, float y0, float x1, float y1, int[] colors) {
        return new LinearGradient(x0, y0, x1, y1, colors, null, Shader.TileMode.CLAMP);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //初始化速度追踪器
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //惯性滑动过程中点击停止滑动
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                lastX = event.getX();
                return true;
            case MotionEvent.ACTION_MOVE:
                int distanceX = (int) (event.getX() - lastX);
                lastX = event.getX();
                if (distanceX > 0 && canScrollHorizontally(-1)) {
                    //右滑并且可以水平滑动
                    scrollBy(-Math.min(getMaxCanScrollX(-1), distanceX), 0);
                } else if (distanceX < 0 && canScrollHorizontally(1)) {
                    //左滑并且可以水平滑动
                    scrollBy(Math.min(getMaxCanScrollX(1), -distanceX), 0);
                }

                break;
            case MotionEvent.ACTION_UP:
                //松开手指后惯性滑动
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                int xVelocity = (int) velocityTracker.getXVelocity();
                if (Math.abs(xVelocity) > minimumVelocity) {
                    scroller.fling(getScrollX(), getScrollY(), -xVelocity, 0, 0, histogramContentWidth - width, 0, 0);
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), 0);
        }
    }

    /**
     * 判断是否可以水平方向滑动
     *
     * @param derection 正数左滑，负数右滑
     * @return
     */
    public boolean canScrollHorizontally(int derection) {
        if (derection > 0) {
            return histogramContentWidth - width - getScrollX() > 0;
        } else {
            return getScrollX() > 0;
        }
    }

    /**
     * 根据滑动方向获取最大可滑动距离
     *
     * @param dereation 正数左滑，负数右滑
     * @return
     */
    public int getMaxCanScrollX(int dereation) {
        if (dereation > 0) {
            return histogramContentWidth - width - getScrollX() > 0 ? histogramContentWidth - width - getScrollX() : 0;
        } else if (dereation < 0) {
            return getScrollX();
        }
        return 0;
    }
}
