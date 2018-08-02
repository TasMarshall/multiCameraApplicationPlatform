package platform.core.utilities.adaptation.core.components;

import platform.core.utilities.LoopTimer;

public class BackgroundDataCalibScan {

    public boolean getSnapShotRequired() {
        return snapShotRequired;
    }

    public enum ScanningState {
        LocatingNECorner,
        LocatingSWCorner,
        PanningRight,
        PanningLeft,
        WAITING_CAMERA_STREAM_INIT, Tilting
    }

    private ScanningState scanningState;
    private ScanningState lastState;

    boolean snapShotRequired;
    boolean isPausedInPlace;

    int similarityCounter;

    private LoopTimer loopTimer;
    private LoopTimer pausingLoopTimer;

    Boolean found = false;

    LoopTimer calibPanTime;
    LoopTimer calibTiltTime;

    public BackgroundDataCalibScan(){

        loopTimer = new LoopTimer();
        loopTimer.start(1,1);

        pausingLoopTimer = new LoopTimer();
        pausingLoopTimer.start(2,1);

        calibPanTime = new LoopTimer();
        calibTiltTime = new LoopTimer();

        scanningState = ScanningState.WAITING_CAMERA_STREAM_INIT;
        lastState = scanningState;
        snapShotRequired = false;
        similarityCounter = 0;
        isPausedInPlace = false;

        found = false;

    }

    public void nextState() {

        System.out.println("next state");

        if (scanningState == ScanningState.WAITING_CAMERA_STREAM_INIT){
            lastState = scanningState;
            scanningState = ScanningState.LocatingNECorner;
        }



            if (scanningState == ScanningState.LocatingNECorner){
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




        getLoopTimer().resetPulse();


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

    public boolean isSnapShotRequired() {
        return snapShotRequired;
    }

    public void setSnapShotRequired(boolean snapShotRequired) {
        this.snapShotRequired = snapShotRequired;
    }

    public boolean isPausedInPlace() {
        return isPausedInPlace;
    }

    public void setPausedInPlace(boolean pausedInPlace) {
        isPausedInPlace = pausedInPlace;
    }

    public Boolean getFound() {
        return found;
    }

    public void setFound(Boolean found) {
        this.found = found;
    }

    public LoopTimer getCalibPanTime() {
        return calibPanTime;
    }

    public void setCalibPanTime(LoopTimer calibPanTime) {
        this.calibPanTime = calibPanTime;
    }

    public LoopTimer getCalibTiltTime() {
        return calibTiltTime;
    }

    public void setCalibTiltTime(LoopTimer calibTiltTime) {
        this.calibTiltTime = calibTiltTime;
    }
}
