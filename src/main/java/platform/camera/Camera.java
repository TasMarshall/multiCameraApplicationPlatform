package platform.camera;

import com.sun.javafx.geom.Vec3d;
import platform.MultiCameraCore;
import platform.camera.components.*;
import platform.goals.MultiCameraGoal;
import platform.utilities.CustomID;

import java.io.Serializable;
import java.net.URL;
import java.util.*;

import static archive.MapView.distanceInLatLong;

public abstract class Camera extends CameraCore implements CameraStandardSpecificFunctions, Serializable{

    private String filename;

    private List<MultiCameraGoal> multiCameraGoalList = new ArrayList<>();      //List of goals the camera is subscribed to

    //////////////////////////
    //     Platform USable  //
    //////////////////////////

    private boolean isWorking;      //Current state of the cameras communication
    private boolean isPTZWorking;   //Current state of the cameras ptz

    private CurrentView currentView;                                                //Current camera view based on the Camera Orientation
    private TargetView targetView;                                                  //Target camera view based on the Camera Orientation

    private MultiCameraGoal viewControllingGoal;                                            //The highest priority active goal
    private List<MultiCameraGoal> currentGoals = new ArrayList<>();               //The other goals which can be achieved at the same time

    private Map<String, Object> additionalAttributes = new HashMap<>();

    public Camera(String id, URL url, String username, String password, ViewCapabilities viewCapabilities, Vector3D globalVector, CameraLocation location, Map<String, Object> additionalAttributes) {
        super(id, url, username, password, viewCapabilities, globalVector, location);
        this.additionalAttributes = additionalAttributes;
    }

    public void simpleInit(){

        canConnectAndSimpleInit();

    }

    public boolean init() {

        setWorking(connectToCamera());

        if (isWorking()) {

            getCameraState().connected = true;

            if (getCameraState().initialized == false){
                acquireAndSetCameraInformation();

            }

            acquireAndSetCameraCurrentView();

        }

        return isWorking();

    }

    public void acquireAndSetCameraCurrentView(){

        CurrentView currentView = getCameraCurrentView();

        if(currentView == null){

            Vector2D vector2D = new Vector2D();
            vector2D.setX(0);
            vector2D.setY(0);

            Vector1D vector1D = new Vector1D();
            vector1D.setX(0);

            PTZVector ptzVector = new PTZVector();
            ptzVector.setPanTilt(vector2D);
            ptzVector.setZoom(vector1D);

            setCurrentView( new CurrentView(this, ptzVector));
        }
        else {
            setCurrentView(getCameraCurrentView());
        }

    }

    /** This function ensures the camera has a unique identifier, is working and gets view capabilities*/
    public void acquireAndSetCameraInformation() {

        if (getId() == null) {
            String cameraUniqueIdentifier = getCameraUniqueIdentifier();
            setId(new CustomID(cameraUniqueIdentifier));
        }

        boolean success = simpleUnsecuredFunctionTest();
        success = videoSimpleFunctionTest();
        success = pvtSimpleMotionFunctionTest();

        setWorking(success);

        getViewCapabilities().setPtzControlDomain(acquireCameraPTZCapabilities());

        if (!getViewCapabilities().getPtzType().contains(ViewCapabilities.PTZ.Nil)) {

            setPTZWorking(false);
            boolean succes2 = pvtSimpleMotionFunctionTest();
            //if no exception is thrown then test must be successful and camera is working
            setPTZWorking(succes2);
        }

    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    /**
     * This function determines the current list of goals according to the goals which have been assigned
     * to it. It orders the goals by priority and then iterates through this list adding goals depending on
     * if they are exclusive, view controlling or passive. Goals are added to the current goals and specifies
     * the highest priority view controlling goal.
     */
    public void determineActiveGoals() {

        //order the goals applicable to the camera
        getMultiCameraGoalList().sort(Comparator.comparingInt(MultiCameraGoal::getPriority));

        List<MultiCameraGoal> goals = getMultiCameraGoalList();

        //remove calibration goals from the goals selection list. Selection of calibration goals uses the following determineActiveGoals(goals) directly.
        List<MultiCameraGoal> noCalibGoals = new ArrayList<>();
        for (MultiCameraGoal m : goals){
            if (!(m.getCameraRequirements() == MultiCameraGoal.CameraRequirements.CALIBRATION) && m.isActivated()){
                noCalibGoals.add(m);
            }
        }

        determineActiveGoals(noCalibGoals);

    }

    /** requires sorted goal list*/
    public void determineActiveGoals(List<MultiCameraGoal> goals) {

        List<MultiCameraGoal> currentGoals = new ArrayList<>();

        boolean exlusive = false;
        boolean viewControlled = false;

        MultiCameraGoal viewControllingGoal = null;

        for (MultiCameraGoal multiCameraGoal: goals){

            if (exlusive) break;

            if (multiCameraGoal.getCameraRequirements() == MultiCameraGoal.CameraRequirements.CALIBRATION){
                if (currentGoals.size() == 0) {
                    currentGoals.add(multiCameraGoal);
                    viewControllingGoal = multiCameraGoal;
                    exlusive = true;
                    viewControlled = true;
                }
            }
            else if (multiCameraGoal.getGoalIndependence() == MultiCameraGoal.GoalIndependence.EXCLUSIVE){
                if (currentGoals.size() == 0) {
                    currentGoals.add(multiCameraGoal);
                    viewControllingGoal = multiCameraGoal;
                    exlusive = true;
                    viewControlled = true;
                }
            }
            else if (multiCameraGoal.getCameraRequirements() == MultiCameraGoal.CameraRequirements.VIEW_CONTROL_REQUIRED){
                if (!viewControlled){
                    currentGoals.add(multiCameraGoal);
                    viewControllingGoal = multiCameraGoal;
                    viewControlled = true;
                }
            }
            else if (multiCameraGoal.getCameraRequirements() == MultiCameraGoal.CameraRequirements.VIEW_CONTROL_OPTIONAL){
                if (!viewControlled){
                    currentGoals.add(multiCameraGoal);
                    viewControllingGoal = multiCameraGoal;
                    viewControlled = true;
                }
                else {
                    currentGoals.add(multiCameraGoal);
                }
            }
            else if(multiCameraGoal.getCameraRequirements() == MultiCameraGoal.CameraRequirements.PASSIVE){
                currentGoals.add(multiCameraGoal);
            }

        }

        setCurrentGoals(currentGoals);
        setViewControllingGoal(viewControllingGoal);

    }

    public void setCalibrationGoals(MultiCameraCore mcp_application) {

        List<MultiCameraGoal> goals = new ArrayList<>();

        for (MultiCameraGoal m: getMultiCameraGoalList()){
            if (m.getCalibrationGoalIds().size() > 0){
                for (String s: m.getCalibrationGoalIds()){
                    if (getAdditionalAttributes().containsKey("completedGoals")){
                        if (!((List<String>)getAdditionalAttributes().get("completedGoals")).contains(s)){
                            MultiCameraGoal calibrationGoal = mcp_application.getGoalById(s);
                            if (calibrationGoal.getCameraRequirements() == MultiCameraGoal.CameraRequirements.CALIBRATION){
                                goals.add(calibrationGoal);
                            }
                        }
                    }
                    else {
                        getAdditionalAttributes().put("completedGoals", new ArrayList<String>(){});
                        MultiCameraGoal calibrationGoal = mcp_application.getGoalById(s);
                        if (calibrationGoal.getCameraRequirements() == MultiCameraGoal.CameraRequirements.CALIBRATION){
                            goals.add(calibrationGoal);
                        }
                    }
                }
            }
        }

        //sort calibration goals by priority
        goals.sort(Comparator.comparingInt(MultiCameraGoal::getPriority));

        determineActiveGoals(goals);

        if (getCurrentGoals().size() == 0){
            getCameraState().setCalibrated(true);
            System.out.println("Camera " + getIdAsString() + " is calibrated.");
        }

    }

    public boolean inRange(platform.map.Map map) {

        boolean inRange = false;

        if (getLocation().getCoordinateSys() == map.getCoordinateSys()) {

            double camLat = getLocation().getLatitude();
            double camLon = getLocation().getLongitude();
            double range;

            if (getAdditionalAttributes().containsKey("range")) {
                range = (double) getAdditionalAttributes().get("range");
            } else {
                range = 50;
            }

            double dLat = distanceInLatLong(range, camLat, camLon, 0)[0];
            double dLon = distanceInLatLong(range, camLat, camLon, 90)[1];

            if (camLat >= map.getLatMin() - dLat
                    && camLat <= map.getLatMax() + dLat
                    && camLon >= map.getLongMin() - dLon
                    && camLon <= map.getLongMax() + dLon) {

                inRange = true;

            }
        }

        return  inRange;

    }

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public void addMultiCameraGoal(MultiCameraGoal multiCameraGoal){
        multiCameraGoalList.add(multiCameraGoal);
    }

    public List<MultiCameraGoal> getMultiCameraGoalList() {
        return multiCameraGoalList;
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

    public MultiCameraGoal getViewControllingGoal() {
        return viewControllingGoal;
    }

    public void setViewControllingGoal(MultiCameraGoal viewControllingGoal) {
        this.viewControllingGoal = viewControllingGoal;
    }

    public List<MultiCameraGoal> getCurrentGoals() {
        return currentGoals;
    }

    public void setCurrentGoals(List<MultiCameraGoal> currentGoals) {
        this.currentGoals = currentGoals;
    }

}




