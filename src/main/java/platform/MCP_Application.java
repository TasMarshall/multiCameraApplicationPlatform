package platform;

import platform.core.camera.core.Camera;
import platform.core.camera.impl.SimulatedCamera;
import platform.core.cameraManager.core.CameraManager;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.imageAnalysis.AnalysisTypeManager;
import platform.core.map.GlobalMap;
import platform.core.utilities.adaptation.AdaptationTypeManager;
import platform.core.utilities.adaptation.core.GoalMAPEBehavior;
import platform.jade.utilities.CommunicationAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static platform.MapView.distanceInLatLong;


public class MCP_Application  {

    private GlobalMap globalMap;

    private CameraManager cameraManager;

    private  List<MultiCameraGoal> multiCameraGoals;

    private AnalysisTypeManager analysisTypeManager;
    private AdaptationTypeManager adaptationTypeManager;

    private Map<String, Object> additionalFields = new HashMap<>();

    private List<CommunicationAction> agentActions = new ArrayList<>();

    ///////////////////////////////////////////////////////////////////////////
    /////                       CONSTRUCTOR                               /////
    ///////////////////////////////////////////////////////////////////////////

    public MCP_Application(List<MultiCameraGoal> multiCameraGoals, List<Camera> cameras, AnalysisTypeManager analysisTypeManager, AdaptationTypeManager adaptationTypeManager, Map<String,Object> additionalFields) {

        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        this.multiCameraGoals = multiCameraGoals;
        this.cameraManager = new CameraManager(cameras);
        this.analysisTypeManager = analysisTypeManager;
        this.adaptationTypeManager = adaptationTypeManager;

        if (additionalFields != null) this.additionalFields.putAll(additionalFields);

        init();
    }

    public void init(){

        createGlobalMap(multiCameraGoals,getAllCameras());

        for(String s: analysisTypeManager.getStringToAnalysisMap().keySet()){
            analysisTypeManager.getImageProcessObject(s).defineInfoKeys();
            analysisTypeManager.getImageProcessObject(s).init();
        }

        for (String s: adaptationTypeManager.getStringToAdaptivePolicyMap().keySet()){
            adaptationTypeManager.getAdaptivePolicy(s).init();
        }

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.init(this,0.1, analysisTypeManager, adaptationTypeManager);
        }

    }


    ///////////////////////////////////////////////////////////////////////////
    /////                       MAPE LOOP                                 /////
    ///////////////////////////////////////////////////////////////////////////

    public List<CommunicationAction> executeMAPELoop() {

        agentActions = new ArrayList<>();

        monitor();
        analyse();
        plan();
        execute();

        removeNewInfo();

        return agentActions;

    }

    private void removeNewInfo() {
        for (MultiCameraGoal multiCameraGoal: getMultiCameraGoals()){
            for (Camera camera: multiCameraGoal.getCameras()) {
                multiCameraGoal.getNewAnalysisResultMap().remove(camera.getIdAsString());
            }
        }
    }

    public void monitor() {

        for (Camera camera: getAllCameras()){
            if (camera.getCameraState().connected == false || camera.getCameraState().initialized == false){
                camera.init();
            }
            if (camera.getCameraState().calibrated == false){
                camera.setCalibrationGoals(this);
            }
            if (camera.getCameraState().initialized == true && camera.getCameraState().calibrated == true && camera.getCameraState().connected == true){
                camera.determineActiveGoals();
            }
        }

        List<CommunicationAction> communicationActions = new ArrayList<>();
        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            //multiCameraGoal.recordResults();

            if (multiCameraGoal.isActivated()) {

                for (Camera camera : getAllCameras()) {
                    if (camera.getCurrentGoals().contains(multiCameraGoal)) {
                        communicationActions = multiCameraGoal.monitorBehaviours(camera);
                        if (communicationActions.size() != 0) agentActions.addAll(communicationActions);
                    }
                }

                for (GoalMAPEBehavior adaptivePolicy : multiCameraGoal.getGoalBehaviours()) {
                    CommunicationAction communicationAction = adaptivePolicy.monitor(multiCameraGoal);
                    if (communicationAction != null) agentActions.add(communicationAction);
                }

            }

        }

    }


    public void analyse() {

        List<CommunicationAction> communicationActions = new ArrayList<>();
        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){

            if (multiCameraGoal.isActivated()) {

                for (Camera camera : getAllCameras()) {
                    communicationActions = multiCameraGoal.analysisBehaviours(camera);

                    if (communicationActions.size() != 0) agentActions.addAll(communicationActions);

                }

                for (GoalMAPEBehavior adaptivePolicy : multiCameraGoal.getGoalBehaviours()) {
                    CommunicationAction communicationAction = adaptivePolicy.analyse(multiCameraGoal);
                    if (communicationAction != null) agentActions.add(communicationAction);
                }

            }
        }

    }

    public void plan() {

        List<CommunicationAction> communicationActions = new ArrayList<>();
        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            if (multiCameraGoal.isActivated()) {

                for (Camera camera : getAllCameras()) {
                    communicationActions = multiCameraGoal.planBehaviours(camera);
                    if (communicationActions.size() != 0) agentActions.addAll(communicationActions);

                }

                for (GoalMAPEBehavior adaptivePolicy : multiCameraGoal.getGoalBehaviours()) {
                    CommunicationAction communicationAction = adaptivePolicy.plan(multiCameraGoal);
                    if (communicationAction != null) agentActions.add(communicationAction);
                }
            }
        }
    }

    public void execute() {

        List<CommunicationAction> communicationActions;
        for (Camera camera: getAllCameras()){
            if (!(camera instanceof SimulatedCamera)) {
                if (camera.getViewCapabilities().isPTZ()) {
                    if (camera.getViewControllingGoal() != null) {
                        camera.getViewControllingGoal().executeCameraMotionAction(camera);
                    }
                }
            }
        }

        for (MultiCameraGoal multiCameraGoal: getMultiCameraGoals()){

            if (multiCameraGoal.isActivated()) {

                for (Camera camera: getAllCameras()) {
                    communicationActions = multiCameraGoal.executeBehaviours(camera);
                    if (communicationActions.size() != 0) agentActions.addAll(communicationActions);
                }

                for (GoalMAPEBehavior adaptivePolicy: multiCameraGoal.getGoalBehaviours()){
                    CommunicationAction communicationAction = adaptivePolicy.execute(multiCameraGoal);
                    if (communicationAction != null ) agentActions.add(communicationAction);
                }

            }

        }

    }


    ///////////////////////////////////////////////////////////////////////////
    /////                       CLASS FUNCTIONS                           /////
    ///////////////////////////////////////////////////////////////////////////


    public void createGlobalMap(List<MultiCameraGoal> multiCameraGoals, List<? extends Camera> cameras) {

        double minLat = Double.POSITIVE_INFINITY;
        double minLong = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double maxLong = Double.NEGATIVE_INFINITY;

        platform.core.map.Map.CoordinateSys coordinateSys = platform.core.map.Map.CoordinateSys.INDOOR;

        if (multiCameraGoals.size() > 0) {

            for (MultiCameraGoal multiCameraGoal : multiCameraGoals) {

                if (multiCameraGoal.getMap().getMapType() == platform.core.map.Map.MapType.LOCAL) {

                    if (multiCameraGoal.getMap().getLongMin() < minLong)
                        minLong = multiCameraGoal.getMap().getLongMin();
                    if (multiCameraGoal.getMap().getLongMax() > maxLong)
                        maxLong = multiCameraGoal.getMap().getLongMax();
                    if (multiCameraGoal.getMap().getLatMin() < minLat)
                        minLat = multiCameraGoal.getMap().getLatMin();
                    if (multiCameraGoal.getMap().getLatMax() > maxLat)
                        maxLat = multiCameraGoal.getMap().getLatMax();

                    if (multiCameraGoal.getMap().getCoordinateSys() == platform.core.map.Map.CoordinateSys.OUTDOOR) {
                        coordinateSys = platform.core.map.Map.CoordinateSys.OUTDOOR;
                    }

                }

            }

        }

        for (Camera camera: cameras){

            double camLat = camera.getLocation().getLatitude();
            double camLong = camera.getLocation().getLongitude();

            double dLat = distanceInLatLong((double)camera.getAdditionalAttributes().get("range"),camLat,camLong,0)[0];
            double dLong = distanceInLatLong((double)camera.getAdditionalAttributes().get("range"),camLat,camLong,90)[1];

            if (camLong - dLong < minLong)
                minLong = camera.getLocation().getLongitude()- dLong;
            if (camera.getLocation().getLongitude() + dLong > maxLong)
                maxLong = camera.getLocation().getLongitude() + dLong;
            if (camera.getLocation().getLatitude() - dLat < minLat)
                minLat = camera.getLocation().getLatitude() - dLat;
            if (camera.getLocation().getLatitude() +  dLat> maxLat)
                maxLat = camera.getLocation().getLatitude() + dLat;

        }

        globalMap = new GlobalMap(minLong - 0.0001, minLat- 0.0001, maxLong + 0.0001, maxLat + 0.0001);


    }

    public MultiCameraGoal getGoalById(String id){

        MultiCameraGoal output = null;

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            if (multiCameraGoal.getId().equals(id)){
                output = multiCameraGoal;
            }
        }

        return  output;

    }


    ///////////////////////////////////////////////////////////////////////////
    /////                       GETTERS AND SETTERS                       /////
    ///////////////////////////////////////////////////////////////////////////

    public List<Camera> getAllCameras(){

        return cameraManager.getCameras();
    }

    public GlobalMap getGlobalMap() {
        return globalMap;
    }

    public void setGlobalMap(GlobalMap globalMap) {
        this.globalMap = globalMap;
    }

    public CameraManager getCameraMonitor() {
        return cameraManager;
    }

    public void setCameraMonitor(CameraManager cameraMonitor) {
        this.cameraManager = cameraMonitor;
    }

    public void setAdditionalFields(Map<String, Object> additionalFields) {
        this.additionalFields = additionalFields;
    }

    public List<MultiCameraGoal> getMultiCameraGoals() {
        return multiCameraGoals;
    }

    public void setMultiCameraGoals(List<MultiCameraGoal> multiCameraGoals) {
        this.multiCameraGoals = multiCameraGoals;
    }

    public Map<String,Object> getAdditionalFields() {
        return additionalFields;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    public AnalysisTypeManager getAnalysisTypeManager() {
        return analysisTypeManager;
    }

    public void setAnalysisTypeManager(AnalysisTypeManager analysisTypeManager) {
        this.analysisTypeManager = analysisTypeManager;
    }

    public AdaptationTypeManager getAdaptationTypeManager() {
        return adaptationTypeManager;
    }
}


