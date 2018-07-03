package platform.core.goals.core;

import platform.MCP_Application;
import platform.core.camera.core.Camera;
import platform.core.goals.core.components.ObjectOfInterest;
import platform.core.goals.core.components.RegionOfInterest;
import platform.core.imageAnalysis.ImageAnalysis;
import platform.core.map.LocalMap;
import platform.core.utilities.LoopTimer;
import platform.core.utilities.adaptation.core.Adaptation;

import java.util.*;

import static platform.MapView.distanceInLatLong;

public class MultiCameraGoal {

    public enum GoalIndependence{
        EXCLUSIVE,
        VIEW_CONTROL,
        PASSIVE
    }

    LoopTimer maximumSpeedTimer = new LoopTimer();

    public MCP_Application mcp_application;

    protected int priority = 0;
    private GoalIndependence goalIndependence;

    private List<RegionOfInterest> regionsOfInterest = new ArrayList<>();
    private List<ObjectOfInterest> objectsOfInterest = new ArrayList<>();


    List<Camera> cameras = new ArrayList<>();

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

    public void init(MCP_Application mcp_application, double timer/* List<UtilityScore> utilityScores*/){

        this.mcp_application = mcp_application;

        //cant set map to the global map in constructor so set here
        if (this.map.getMapType() == platform.core.map.Map.MapType.GLOBAL){
            this.map = mcp_application.getGlobalMap();
        }

        addCamerasToGoalsAndGoalsToCameras();

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

    public Set getImageAnalysisAlgorithms(){

        Set<ImageAnalysis> analysisAlgorithmsSet = new HashSet<>();

        for (RegionOfInterest regionOfInterest: regionsOfInterest){
            analysisAlgorithmsSet.addAll(regionOfInterest.getAnalysisAlgorithmsSet());
        }
        for(ObjectOfInterest objectOfInterest: objectsOfInterest){
            analysisAlgorithmsSet.addAll(objectOfInterest.getAnalysisAlgorithmsSet());
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
}
