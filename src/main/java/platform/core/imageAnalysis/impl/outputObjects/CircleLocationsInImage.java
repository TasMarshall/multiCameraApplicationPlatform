package platform.core.imageAnalysis.impl.outputObjects;

import org.bytedeco.javacpp.opencv_core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CircleLocationsInImage implements Serializable {

    List<CircleLocationInImage> circleLocationInImageList = new ArrayList<>();

    public void addCircle(CircleLocationInImage circleLocationInImage){
        circleLocationInImageList.add(circleLocationInImage);
    }

    public List<CircleLocationInImage> getCircleLocationInImageList() {
        return circleLocationInImageList;
    }

    public void setCircleLocationInImageList(List<CircleLocationInImage> circleLocationInImageList) {
        this.circleLocationInImageList = circleLocationInImageList;
    }
}
