package platform;

import org.opencv.core.Core;
import platform.core.camera.core.Camera;
import platform.core.cameraManager.core.CameraManager;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.imageAnalysis.AnalysisTypeManager;
import platform.core.imageAnalysis.ImageAnalysis;
import platform.core.map.GlobalMap;
import platform.core.utilities.NanoTimeValue;
import platform.jade.utilities.CameraAnalysisMessage;

import java.util.*;

import static platform.MapView.distanceInLatLong;


public class MCP_Application  {

    private GlobalMap globalMap;

    /*private List<CameraMonitor> cameraMonitors = new ArrayList<>();*/

    private CameraManager cameraManager;

    private  List<MultiCameraGoal> multiCameraGoals;

    private NanoTimeValue lastTime;
    private NanoTimeValue currentTime;

    private AnalysisTypeManager analysisTypeManager;

/*    private ComponentState state = new ComponentState();*/

    private Map<String, Object> additionalFields = new HashMap<>();

    ///////////////////////////////////////////////////////////////////////////
    /////                       CONSTRUCTOR                               /////
    ///////////////////////////////////////////////////////////////////////////

    public MCP_Application(List<MultiCameraGoal> multiCameraGoals, List<Camera> cameras, AnalysisTypeManager analysisTypeManager, Map<String,Object> additionalFields) {

        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        this.multiCameraGoals = multiCameraGoals;
        this.cameraManager = new CameraManager(cameras);
        this.analysisTypeManager = analysisTypeManager;

        if (additionalFields != null) this.additionalFields.putAll(additionalFields);

        this.lastTime = new NanoTimeValue(System.nanoTime());

        init();
    }

    public void init(){

        createGlobalMap(multiCameraGoals,getAllCameras());

        for(String s: analysisTypeManager.getStringToAnalysisMap().keySet()){
            analysisTypeManager.getImageProcessObject(s).defineInfoKeys();
        }

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.init(this,0.1,analysisTypeManager);
        }

    }


    ///////////////////////////////////////////////////////////////////////////
    /////                       MAPE LOOP                                 /////
    ///////////////////////////////////////////////////////////////////////////

    public void executeMAPELoop() {

        currentTime = new NanoTimeValue(System.nanoTime());

        monitor();
        analyse();
        plan();
        execute();

        lastTime = currentTime;

    }

    public void monitor() {

/*        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){

        }*/

        for (Camera camera: getAllCameras()){
            if (camera.getCameraState().connected == false || camera.getCameraState().initialized == false){
                camera.init();
            }
            if (camera.getCameraState().calibrated == false){
                //camera.setCalibrationGoal();
                camera.getCameraState().setCalibrated(true); //TODO
            }
            if (camera.getCameraState().initialized == true && camera.getCameraState().calibrated == true && camera.getCameraState().connected == true){
                camera.determineActiveGoals();
            }
        }

        //cameraManager.monitor();

    }



    public void analyse() {

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.recordResults();
        }

        /*localONVIFCameraMonitor.analyse();
        simulatedCameraMonitor.analyse();*/

    }
    public void plan() {

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.planCameraActions();
        }

        /*localONVIFCameraMonitor.plan();
        simulatedCameraMonitor.plan();*/
        //plan goal distribution between independent groups of affected cameras

    }

        public void execute() {



            for (Camera camera: getAllCameras()){

                if (camera.getViewControllingGoal()!= null){
                    camera.getViewControllingGoal().executeCameraMotionAction(camera);
                }

                for (MultiCameraGoal multiCameraGoal : camera.getCurrentGoals()){
                    multiCameraGoal.executeCameraActions(camera);
                }

            }

            /*

            localONVIFCameraMonitor.execute();
            simulatedCameraMonitor.execute();
*/
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

            globalMap = new GlobalMap(minLong - 0.0001, minLat- 0.0001, maxLong + 0.0001, maxLat + 0.0001);

        }

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

    public NanoTimeValue getLastTime() {
        return lastTime;
    }

    public void setLastTime(NanoTimeValue lastTime) {
        this.lastTime = lastTime;
    }

    public NanoTimeValue getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(NanoTimeValue currentTime) {
        this.currentTime = currentTime;
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
}


