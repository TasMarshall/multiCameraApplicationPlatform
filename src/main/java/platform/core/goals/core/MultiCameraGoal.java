package platform.core.goals.core;

import platform.MCP_Application;
import platform.core.camera.core.Camera;
import platform.core.goals.core.components.BehaviourOfInterest;
import platform.core.goals.core.components.ObjectOfInterest;
import platform.core.goals.core.components.RegionOfInterest;
import platform.core.goals.core.components.UtilityScore;
import platform.core.imageAnalysis.AnalysisAlgorithm;
import platform.core.utilities.LoopTimer;
import platform.core.utilities.adaptation.core.Adaptation;
import platform.core.utilities.mapeLoop;

import java.util.*;

import static platform.MapView.distanceInLatLong;

public abstract class MultiCameraGoal implements mapeLoop {

    LoopTimer maximumSpeedTimer = new LoopTimer();

    public MCP_Application mcp_application;

    protected int priority = 0;

    private List<RegionOfInterest> regionsOfInterest = new ArrayList<>();
    private List<ObjectOfInterest> objectsOfInterest = new ArrayList<>();
    private List<BehaviourOfInterest> behavioursOfInterest = new ArrayList<>();
    private List<UtilityScore> utilityScores = new ArrayList<>();

    List<Camera> activeCameras = new ArrayList<>();
    Map<RegionOfInterest,List<Camera>> activeCamerasPerRegion = new HashMap<>();

    protected Map<String,Adaptation> adaptationMap = new HashMap<>();

    private double timer;

    protected MultiCameraGoal( int priority,List<RegionOfInterest> regionsOfInterest,List<ObjectOfInterest> objectsOfInterest,List<BehaviourOfInterest> behavioursOfInterest, double looptimer){
        this.regionsOfInterest.addAll(regionsOfInterest);
        this.behavioursOfInterest.addAll(behavioursOfInterest);
        this.objectsOfInterest.addAll(objectsOfInterest);
        this.priority = priority;
        this.timer = looptimer;
    }

    public void init(MCP_Application mcp_application, double timer/* List<UtilityScore> utilityScores*/){

        this.mcp_application = mcp_application;
        addCamerasToRegionsAndGoalsToCameras();
        maximumSpeedTimer.start(timer,4);

    }

    @Override
    public void monitor() {

        if (maximumSpeedTimer.checkPulse()) {

            //look for cameras who have changed goals
            addAndRemoveChangedCamerasToActiveList();

            for (RegionOfInterest regionOfInterest : getRegionsOfInterest()) {
                regionOfInterest.monitor();
            }

            for (ObjectOfInterest objectOfInterest : getObjectsOfInterest()) {
                objectOfInterest.monitor();
            }

            for (BehaviourOfInterest behaviourOfInterest : getBehavioursOfInterest()) {
                behaviourOfInterest.monitor();
            }

        }

    }

    @Override
    public void analyse() {

        if (maximumSpeedTimer.checkPulse()) {

            //divide active cameras by region
            countActiveCamerasPerRegion();

            for (RegionOfInterest regionOfInterest : getRegionsOfInterest()) {
                regionOfInterest.analyse();
            }

            for (ObjectOfInterest objectOfInterest : getObjectsOfInterest()) {
                objectOfInterest.analyse();
            }

            for (BehaviourOfInterest behaviourOfInterest : getBehavioursOfInterest()) {
                behaviourOfInterest.analyse();
            }

            for (Camera camera: getActiveCameras()){
                camera.getAnalysisManager().analyse();
            }

        }

    }

    @Override
    public void plan() {

        if (maximumSpeedTimer.checkPulse()) {

            for (RegionOfInterest regionOfInterest : getRegionsOfInterest()) {
                regionOfInterest.plan();
            }

            for (ObjectOfInterest objectOfInterest : getObjectsOfInterest()) {
                objectOfInterest.plan();
            }

            for (BehaviourOfInterest behaviourOfInterest : getBehavioursOfInterest()) {
                behaviourOfInterest.plan();
            }

        }

    }

    @Override
    public void execute() {

        if (maximumSpeedTimer.checkPulse()) {

            for (RegionOfInterest regionOfInterest : getRegionsOfInterest()) {
                regionOfInterest.execute();
            }

            for (ObjectOfInterest objectOfInterest : getObjectsOfInterest()) {
                objectOfInterest.execute();
            }

            for (BehaviourOfInterest behaviourOfInterest : getBehavioursOfInterest()) {
                behaviourOfInterest.execute();
            }

        }

    }

    private void addAndRemoveChangedCamerasToActiveList() {

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
    }

    public void countActiveCamerasPerRegion(){
        for(RegionOfInterest regionOfInterest: getRegionsOfInterest()){
            List<Camera> cameras = new ArrayList<>();
            for (Camera camera: getActiveCameras()){
                if (regionOfInterest.getCamerasInRegion().contains(camera)){
                    cameras.add(camera);
                }
            }
            getActiveCamerasPerRegion().put(regionOfInterest,cameras);
        }
    }

    public void addActiveCamera(Camera camera){
        getActiveCameras().add(camera);
        //update working camera list per region
        countActiveCamerasPerRegion();
    }

    public void removeActiveCamera(Camera camera){
        getActiveCameras().remove(camera);

        countActiveCamerasPerRegion();
    }



    protected void addCamerasToRegionsAndGoalsToCameras() {

        for (RegionOfInterest regionOfInterest : getRegionsOfInterest()) {

            List<Camera> camerasInRegion = new ArrayList<>();

            for (Camera camera : getMcp_application().getAllCameras()) {

                double camLat = camera.getLocation().getLatitude();
                double camLon = camera.getLocation().getLongitude();

                double dLat = distanceInLatLong((double)camera.getAdditionalAttributes().get("range"), camera.getLocation().getLatitude(), camera.getLocation().getLongitude(), 0)[0];
                double dLon = distanceInLatLong((double)camera.getAdditionalAttributes().get("range"), camera.getLocation().getLatitude(), camera.getLocation().getLongitude(), 90)[1];

                if (camLat > regionOfInterest.getArea().getLatMin() - dLat
                        && camLat < regionOfInterest.getArea().getLatMax() + dLat
                        && camLon > regionOfInterest.getArea().getLongMin() - dLon
                        && camLon < regionOfInterest.getArea().getLongMax() + dLon) {
                    camerasInRegion.add(camera);
                    camera.addMultiCameraGoal(this);
                }
            }

            regionOfInterest.setCamerasInRegion(camerasInRegion);

        }
    }

    public Set getImageAnalysisAlgorithms(){

        Set<AnalysisAlgorithm> analysisAlgorithmsSet = new HashSet<>();

        for (RegionOfInterest regionOfInterest: regionsOfInterest){
            analysisAlgorithmsSet.addAll(regionOfInterest.getAnalysisAlgorithmsSet());
        }
        for(ObjectOfInterest objectOfInterest: objectsOfInterest){
            analysisAlgorithmsSet.addAll(objectOfInterest.getAnalysisAlgorithmsSet());
        }
        for(BehaviourOfInterest behaviourOfInterest: behavioursOfInterest){
            analysisAlgorithmsSet.addAll(behaviourOfInterest.getAnalysisAlgorithmsSet());
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

    public List<BehaviourOfInterest> getBehavioursOfInterest() {
        return behavioursOfInterest;
    }

    public void setBehavioursOfInterest(List<BehaviourOfInterest> behavioursOfInterest) {
        this.behavioursOfInterest = behavioursOfInterest;
    }

    public List<UtilityScore> getUtilityScores() {
        return utilityScores;
    }

    public void setUtilityScores(List<UtilityScore> utilityScores) {
        this.utilityScores = utilityScores;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<Camera> getActiveCameras() {
        return activeCameras;
    }

    public void setActiveCameras(List<Camera> activeCameras) {
        this.activeCameras = activeCameras;
    }

    public Map<RegionOfInterest, List<Camera>> getActiveCamerasPerRegion() {
        return activeCamerasPerRegion;
    }

    public void setActiveCamerasPerRegion(Map<RegionOfInterest, List<Camera>> activeCamerasPerRegion) {
        this.activeCamerasPerRegion = activeCamerasPerRegion;
    }

}
