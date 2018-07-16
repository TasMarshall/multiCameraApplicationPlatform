package platform.core.goals.core;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;
import platform.MCP_Application;
import platform.core.camera.core.Camera;
import platform.core.camera.core.components.TargetView;
import platform.core.goals.core.components.Interest;
import platform.core.goals.core.components.ObjectOfInterest;
import platform.core.goals.core.components.RegionOfInterest;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.AnalysisTypeManager;
import platform.core.imageAnalysis.ImageAnalysis;
import platform.core.imageAnalysis.impl.outputObjects.CircleLocationInImage;
import platform.core.imageAnalysis.impl.outputObjects.CircleLocationsInImage;
import platform.core.map.LocalMap;
import platform.core.utilities.LoopTimer;
import platform.core.utilities.adaptation.AdaptationTypeManager;
import platform.core.utilities.adaptation.core.Adaptation;
import platform.core.utilities.adaptation.core.MotionController;
import platform.core.utilities.adaptation.impl.SimpleInScreenPointViewAdaptation;
import platform.jade.utilities.AnalysisResultsMessage;
import platform.jade.utilities.MotionActionMessage;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.util.*;

import static platform.MapView.distanceInLatLong;

public class MultiCameraGoal {

    public enum GoalIndependence{
        EXCLUSIVE,
        VIEW_CONTROL_REQUIRED,
        VIEW_CONTROL_OPTIONAL,
        PASSIVE
    }

    private String id = UUID.randomUUID().toString();

    public MCP_Application mcp_application;

    private List<RegionOfInterest> regionsOfInterest = new ArrayList<>();
    private List<ObjectOfInterest> objectsOfInterest = new ArrayList<>();
    List<Camera> cameras = new ArrayList<>();

    protected int priority = 0;
    private List<String> requiredCalibrationGoalIds;
    private GoalIndependence goalIndependence;

    private Map<String, Map<String,Serializable>> newAnalysisResultsMap;

    Map<String, Long> motionActionStartTimes;
    Map<String, AnalysisResultsMessage> lastAnalysisResultTimes;

    String motionControllerType;
    MotionController motionController;

    LoopTimer maximumSpeedTimer = new LoopTimer();

    private platform.core.map.Map map;

    public MultiCameraGoal(int priority, GoalIndependence goalIndependence, List<RegionOfInterest> regionsOfInterest, List<ObjectOfInterest> objectsOfInterest
                           , platform.core.map.Map map, double looptimer, String motionControllerType, List<String> requiredCalibrationGoalIds){

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
        this.requiredCalibrationGoalIds = requiredCalibrationGoalIds;

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
        motionActionStartTimes =  new HashMap<>();

        if (goalIndependence != GoalIndependence.PASSIVE){
            if(adaptationTypeManager.getAdaptivePolicy(motionControllerType) instanceof MotionController) {
                motionController = (MotionController) adaptationTypeManager.getAdaptivePolicy(motionControllerType);
                motionController.motInit();
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
                regionOfInterest.recordResult(results);
            }

            for (ObjectOfInterest objectOfInterest: objectsOfInterest){
                objectOfInterest.recordResult(results);
            }

            Camera camera = mcp_application.getCameraManager().getCameraByID(key);

        }

    }

    public void planCameraActions() {

    }

    public void executeCameraMotionAction(Camera camera) {

        if (motionActionStartTimes.get(camera.getIdAsString()) == null){
            motionActionStartTimes.put(camera.getIdAsString(),Long.valueOf(0));
        }

        String message = "";
        boolean moveCommanded = false;


        motionController.planMotion(this, camera);
        motionController.executeMotion(motionActionStartTimes, camera);

    }

    public void executeCameraActions(Camera camera) {

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

        for (Camera camera : getMcp_application().getAllCameras()) {

            if (camera.getLocation().getCoordinateSys() == map.getCoordinateSys()) {

                double camLat = camera.getLocation().getLatitude();
                double camLon = camera.getLocation().getLongitude();
                double range;

                if (camera.getAdditionalAttributes().containsKey("range")) {
                    range = (double) camera.getAdditionalAttributes().get("range");
                } else {
                    range = 50;
                }

                double dLat = distanceInLatLong(range, camLat, camLon, 0)[0];
                double dLon = distanceInLatLong(range, camLat, camLon, 90)[1];

                if (camLat > map.getLatMin() - dLat
                        && camLat < map.getLatMax() + dLat
                        && camLon > map.getLongMin() - dLon
                        && camLon < map.getLongMax() + dLon) {
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
        return cameras;
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
}
