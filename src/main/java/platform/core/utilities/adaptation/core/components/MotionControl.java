package platform.core.utilities.adaptation.core.components;

import platform.core.utilities.LoopTimer;

public class MotionControl {


    private LoopTimer loopTimer;

    public MotionControl(int timer) {

        loopTimer = new LoopTimer();
        loopTimer.start(timer, 1);

    }

    public LoopTimer getLoopTimer() {
        return loopTimer;
    }

    public void setLoopTimer(LoopTimer loopTimer) {
        this.loopTimer = loopTimer;
    }
}