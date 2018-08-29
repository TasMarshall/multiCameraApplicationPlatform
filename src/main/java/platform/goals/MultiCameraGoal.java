/**
 * This class is a java implementation of a multi-camera application goal model.
 *
 * @author  Tasman Marshall
 * @version 1.0
 * @since   08/2018
 */

package platform.goals;

import platform.MultiCameraCore;
import platform.camera.Camera;
import platform.imageAnalysis.AnalysisTypeManager;
import platform.imageAnalysis.ImageAnalysis;
import platform.map.LocalMap;
import platform.behaviors.AdaptationTypeManager;
import platform.behaviors.CameraMAPEBehavior;
import platform.behaviors.GoalMAPEBehavior;
import platform.behaviors.MotionController;
import platform.jade.utilities.CommunicationAction;

import java.io.Serializable;
import java.util.*;

public class MultiCameraGoal implements Serializable{

    /**
     * This enumeration is intended to define possible relationships between goals which could be used to
     * confirm the applicability of a goal to any given camera in the MAPE execution loop.
     * */
    public enum GoalIndependence{
        EXCLUSIVE,
        NONEXCLUSIVE
    }

    /**
     * This enumeration is intended to define possible camera requirements a goal has which could be used to
     * confirm the applicability of a goal to any given camera in the MAPE execution loop.
     * */
    public enum CameraRequirements{
        VIEW_CONTROL_REQUIRED,
        VIEW_CONTROL_OPTIONAL,
        PASSIVE,
        CALIBRATION
    }


    ////////////////////////////////////////////////////////////////////////
    /////                       MODEL                                  /////
    ////////////////////////////////////////////////////////////////////////

    /** ID - an identifier to allow selection of goals based on a specified identifier. */
    private String id = UUID.randomUUID().toString();

    /** Activated boolean - a field which can be used to activate and deactivate a goal. */
    private boolean activated;

    /** Visual Observations of Interest - a list of the visual objects of interest which the
     * goal will use to perform the various analysis of camera streams which are required */
    private List<VisualObservationOfInterest> visualObservationsOfInterest = new ArrayList<>();

    /** Cameras - a list of cameras which the goal can use to store the cameras it is applicable to.*/
    private List<Camera> cameras = new ArrayList<>();

    /** Priority - a integer to define the priority of the goal which can be used as a comparative value
     *  with  other goals to determine which goal takes precedence. */
    protected int priority = 0;

    /** Calibration goal ids - a list of calibration goals ids which can be used to define the calibration
     * goals which must have been completed prior to execution of a goal.*/
    private List<String> requiredCalibrationGoalIds;

    /**
     * Goal independence used to confirm the applicability of a goal to any given camera in the MAPE execution loop.
     * */
    private GoalIndependence goalIndependence;

    /**
     * Cmera requirements a goal has which could be used to confirm the applicability of a goal to any given camera in
     * the MAPE execution loop.
     * */
    private CameraRequirements cameraRequirements;

    /**Motion controller - a goal can have a single motion controller behavior defined at initialization via a string
     * which corresponds to a specialized behavior in the multi-camera applications library of behaviors.*/
    private String motionControllerType;

    /** Non-motion behaviors - a goal can have a list of non-motion behaviors defined at initialization via a string
     * which corresponds to a specialized behavior in the multi-camera applications library of behaviors.*/
    private List<String> actionTypes;

    /** Map - a geographical map which can be used to constrain the goal to a geographical location.*/
    private platform.map.Map map;

    ////////////////////////////////////////////////////////////////////////
    /////                       INTERNAL                               /////
    ////////////////////////////////////////////////////////////////////////

    /** The multi-camera application itself proving information on the applications cameras and goals*/
    private MultiCameraCore mcp_application;

    /**Motion controller - instantiated motion controller behavior defined at initialization via a string which
     * corresponds to a specialized behavior in the multi-camera applications library of behaviors.*/
    private MotionController motionController;

    /**Non-motion camera behaviors - instantiated list of non-motion camera behaviors defined at initialization via strings
     * which corresponds to a specialized behavior in the multi-camera applications library of behaviors.*/
    private List<CameraMAPEBehavior> cameraBehaviours;

    /**Non-motion goal behaviors - instantiated list of non-motion goal behaviors defined at initialization via strings
     * which corresponds to a specialized behavior in the multi-camera applications library of behaviors.*/
    private List<GoalMAPEBehavior> goalBehaviours;

    /**Processed information map - a camera to result id to result map populated in behaviors for the purpose of
     * persisting processed information for use in the execution of subsequent behaviors.*/
    private Map<Camera,Map<String,Object>> processedInfoMap;

    /**Processed information map - a camera id to result name to result map populated in behaviors for the purpose of
     * persisting processed information for use in the execution of subsequent behaviors.*/
    private Map<String, Map<String,Serializable>> newAnalysisResultsMap;

    /** Latest visual analysis information map - a camera id to result map of the all the latest visual
     * analysis results. This information provides a complete picture of most recent results to a goal,
     * including the system time the analysis result was obtained for performing cooperative behaviors.*/
    private Map<String, Map<String,Serializable>> lastAnalysisResultTimes;

    /** Motion action timer map of camera string to commanded motion end time for use in motion controllers to achieve
     * system ticker stepped motion control. */
    private Map<String, Long> motionActionEndTimes;

    /**Additional field map -  a map of string to object entries for the purpose of specifying customized versions of
     * the above and hence use is specialized behaviors. For example, specialized camera requirements or goal relationships
     * would be defined here.*/
    private Map<String,Object> additionalFieldMap;

    ////////////////////////////////////////////////////////////////////////
    /////                       CONSTRUCTOR                            /////
    ////////////////////////////////////////////////////////////////////////

    public MultiCameraGoal(String id, boolean activated, int priority, GoalIndependence goalIndependence, CameraRequirements cameraRequirements, List<VisualObservationOfInterest> visualObservationOfInterests
            , platform.map.Map map, String motionControllerType, List<String> actionTypes, List<String> requiredCalibrationGoalIds, Map<String,Object> additionalFieldMap){

        this.id = id;

        this.activated = activated;

        if(visualObservationOfInterests != null) {
            this.visualObservationsOfInterest.addAll(visualObservationOfInterests);
        }

        //set map, if want global map which might not be defined yet set a placeholder, implies goal must be initialized
        if(map.getMapType() == platform.map.Map.MapType.GLOBAL){
            this.map = map;
        }
        else if (map.getMapType() == platform.map.Map.MapType.LOCAL){
            this.map = new LocalMap(map.getCoordinateSys(),map.getX(),map.getY());
        }

        this.priority = priority;
        this.goalIndependence = goalIndependence;
        this.cameraRequirements = cameraRequirements;
        this.motionControllerType = motionControllerType;
        this.actionTypes = actionTypes;

        if (requiredCalibrationGoalIds != null) {
            this.requiredCalibrationGoalIds = requiredCalibrationGoalIds;
        }
        else {
            this.requiredCalibrationGoalIds = new ArrayList<>();
        }

        if (additionalFieldMap != null) {
            this.additionalFieldMap = additionalFieldMap;
        }
        else {
            this.additionalFieldMap =  new HashMap<>();
        }

    }

    public void init(MultiCameraCore mcp_application, double timer, AnalysisTypeManager analysisTypeManager, AdaptationTypeManager adaptationTypeManager){

        this.mcp_application = mcp_application;

        //cant set map to the global map in constructor so set here
        if (this.map.getMapType() == platform.map.Map.MapType.GLOBAL){
            this.map = mcp_application.getGlobalMap();
        }

        newAnalysisResultsMap = new HashMap<>();
        lastAnalysisResultTimes = new HashMap<>();
        motionActionEndTimes =  new HashMap<>();
        processedInfoMap = new HashMap<Camera,Map<String,Object>>();

        cameraBehaviours = new ArrayList<>();
        goalBehaviours = new ArrayList<>();

        for (String s: actionTypes){
            if (adaptationTypeManager.getAdaptivePolicy(s) instanceof CameraMAPEBehavior) {
                cameraBehaviours.add((CameraMAPEBehavior) adaptationTypeManager.getAdaptivePolicy(s));
            }
            else if (adaptationTypeManager.getAdaptivePolicy(s) instanceof GoalMAPEBehavior) {
                goalBehaviours.add((GoalMAPEBehavior) adaptationTypeManager.getAdaptivePolicy(s));
            }
        }

        for (CameraMAPEBehavior a: cameraBehaviours){
            a.behaviourInit();
        }

        for (GoalMAPEBehavior a: goalBehaviours){
            a.behaviourInit();
        }

        if (cameraRequirements != CameraRequirements.PASSIVE){
            if(adaptationTypeManager.getAdaptivePolicy(motionControllerType) instanceof MotionController) {
                motionController = (MotionController) adaptationTypeManager.getAdaptivePolicy(motionControllerType);
                motionController.motInit();
            }
        }

        initOOI(analysisTypeManager);

        addCamerasToGoalsAndGoalsToCameras();

    }

    private void initOOI(AnalysisTypeManager analysisTypeManager) {

        for (VisualObservationOfInterest visualObservationOfInterests: visualObservationsOfInterest){
            if(visualObservationOfInterests.getAnalysisAlgorithmsSet() != null) {
                for (ImageAnalysis imageAnalysis : visualObservationOfInterests.getAnalysisAlgorithmsSet()) {
                    imageAnalysis.setAnalysisTypeManager(analysisTypeManager);
                }

            }
            visualObservationOfInterests.init();

        }
    }

    public List<CommunicationAction> monitorBehaviours(Camera camera) {
        List<CommunicationAction> commuinicationActions = new ArrayList<>();

        for (CameraMAPEBehavior adaptivePolicy: cameraBehaviours){
            CommunicationAction communicationAction = adaptivePolicy.monitor(camera,this);
            if (communicationAction != null ) commuinicationActions.add(communicationAction);
        }

        return commuinicationActions;
    }

    public List<CommunicationAction> analysisBehaviours(Camera camera) {
        List<CommunicationAction> commuinicationActions = new ArrayList<>();
        for (CameraMAPEBehavior adaptivePolicy: cameraBehaviours){
            CommunicationAction communicationAction = adaptivePolicy.analyse(camera,this);
            if (communicationAction != null ) commuinicationActions.add(communicationAction);
        }

        return commuinicationActions;
    }

    public List<CommunicationAction> planBehaviours(Camera camera) {
        List<CommunicationAction> commuinicationActions = new ArrayList<>();
        for (CameraMAPEBehavior adaptivePolicy: cameraBehaviours){
            CommunicationAction communicationAction = adaptivePolicy.plan(camera,this);
            if (communicationAction != null ) commuinicationActions.add(communicationAction);
        }

        return commuinicationActions;
    }

    public List<CommunicationAction> executeBehaviours(Camera camera) {
        List<CommunicationAction> commuinicationActions = new ArrayList<>();
        for (CameraMAPEBehavior adaptivePolicy: cameraBehaviours){
            CommunicationAction communicationAction = adaptivePolicy.execute(camera,this);
            if (communicationAction != null ) commuinicationActions.add(communicationAction);
        }

        return commuinicationActions;
    }

    public void executeCameraMotionAction(Camera camera) {

        if (motionActionEndTimes.get(camera.getIdAsString()) == null){
            motionActionEndTimes.put(camera.getIdAsString(),Long.valueOf(0));
        }

        String message = "";
        boolean moveCommanded = false;

        if (motionController != null) {
            motionController.planMotion(this, camera);
            motionController.executeMotion(motionActionEndTimes, camera);
        }

    }


/*    private void addAndRemoveChangedCamerasToActiveList() {

        for( Camera camera: getMcp_application().getAllCameras()){
            if (!getActiveCameras().contains(camera)){
                if (camera.getCurrentGoal() == this){
                    addActiveCamera(camera);
                }
            }
            else if (getActiveCameras().contains(camera)){
                if(camera.getCurrentGoal() != this){
                    removeActiveCamera(camera);
                }
            }
        }
    }*/

/*    public void countActiveCamerasPerRegion(){
        for(RegionOfInterest regionOfInterest: getRegionsOfInterest()){
            List<Camera> cameras = new ArrayList<>();
            for (Camera camera: getActiveCameras()){
                if (regionOfInterest.getCamerasInRegion().contains(camera)){
                    cameras.add(camera);
                }
            }
            getActiveCamerasPerRegion().put(regionOfInterest,cameras);
        }
    }*/

   /* public void addActiveCamera(Camera camera){
        getActiveCameras().add(camera);
        //update working camera list per region
        countActiveCamerasPerRegion();
    }

    public void removeActiveCamera(Camera camera){
        getActiveCameras().remove(camera);

        countActiveCamerasPerRegion();
    }*/



   protected void addCamerasToGoalsAndGoalsToCameras() {

        List<Camera> camerasInRegion = new ArrayList<>();

        //dont add deactivated goals by default?
        if (activated) {

            for (Camera camera : getMcp_application().getWorkingCameras()) {

                if (camera.inRange(map)){
                    camerasInRegion.add(camera);
                    camera.addMultiCameraGoal(this);
                }

            }

        }

        cameras = camerasInRegion;

    }

    public Set<ImageAnalysis> getImageAnalysisAlgorithms(){

        Set<ImageAnalysis> analysisAlgorithmsSet = new HashSet<>();

        for(VisualObservationOfInterest visualObservationOfInterests: visualObservationsOfInterest){
            if (visualObservationOfInterests.getAnalysisAlgorithmsSet() != null) {
                analysisAlgorithmsSet.addAll(visualObservationOfInterests.getAnalysisAlgorithmsSet());
            }
        }

        return analysisAlgorithmsSet;
    }

    //CUSTOM
    public VisualObservationOfInterest getInterestById(String id){
       for (VisualObservationOfInterest o: visualObservationsOfInterest){
           if (o.getId().equals(id)){
               return o;
           }
       }
       System.out.println("Requested Object of Interest Does Not Exist.");
       return null;
    }

    //GENERATED

    public List<String> getCalibrationGoalIds() {
        return requiredCalibrationGoalIds;
    }

    public void setCalibrationGoalIds(List<String> requiredCalibrationGoalIds) {
        this.requiredCalibrationGoalIds = requiredCalibrationGoalIds;
    }

    public MultiCameraCore getMcp_application() {
        return mcp_application;
    }

    public void setMcp_application(MultiCameraCore mcp_application) {
        this.mcp_application = mcp_application;
    }

    public List<VisualObservationOfInterest> getObjectsOfInterest() {
        return visualObservationsOfInterest;
    }

    public void setVisualObservationsOfInterest(List<VisualObservationOfInterest> objectsOfInterest) {
        this.visualObservationsOfInterest = objectsOfInterest;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<Camera> getActiveCameras() {

       List<Camera> activeCams = new ArrayList<>();
        for (Camera camera: cameras){
            if (camera.getCurrentGoals().contains(this)){
                activeCams.add(camera);
            }
        }
        return activeCams;
    }

    public void setActiveCameras(List<Camera> cameras) {
        this.cameras = cameras;
    }

    public platform.map.Map getMap() {
        return map;
    }

    public void setMap(platform.map.Map map) {
        this.map = map;
    }

    public GoalIndependence getGoalIndependence() {
        return goalIndependence;
    }

    public CameraRequirements getCameraRequirements() {
        return cameraRequirements;
    }

    public void setCameraRequirements(CameraRequirements cameraRequirements) {
        this.cameraRequirements = cameraRequirements;
    }

    public void setGoalIndependence(GoalIndependence goalIndependence) {
        this.goalIndependence = goalIndependence;
    }

    public String getId() {
        return id;
    }

    public Map<String, Map<String,Serializable>> getNewAnalysisResultMap() {
        return newAnalysisResultsMap;
    }

    public void setAnalysisResultMap(Map<String, Map<String,Serializable>> newAnalysisResultsMap) {
        this.newAnalysisResultsMap = newAnalysisResultsMap;
    }

    public Map<String, Map<String, Serializable>> getLatestAnalysisResults() {
        return lastAnalysisResultTimes;
    }

    public void setLatestAnalysisResults(Map<String, Map<String, Serializable>> lastAnalysisResultTimes) {
        this.lastAnalysisResultTimes = lastAnalysisResultTimes;
    }

    public String getMotionControllerType() {
        return motionControllerType;
    }

    public Map<Camera, Map<String, Object>> getProcessedInfoMap() {
        return processedInfoMap;
    }

    public void setProcessedInfoMap(Map<Camera, Map<String, Object>> processedInfoMap) {
        this.processedInfoMap = processedInfoMap;
    }


    public List<String> getActionTypes() {
        return actionTypes;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public void setCameras(List<Camera> cameras) {
        this.cameras = cameras;
    }
    public List<GoalMAPEBehavior> getGoalBehaviours() {
        return goalBehaviours;
    }

    public Map<String, Object> getAdditionalFieldMap() {
        return additionalFieldMap;
    }

    public void setAdditionalFieldMap(Map<String, Object> additionalFieldMap) {
        this.additionalFieldMap = additionalFieldMap;
    }
}
