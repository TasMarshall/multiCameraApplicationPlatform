package platform.core.goals.core;

import platform.core.goals.components.Area;
import platform.core.goals.core.components.RegionOfInterest;

import java.util.List;

public class GlobalRegionOfInterest extends RegionOfInterest {

    public GlobalRegionOfInterest (Area area, Area.CoordinateSys inOrOut){
        super(area, inOrOut);
    }

    //subordinate regions
    List<MultiCameraGoal> containedGoals;

    public List<MultiCameraGoal> getContainedGoals() {
        return containedGoals;
    }

    public void setContainedGoals(List<MultiCameraGoal> containedGoals) {
        this.containedGoals = containedGoals;
    }

    @Override
    public void plan() {

    }

    @Override
    public void execute() {

    }
}
