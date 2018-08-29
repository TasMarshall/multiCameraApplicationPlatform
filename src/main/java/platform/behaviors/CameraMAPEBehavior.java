package platform.behaviors;

import platform.camera.Camera;
import platform.goals.MultiCameraGoal;
import platform.jade.utilities.CommunicationAction;

import java.util.HashMap;
import java.util.Map;

public abstract class CameraMAPEBehavior implements AdaptivePolicy {

    private Map<Camera, HashMap<String,Object>> cameraHashMapMap;

    public void behaviourInit(){
        cameraHashMapMap = new HashMap<Camera,HashMap<String,Object>>();
    }

    public Map<Camera, HashMap<String, Object>> getCameraHashMapMap() {
        return cameraHashMapMap;
    }

    public void setCameraHashMapMap(Map<Camera, HashMap<String, Object>> cameraHashMapMap) {
        this.cameraHashMapMap = cameraHashMapMap;
    }

    public abstract CommunicationAction plan(Camera camera, MultiCameraGoal multiCameraGoal);

    public abstract CommunicationAction analyse(Camera camera, MultiCameraGoal multiCameraGoal);

    public abstract CommunicationAction monitor(Camera camera, MultiCameraGoal multiCameraGoal);

    public abstract CommunicationAction execute(Camera camera, MultiCameraGoal multiCameraGoal);

}
