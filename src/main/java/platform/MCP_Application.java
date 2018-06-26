package platform;

import org.opencv.core.Core;

import platform.core.camera.core.Camera;
import platform.core.cameraMonitor.impl.LocalONVIFCameraMonitor;
import platform.core.cameraMonitor.impl.SimulatedCameraMonitor;
import platform.core.goals.components.Area;
import platform.core.goals.components.RectangleArea;
import platform.core.goals.core.GlobalRegionOfInterest;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.goals.core.components.RegionOfInterest;
import platform.core.imageAnalysis.AnalysisManager;
import platform.core.utilities.ComponentState;
import platform.core.utilities.NanoTimeValue;
import platform.core.utilities.mapeLoop;

import java.util.ArrayList;
import java.util.List;

import static platform.MapView.distanceInLatLong;


public class MCP_Application implements mapeLoop {

    private GlobalRegionOfInterest globalArea;

    /*private List<CameraMonitor> cameraMonitors = new ArrayList<>();*/

    private SimulatedCameraMonitor simulatedCameraMonitor;
    private LocalONVIFCameraMonitor localONVIFCameraMonitor;

    private  List<MultiCameraGoal> multiCameraGoals;

    private NanoTimeValue lastTime;
    private NanoTimeValue currentTime;

    private ComponentState state = new ComponentState();

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

        for (MultiCameraGoal multiCameraGoal: multiCameraGoals){
            multiCameraGoal.init(this,0.1);
        }

        for (Camera camera: getAllCameras()) {
            camera.setAnalysisManager(new AnalysisManager(this, camera));
            camera.getAnalysisManager().monitor();
        }

        createGlobalRegionOfInterest(multiCameraGoals,getAllCameras());

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

    }


    ///////////////////////////////////////////////////////////////////////////
    /////                       CLASS FUNCTIONS                           /////
    ///////////////////////////////////////////////////////////////////////////


    public void createGlobalRegionOfInterest(List<MultiCameraGoal> multiCameraGoals, List<? extends Camera> cameras) {

        if (multiCameraGoals.size() > 0) {

            double minLat = Double.POSITIVE_INFINITY;
            double minLong = Double.POSITIVE_INFINITY;
            double maxLat = Double.NEGATIVE_INFINITY;
            double maxLong = Double.NEGATIVE_INFINITY;


            Area.CoordinateSys coordinateSys = Area.CoordinateSys.INDOOR;

            for (MultiCameraGoal multiCameraGoal : multiCameraGoals) {

                for (RegionOfInterest regionOfInterest : multiCameraGoal.getRegionsOfInterest()) {

                    if (regionOfInterest.definedArea == true) {
                        if (regionOfInterest.getArea().getLongMin() < minLong)
                            minLong = regionOfInterest.getArea().getLongMin();
                        if (regionOfInterest.getArea().getLongMax() > maxLong)
                            maxLong = regionOfInterest.getArea().getLongMax();
                        if (regionOfInterest.getArea().getLatMin() < minLat)
                            minLat = regionOfInterest.getArea().getLatMin();
                        if (regionOfInterest.getArea().getLatMax() > maxLat)
                            maxLat = regionOfInterest.getArea().getLatMax();
                    }

                    if (regionOfInterest.getCoordinateSys() == Area.CoordinateSys.OUTDOOR) {
                        coordinateSys = Area.CoordinateSys.OUTDOOR;
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

            globalArea = new GlobalRegionOfInterest(new RectangleArea(minLong - 0.0001, minLat- 0.0001, maxLong + 0.0001, maxLat + 0.0001), coordinateSys);

        }

        globalArea.setContainedGoals(multiCameraGoals);

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

    public GlobalRegionOfInterest getGlobalArea() {
        return globalArea;
    }

    public void setGlobalArea(GlobalRegionOfInterest globalArea) {
        this.globalArea = globalArea;
    }

    public LocalONVIFCameraMonitor getLocalONVIFCameraMonitor() {
        return localONVIFCameraMonitor;
    }

    public SimulatedCameraMonitor getSimulatedCameraMonitor() {
        return simulatedCameraMonitor;
    }



}


