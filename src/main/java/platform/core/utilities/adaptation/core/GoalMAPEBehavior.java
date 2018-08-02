package platform.core.utilities.adaptation.core;

import platform.core.camera.core.Camera;
import platform.core.goals.core.MultiCameraGoal;
import platform.jade.utilities.CommunicationAction;

import java.util.HashMap;
import java.util.Map;

public abstract class GoalMAPEBehavior implements AdaptivePolicy {

    private Map<String,Object> hashMap;

    public void behaviourInit(){
        hashMap = new HashMap<String,Object>();
    }

    public Map<String, Object> getHaspMap() {
        return hashMap;
    }

    public void setCameraHashMapMap(HashMap<String, Object> hashMap) {
        this.hashMap = hashMap;
    }

    public abstract CommunicationAction plan(MultiCameraGoal multiCameraGoal);

    public abstract CommunicationAction analyse(MultiCameraGoal multiCameraGoal);

    public abstract CommunicationAction monitor(MultiCameraGoal multiCameraGoal);

    public abstract CommunicationAction execute(MultiCameraGoal multiCameraGoal);


}
