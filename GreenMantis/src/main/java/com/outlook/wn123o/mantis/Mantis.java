package com.outlook.wn123o.mantis;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.outlook.wn123o.mantis.common.LoopThread;
import com.outlook.wn123o.mantis.interfaces.MantisListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mantis {

    private final int mPid;
    private final MantisListener mListener;
    private final Handler mHandler;

    private Mantis(@NonNull MantisListener listener, @Nullable Handler handler) {
        mListener = listener;
        mHandler = handler;
        mPid = android.os.Process.myPid();
    }
    private MonitorThread mMonitorThread;


    public void follow() {
        mMonitorThread = new MonitorThread();
        mMonitorThread.start();
    }

    public void kill() {
        mMonitorThread.interrupt();
        mMonitorThread = null;
    }

    private class MonitorThread extends LoopThread {
        private Process process;
        private OutputStream console;
        private BufferedReader stdin;

        @Override
        protected boolean onLoop() {
            try {
                String line = stdin.readLine().trim();
                if (line.contains("Mem")) {
                    onParseSystemMem(line);
                } else if (line.contains(String.valueOf(mPid))) {
                    onParseSelfInfo(line);
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
            return true;
        }

        @Override
        protected void onExitLoop() {
            super.onExitLoop();
            try {
                console.close();
                stdin.close();
                process.waitFor();
            } catch (IOException | InterruptedException ignored) {}
            process.destroy();
        }

        @Override
        protected void onEnterLoop() {
            super.onEnterLoop();
            final String command = String.format(Locale.US, "top -p %d -b", mPid);
            try {
                process = Runtime.getRuntime().exec(command);
                console = process.getOutputStream();
                stdin = new BufferedReader(new InputStreamReader(process.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private void onParseSystemMem(String text) {
        Matcher matcher = MEM_PATTERN.matcher(text);
        while (matcher.find()) {
            try {
                long total = Long.parseLong(matcher.group(1));
                long used = Long.parseLong(matcher.group(2));
                long free = Long.parseLong(matcher.group(3));
                long buffers = Long.parseLong(matcher.group(4));
                if (mHandler != null) {
                    mHandler.post(() -> mListener.onSystemMemSummary(total, used, free, buffers));
                } else {
                    mListener.onSystemMemSummary(total, used, free, buffers);
                }
            } catch (NumberFormatException ignored) {}
        }
    }

    private void onParseSelfInfo(String text) {
        String[] info = Arrays.stream(text.split(" "))
                .filter(s -> !s.isEmpty())
                .map(String::trim)
                .toArray(String[]::new);
        try {
            int pid = Integer.parseInt(info[0]);
            String stat = info[7];
            float cpuUsed = Float.parseFloat(info[8]);
            float memUsed = Float.parseFloat(info[9]);
            if (mHandler != null) {
                mHandler.post(() -> mListener.onSummary(pid, stat, cpuUsed, memUsed));
            } else {
                mListener.onSummary(pid, stat, cpuUsed, memUsed);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace(System.err);
        }
    }

    public static Mantis create(@NonNull MantisListener listener, @Nullable Handler handler) {
        Objects.requireNonNull(listener);
        return new Mantis(listener, handler);
    }

    public static Mantis create(@NonNull MantisListener listener) {
        return create(listener, null);
    }

    private static final Pattern MEM_PATTERN = Pattern.compile("Mem:\\s+(\\d+)K\\stotal,\\s+(\\d+)K\\sused,\\s+(\\d+)K\\sfree,\\s+(\\d+)K\\sbuffers");
}
