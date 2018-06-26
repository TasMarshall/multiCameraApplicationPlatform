package platform.core.utilities;

public class LoopTimer {

    private double loopTimer;
    private NanoTimeValue lastTime;
    private boolean loopActive = false;

    int pulsesPerLoop = 1;
    int pulseCounter = 0;

    public void start(double periodSeconds, int pulsesPerLoop) {
        this.loopTimer = periodSeconds;
        loopActive = true;
        this.lastTime = (new NanoTimeValue(System.nanoTime()));
        this.pulsesPerLoop = pulsesPerLoop;
    }

    public boolean checkPulse() {

        NanoTimeValue currentTime = new NanoTimeValue(System.nanoTime());
        double time = (currentTime.value - lastTime.value) / 1000000000.0;
        if (time > loopTimer) {
            pulseCounter++;
            if (pulseCounter >= pulsesPerLoop){
                lastTime = (new NanoTimeValue(currentTime.value));
                pulseCounter = 0;
            }
            return true;
        }
        else {
            return false;
        }
    }

    public void stop(){
        loopActive = (false);
    }
}
