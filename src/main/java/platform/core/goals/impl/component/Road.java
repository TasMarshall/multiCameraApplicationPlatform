package platform.core.goals.impl.component;

import platform.core.goals.components.Area;
import platform.core.goals.components.RectangleArea;
import platform.core.goals.core.components.RegionOfInterest;

public class Road extends RegionOfInterest {

    public Road(RectangleArea rectangleArea, Area.CoordinateSys outdoor){
        super(rectangleArea,Area.CoordinateSys.OUTDOOR);
    }

    @Override
    public void plan() {

    }

    @Override
    public void execute() {

    }
}
