package com.outlook.wn123o.mantis;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.outlook.wn123o.mantis.interfaces.MantisListener;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MantisListener {

    private TextView mSystemTextView;
    private TextView mSelfTextView;
    private Mantis mMantis;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        mSystemTextView = findViewById(R.id.text_view_system);
        mSelfTextView = findViewById(R.id.text_view_self);
        MaterialSwitch mSwitch = findViewById(R.id.switch_follow);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mMantis = Mantis.create(this, mHandler);
                mMantis.follow();
            } else {
                mMantis.kill();
                mMantis = null;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMantis != null) {
            mMantis.kill();
            mMantis = null;
        }
    }

    @Override
    public void onSystemMemSummary(long total, long used, long free, long buffers) {
        String summary = String.format(Locale.US, "Total: %dKib\nUsed: %dKib\nFree: %dKib\nBuffers: %dKib", total, used, free, buffers);
        mSystemTextView.setText(summary);
    }

    @Override
    public void onSummary(int pid, String stat, float cpuUsed, float memUsed) {
        String summary = String.format(Locale.US, "Pid: %d\nCpu: %.1f%%\nMem: %.1f%%", pid, cpuUsed, memUsed);
        mSelfTextView.setText(summary);
    }
}