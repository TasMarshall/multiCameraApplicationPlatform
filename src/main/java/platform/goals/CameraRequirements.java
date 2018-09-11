package platform.goals;

import platform.camera.Camera;
import platform.camera.components.ViewCapabilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CameraRequirements implements Serializable{

    private static Logger LOGGER;

    public void setUpLogger(){
        LOGGER = Logger.getLogger(CameraRequirements.class.getName());
        LOGGER.setLevel(Level.FINE);
    }

    boolean working;
    boolean inRange;
    boolean motionAvailable;
    boolean motionNotAvailable;
    boolean pan;
    boolean tilt;
    boolean zoom;
    boolean specificID;
    boolean exclusive;
    boolean calibrated;

    List<String> specificIDs = new ArrayList<>();

    /** Calibration goal ids - a list of calibration goals ids which can be used to define the calibration
     * goals which must have been completed prior to execution of a goal.*/
    List<String> calibrationIDs = new ArrayList<>();

    public CameraRequirements(boolean working, boolean inRange, boolean motionAvailable, boolean motionNotAvailable, boolean pan, boolean tilt, boolean zoom, boolean specificID, boolean exclusive, boolean calibrated, List<String> specificIDs, List<String> calibrationIds) {
        this.working = working;
        this.inRange = inRange;
        this.motionAvailable = motionAvailable;
        this.motionNotAvailable = motionNotAvailable;
        this.pan = pan;
        this.tilt = tilt;
        this.zoom = zoom;
        this.specificID = specificID;
        this.exclusive = exclusive;
        this.calibrated = calibrated;
        this.specificIDs = specificIDs;
        this.calibrationIDs = calibrationIds;
    }

    public boolean checkBaseRequirements(MultiCameraGoal multiCameraGoal, Camera camera) {
        boolean valid = true;

        if (camera.getIdAsString().equals("dahua_fixed")){
            valid = valid;
        }

        if (inRange){
            if (!camera.inRange(multiCameraGoal.map)){
                valid = false;
                LOGGER.fine("Camera " + camera.getIdAsString() + " does not meet inRange requirement of Goal " + multiCameraGoal.getId());
            }
        }

        if (pan){
            if (!camera.getViewCapabilities().getPtzType().contains(ViewCapabilities.PTZ.P)){
                valid = false;
                LOGGER.fine("Camera " + camera.getIdAsString() + " does not meet pan requirement of Goal " + multiCameraGoal.getId());
            }
        }

        if (tilt){
            if (!camera.getViewCapabilities().getPtzType().contains(ViewCapabilities.PTZ.T)){
                valid = false;
                LOGGER.fine("Camera " + camera.getIdAsString() + " does not meet tilt requirement of Goal " + multiCameraGoal.getId());
            }
        }

        if (zoom){
            if (!camera.getViewCapabilities().getPtzType().contains(ViewCapabilities.PTZ.Z)){
                valid = false;
                LOGGER.fine("Camera " + camera.getIdAsString() + " does not meet zoom requirement of Goal " + multiCameraGoal.getId());
            }
        }

        if(specificID){
            if (!(specificIDs.contains(camera.getIdAsString()))){
                valid = false;
                LOGGER.fine("Camera " + camera.getIdAsString() + " does not meet specificID requirement of Goal " + multiCameraGoal.getId());
            }
        }

        return  valid;
    }

    public boolean checkLiveRequirements(MultiCameraGoal multiCameraGoal, Camera camera, boolean viewControlled, boolean exclusive){
        boolean valid = true;

        if (working){
            if (!camera.isWorking()){
                valid = false;
                LOGGER.finest("Camera " + camera.getIdAsString() + " does not meet working requirement of Goal " + multiCameraGoal.getId());
            }
        }

        if (calibrated){
            if (!(((List<String>)camera.getAdditionalAttributes().get("completedGoals")).containsAll(calibrationIDs))){
                valid = false;
                LOGGER.finest("Camera " + camera.getIdAsString() + " does not meet calibration requirement of Goal " + multiCameraGoal.getId());
            }
        }

        if (motionAvailable){
            if ((viewControlled)){
                if (!motionNotAvailable) {
                    valid = false;
                   LOGGER.finest("Camera " + camera.getIdAsString() + " does not meet motionAvailable requirement of Goal " + multiCameraGoal.getId());
                }
            }
        }

        if(this.exclusive){
            if (exclusive){
                valid = false;
                LOGGER.finest("Camera " + camera.getIdAsString() + " does not meet exclusive requirement of Goal " + multiCameraGoal.getId());
            }
        }

        return valid;
    }

    public boolean getCalibrated() {
        return calibrated;
    }

    public boolean getExclusive() {
        return exclusive;
    }

    public boolean getMotionAvailable() {
        return motionAvailable;
    }

    public boolean getMotionNotAvailable() {
        return motionNotAvailable;
    }

    public List<String> getCalibrationIDs() {
        return calibrationIDs;
    }

    public void init() {

        setUpLogger();

        if (calibrationIDs == null){
            calibrationIDs = new ArrayList<>();
        }

        if(specificIDs == null){
            specificIDs = new ArrayList<>();
        }
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public static void setLOGGER(Logger LOGGER) {
        CameraRequirements.LOGGER = LOGGER;
    }

    public boolean isWorking() {
        return working;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }

    public boolean isInRange() {
        return inRange;
    }

    public void setInRange(boolean inRange) {
        this.inRange = inRange;
    }

    public boolean isMotionAvailable() {
        return motionAvailable;
    }

    public void setMotionAvailable(boolean motionAvailable) {
        this.motionAvailable = motionAvailable;
    }

    public boolean isMotionNotAvailable() {
        return motionNotAvailable;
    }

    public void setMotionNotAvailable(boolean motionNotAvailable) {
        this.motionNotAvailable = motionNotAvailable;
    }

    public boolean isPan() {
        return pan;
    }

    public void setPan(boolean pan) {
        this.pan = pan;
    }

    public boolean isTilt() {
        return tilt;
    }

    public void setTilt(boolean tilt) {
        this.tilt = tilt;
    }

    public boolean isZoom() {
        return zoom;
    }

    public void setZoom(boolean zoom) {
        this.zoom = zoom;
    }

    public boolean isSpecificID() {
        return specificID;
    }

    public void setSpecificID(boolean specificID) {
        this.specificID = specificID;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public boolean isCalibrated() {
        return calibrated;
    }

    public void setCalibrated(boolean calibrated) {
        this.calibrated = calibrated;
    }

    public List<String> getSpecificIDs() {
        return specificIDs;
    }

    public void setSpecificIDs(List<String> specificIDs) {
        this.specificIDs = specificIDs;
    }

    public void setCalibrationIDs(List<String> calibrationIDs) {
        this.calibrationIDs = calibrationIDs;
    }
}
