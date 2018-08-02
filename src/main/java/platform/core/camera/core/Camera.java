package platform.core.camera.core;

import com.sun.javafx.geom.Vec3d;
import de.onvif.soap.OnvifDevice;
import org.onvif.ver10.schema.PTZVector;
import org.onvif.ver10.schema.Vector1D;
import org.onvif.ver10.schema.Vector2D;
import platform.MCP_Application;
import platform.core.camera.core.components.CameraLocation;
import platform.core.camera.core.components.CurrentView;
import platform.core.camera.core.components.ViewCapabilities;
import platform.core.camera.impl.SimulatedCamera;
import platform.core.goals.core.MultiCameraGoal;
import platform.core.imageAnalysis.AnalysisResult;
import platform.core.map.LocalMap;
import platform.core.utilities.CustomID;

import javax.xml.soap.SOAPException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.URL;
import java.util.*;

import static platform.MapView.distanceInLatLong;

public abstract class Camera extends CameraCore implements CameraStandardSpecificFunctions, Serializable{

    private String filename;


    public Camera(String id, URL url, String username, String password, ViewCapabilities viewCapabilities, Vec3d globalVector, CameraLocation location, Map<String, Object> additionalAttributes) {
        super(id, url, username, password, viewCapabilities, globalVector, location, additionalAttributes);
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

    private void acquireAndSetCameraInformation() {

        if (getId() == null) {
            String cameraUniqueIdentifier = getCameraUniqueIdentifier();
            setId(new CustomID(cameraUniqueIdentifier));
        }

        boolean success = videoSimpleFunctionTest();

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

        getMultiCameraGoalList().sort(Comparator.comparingInt(MultiCameraGoal::getPriority));

        List<MultiCameraGoal> goals = getMultiCameraGoalList();

        //remove calibration goals from the goals selection list. Selection of calibration goals uses the following determineActiveGoals(goals) directly.
        List<MultiCameraGoal> noCalibGoals = new ArrayList<>();
        for (MultiCameraGoal m : goals){
            if (!(m.getGoalIndependence() == MultiCameraGoal.GoalIndependence.CALIBRATION && m.isActivated())){
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

            if (multiCameraGoal.getGoalIndependence() == MultiCameraGoal.GoalIndependence.CALIBRATION){
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
            else if (multiCameraGoal.getGoalIndependence() == MultiCameraGoal.GoalIndependence.VIEW_CONTROL_REQUIRED){
                if (!viewControlled){
                    currentGoals.add(multiCameraGoal);
                    viewControllingGoal = multiCameraGoal;
                    viewControlled = true;
                }
            }
            else if (multiCameraGoal.getGoalIndependence() == MultiCameraGoal.GoalIndependence.VIEW_CONTROL_OPTIONAL){
                if (!viewControlled){
                    currentGoals.add(multiCameraGoal);
                    viewControllingGoal = multiCameraGoal;
                    viewControlled = true;
                }
                else {
                    currentGoals.add(multiCameraGoal);
                }
            }
            else if(multiCameraGoal.getGoalIndependence() == MultiCameraGoal.GoalIndependence.PASSIVE){
                currentGoals.add(multiCameraGoal);
            }

        }

        setCurrentGoals(currentGoals);
        setViewControllingGoal(viewControllingGoal);

    }

    public void setCalibrationGoals(MCP_Application mcp_application) {

        List<MultiCameraGoal> goals = new ArrayList<>();

        for (MultiCameraGoal m: getMultiCameraGoalList()){
            if (m.getCalibrationGoalIds().size() > 0){
                for (String s: m.getCalibrationGoalIds()){
                    if (getAdditionalAttributes().containsKey("completedGoals")){
                        if (!((List<String>)getAdditionalAttributes().get("completedGoals")).contains(s)){
                            MultiCameraGoal calibrationGoal = mcp_application.getGoalById(s);
                            if (calibrationGoal.getGoalIndependence() == MultiCameraGoal.GoalIndependence.CALIBRATION){
                                goals.add(calibrationGoal);
                            }
                        }
                    }
                    else {
                        getAdditionalAttributes().put("completedGoals", new ArrayList<String>(){});
                        MultiCameraGoal calibrationGoal = mcp_application.getGoalById(s);
                        if (calibrationGoal.getGoalIndependence() == MultiCameraGoal.GoalIndependence.CALIBRATION){
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

    public boolean inRange(platform.core.map.Map map) {

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

    /* public void monitor(){

        if (getCameraState().initialized == false){



        }
        else if (getCameraState().calibrated == false){



        }
        else if (getCameraState().connected == false){



        }

    }

    public void analyse(){

    }

    public void plan(){
        int highestPriority;

        if (getCurrentGoal() == null) {
            highestPriority = Integer.MAX_VALUE;
        }
        else {
            highestPriority = getCurrentGoal().getPriority();
        }

        for (MultiCameraGoal multiCameraGoal: getMultiCameraGoalList()){
            if (multiCameraGoal.getPriority() < highestPriority){
                setCurrentGoal(multiCameraGoal);
                highestPriority = multiCameraGoal.getPriority();
            }
        }

    }

    public void execute(){

    }*/



  /*  @Override
    public void render(Graphics g, double delta, platform.components.MCP_Application application) {
   *//*     int x,y,width,height;

        double xInitRatio = getLocation().getLongitude() / application.getGlobalArea().getArea().getxDiff();
        double yInitRatio = getLocation().getLatitude() / application.getGlobalArea().getArea().getyDiff();

        x = (int)(xInitRatio*application.actualWidth);
        y = (int)(yInitRatio*application.actualHeight);

        width = 5;
        height = 5;

        g.setColor(Color.red);
        g.drawRect(application.txfm.plotX((int)(x)) - width / 2,application.txfm.plotY((int)(y + height/2)),(int)(width),(int)(height));

        Vec2d vec2d = getCurrentView().getGlobalVec2d();
        g.setColor(Color.green);

        //direction info
        float directionAngle;
        if (vec2d.x == 0){directionAngle = 90;}
        else if(vec2d.y == 0){directionAngle = 0;}
        else {
            directionAngle = (float) Math.toDegrees(Math.atan(((vec2d.y / vec2d.x))));
            if(vec2d.x < 0 && vec2d.y < 0){
                directionAngle = 270 - directionAngle;
            }
            else if (vec2d.x < 0 && vec2d.y > 0 ){
                directionAngle = 270 + directionAngle;
            }
            else if (vec2d.x > 0 && vec2d.y < 0 ){
                directionAngle = 90 + directionAngle;
            }
            else if (vec2d.x > 0 && vec2d.y > 0 ){
                directionAngle = directionAngle;
            }
        }

        float xRangeNormalised = (float) (getEffectiveRange() * vec2d.x / application.getGlobalArea().getArea().getxDiff());
        float yRangeNormalised = (float) (getEffectiveRange() *vec2d.y / application.getGlobalArea().getArea().getyDiff());

        //arrow line
        //g.drawLine(x,y, (int)(x + vec2d.x*15), (int)(y + vec2d.y*15));

        //boundarys
        float angle = getViewAngle();
        float range = getEffectiveRange();
        //boundary 1
        float boundaryAngle1 = directionAngle - angle/2;
        Vec2d b1Vec2d = new Vec2d(Math.cos(Math.toRadians(boundaryAngle1)),Math.sin(Math.toRadians(boundaryAngle1)));
        float xB1RangeNormalised = (float) (getEffectiveRange() * b1Vec2d.x / application.getGlobalArea().getArea().getxDiff());
        float yB1RangeNormalised = (float) (getEffectiveRange() * b1Vec2d.y / application.getGlobalArea().getArea().getyDiff());
        g.drawLine(application.txfm.plotX((int)(x)),application.txfm.plotY((int)(y)) , application.txfm.plotX((int)(x + xB1RangeNormalised*application.getGlobalArea().getArea().getxDiff())), application.txfm.plotY((int)(y + yB1RangeNormalised*application.getGlobalArea().getArea().getyDiff())));
        //boundary 2
        float boundaryAngle2 = directionAngle + angle/2;
        Vec2d b2Vec2d = new Vec2d(Math.cos(Math.toRadians(boundaryAngle2)),Math.sin(Math.toRadians(boundaryAngle2)));
        float xB2RangeNormalised = (float) (getEffectiveRange() * b2Vec2d.x / application.getGlobalArea().getArea().getxDiff());
        float yB2RangeNormalised = (float) (getEffectiveRange() * b2Vec2d.y / application.getGlobalArea().getArea().getyDiff());
        g.drawLine(application.txfm.plotX((int)(x)),application.txfm.plotY((int)(y )), application.txfm.plotX((int)(x + xB2RangeNormalised*application.getGlobalArea().getArea().getxDiff())), application.txfm.plotY((int)(y + yB2RangeNormalised*application.getGlobalArea().getArea().getyDiff())));
        //arc boundary

        g.drawArc(application.txfm.plotX((int)(x - getEffectiveRange()/2)),application.txfm.plotY((int)(y - getEffectiveRange()/2)), (int)(getEffectiveRange() / application.getGlobalArea().getArea().getxDiff()),(int)(getEffectiveRange() / application.getGlobalArea().getArea().getxDiff()),(int)0,(int)360);
*//*
 *//*       float minX;
        if (xB1RangeNormalised < xB2RangeNormalised) minX = xB1RangeNormalised;
        else { minX = xB2RangeNormalised; }

        float minY;
        if (yB1RangeNormalised < yB2RangeNormalised) minY = yB1RangeNormalised;
        else { minY = xB2RangeNormalised; }

        g.drawArc(x + (int)(minX*application.getGlobalArea().getArea().getxDiff()),y + (int)(minY*application.getGlobalArea().getArea().getyDiff()),(int)range,(int)range,(int)(boundaryAngle2 + 225 - angle), (int)360);
*//*
        //g.drawArc();*/

    //}

}




