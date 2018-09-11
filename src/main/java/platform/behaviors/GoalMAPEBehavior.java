package platform.behaviors;

import platform.goals.MultiCameraGoal;
import platform.jade.utilities.CommunicationAction;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public abstract class GoalMAPEBehavior implements AdaptivePolicy {


    private Map<String,Object> goalBehaviorInfoMap;

    public void behaviourInit(){
        goalBehaviorInfoMap = new HashMap<String,Object>();
    }

    public Map<String, Object> getGoalBehaviorInfoMap() {
        return goalBehaviorInfoMap;
    }

    public void setGoalBehaviorInfoMap(HashMap<String, Object> hashMap) {
        this.goalBehaviorInfoMap = hashMap;
    }

    public abstract CommunicationAction plan(MultiCameraGoal multiCameraGoal);

    public abstract CommunicationAction analyse(MultiCameraGoal multiCameraGoal);

    public abstract CommunicationAction monitor(MultiCameraGoal multiCameraGoal);

    public abstract CommunicationAction execute(MultiCameraGoal multiCameraGoal);


}
