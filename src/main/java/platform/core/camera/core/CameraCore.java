package platform.core.camera.core;


import com.sun.javafx.geom.Vec3d;
import platform.core.camera.core.components.*;
import platform.core.cameraMonitor.core.CameraStreamManager;
import platform.core.goals.core.MultiCameraGoal;

import platform.core.utilities.CustomID;

import javax.persistence.Id;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CameraCore {

    //////////////////////////
    //     Model Generated  //
    //////////////////////////

    @Id
    private CustomID id;    //Unique identifier defined at run time

    private URL url;    //URL required as it is used to define the IP
    private String IP;  //Defined by the URL at initialization

    private String username, password;  //Credentials

    private ViewCapabilities viewCapabilities;

    private CameraOrientation cameraOrientation;

    private CameraLocation location;    //Location of Camera in a specified Map

    private List<MultiCameraGoal> multiCameraGoalList = new ArrayList<>();      //List of goals the camera is subscribed to

    private Map<String, Object> additionalAttributes = new HashMap<>();

    //////////////////////////
    //     Platform USable  //
    //////////////////////////

    private boolean isWorking;      //Current state of the cameras communication
    private boolean isPTZWorking;   //Current state of the cameras ptz

    private CameraState cameraState = new CameraState();                     // Current component model state

    private CameraStreamManager cameraStreamManager = new CameraStreamManager();    //Camera stream video
    /*private AnalysisManager analysisManager;                                        //Populates image processing algorithms based on the cameras current goals*/

    private CurrentView currentView;                                                //Current camera view based on the Camera Orientation
    private TargetView targetView;                                                  //Target camera view based on the Camera Orientation

    private MultiCameraGoal currentGoal;                                            //The highest priority active goal
    private List<MultiCameraGoal> secondaryGoals = new ArrayList<>();               //The other goals which can be achieved at the same time

    //////////////////////////
    //     Private ONLY     //
    //////////////////////////

    private String streamURI;   //Private attribute used by the CameraStreamManager

    //////////////////////////
    //     CONSTRUCTOR      //
    //////////////////////////

    public CameraCore(String id, URL url, String username, String password, ViewCapabilities viewCapabilities, Vec3d globalVector, CameraLocation location, List<MultiCameraGoal> multiCameraGoalList, Map<String, Object> additionalAttributes) {
        this.setId(new CustomID(id));

        this.url = url;
        if (url.getPort()!= -1) {
            this.IP = url.getHost() + ":" + url.getPort();
        }
        else {
            this.IP = url.getHost();
        }

        this.username = username;
        this.password = password;

        this.viewCapabilities = viewCapabilities; //todo get info to populate viewCapabilities PTZControlDomain()
        this.cameraOrientation = new CameraOrientation(globalVector, viewCapabilities);
        this.location = location;

        this.multiCameraGoalList.addAll(multiCameraGoalList);
        this.additionalAttributes = additionalAttributes;

    }

    ///////////////////////////////
    // SPECIALIZEDD GET AND SET  //
    ///////////////////////////////

    public String getIdAsString(){
        return id.getSerialNumber();
    }

    public void addMultiCameraGoal(MultiCameraGoal multiCameraGoal){
        multiCameraGoalList.add(multiCameraGoal);
    }

    ///////////////////////////////
    // GENERATED GET AND SET     //
    ///////////////////////////////

    public CustomID getId() {
        return id;
    }

    public void setId(CustomID id) {
        this.id = id;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ViewCapabilities getViewCapabilities() {
        return viewCapabilities;
    }

    public void setViewCapabilities(ViewCapabilities viewCapabilities) {
        this.viewCapabilities = viewCapabilities;
    }

    public CameraOrientation getCameraOrientation() {
        return cameraOrientation;
    }

    public void setCameraOrientation(CameraOrientation cameraOrientation) {
        this.cameraOrientation = cameraOrientation;
    }

    public CameraLocation getLocation() {
        return location;
    }

    public void setLocation(CameraLocation location) {
        this.location = location;
    }

    public List<MultiCameraGoal> getMultiCameraGoalList() {
        return multiCameraGoalList;
    }

    public void setMultiCameraGoalList(List<MultiCameraGoal> multiCameraGoalList) {
        this.multiCameraGoalList = multiCameraGoalList;
    }

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void setWorking(boolean working) {
        isWorking = working;
    }

    public boolean isPTZWorking() {
        return isPTZWorking;
    }

    public void setPTZWorking(boolean PTZWorking) {
        isPTZWorking = PTZWorking;
    }

    public CameraState getCameraState() {
        return cameraState;
    }

    public void setCameraState(CameraState cameraState) {
        this.cameraState = cameraState;
    }

    public CameraStreamManager getCameraStreamManager() {
        return cameraStreamManager;
    }

    public void setCameraStreamManager(CameraStreamManager cameraStreamManager) {
        this.cameraStreamManager = cameraStreamManager;
    }

/*
    public AnalysisManager getAnalysisManager() {
        return analysisManager;
    }

    public void setAnalysisManager(AnalysisManager analysisManager) {
        this.analysisManager = analysisManager;
    }*/

    public CurrentView getCurrentView() {
        return currentView;
    }

    public void setCurrentView(CurrentView currentView) {
        this.currentView = currentView;
    }

    public TargetView getTargetView() {
        return targetView;
    }

    public void setTargetView(TargetView targetView) {
        this.targetView = targetView;
    }

    public MultiCameraGoal getCurrentGoal() {
        return currentGoal;
    }

    public void setCurrentGoal(MultiCameraGoal currentGoal) {
        this.currentGoal = currentGoal;
    }

    public List<MultiCameraGoal> getSecondaryGoals() {
        return secondaryGoals;
    }

    public void setSecondaryGoals(List<MultiCameraGoal> secondaryGoals) {
        this.secondaryGoals = secondaryGoals;
    }

    public String getStreamURI() {
        return streamURI;
    }

    public void setStreamURI(String streamURI) {
        this.streamURI = streamURI;
    }

}



/*        int[] lons = new int[36];
        int[] lats = new int[36];

        if (ptzCapability == PTZ.PT){
            int counter = 0;
            for (float angle = minPanViewAngle; angle < maxPanViewAngle; angle +=10){

                int xChange = (int) Math.round(effectiveRange*Math.sin(angle*Math.PI/180));
                int yChange = (int) Math.round(effectiveRange*Math.cos(angle*Math.PI/180));

                lons[counter] = xChange;
                lats[counter] = yChange;

                counter++;
            }
        }

        LocalMap localMap = new LocalMap(lons,lats);
        mapManager = new MapManager(location.getLongitude(),location.getLatitude(),localMap, MapManager.MapUnit.METRES);*/

//}
