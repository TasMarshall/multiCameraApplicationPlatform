package platform.core.utilities.adaptation.core.components;

import platform.core.utilities.LoopTimer;

public class SearchForCrashStateMachine {

    public boolean getSnapShotRequired() {
        return snapShotRequired;
    }

    public enum ScanningState {
        InitCheckForObject,
        SearchScanToBottomOfFeature,
        SearchScanToTopOfFeature,
        FocusOnCrash,
        Exit,
        RecordCrash
    }

    private SearchForCrashStateMachine.ScanningState scanningState;
    private SearchForCrashStateMachine.ScanningState lastState;

    boolean snapShotRequired;
    boolean isPausedInPlace;

    int similarityCounter;

    private LoopTimer loopTimer;
    private LoopTimer pausingLoopTimer;

    Boolean found = false;

    public SearchForCrashStateMachine (){

        loopTimer = new LoopTimer();
        loopTimer.start(2,1);

        pausingLoopTimer = new LoopTimer();
        pausingLoopTimer.start(3,1);

        scanningState = ScanningState.InitCheckForObject;
        lastState = scanningState;
        snapShotRequired = false;
        similarityCounter = 0;
        isPausedInPlace = false;

        found = false;

    }

    public void nextState(boolean found, boolean selectAsRecorder) {

        if (scanningState == ScanningState.InitCheckForObject){
            lastState = scanningState;
            if (found) {
                scanningState = ScanningState.FocusOnCrash;
            }
            else {
                scanningState = ScanningState.SearchScanToBottomOfFeature;
            }
        }
        else if (scanningState == SearchForCrashStateMachine.ScanningState.SearchScanToBottomOfFeature){
            lastState = scanningState;
            if (found) {
                scanningState = ScanningState.FocusOnCrash;
            }
            else {
                scanningState = ScanningState.SearchScanToTopOfFeature;
            }
        }
        else if (scanningState == SearchForCrashStateMachine.ScanningState.SearchScanToTopOfFeature){
            lastState = scanningState;
            if (found) {
                scanningState = ScanningState.FocusOnCrash;
            }
            else {
                scanningState = ScanningState.Exit;
            }
        }
        else if (scanningState == ScanningState.FocusOnCrash){
            if (!snapShotRequired) {

                lastState = scanningState;
                if (selectAsRecorder) {
                    scanningState = ScanningState.RecordCrash;
                } else {
                    scanningState = ScanningState.Exit;
                }

            }
        }
        else if (scanningState == ScanningState.Exit){ }
        else if (scanningState == ScanningState.RecordCrash){

            lastState = ScanningState.RecordCrash;
            scanningState = ScanningState.Exit;

        }

        getLoopTimer().resetPulse();

        System.out.println("next state: " + scanningState + " -> " + lastState);

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

    public ScanningState getScanningState() {
        return scanningState;
    }

    public void setScanningState(ScanningState scanningState) {
        this.scanningState = scanningState;
    }

    public ScanningState getLastState() {
        return lastState;
    }
}

