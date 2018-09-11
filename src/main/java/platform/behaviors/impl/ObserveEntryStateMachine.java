package platform.behaviors.impl;

import platform.utilities.LoopTimer;

import java.util.logging.Logger;

public class ObserveEntryStateMachine {

    private Logger LOGGER;

    public enum ScanningState {
        Init,
        OrientateOnEntrys,
        FocusOnEntry,
        ReturnToRoad,
        Exit
    }

    private ObserveEntryStateMachine.ScanningState scanningState;
    private ObserveEntryStateMachine.ScanningState lastState;

    boolean isPausedInPlace;

    int similarityCounter;

    private LoopTimer loopTimer;
    private LoopTimer pausingLoopTimer;

    Boolean found = false;

    public ObserveEntryStateMachine(Logger logger){

        this.LOGGER = logger;

        loopTimer = new LoopTimer();
        loopTimer.start(2,1);

        pausingLoopTimer = new LoopTimer();
        pausingLoopTimer.start(3,1);

        scanningState = ScanningState.OrientateOnEntrys;
        lastState = scanningState;
        similarityCounter = 0;
        isPausedInPlace = false;

        found = false;

    }

    public void nextState() {

        if (scanningState == ScanningState.OrientateOnEntrys){
            lastState = scanningState;
            scanningState = ScanningState.FocusOnEntry;
        }
        else if (scanningState == ScanningState.FocusOnEntry){
            lastState = scanningState;
            scanningState = ScanningState.ReturnToRoad;
        }
        else if (scanningState == ScanningState.ReturnToRoad){
            lastState = scanningState;
            scanningState = ScanningState.Exit;
        }

        getLoopTimer().resetPulse();

        LOGGER.info("next state: " + lastState + " -> " + scanningState);

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

