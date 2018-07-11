package platform.core.goals.core;

import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;
import platform.MCP_Application;
import platform.core.camera.core.Camera;
import platform.core.camera.core.components.TargetView;
import platform.core.goals.core.components.ObjectOfInterest;
import platform.core.goals.core.components.RegionOfInterest;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.imageAnalysis.AnalysisTypeManager;
import platform.core.imageAnalysis.ImageAnalysis;
import platform.core.imageAnalysis.impl.outputObjects.CircleLocationInImage;
import platform.core.imageAnalysis.impl.outputObjects.CircleLocationsInImage;
import platform.core.map.LocalMap;
import platform.core.utilities.LoopTimer;
import platform.core.utilities.adaptation.core.Adaptation;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.util.*;

import static platform.MapView.distanceInLatLong;

public class MultiCameraGoal {

    long lastAnalysisResultTime;

    public enum GoalIndependence{
        EXCLUSIVE,
        VIEW_CONTROL_REQUIRED,
        VIEW_CONTROL_OPTIONAL,
        PASSIVE
    }

    private String id = UUID.randomUUID().toString();

    LoopTimer maximumSpeedTimer = new LoopTimer();

    public MCP_Application mcp_application;

    protected int priority = 0;
    private GoalIndependence goalIndependence;

    private List<RegionOfInterest> regionsOfInterest = new ArrayList<>();
    private List<ObjectOfInterest> objectsOfInterest = new ArrayList<>();


    List<Camera> cameras = new ArrayList<>();
    private Map<String, Map<String,Serializable>> analysisResultMap;

    protected Map<String,Adaptation> adaptationMap = new HashMap<>();

    private platform.core.map.Map map;

    public MultiCameraGoal(int priority, GoalIndependence goalIndependence, List<RegionOfInterest> regionsOfInterest, List<ObjectOfInterest> objectsOfInterest
                           , platform.core.map.Map map, double looptimer){

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

        maximumSpeedTimer.start(looptimer,1);
    }

    public void init(MCP_Application mcp_application, double timer,AnalysisTypeManager analysisTypeManager){

        this.mcp_application = mcp_application;

        //cant set map to the global map in constructor so set here
        if (this.map.getMapType() == platform.core.map.Map.MapType.GLOBAL){
            this.map = mcp_application.getGlobalMap();
        }

        analysisResultMap = new HashMap<>();

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

        for (String key : analysisResultMap.keySet()){

            Camera camera = mcp_application.getCameraManager().getCameraByID(key);
            Map<String, Serializable> results = analysisResultMap.get(key);

            for (RegionOfInterest regionOfInterest: regionsOfInterest) {
                regionOfInterest.recordResult(results);
            }

            for (ObjectOfInterest objectOfInterest: objectsOfInterest){
                objectOfInterest.recordResult(results);
            }

        }

    }

    public void planCameraActions() {

    }

    public void executeCameraMotionAction(Camera camera) {

        //simple no obfuscation motion controller using pixel difference

        ObjectOfInterest objectOfInterest = objectsOfInterest.get(0);

        Map<String,Object> inputs = new HashMap<>();

        if (objectOfInterest.getResults() != null && objectOfInterest.getResults().size() != 0 && (System.nanoTime() - lastAnalysisResultTime)/1000000 < 50) {

            CircleLocationInImage circleLocationInImage = ((CircleLocationsInImage) objectOfInterest.getResults().get("circles")).getCircleLocationInImageList().get(0);

            inputs.put("object",circleLocationInImage);

            boolean moveCommanded = false;

            float moveX = 0.5F - (circleLocationInImage.getRelX());
            float moveY = 0.5F - (circleLocationInImage.getRelY());

            Vector2D vector2D = new Vector2D();
            Vector1D vector1D = new Vector1D();

            if (Math.abs(moveX) > 0.1) {
                //add x movement to command
                if (moveX > 0) {
                    System.out.println("move to the left");
                    vector2D.setX((float)-45);
                } else {
                    System.out.println("move to the right");
                    vector2D.setX((float)+45);
                }
                moveCommanded = true;
            }
            else {
                vector2D.setX((float)0);
            }

            if (Math.abs(moveY) > 0.1) {
                //add y movement to command
                if (moveY > 0) {
                    System.out.println("move to the down");
                    vector2D.setY((float)+45);
                } else {
                    System.out.println("move to the up");
                    vector2D.setY((float)-45);
                }
                moveCommanded = true;
            }
            else {
                vector2D.setY((float)0);
            }

            if(moveCommanded) {
                System.out.println("move commanded");
            }
            else {
                System.out.println("stop commanded");
            }

            vector1D.setX((float)0);

            PTZVector ptzVectorCommand = new PTZVector();
            ptzVectorCommand.setPanTilt(vector2D);
            ptzVectorCommand.setZoom(vector1D);
            camera.commandPTZMovement(ptzVectorCommand);

            long startTime = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();

            while( currentTime  - startTime < 10) {
                currentTime = System.currentTimeMillis();
            }

            camera.commandPTZStop();
            /*analysisResultMap.get(camera.getIdAsString()).remove("circles");*/
            objectOfInterest.getResults().remove("circles");

        }

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

    public Map<String, Adaptation> getAdaptationMap() {
        return adaptationMap;
    }

    public void setAdaptationMap(Map<String, Adaptation> adaptationMap) {
        this.adaptationMap = adaptationMap;
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

    public Map<String, Map<String,Serializable>> getAnalysisResultMap() {
        return analysisResultMap;
    }

    public void setAnalysisResultMap(Map<String, Map<String,Serializable>> analysisResultMap) {
        this.analysisResultMap = analysisResultMap;
    }

    public void setLastAnalysisResultTime(long lastAnalysisResultTime) {
        this.lastAnalysisResultTime = lastAnalysisResultTime;
    }
}
