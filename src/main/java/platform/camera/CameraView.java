package platform.camera;

import platform.camera.components.CameraLocation;
import platform.camera.components.CameraOrientation;
import platform.camera.components.Vector3D;
import platform.camera.components.ViewCapabilities;
import platform.goals.MultiCameraGoal;
import platform.utilities.CustomID;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CameraView extends CameraCore {

    List<String> applicableGoalIds = new ArrayList<>();
    List<String> currentGoalIds = new ArrayList<>();
    String motionGoalId;
    private boolean isWorking;      //Current state of the cameras communication
    private boolean isPTZWorking;   //Current state of the cameras ptz

    public CameraView (Camera camera){

        this(camera.getIdAsString(),camera.getUrl(),camera.getUsername(),camera.getPassword(),camera.getViewCapabilities(),camera.getCameraOrientation().getGlobalVector(),camera.getLocation());

        for (MultiCameraGoal multiCameraGoal : camera.getMultiCameraGoalList()){
            applicableGoalIds.add(multiCameraGoal.getId());
        }

        for (MultiCameraGoal multiCameraGoal : camera.getCurrentGoals()){
            currentGoalIds.add(multiCameraGoal.getId());
        }

        this.motionGoalId = camera.getViewControllingGoal().getId();

        this.isPTZWorking = camera.isPTZWorking();
        this.isWorking = camera.isWorking();

    }

    public CameraView(String id, URL url, String username, String password, ViewCapabilities viewCapabilities, Vector3D globalVector, CameraLocation location) {
        super(id, url, username, password, viewCapabilities, globalVector, location);
    }

    public List<String> getApplicableGoalIds() {
        return applicableGoalIds;
    }

    public void setApplicableGoalIds(List<String> applicableGoalIds) {
        this.applicableGoalIds = applicableGoalIds;
    }

    public List<String> getCurrentGoalIds() {
        return currentGoalIds;
    }

    public void setCurrentGoalIds(List<String> currentGoalIds) {
        this.currentGoalIds = currentGoalIds;
    }

    public String getMotionGoalId() {
        return motionGoalId;
    }

    public void setMotionGoalId(String motionGoalId) {
        this.motionGoalId = motionGoalId;
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
}
