package platform;

import org.opencv.core.Core;
import platform.core.camera.core.Camera;
import platform.core.cameraMonitor.impl.LocalONVIFCameraMonitor;
import platform.core.cameraMonitor.impl.SimulatedCameraMonitor;
import platform.core.goals.components.Area;
import platform.core.goals.components.RectangleArea;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.map.GlobalMap;
import platform.core.map.LocalMap;
import platform.core.utilities.ComponentState;
import platform.core.utilities.NanoTimeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static platform.MapView.distanceInLatLong;


public class MCP_Application  {

    private GlobalMap globalMap;

    /*private List<CameraMonitor> cameraMonitors = new ArrayList<>();*/

    private SimulatedCameraMonitor simulatedCameraMonitor;
    private LocalONVIFCameraMonitor localONVIFCameraMonitor;

    private  List<MultiCameraGoal> multiCameraGoals;

    private NanoTimeValue lastTime;
    private NanoTimeValue currentTime;

/*    private ComponentState state = new ComponentState();*/

    private Map<String, Object> additionalFields = new HashMap<>();

    ///////////////////////////////////////////////////////////////////////////
    /////                       CONSTRUCTOR                               /////
    ///////////////////////////////////////////////////////////////////////////

    public MCP_Application(List<MultiCameraGoal> multiCameraGoals, List<Camera> localONVIFCameras, List<Camera> simulatedCameras) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        this.multiCameraGoals = multiCameraGoals;
        this.localONVIFCameraMonitor = new LocalONVIFCameraMonitor(localONVIFCameras);
        this.simulatedCameraMonitor = new SimulatedCameraMonitor(simulatedCameras);

        this.lastTime = new NanoTimeValue(System.nanoTime());

        init();
    }

    public void init(){

        createGlobalMap(multiCameraGoals,getAllCameras());

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.init(this,0.1);
        }

    }


    ///////////////////////////////////////////////////////////////////////////
    /////                       MAPE LOOP                                 /////
    ///////////////////////////////////////////////////////////////////////////

    public void executeMAPELoop() {

        currentTime = new NanoTimeValue(System.nanoTime());
/*
        monitor();
        analyse();
        plan();
        execute();*/

        lastTime = currentTime;

    }

/*    public void monitor() {

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.monitor();
        }

        for (Camera camera: getAllCameras()) {
            camera.getAnalysisManager().monitor();
        }

        localONVIFCameraMonitor.monitor();
        simulatedCameraMonitor.monitor();

    }

    public void analyse() {

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.analyse();
        }

        localONVIFCameraMonitor.analyse();
        simulatedCameraMonitor.analyse();

        //if goals to be analysed
        //gather affected cameras

    }

    public void plan() {

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.plan();
        }

        localONVIFCameraMonitor.plan();
        simulatedCameraMonitor.plan();
        //plan goal distribution between independent groups of affected cameras

    }

        public void execute() {

            for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
                multiCameraGoal.execute();
            }

            localONVIFCameraMonitor.execute();
            simulatedCameraMonitor.execute();

    }*/


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


    ///////////////////////////////////////////////////////////////////////////
    /////                       GETTERS AND SETTERS                       /////
    ///////////////////////////////////////////////////////////////////////////

    public List<? extends Camera> getAllCameras(){
        List<Camera> allCameras = new ArrayList<>();
        allCameras.addAll(localONVIFCameraMonitor.getCameras());
        allCameras.addAll(simulatedCameraMonitor.getCameras());
        return allCameras;
    }

    public GlobalMap getGlobalMap() {
        return globalMap;
    }

    public void setGlobalMap(GlobalMap globalMap) {
        this.globalMap = globalMap;
    }

    public void setSimulatedCameraMonitor(SimulatedCameraMonitor simulatedCameraMonitor) {
        this.simulatedCameraMonitor = simulatedCameraMonitor;
    }

    public void setLocalONVIFCameraMonitor(LocalONVIFCameraMonitor localONVIFCameraMonitor) {
        this.localONVIFCameraMonitor = localONVIFCameraMonitor;
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

    public LocalONVIFCameraMonitor getLocalONVIFCameraMonitor() {
        return localONVIFCameraMonitor;
    }

    public SimulatedCameraMonitor getSimulatedCameraMonitor() {
        return simulatedCameraMonitor;
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

}


