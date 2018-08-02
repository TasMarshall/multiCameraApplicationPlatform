package platform.core.goals.core;

import platform.MCP_Application;
import platform.core.camera.core.Camera;
import platform.core.goals.core.components.Interest;
import platform.core.goals.core.components.ObjectOfInterest;
import platform.core.goals.core.components.RegionOfInterest;
import platform.core.imageAnalysis.AnalysisTypeManager;
import platform.core.imageAnalysis.ImageAnalysis;
import platform.core.map.LocalMap;
import platform.core.utilities.LoopTimer;
import platform.core.utilities.adaptation.AdaptationTypeManager;
import platform.core.utilities.adaptation.core.CameraMAPEBehavior;
import platform.core.utilities.adaptation.core.GoalMAPEBehavior;
import platform.core.utilities.adaptation.core.MotionController;
import platform.jade.utilities.AnalysisResultsMessage;
import platform.jade.utilities.CommunicationAction;

import java.io.Serializable;
import java.util.*;

public class MultiCameraGoal {


    public List<GoalMAPEBehavior> getGoalBehaviours() {
        return goalBehaviours;
    }

    public enum GoalIndependence{
        EXCLUSIVE,
        VIEW_CONTROL_REQUIRED,
        VIEW_CONTROL_OPTIONAL,
        PASSIVE,
        CALIBRATION
    }

    private String id = UUID.randomUUID().toString();

    private boolean activated;

    public MCP_Application mcp_application;

    private List<RegionOfInterest> regionsOfInterest = new ArrayList<>();
    private List<ObjectOfInterest> objectsOfInterest = new ArrayList<>();
    List<Camera> cameras = new ArrayList<>();

    protected int priority = 0;
    private List<String> requiredCalibrationGoalIds;
    private GoalIndependence goalIndependence;

    private Map<String, Map<String,Serializable>> newAnalysisResultsMap;

    Map<String, Long> motionActionEndTimes;
    Map<String, AnalysisResultsMessage> lastAnalysisResultTimes;

    String motionControllerType;
    MotionController motionController;
    boolean motionConfigured;

    List<String> actionTypes;
    List<CameraMAPEBehavior> cameraBehaviours;
    List<GoalMAPEBehavior> goalBehaviours;

    Map<Camera,Map<String,Object>> processedInfoMap;


    LoopTimer maximumSpeedTimer = new LoopTimer();

    private platform.core.map.Map map;

    public MultiCameraGoal(String id, boolean activated, int priority, GoalIndependence goalIndependence, List<RegionOfInterest> regionsOfInterest, List<ObjectOfInterest> objectsOfInterest
            , platform.core.map.Map map, double looptimer, String motionControllerType, List<String> actionTypes, List<String> requiredCalibrationGoalIds){

        this.id = id;

        this.activated = activated;

        if (regionsOfInterest != null) {
            this.regionsOfInterest.addAll(regionsOfInterest);
        }

        if(objectsOfInterest != null) {
            this.objectsOfInterest.addAll(objectsOfInterest);
        }

        //set map, if want global map which might not be defined yet set a placeholder, implies goal must be initialized
        if(map.getMapType() == platform.core.map.Map.MapType.GLOBAL){
            this.map = map;
        }
        else if (map.getMapType() == platform.core.map.Map.MapType.LOCAL){
            this.map = new LocalMap(map.getCoordinateSys(),map.getX(),map.getY());
        }

        this.priority = priority;
        this.goalIndependence = goalIndependence;
        this.motionControllerType = motionControllerType;
        this.motionConfigured = false;
        this.actionTypes = actionTypes;


        if (requiredCalibrationGoalIds != null) {
            this.requiredCalibrationGoalIds = requiredCalibrationGoalIds;
        }
        else {
            this.requiredCalibrationGoalIds = new ArrayList<>();
        }

        maximumSpeedTimer.start(looptimer,1);
    }

    public void init(MCP_Application mcp_application, double timer, AnalysisTypeManager analysisTypeManager, AdaptationTypeManager adaptationTypeManager){

        this.mcp_application = mcp_application;

        //cant set map to the global map in constructor so set here
        if (this.map.getMapType() == platform.core.map.Map.MapType.GLOBAL){
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

        if (goalIndependence != GoalIndependence.PASSIVE){
            if(adaptationTypeManager.getAdaptivePolicy(motionControllerType) instanceof MotionController) {
                motionController = (MotionController) adaptationTypeManager.getAdaptivePolicy(motionControllerType);
                motionController.motInit();
                motionConfigured = true;
            }
        }

        initROIandOOI(analysisTypeManager);

        addCamerasToGoalsAndGoalsToCameras();

    }

    private void initROIandOOI(AnalysisTypeManager analysisTypeManager) {

        for (RegionOfInterest regionOfInterest: regionsOfInterest){
            if(regionOfInterest.getAnalysisAlgorithmsSet() != null) {
                for (ImageAnalysis imageAnalysis : regionOfInterest.getAnalysisAlgorithmsSet()) {
                    imageAnalysis.setAnalysisTypeManager(analysisTypeManager);
                }

            }
            regionOfInterest.init();

        }

        for (ObjectOfInterest objectOfInterest: objectsOfInterest){
            if(objectOfInterest.getAnalysisAlgorithmsSet() != null) {
                for (ImageAnalysis imageAnalysis : objectOfInterest.getAnalysisAlgorithmsSet()) {
                    imageAnalysis.setAnalysisTypeManager(analysisTypeManager);
                }

            }
            objectOfInterest.init();

        }
    }

    /** This function plans camera actions from the collected goal based analysis results */
    public void recordResults() {

        for (String key : newAnalysisResultsMap.keySet()){

            Map<String, Serializable> results = newAnalysisResultsMap.get(key);

            for (RegionOfInterest regionOfInterest: regionsOfInterest) {
                regionOfInterest.recordResult(newAnalysisResultsMap.get(key), key);
            }

            for (ObjectOfInterest objectOfInterest: objectsOfInterest){
                objectOfInterest.recordResult(newAnalysisResultsMap.get(key),key);
            }

            Camera camera = mcp_application.getCameraManager().getCameraByID(key);

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

        if (motionConfigured) {
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

        //dont add activated goals by default?
        if (activated) {

            for (Camera camera : getMcp_application().getAllCameras()) {

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

        for (RegionOfInterest regionOfInterest: regionsOfInterest){
            if (regionOfInterest.getAnalysisAlgorithmsSet() != null) {
                analysisAlgorithmsSet.addAll(regionOfInterest.getAnalysisAlgorithmsSet());
            }

        }
        for(ObjectOfInterest objectOfInterest: objectsOfInterest){
            if (objectOfInterest.getAnalysisAlgorithmsSet() != null) {
                analysisAlgorithmsSet.addAll(objectOfInterest.getAnalysisAlgorithmsSet());
            }
        }

        return analysisAlgorithmsSet;
    }

    //CUSTOM
    public Interest getInterestById(String id){
       List<Interest> a = new ArrayList<>();
       a.addAll(objectsOfInterest);
       a.addAll(regionsOfInterest);
       for (Interest o: a){
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

    public MCP_Application getMcp_application() {
        return mcp_application;
    }

    public void setMcp_application(MCP_Application mcp_application) {
        this.mcp_application = mcp_application;
    }

    public List<RegionOfInterest> getRegionsOfInterest() {
        return regionsOfInterest;
    }

    public void setRegionsOfInterest(List<RegionOfInterest> regionsOfInterest) {
        this.regionsOfInterest = regionsOfInterest;
    }

    public List<ObjectOfInterest> getObjectsOfInterest() {
        return objectsOfInterest;
    }

    public void setObjectsOfInterest(List<ObjectOfInterest> objectsOfInterest) {
        this.objectsOfInterest = objectsOfInterest;
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

    public platform.core.map.Map getMap() {
        return map;
    }

    public void setMap(platform.core.map.Map map) {
        this.map = map;
    }

    public GoalIndependence getGoalIndependence() {
        return goalIndependence;
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

    public Map<String, AnalysisResultsMessage> getLatestAnalysisResults() {
        return lastAnalysisResultTimes;
    }

    public void setLatestAnalysisResults(Map<String, AnalysisResultsMessage> lastAnalysisResultTimes) {
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
}
