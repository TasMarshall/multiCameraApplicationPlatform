package platform.goals;

import platform.behaviors.CameraMAPEBehavior;
import platform.behaviors.GoalMAPEBehavior;
import platform.behaviors.MotionController;
import platform.camera.Camera;
import platform.map.LocalMap;

import java.io.Serializable;
import java.util.*;

public class MultiCameraGoalCore implements  Serializable {

    /**
     * This enumeration is intended to define possible relationships between goals which could be used to
     * confirm the applicability of a goal to any given camera in the MAPE execution loop.
     * */
    public enum GoalType{
        NORMAL,
        CALIBRATION
    }

        ////////////////////////////////////////////////////////////////////////
    /////                       MODEL                                  /////
    ////////////////////////////////////////////////////////////////////////

    /** ID - an identifier to allow selection of goals based on a specified identifier. */
    private String id;

    /** Activated boolean - a field which can be used to activate and deactivate a goal. */
    protected boolean activated;

    /** Priority - a integer to define the priority of the goal which can be used as a comparative value
     *  with  other goals to determine which goal takes precedence. */
    protected int priority;

    /**
     * Goal independence used to confirm the applicability of a goal to any given camera in the MAPE execution loop.
     * */
    protected GoalType goalType;

    /**
     * Cmera requirements a goal has which could be used to confirm the applicability of a goal to any given camera in
     * the MAPE execution loop.
     * */
    protected CameraRequirements cameraRequirements;

    /**Motion controller - a goal can have a single motion controller behavior defined at initialization via a string
     * which corresponds to a specialized behavior in the multi-camera applications library of behaviors.*/
    protected String motionControllerType;

    /** Non-motion behaviors - a goal can have a list of non-motion behaviors defined at initialization via a string
     * which corresponds to a specialized behavior in the multi-camera applications library of behaviors.*/
    protected List<String> nonMotionBehaviors;

    /** Map - a geographical map which can be used to constrain the goal to a geographical location.*/
    protected platform.map.Map map;

    /**Processed information map - a camera to result id to result map populated in behaviors for the purpose of
     * persisting processed information for use in the execution of subsequent behaviors.*/
    protected Map<Camera,Map<String,Serializable>> processedInfoMap;

    public MultiCameraGoalCore (String id, boolean activated, int priority, MultiCameraGoalCore.GoalType goalType, CameraRequirements cameraRequirements
            , platform.map.Map map, String motionControllerType, List<String> nonMotionBehaviors) {

        this.id = id;

        this.activated = activated;

        //set map, if want global map which might not be defined yet set a placeholder, implies goal must be initialized
        if(map.getMapType() == platform.map.Map.MapType.GLOBAL){
            this.map = map;
        }
        else if (map.getMapType() == platform.map.Map.MapType.LOCAL){
            this.map = new LocalMap(map.getCoordinateSys(),map.getLons(),map.getLat());
        }

        this.priority = priority;
        this.goalType = goalType;
        this.cameraRequirements = cameraRequirements;

        cameraRequirements.init();

        this.motionControllerType = motionControllerType;
        this.nonMotionBehaviors = nonMotionBehaviors;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }



    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public void setGoalType(GoalType goalType) {
        this.goalType = goalType;
    }

    public CameraRequirements getCameraRequirements() {
        return cameraRequirements;
    }

    public void setCameraRequirements(CameraRequirements cameraRequirements) {
        this.cameraRequirements = cameraRequirements;
    }

    public String getMotionControllerType() {
        return motionControllerType;
    }

    public void setMotionControllerType(String motionControllerType) {
        this.motionControllerType = motionControllerType;
    }

    public List<String> getNonMotionBehaviors() {
        return nonMotionBehaviors;
    }

    public void setNonMotionBehaviors(List<String> actionTypes) {
        this.nonMotionBehaviors = actionTypes;
    }

    public platform.map.Map getMap() {
        return map;
    }

    public void setMap(platform.map.Map map) {
        this.map = map;
    }

    public Map<Camera, Map<String, Serializable>> getProcessedInfoMap() {
        return processedInfoMap;
    }

    public void setProcessedInfoMap(Map<Camera, Map<String, Serializable>> processedInfoMap) {
        this.processedInfoMap = processedInfoMap;
    }



}
