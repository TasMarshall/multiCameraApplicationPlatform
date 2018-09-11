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
import platform.camera.CameraCore;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiCameraGoal extends  MultiCameraGoalCore implements Serializable{

    /** Cameras - a list of cameras which the goal can use to store the cameras it is applicable to.*/
    protected List<Camera> cameras = new ArrayList<>();

    /**Motion controller - instantiated motion controller behavior defined at initialization via a string which
     * corresponds to a specialized behavior in the multi-camera applications library of behaviors.*/
    protected MotionController motionController;

    /**Non-motion camera behaviors - instantiated list of non-motion camera behaviors defined at initialization via strings
     * which corresponds to a specialized behavior in the multi-camera applications library of behaviors.*/
    protected List<CameraMAPEBehavior> cameraBehaviours;

    /**Non-motion goal behaviors - instantiated list of non-motion goal behaviors defined at initialization via strings
     * which corresponds to a specialized behavior in the multi-camera applications library of behaviors.*/
    protected List<GoalMAPEBehavior> goalBehaviours;

    /** Visual Observations of Interest - a list of the visual objects of interest which the
     * goal will use to perform the various analysis of camera streams which are required */
    protected List<VisualObservationOfInterest> visualObservationsOfInterest = new ArrayList<>();

    /**Additional field map -  a map of string to object entries for the purpose of specifying customized versions of
     * the above and hence use is specialized behaviors. For example, specialized camera requirements or goal relationships
     * would be defined here.*/
    private Map<String,Object> additionalFieldMap;

    ////////////////////////////////////////////////////////////////////////
    /////                       INTERNAL                               /////
    ////////////////////////////////////////////////////////////////////////

    /** The multi-camera application itself proving information on the applications cameras and goals*/
    private MultiCameraCore mcp_application;

    /**Processed information map - a camera id to result name to result map populated in behaviors for the purpose of
     * persisting processed information for use in the execution of subsequent behaviors.*/
    protected Map<String, Map<String,Serializable>> newAnalysisResultsMap;

    /** Latest visual analysis information map - a camera id to result map of the all the latest visual
     * analysis results. This information provides a complete picture of most recent results to a goal,
     * including the system time the analysis result was obtained for performing cooperative behaviors.*/
    private Map<String, Map<String,Serializable>> lastAnalysisResultTimes;

    /** Motion action timer map of camera string to commanded motion end time for use in motion controllers to achieve
     * system ticker stepped motion control. */
    private Map<String, Long> motionActionEndTimes;

    ////////////////////////////////////////////////////////////////////////
    /////                       CONSTRUCTOR                            /////
    ////////////////////////////////////////////////////////////////////////

    public MultiCameraGoal(String id, boolean activated, int priority, MultiCameraGoalCore.GoalType goalType, CameraRequirements cameraRequirements, List<VisualObservationOfInterest> visualObservationOfInterests
            , platform.map.Map map, String motionControllerType, List<String> actionTypes, Map<String,Object> additionalFieldMap){

        super(id,activated,priority,goalType,cameraRequirements,map,motionControllerType,actionTypes);

        if(visualObservationOfInterests != null) {
            this.visualObservationsOfInterest.addAll(visualObservationOfInterests);
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
        processedInfoMap = new HashMap<Camera,Map<String,Serializable>>();

        cameraBehaviours = new ArrayList<>();
        goalBehaviours = new ArrayList<>();

        for (String s: nonMotionBehaviors){
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

        if (cameraRequirements.pan || cameraRequirements.tilt || cameraRequirements.zoom || cameraRequirements.motionAvailable){
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

/*    public void cameraMotionAccurateController(Camera camera){
        if (motionActionEndTimes.get(camera.getIdAsString()) == null){
            motionActionEndTimes.put(camera.getIdAsString(),Long.valueOf(0));
        }

        String message = "";
        boolean moveCommanded = false;

        if (motionController != null) {
            motionController.executeMotion(motionActionEndTimes, camera);
        }

    }*/

   protected void addCamerasToGoalsAndGoalsToCameras() {

        List<Camera> camerasApplicable = new ArrayList<>();

        //dont add deactivated goals by default?
        if (activated) {

            for (Camera camera : getMcp_application().getWorkingCameras()) {

                if(cameraRequirements.checkBaseRequirements(this,camera)){
                    camerasApplicable.add(camera);
                    camera.addMultiCameraGoal(this);
                }
                else {
                    //LOGGER.fine("Goal " + getId() + " not assigned camera " + camera.getIdAsString() + " due failure to meet base camera requirements.");
                }

            }

        }
        else {
            //LOGGER.fine("Goal " + getId() + " not assigned any cameras due set to inactive.");
        }

        cameras = camerasApplicable;

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

    public MultiCameraCore getMcp_application() {
        return mcp_application;
    }

    public void setMcp_application(MultiCameraCore mcp_application) {
        this.mcp_application = mcp_application;
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
    public Map<String, Object> getAdditionalFieldMap() {
        return additionalFieldMap;
    }

    public void setAdditionalFieldMap(Map<String, Object> additionalFieldMap) {
        this.additionalFieldMap = additionalFieldMap;
    }

    public List<GoalMAPEBehavior> getGoalBehaviours() {
        return goalBehaviours;
    }

    public List<VisualObservationOfInterest> getVisualObservationsOfInterest() {
        return visualObservationsOfInterest;
    }

    public void setVisualObservationsOfInterest(List<VisualObservationOfInterest> visualObservationsOfInterest) {
        this.visualObservationsOfInterest = visualObservationsOfInterest;
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public void setCameras(List<Camera> cameras) {
        this.cameras = cameras;
    }


    public MotionController getMotionController() {
        return motionController;
    }

    public void setMotionController(MotionController motionController) {
        this.motionController = motionController;
    }

    public List<CameraMAPEBehavior> getCameraBehaviours() {
        return cameraBehaviours;
    }

    public void setCameraBehaviours(List<CameraMAPEBehavior> cameraBehaviours) {
        this.cameraBehaviours = cameraBehaviours;
    }

    public void setGoalBehaviours(List<GoalMAPEBehavior> goalBehaviours) {
        this.goalBehaviours = goalBehaviours;
    }

    public Map<String, Map<String, Serializable>> getNewAnalysisResultsMap() {
        return newAnalysisResultsMap;
    }

    public void setNewAnalysisResultsMap(Map<String, Map<String, Serializable>> newAnalysisResultsMap) {
        this.newAnalysisResultsMap = newAnalysisResultsMap;
    }
}
