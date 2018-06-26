package platform.core.utilities;

public class FrameCount {

    public int counter = 0;
    public int fps;

    private NanoTimeValue oldTime;

    public FrameCount(double value) {

        oldTime = new NanoTimeValue(value);

    }

    public double getOldTime() {
        return oldTime.value;
    }

    public void setOldTime(double oldTime) {
        this.oldTime.value = oldTime;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public void inc(){
        counter++;
    }

    public void reset(){
        counter = 0;
    }

    public void tick(long currentNanoTime) {

        if ((currentNanoTime - getOldTime())/1000000000 > 1){
            setFps(counter);
            setOldTime(currentNanoTime);
            reset();

            //System.out.println(fps);
        }
        else{
            inc();
        }

    }
}
