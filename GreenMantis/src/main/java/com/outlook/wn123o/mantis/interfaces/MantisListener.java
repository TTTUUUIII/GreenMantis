package com.outlook.wn123o.mantis.interfaces;

public interface MantisListener {
    void onSystemMemSummary(long total, long used, long free, long buffers);

    void onSummary(int pid, String stat, float cpuUsed, float memUsed);
}
