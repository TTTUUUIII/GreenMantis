package com.outlook.wn123o.mantis.common;

public abstract class LoopThread extends Thread {

    public LoopThread() {
        this("Mantis#LoopThread#" + nextUniqueId());
    }

    public LoopThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        onEnterLoop();
        while (isActive()) {
            if (onLoop()) continue;
            try {
                sleep(30);
            } catch (InterruptedException e) {
                break;
            }
        }
        onExitLoop();
    }

    public boolean isActive() {
        return !isInterrupted();
    }

    protected abstract boolean onLoop();
    protected void onExitLoop() {
        Logger.debug("onExitLoop@" + getName());
    }
    protected void onEnterLoop() {
        Logger.debug("onEnterLoop@" + getName());
    }

    private static int sUniqueId = 0;
    private static int nextUniqueId() {
        return sUniqueId++;
    }
}
