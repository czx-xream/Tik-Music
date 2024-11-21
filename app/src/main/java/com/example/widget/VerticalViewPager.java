package com.example.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.viewpager.widget.ViewPager;

public class VerticalViewPager extends ViewPager {

    public VerticalViewPager(Context context) {
        super(context);
        init();
    }

    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 设置 PageTransformer，用于实现页面垂直滑动的效果
        setPageTransformer(true, new VerticalPageTransformer());
        // 取消原生的 OverScroll 滑动效果
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = super.onInterceptTouchEvent(swapTouchEvent(ev));
        // 将事件还原为原始坐标系
        swapTouchEvent(ev);
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = super.onTouchEvent(swapTouchEvent(ev));
        // 将事件还原为原始坐标系
        swapTouchEvent(ev);
        return handled;
    }

    private MotionEvent swapTouchEvent(MotionEvent ev) {
        float width = getWidth();
        float height = getHeight();

        float swappedX = (ev.getY() / height) * width;
        float swappedY = (ev.getX() / width) * height;

        ev.setLocation(swappedX, swappedY);
        return ev;
    }

    private static class VerticalPageTransformer implements PageTransformer {
        @Override
        public void transformPage(View view, float position) {
            if (position < -1) { // 页面不可见
                view.setAlpha(0);
            } else if (position <= 1) { // 页面可见
                view.setAlpha(1);
                // 防止页面重叠
                view.setTranslationX(view.getWidth() * -position);
                // 垂直滑动效果
                view.setTranslationY(position * view.getHeight());
            } else { // 页面不可见
                view.setAlpha(0);
            }
        }
    }
}
