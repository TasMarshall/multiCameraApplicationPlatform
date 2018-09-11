package platform.behaviors;

import platform.camera.Camera;
import platform.goals.MultiCameraGoal;
import platform.jade.utilities.CommunicationAction;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class CameraMAPEBehavior implements AdaptivePolicy {


    private Map<Camera, HashMap<String,Object>> cameraBehaviorInfoMap;

    public void behaviourInit(){
        cameraBehaviorInfoMap = new HashMap<>();
    }

    public Map<Camera, HashMap<String, Object>> getCameraBehaviorInfoMap() {
        return cameraBehaviorInfoMap;
    }

    public void setCameraBehaviorInfoMap(Map<Camera, HashMap<String, Object>> cameraBehaviorInfoMap) {
        this.cameraBehaviorInfoMap = cameraBehaviorInfoMap;
    }

    public abstract CommunicationAction plan(Camera camera, MultiCameraGoal multiCameraGoal);

    public abstract CommunicationAction analyse(Camera camera, MultiCameraGoal multiCameraGoal);

    public abstract CommunicationAction monitor(Camera camera, MultiCameraGoal multiCameraGoal);

    public abstract CommunicationAction execute(Camera camera, MultiCameraGoal multiCameraGoal);

}
