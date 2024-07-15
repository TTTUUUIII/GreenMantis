package com.outlook.wn123o.mantis.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.cardview.widget.CardView;

import com.outlook.wn123o.mantis.Mantis;
import com.outlook.wn123o.mantis.R;
import com.outlook.wn123o.mantis.interfaces.MantisListener;
import com.outlook.wn123o.mantis.util.AndroidUtils;

import java.util.Locale;

public class FloatWindow implements View.OnTouchListener, MantisListener {
    private final Context mContext;
    private final WindowManager mWindowManager;

    private View mView;

    private TextView mPidTextView;
    private TextView mCpuStatTextView;
    private TextView mMemoryStatTextView;

    private WindowManager.LayoutParams mLayoutParams;
    private boolean mShowing = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Mantis mMantis;
    private boolean mUseDarkMode = false;

    public FloatWindow(Context context) {
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    protected View onCreateView() {
        CardView root = new CardView(mContext);
        root.setCardElevation(0);
        root.setRadius(AndroidUtils.dpToPx(8));
        root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        FrameLayout container = new FrameLayout(mContext);
        int padding = AndroidUtils.dpToPx(15);
        container.setPadding(padding, padding, padding, padding);
        FrameLayout.LayoutParams containerLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.setLayoutParams(containerLayoutParams);
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        layoutInflater.inflate(R.layout.layout_mantis_floating_window, container, true);
        root.addView(container);
        return root;
    }

    protected WindowManager.LayoutParams onWindowLayout() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.format = PixelFormat.TRANSPARENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }
        return lp;
    }

    protected void onAttachToWindow() {
        mLayoutParams = onWindowLayout();
        mWindowManager.addView(mView, mLayoutParams);
        onBindTouchEvent();
        onUiModeUpdated();
        mShowing = true;
    }

    protected void onDetachFromWindow() {
        mWindowManager.removeView(mView);
        mShowing = false;
    }

    protected void onBindTouchEvent() {
        mView.setOnTouchListener(this);
        mMemoryStatTextView = mView.findViewById(R.id.text_view_mem_stat);
        mCpuStatTextView = mView.findViewById(R.id.text_view_cpu_stat);
        mPidTextView = mView.findViewById(R.id.text_view_pid);
    }

    protected void onUiModeUpdated() {
        if (mView != null) {
            int backgroundColor = Color.argb(118, 255, 255, 255);
            int fontColor = Color.BLACK;
            if (mUseDarkMode) {
                backgroundColor = Color.argb(118, 0, 0, 0);
                fontColor = Color.WHITE;
            }
            if (mView instanceof CardView) {
                ((CardView) mView).setCardBackgroundColor(backgroundColor);
            } else {
                mView.setBackgroundColor(backgroundColor);
            }
            updateTextColor(mView, fontColor);
        }
    }

    private void updateTextColor(View view, @ColorInt int color) {
        if (view instanceof ViewGroup) {
            int childCount = ((ViewGroup) view).getChildCount();
            for (int i = 0; i < childCount; ++i) {
                View child = ((ViewGroup) view).getChildAt(i);
                updateTextColor(child, color);
            }
        }
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
        }
    }

    public void show() {
        if (!mShowing) {
            if (mView == null) {
                mView = onCreateView();
            }
            onAttachToWindow();
            onAttachToMantis();
        }
    }

    public void dismiss() {
        if (mShowing) {
            onDetachFromWindow();
            onDetachFromMantis();
        }
    }

    public void isDarkMode(boolean isDarkMode) {
        if (mUseDarkMode != isDarkMode) {
            mUseDarkMode = isDarkMode;
            onUiModeUpdated();
        }
    }

    private float _x = 0;
    private float _y = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                _x = event.getRawX();
                _y = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getRawX();
                float y = event.getRawY();
                float offsetX = x - _x;
                float offsetY = y - _y;
                _x = x;
                _y = y;
                onWindowMove(offsetX, offsetY);
                break;
            default:
                return false;
        }
        return true;
    }

    private void onWindowMove(float offsetX, float offsetY) {
        mLayoutParams.x += (int) offsetX;
        mLayoutParams.y += (int) offsetY;
        mWindowManager.updateViewLayout(mView, mLayoutParams);
    }

    private void onAttachToMantis() {
        assert mMantis == null;
        mMantis =  Mantis.create(this, mHandler);
        mMantis.follow();
    }

    private void onDetachFromMantis() {
        if (mMantis != null) {
            mMantis.kill();
            mMantis = null;
        }
    }

    @Override
    public void onSystemMemSummary(long total, long used, long free, long buffers) {

    }

    @Override
    public void onSummary(int pid, String stat, float cpuUsed, float memUsed) {
        if (mShowing) {
            String cpuStat = String.format(Locale.US, "%.1f%%", cpuUsed);
            String memoryStat = String.format(Locale.US, "%.1f%%", memUsed);
            mPidTextView.setText(String.valueOf(pid));
            mCpuStatTextView.setText(cpuStat);
            mMemoryStatTextView.setText(memoryStat);
        }
    }
}
