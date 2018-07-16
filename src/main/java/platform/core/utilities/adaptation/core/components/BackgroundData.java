package platform.core.utilities.adaptation.core.components;

import platform.core.utilities.LoopTimer;

public class BackgroundData {

    public enum ScanningState {
        LocatingSWCorner,
        PanningRight,
        PanningLeft,
        WAITING_CAMERA_STREAM_INIT, Tilting
    }

    boolean pausedInPlace;

    private ScanningState scanningState;
    private ScanningState lastState;

    int similarityCounter;

    private LoopTimer loopTimer;
    private LoopTimer pausingLoopTimer;

    public BackgroundData (){

        loopTimer = new LoopTimer();
        loopTimer.start(1,1);

        pausingLoopTimer = new LoopTimer();
        pausingLoopTimer.start(2,1);

        scanningState = ScanningState.WAITING_CAMERA_STREAM_INIT;
        lastState = scanningState;
        pausedInPlace = false;
        similarityCounter = 0;

    }

    public void nextState() {

        System.out.println("next state");
        getLoopTimer().resetPulse();

        if (scanningState == ScanningState.WAITING_CAMERA_STREAM_INIT){
            lastState = scanningState;
            scanningState = ScanningState.LocatingSWCorner;
        }
        else if (scanningState == ScanningState.LocatingSWCorner){
            lastState = scanningState;
            scanningState = ScanningState.PanningRight;
        }
        else if(scanningState == ScanningState.PanningRight) {
            lastState = scanningState;
            scanningState = ScanningState.Tilting;
        }
        else if(scanningState == ScanningState.PanningLeft){
            lastState = scanningState;
            scanningState = ScanningState.Tilting;
        }
        else if (scanningState == ScanningState.Tilting){

            if (lastState == ScanningState.PanningRight){
                scanningState = ScanningState.PanningLeft;
            }
            else if (lastState == ScanningState.PanningLeft){
                scanningState = ScanningState.PanningRight;
            }
            lastState = ScanningState.Tilting;

        }

    }

    public LoopTimer getPausingLoopTimer() {
        return pausingLoopTimer;
    }

    public void incrementSimilarityCounter() {
        similarityCounter++;
    }

    public void resetSimilarityCounter() {
        similarityCounter =0;
    }

    public int getSimilarityCounter() {
        return similarityCounter;
    }

    public ScanningState getScanningState() {
        return scanningState;
    }

    public void setScanningState(ScanningState scanningState) {
        this.scanningState = scanningState;
    }

    public LoopTimer getLoopTimer() {
        return loopTimer;
    }

    public boolean getPausedInPlace() {
        return pausedInPlace;
    }

    public void setPausedInPlace(boolean pausedInPlace) {
        this.pausedInPlace = pausedInPlace;
    }
}
